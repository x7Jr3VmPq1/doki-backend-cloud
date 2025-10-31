package com.megrez.service;

import com.megrez.redis.AnalyticsRedisClient;
import com.megrez.result.Result;
import com.megrez.vo.analytics_service.VideoHistory;
import com.megrez.vo.analytics_service.VideoWatched;
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

    public Result<List<VideoHistory>> getVideoHistory(Integer userId, Long cursor) {
        return Result.success(analyticsRedisClient.getHistory(userId, cursor == null ? System.currentTimeMillis() : cursor.doubleValue()));
    }

    public Result<List<VideoWatched>> getVideoWatched(Integer userId, List<Integer> videoId) {
        return Result.success(analyticsRedisClient.getWatched(userId, videoId));
    }

    public Result<Void> clearVideoHistory(Integer userId, Integer close) {
        analyticsRedisClient.clearHistory(userId);
        return Result.success(null);
    }
}
