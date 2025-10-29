package com.megrez.service;

import com.megrez.redis.AnalyticsRedisClient;
import com.megrez.result.Result;
import com.megrez.utils.RedisUtils;
import com.megrez.vo.analytics_service.VideoHistoryVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VideoHistoryService {

    private final AnalyticsRedisClient analyticsRedisClient;

    public VideoHistoryService(AnalyticsRedisClient analyticsRedisClient) {
        this.analyticsRedisClient = analyticsRedisClient;
    }

    public Result<Void> updateVideoHistory(Integer userId, Integer videoId, Double time) {
        analyticsRedisClient.setHistory(userId, videoId, time);
        return Result.success(null);
    }

    public Result<List<VideoHistoryVO>> getVideoHistory(Integer userId) {
        return Result.success(analyticsRedisClient.getHistory(userId));
    }
}
