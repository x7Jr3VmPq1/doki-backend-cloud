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

    public VideoInfoService(VideoMapper videoMapper, LikeFavoriteClient likeFavoriteClient, AnalyticsServiceClient analyticsServiceClient, UserServiceClient userServiceClient, SocialServiceClient socialServiceClient) {
        this.videoMapper = videoMapper;
        this.likeFavoriteClient = likeFavoriteClient;
        this.analyticsServiceClient = analyticsServiceClient;
        this.userServiceClient = userServiceClient;
        this.socialServiceClient = socialServiceClient;
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

    // 批量查询视频信息
    public Result<List<Video>> getVideoInfoByIds(List<Integer> vid) {
        if (vid.isEmpty()) {
            return Result.success(List.of());
        }
        List<Video> videos = getVideos(vid);

        return Result.success(videos);
    }

    public Result<VideoVO> getVideoInfoByIdV2(Integer userId, Integer vid) {
        // 1. 查询视频数据
        Video video = videoMapper.selectById(vid);

        if (video == null) {
            return Result.success(null);
        }
        video.setCoverName(GatewayHttpPath.VIDEO_COVER_IMG + video.getCoverName());
        video.setVideoFilename(GatewayHttpPath.VIDEO_PLAY + video.getVideoFilename());
        // 2. 查询统计信息
        Result<List<VideoStatistics>> stat = analyticsServiceClient.getVideoStatById(List.of(vid));
        // 3. 查询上传者信息
        Result<List<User>> userinfo = userServiceClient.getUserinfoById(List.of(video.getUploaderId()));

        if (!stat.isSuccess() || !userinfo.isSuccess()) {
            log.error("外部服务调用失败");
            throw new RuntimeException();
        }

        // 4. 查询是否点赞
        // 5. 查询上次观看时长和观看的时间
        // 6. 查询是否关注了上传者
        boolean liked = false;
        boolean followed = false;
        double watchedTime = 0;
        long watchedAt = 0;
        if (userId > 0) {
            Result<Boolean> likedResult = likeFavoriteClient.existLikeRecord(userId, vid);
            Result<List<VideoWatched>> videoWatchedResult = analyticsServiceClient.getVideoWatched(userId, List.of(vid));
            if (!userId.equals(video.getUploaderId())) {
                Result<List<UserFollow>> follow = socialServiceClient.checkFollow(new CheckFollowDTO(userId, List.of(video.getUploaderId())));
                if (!follow.isSuccess()) {
                    log.error("关系服务调用失败");
                    throw new RuntimeException();
                }
                followed = !follow.getData().isEmpty(); // 如果为空，说明没有关注
            }
            if (!likedResult.isSuccess() || !videoWatchedResult.isSuccess()) {
                log.error("外部服务调用失败");
                throw new RuntimeException();
            }
            liked = likedResult.getData();
            watchedTime = videoWatchedResult.getData().get(0).getTime();

        }
        // 7. 聚合
        VideoVO vo = new VideoVO();
        BeanUtils.copyProperties(video, vo);
        vo.setStatistics(stat.getData().get(0));
        vo.setUser(userinfo.getData().get(0));
        vo.setLiked(liked);
        vo.setFollowed(followed);
        vo.setWatchedTime(watchedTime);
        vo.setWatchedAt(watchedAt);
        return Result.success(vo);
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
        return null;
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
        return null;
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


//
//    public Result<List<Video>> getFavoriteInfoByUserId(Integer userId, Integer targetId) {
//    }
//
}
