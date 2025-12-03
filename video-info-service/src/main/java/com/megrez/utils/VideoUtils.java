package com.megrez.utils;

import com.megrez.client.AnalyticsServiceClient;
import com.megrez.client.LikeFavoriteClient;
import com.megrez.client.UserServiceClient;
import com.megrez.constant.GatewayHttpPath;
import com.megrez.mysql_entity.Video;
import com.megrez.mysql_entity.VideoLikes;
import com.megrez.mysql_entity.VideoStatistics;
import com.megrez.result.Result;
import com.megrez.vo.analytics_service.VideoWatched;
import com.megrez.vo.user_service.UsersVO;
import com.megrez.vo.video_info_service.VideoVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class VideoUtils {

    private static final Logger log = LoggerFactory.getLogger(VideoUtils.class);
    private final AnalyticsServiceClient analyticsServiceClient;
    private final LikeFavoriteClient likeFavoriteClient;
    private final UserServiceClient userServiceClient;

    public VideoUtils(AnalyticsServiceClient analyticsServiceClient, LikeFavoriteClient likeFavoriteClient, UserServiceClient userServiceClient) {
        this.analyticsServiceClient = analyticsServiceClient;
        this.likeFavoriteClient = likeFavoriteClient;
        this.userServiceClient = userServiceClient;
    }

    /**
     * 这是一个用来批量把Video转换为VideoVO的方法
     *
     * @param uid  用户ID
     * @param list Video列表
     * @return VideoVO列表
     * @throws RuntimeException
     */
    public List<VideoVO> batchToVO(Integer uid, List<Video> list) throws RuntimeException {
        List<Integer> vIds = list.stream().map(Video::getId).toList();
        List<Integer> uploaderIds = list.stream().map(Video::getUploaderId).toList();
        // 获取观看时长
        Result<List<VideoWatched>> videoWatched = analyticsServiceClient.getVideoWatched(uid, vIds);
        // 获取统计数据
        Result<List<VideoStatistics>> videoStatById = analyticsServiceClient.getVideoStatById(vIds);
        // 获取视频上传者信息和是否关注信息
        Result<List<UsersVO>> userinfoByIdWithIfFollowed = userServiceClient.getUserinfoByIdWithIfFollowed(uid, uploaderIds);
        // 获取点赞情况
        Result<List<VideoLikes>> recordsByBatchVIds = likeFavoriteClient.getLikeRecordsByBatchVIds(uid, vIds);
        // 判断调用成功情况
        boolean success = Result.allSuccess(List.of(videoWatched, videoStatById, userinfoByIdWithIfFollowed, recordsByBatchVIds));
        if (!success) {
            throw new RuntimeException();
        }
        // 把调用结果转化为MAP
        Map<Integer, VideoWatched> videoWatchedMap = CollectionUtils.toMap(videoWatched.getData(), VideoWatched::getVideoId);
        Map<Integer, VideoStatistics> statisticsMap = CollectionUtils.toMap(videoStatById.getData(), VideoStatistics::getVideoId);
        Map<Integer, UsersVO> usersVOMap = CollectionUtils.toMap(userinfoByIdWithIfFollowed.getData(), UsersVO::getId);
        Map<Integer, VideoLikes> likesMap = CollectionUtils.toMap(recordsByBatchVIds.getData(), VideoLikes::getVideoId);

        return list.stream().map(v -> {
            v.setVideoFilename(GatewayHttpPath.VIDEO_PLAY + v.getVideoFilename());
            v.setCoverName(GatewayHttpPath.VIDEO_COVER_IMG + v.getCoverName());

            VideoWatched watched = videoWatchedMap.get(v.getId());
            VideoStatistics statistics = statisticsMap.get(v.getId());
            VideoLikes likes = likesMap.get(v.getId());
            UsersVO usersVO = usersVOMap.get(v.getUploaderId());
            VideoVO vo = new VideoVO();
            BeanUtils.copyProperties(v, vo);
            vo.setStatistics(statistics);
            vo.setLiked(likes != null);
            vo.setWatchedTime(watched == null ? 0 : watched.getTime());
            vo.setUser(usersVO);
            vo.setFollowed(usersVO.getFollowed());

            return vo;
        }).toList();
    }
}
