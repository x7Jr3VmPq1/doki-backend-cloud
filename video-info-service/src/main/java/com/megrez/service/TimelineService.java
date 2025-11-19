package com.megrez.service;

import com.megrez.redis.VideoInfoRedisClient;
import com.megrez.result.Result;
import com.megrez.vo.video_info_service.VideoVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TimelineService {

    private final VideoInfoRedisClient redisClient;

    public TimelineService(VideoInfoRedisClient videoInfoRedisClient) {
        this.redisClient = videoInfoRedisClient;
    }

    public Result<List<VideoVO>> getFollowTimeline(Integer uid) {
        List<Integer> followTimeline = redisClient.getFollowTimeline(uid, 20);
        return Result.success(List.of());
    }
}
