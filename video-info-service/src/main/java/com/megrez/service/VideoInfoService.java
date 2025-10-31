package com.megrez.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.megrez.client.AnalyticsServiceClient;
import com.megrez.client.LikeFavoriteClient;
import com.megrez.constant.GatewayHttpPath;
import com.megrez.mysql_entity.Video;
import com.megrez.mysql_entity.VideoLikes;
import com.megrez.mysql_entity.VideoStatistics;
import com.megrez.mapper.VideoMapper;
import com.megrez.result.Response;
import com.megrez.result.Result;
import com.megrez.utils.CollectionUtils;
import com.megrez.utils.PageTokenUtils;
import com.megrez.vo.CursorLoad;
import com.megrez.vo.analytics_service.VideoHistory;
import com.megrez.vo.analytics_service.VideoWatched;
import com.megrez.vo.video_info_service.VideoVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class VideoInfoService {
    private static final Logger log = LoggerFactory.getLogger(VideoInfoService.class);
    private final VideoMapper videoMapper;
    private final LikeFavoriteClient likeFavoriteClient;
    private final AnalyticsServiceClient analyticsServiceClient;

    public VideoInfoService(VideoMapper videoMapper, LikeFavoriteClient likeFavoriteClient, AnalyticsServiceClient analyticsServiceClient) {
        this.videoMapper = videoMapper;
        this.likeFavoriteClient = likeFavoriteClient;
        this.analyticsServiceClient = analyticsServiceClient;
    }

    /**
     * 根据ID获取指定视频元数据
     *
     * @param userId  当前用户
     * @param videoId 视频ID
     * @return 视频元数据
     */
    public Result<Video> getVideoInfo(Integer userId, Integer videoId) {
        Video video = videoMapper.selectById(videoId);
        video.setCoverName(GatewayHttpPath.VIDEO_COVER_IMG + video.getCoverName());
        video.setVideoFilename(GatewayHttpPath.VIDEO_PLAY + video.getVideoFilename());

        VideoVO videoVO = new VideoVO();
        BeanUtils.copyProperties(video, videoVO);
        // 获取统计信息
        Result<List<VideoStatistics>> videoStatById = analyticsServiceClient.getVideoStatById(List.of(video.getId()));
        if (videoStatById.isSuccess()) {
            videoVO.setStatistics(videoStatById.getData().get(0));
        }

        if (userId > 0) {

        }

        return Result.success(video);
    }

    /**
     * 根据用户ID查询作品信息
     *
     * @param targetId 目标用户ID
     * @param uid      当前操作用户ID
     * @return 包含视频元数据的集合
     */
    public Result<CursorLoad<VideoVO>> getVideosInfoByUserId(Integer uid, Integer targetId, String cursor) throws Exception {

        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<Video>()
                .eq(Video::getUploaderId, targetId)
                .orderByDesc(Video::getPublishTime)
                .last("LIMIT 21");

        if (cursor != null && !cursor.isEmpty()) {
            VideoVO videoVO = PageTokenUtils.decryptState(cursor, VideoVO.class);
            wrapper.le(Video::getPublishTime, videoVO.getPublishTime());
            wrapper.ne(Video::getId, videoVO.getId());
        }

        List<Video> videos = videoMapper.selectList(wrapper);

        if (videos.isEmpty()) {
            return Result.success(CursorLoad.empty());
        }


        boolean hasMore = false;
        cursor = null;
        if (videos.size() > 20) {
            hasMore = true;
            videos = videos.subList(0, videos.size() - 1);
        }

        List<Integer> ids = videos.stream().map(Video::getId).toList();
        // 查询统计信息
        Result<List<VideoStatistics>> videoStatById = analyticsServiceClient.getVideoStatById(ids);
        List<VideoStatistics> statisticsList;
        if (videoStatById.isSuccess()) {
            statisticsList = videoStatById.getData();
        } else {
            throw new Exception(videoStatById.getMsg());
        }

        Map<Integer, VideoStatistics> statisticsMap = statisticsList.stream().collect(Collectors.toMap(VideoStatistics::getVideoId, Function.identity()));


        List<VideoVO> list = videos.stream().map(video -> {
            VideoVO videoVO = new VideoVO();
            BeanUtils.copyProperties(video, videoVO);
            log.warn("{}", video);
            videoVO.setStatistics(statisticsMap.get(video.getId()));
            videoVO.setCoverName(GatewayHttpPath.VIDEO_COVER_IMG + videoVO.getCoverName());
            videoVO.setVideoFilename(GatewayHttpPath.VIDEO_PLAY + videoVO.getVideoFilename());
            return videoVO;
        }).toList();

        if (hasMore) {
            cursor = PageTokenUtils.encryptState(list.get(list.size() - 1));
        }
        return Result.success(CursorLoad.of(list, hasMore, cursor));
    }

    public Result<CursorLoad<VideoVO>> getLikeVideosInfoByUserId(Integer userId, Integer tid, String cursor) {

        // 1. 视频服务调用点赞服务询问目标用户点赞记录
        Result<CursorLoad<VideoLikes>> recordsByUserId = likeFavoriteClient.getRecordsByUserId(tid, cursor);
        List<Integer> ids = List.of(); // 视频ID数组
        if (recordsByUserId.isSuccess()) {
            List<VideoLikes> list = recordsByUserId.getData().getList();
            // 2. 获取点赞列表，提取视频ID
            ids = list.stream().map(VideoLikes::getVideoId).toList();
        } else {
            log.error("调用点赞/收藏服务发生错误：{}", recordsByUserId.getMsg());
            return Result.error(Response.UNKNOWN_WRONG);
        }
        Boolean hasMore = recordsByUserId.getData().getHasMore();
        cursor = recordsByUserId.getData().getCursor();
        if (ids.isEmpty()) {
            return Result.success(CursorLoad.empty());
        }
        // 3. 查询视频统计信息
        List<VideoStatistics> statistics = List.of();
        Result<List<VideoStatistics>> videoStatById = analyticsServiceClient.getVideoStatById(ids);
        if (videoStatById.isSuccess()) {
            statistics = videoStatById.getData();
        }
        // 4. 查询视频信息
        LambdaQueryWrapper<Video> query = new LambdaQueryWrapper<Video>().in(Video::getId, ids);
        List<Video> videos = videoMapper.selectList(query);

        // 5. 组装数据
        Map<Integer, VideoStatistics> statisticsMap = statistics.stream().collect(Collectors.toMap(VideoStatistics::getVideoId, Function.identity()));
        Map<Integer, Video> videoMap = videos.stream().collect(Collectors.toMap(Video::getId, Function.identity()));

        List<VideoVO> list = ids.stream().map(id -> {
            VideoVO videoVO = new VideoVO();
            BeanUtils.copyProperties(Objects.requireNonNullElse(videoMap.get(id), new Video()), videoVO);
            videoVO.setStatistics(statisticsMap.get(id));
            videoVO.setVideoFilename(GatewayHttpPath.VIDEO_PLAY + videoVO.getVideoFilename());
            videoVO.setCoverName(GatewayHttpPath.VIDEO_COVER_IMG + videoVO.getCoverName());
            return videoVO;
        }).toList();

        return Result.success(CursorLoad.of(list, hasMore, cursor));
    }

    public Result<CursorLoad<VideoVO>> getHistoryInfoByUserId(Integer userId, Long cursor) {
        // 1. 调用统计服务，获取用户的历史记录
        Result<List<VideoHistory>> videoHistory = analyticsServiceClient.getVideoHistory(userId, cursor);
        if (!videoHistory.isSuccess()) {
            return Result.error(Response.UNKNOWN_WRONG);
        }
        // 2. 获取其中的视频ID
        List<VideoHistory> videoHistories = videoHistory.getData();
        if (videoHistories.isEmpty()) {
            return Result.success(CursorLoad.empty());
        }
        List<Integer> videoIds = CollectionUtils.toList(videoHistories, VideoHistory::getVideoId);
        // 3. 查询视频信息
        List<Video> videos = videoMapper.selectList(new LambdaQueryWrapper<Video>().in(Video::getId, videoIds));
        // 4. 查询统计数据
        Result<List<VideoStatistics>> videoStatById = analyticsServiceClient.getVideoStatById(videoIds);
        // 5. 查询观看时长
        Result<List<VideoWatched>> videoWatched = analyticsServiceClient.getVideoWatched(userId, videoIds);
        if (!videoStatById.isSuccess() || !videoWatched.isSuccess()) {
            return Result.error(Response.UNKNOWN_WRONG);
        }
        // 6. 组装数据
        List<VideoStatistics> videoStatistics = videoStatById.getData();
        List<VideoWatched> videoWatchedData = videoWatched.getData();
        Map<Integer, Video> videoMap = CollectionUtils.toMap(videos, Video::getId);
        Map<Integer, VideoHistory> videoHistoryMap = CollectionUtils.toMap(videoHistories, VideoHistory::getVideoId);
        Map<Integer, VideoStatistics> statisticsMap = CollectionUtils.toMap(videoStatistics, VideoStatistics::getVideoId);
        Map<Integer, VideoWatched> videoWatchedMap = CollectionUtils.toMap(videoWatchedData, VideoWatched::getVideoId);
        List<VideoVO> list = videoIds.stream().map(id -> {
            VideoVO vo = new VideoVO();
            Video video = videoMap.get(id);
            VideoStatistics stat = statisticsMap.get(id);
            VideoWatched watched = videoWatchedMap.get(id);
            BeanUtils.copyProperties(video, vo);
            vo.setStatistics(stat);
            vo.setWatchedTime(watched.getTime());
            vo.setWatchedAt(videoHistoryMap.get(id).getCreatedAt());
            vo.setVideoFilename(GatewayHttpPath.VIDEO_PLAY + vo.getVideoFilename());
            vo.setCoverName(GatewayHttpPath.VIDEO_COVER_IMG + vo.getCoverName());
            return vo;
        }).toList();

        boolean hasMore = false;
        cursor = null;
        if (list.size() > 20) {
            hasMore = true;
            list = list.subList(0, list.size() - 1);
            cursor = list.get(list.size() - 1).getWatchedAt();
        }
        return Result.success(CursorLoad.of(list, hasMore, cursor == null ? null : cursor.toString()));
    }
//
//    public Result<List<Video>> getFavoriteInfoByUserId(Integer userId, Integer targetId) {
//    }
//
}
