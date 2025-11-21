package com.megrez.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.megrez.mapper.VideoMapper;
import com.megrez.mysql_entity.Video;
import com.megrez.redis.VideoInfoRedisClient;
import com.megrez.result.Result;
import com.megrez.utils.VideoUtils;
import com.megrez.vo.video_info_service.VideoVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TimelineService {

    private final VideoInfoRedisClient redisClient;
    private final VideoMapper videoMapper;
    private final VideoUtils videoUtils;

    public TimelineService(VideoInfoRedisClient videoInfoRedisClient, VideoMapper videoMapper, VideoUtils videoUtils) {
        this.redisClient = videoInfoRedisClient;
        this.videoMapper = videoMapper;
        this.videoUtils = videoUtils;
    }

    public Result<List<VideoVO>> getFollowTimeline(Integer uid) {
        // 获取timeline
        List<Integer> followTimeline = redisClient.getFollowTimeline(uid, 20);
        if (followTimeline.isEmpty()) {
            return Result.success(List.of());
        }
        // 查询视频元数据
        List<Video> videos = videoMapper.selectBatchIds(followTimeline);

        // 按照 followTimeline 顺序重排
        Map<Integer, Video> videoMap = videos.stream()
                .collect(Collectors.toMap(Video::getId, v -> v));

        // 构造有序列表
        List<Video> sortedVideos = followTimeline.stream()
                .map(videoMap::get)
                .filter(Objects::nonNull) // 防止 timeline 中有数据库没有的 id
                .toList();

        List<VideoVO> videoVOS = videoUtils.batchToVO(uid, sortedVideos);
        return Result.success(videoVOS);
    }
}
