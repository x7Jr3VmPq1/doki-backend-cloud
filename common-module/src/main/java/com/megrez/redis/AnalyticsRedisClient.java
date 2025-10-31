package com.megrez.redis;

import com.megrez.vo.analytics_service.VideoHistory;
import com.megrez.vo.analytics_service.VideoWatched;
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

    /**
     * 获取视频历史记录
     *
     * @param userId 用户ID
     * @return 历史记录
     */
    public List<VideoHistory> getHistory(Integer userId, Double score) {

        Map<Integer, Double> historyMap = getZSetAsMapByScore(KEY_USER_HISTORY + userId, Integer.class, 0, score, 20 + 1);

        ArrayList<VideoHistory> videoHistories = new ArrayList<>();
        historyMap.forEach((key, value) -> {
            videoHistories.add(new VideoHistory(key, value.longValue()));
        });
        return videoHistories;
    }

    public void clearHistory(Integer userId) {
        redisTemplate.delete(KEY_USER_HISTORY + userId);
    }

    /**
     * 获取观看时长
     *
     * @param userId 用户ID
     * @param vIds   视频ID集合
     * @return 观看时长记录
     */
    public List<VideoWatched> getWatched(Integer userId, List<Integer> vIds) {
        Map<Integer, Double> multiAsMap = getMultiAsMap(KEY_USER_WATCHED + userId, vIds, Integer.class, Double.class);
        ArrayList<VideoWatched> watched = new ArrayList<>();
        multiAsMap.forEach((key, value) -> {
            VideoWatched videoWatched = new VideoWatched(key, value);
            watched.add(videoWatched);
        });
        return watched;
    }

    public <K, V> Map<K, V> getHashAsMap(String redisKey, Class<K> keyClass, Class<V> valueClass) {
        Map<Object, Object> raw = redisTemplate.opsForHash().entries(redisKey);
        Map<K, V> result = new HashMap<>();
        for (Map.Entry<Object, Object> entry : raw.entrySet()) {
            result.put(keyClass.cast(entry.getKey()), valueClass.cast(entry.getValue()));
        }
        return result;
    }

    public <K, V> Map<K, V> getMultiAsMap(String redisKey, List<K> hashKeys, Class<K> keyClass, Class<V> valueClass) {
        // 获取多个 key 对应的值
        List<Object> objects = redisTemplate.opsForHash().multiGet(redisKey, Arrays.asList(hashKeys.toArray()));

        HashMap<K, V> kvHashMap = new HashMap<>();
        for (int i = 0; i < hashKeys.size(); i++) {
            Object value = objects.get(i);
            if (value != null) {
                kvHashMap.put(keyClass.cast(hashKeys.get(i)), valueClass.cast(value));
            }
        }
        return kvHashMap;
    }


    public <V> Map<V, Double> getZSetAsMapByScore(
            String redisKey,
            Class<V> valueClass,
            double minScore,
            double maxScore,
            int limit) {

        Set<ZSetOperations.TypedTuple<Integer>> raw =
                redisTemplate.opsForZSet().reverseRangeByScoreWithScores(redisKey, minScore, maxScore - 0.1, 0, limit);

        Map<V, Double> result = new LinkedHashMap<>();
        if (raw != null) {
            for (ZSetOperations.TypedTuple<Integer> tuple : raw) {
                if (tuple.getValue() != null && tuple.getScore() != null) {
                    result.put(valueClass.cast(tuple.getValue()), tuple.getScore());
                }
            }
        }

        return result;
    }

}
