package com.megrez.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.megrez.client.AnalyticsServiceClient;
import com.megrez.client.LikeFavoriteClient;
import com.megrez.client.SocialServiceClient;
import com.megrez.client.UserServiceClient;
import com.megrez.constant.GatewayHttpPath;
import com.megrez.dto.social_service.CheckFollowDTO;
import com.megrez.mysql_entity.*;
import com.megrez.mapper.VideoMapper;
import com.megrez.result.Response;
import com.megrez.result.Result;
import com.megrez.utils.CollectionUtils;
import com.megrez.utils.PageTokenUtils;
import com.megrez.utils.VideoUtils;
import com.megrez.vo.CursorLoad;
import com.megrez.vo.analytics_service.VideoHistory;
import com.megrez.vo.analytics_service.VideoWatched;
import com.megrez.vo.video_info_service.VideoVO;
import org.jetbrains.annotations.NotNull;
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
    private final UserServiceClient userServiceClient;
    private final SocialServiceClient socialServiceClient;
    private final VideoUtils videoUtils;

    public VideoInfoService(VideoMapper videoMapper, LikeFavoriteClient likeFavoriteClient, AnalyticsServiceClient analyticsServiceClient, UserServiceClient userServiceClient, SocialServiceClient socialServiceClient, VideoUtils videoUtils) {
        this.videoMapper = videoMapper;
        this.likeFavoriteClient = likeFavoriteClient;
        this.analyticsServiceClient = analyticsServiceClient;
        this.userServiceClient = userServiceClient;
        this.socialServiceClient = socialServiceClient;
        this.videoUtils = videoUtils;
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

        List<VideoVO> list = videoUtils.batchToVO(uid, videos);

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
        // 查询视频信息
        LambdaQueryWrapper<Video> query = new LambdaQueryWrapper<Video>().in(Video::getId, ids);
        List<Video> videos = videoMapper.selectList(query);

        // 排序
        Map<Integer, Video> videoMap = CollectionUtils.toMap(videos, Video::getId);
        videos = ids.stream().map(videoMap::get).filter(Objects::nonNull).toList();

        List<VideoVO> list = videoUtils.batchToVO(userId, videos);

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

        // 排序
        Map<Integer, Video> videoMap = CollectionUtils.toMap(videos, Video::getId);

        videos = videoIds.stream()
                .map(videoMap::get)
                .filter(Objects::nonNull)
                .toList();
        List<VideoVO> list = videoUtils.batchToVO(userId, videos);

        boolean hasMore = false;
        cursor = null;
        if (list.size() > 20) {
            hasMore = true;
            list = list.subList(0, list.size() - 1);
            cursor = list.get(list.size() - 1).getWatchedAt();
        }
        return Result.success(CursorLoad.of(list, hasMore, cursor == null ? null : cursor.toString()));
    }

    // 批量查询视频信息
    public Result<List<Video>> getVideoInfoByIds(List<Integer> vid) {
        if (vid.isEmpty()) {
            return Result.success(List.of());
        }
        List<Video> videos = getVideos(vid);
        return Result.success(videos);
    }

    public Result<VideoVO> getVideoInfoByIdV2(Integer userId, Integer vid) {
        // 查询视频数据
        Video video = videoMapper.selectById(vid);

        if (video == null) {
            return Result.success(null);
        }

        List<VideoVO> list = videoUtils.batchToVO(userId, List.of(video));

        return Result.success(list.get(0));
    }

    public Result<List<Video>> getRecentLikes(Integer userId, int count) {
        Result<List<VideoLikes>> recordsByCount = likeFavoriteClient.getRecordsByCount(userId, count);

        if (!recordsByCount.isSuccess()) {
            log.error("点赞/收藏服务调用失败");
            throw new RuntimeException();
        }
        List<VideoLikes> data = recordsByCount.getData();

        if (data.isEmpty()) {
            return Result.success(List.of());
        }

        List<Integer> vIds = CollectionUtils.toList(data, VideoLikes::getVideoId);
        List<Video> videos = getVideos(vIds);

        Map<Integer, Video> videoMap = CollectionUtils.toMap(videos, Video::getId);
        // 排序
        videos = vIds.stream()
                .map(videoMap::get)
                .filter(Objects::nonNull)
                .toList();
        return Result.success(videos);
    }


    public Result<List<Video>> getRecentFavorites(Integer userId, int count) {
        return Result.success(List.of());
    }

    public Result<List<Video>> getRecentHistories(Integer userId, int count) {
        Result<List<VideoHistory>> videoHistoryByCount = analyticsServiceClient.getVideoHistoryByCount(userId, count);
        if (!videoHistoryByCount.isSuccess()) {
            log.info("统计服务调用失败！");
            throw new RuntimeException();
        }

        List<VideoHistory> videoHistories = videoHistoryByCount.getData();

        if (videoHistories.isEmpty()) {
            return Result.success(List.of());
        }

        List<Integer> vIds = CollectionUtils.toList(videoHistories, VideoHistory::getVideoId);

        List<Video> videos = getVideos(vIds);

        Map<Integer, Video> videoMap = CollectionUtils.toMap(videos, Video::getId);

        List<Video> list = vIds.stream().map(videoMap::get).toList();

        return Result.success(list);
    }

    public Result<List<Video>> getRecentWorks(Integer userId, int count) {

        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<Video>()
                .eq(Video::getUploaderId, userId)
                .last("LIMIT " + count)
                .orderByDesc(Video::getPublishTime);
        List<Video> videos = videoMapper.selectList(wrapper);
        videos.forEach(v -> {
            v.setCoverName(GatewayHttpPath.VIDEO_COVER_IMG + v.getCoverName());
        });

        return Result.success(videos);
    }

    /**
     * 辅助方法，批量查询视频信息并转换播放链接和封面图链接
     *
     * @param vIds
     * @return
     */
    private @NotNull List<Video> getVideos(List<Integer> vIds) {
        List<Video> videos = videoMapper.selectList(new LambdaQueryWrapper<Video>()
                .in(Video::getId, vIds));

        videos.forEach(video -> {
            video.setVideoFilename(GatewayHttpPath.VIDEO_PLAY + video.getVideoFilename());
            video.setCoverName(GatewayHttpPath.VIDEO_COVER_IMG + video.getCoverName());
        });
        return videos;
    }

    public Result<List<VideoVO>> randomVideo(Integer uid) {
        List<Video> videos = videoMapper.selectList(new LambdaQueryWrapper<Video>().last("ORDER BY RAND() LIMIT 5"));
        List<VideoVO> list = videoUtils.batchToVO(uid, videos);
        return Result.success(list);
    }


//
//    public Result<List<Video>> getFavoriteInfoByUserId(Integer userId, Integer targetId) {
//    }
//
}
