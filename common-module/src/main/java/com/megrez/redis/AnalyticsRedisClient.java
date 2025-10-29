package com.megrez.redis;

import com.megrez.vo.analytics_service.VideoHistoryVO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.*;


@Component
public class AnalyticsRedisClient {
    private final RedisTemplate<String, Integer> redisTemplate;

    public AnalyticsRedisClient(RedisTemplate<String, Integer> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public static final String KEY_USER_HISTORY = "user:history:";
    public static final String KEY_USER_WATCHED = "user:watched:";

    /**
     * 设置历史记录
     *
     * @param userId  用户ID
     * @param videoId 视频ID
     * @param time    观看时长
     */
    public void setHistory(Integer userId, Integer videoId, Double time) {
        ZSetOperations<String, Integer> operations = redisTemplate.opsForZSet();
        // 按观看时间排序
        operations.add(KEY_USER_HISTORY + userId, videoId, System.currentTimeMillis());
        // 存储播放时长
        String key = KEY_USER_WATCHED + userId;
        redisTemplate.opsForHash().put(key, videoId, time);
    }

    public List<VideoHistoryVO> getHistory(Integer userId) {
        Map<Integer, Double> hashAsMap = getHashAsMap(KEY_USER_WATCHED + userId, Integer.class, Double.class);

        List<Integer> zSet = getZSet(KEY_USER_HISTORY + userId, 0, 10);

        return zSet.stream().map(e -> {
            VideoHistoryVO vo = new VideoHistoryVO();
            vo.setVideoId(e);
            vo.setWatchedTime(hashAsMap.get(e));
            return vo;
        }).toList();
    }

    public <K, V> Map<K, V> getHashAsMap(String redisKey, Class<K> keyClass, Class<V> valueClass) {
        Map<Object, Object> raw = redisTemplate.opsForHash().entries(redisKey);
        Map<K, V> result = new HashMap<>();
        for (Map.Entry<Object, Object> entry : raw.entrySet()) {
            result.put(keyClass.cast(entry.getKey()), valueClass.cast(entry.getValue()));
        }
        return result;
    }

    public List<Integer> getZSet(String redisKey, long start, long end) {
        Set<ZSetOperations.TypedTuple<Integer>> typedTuples = redisTemplate.opsForZSet().reverseRangeWithScores(redisKey, start, end);
        if (typedTuples == null) {
            return List.of();
        }
        return typedTuples.stream()
                .map(ZSetOperations.TypedTuple::getValue)
                .toList();
    }
}
