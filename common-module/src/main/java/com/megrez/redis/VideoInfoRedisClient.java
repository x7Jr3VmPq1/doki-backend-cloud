package com.megrez.redis;

import com.megrez.mysql_entity.Video;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class VideoInfoRedisClient {

    private static final Logger log = LoggerFactory.getLogger(VideoInfoRedisClient.class);
    private final RedisTemplate<String, String> redisTemplate;

    public VideoInfoRedisClient(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 添加新内容到时间线
     *
     * @param tid    目标时间线用户ID
     * @param videos 待添加的视频
     */
    public void pushFollowTimeline(Integer tid, List<Video> videos) {

        if (videos.isEmpty()) {
            return;
        }

        Set<ZSetOperations.TypedTuple<String>> tuples = new HashSet<>();

        for (Video video : videos) {
            tuples.add(ZSetOperations.TypedTuple.of(video.getUploaderId() + ":" + video.getId(), video.getCreatedTime().doubleValue()));
        }
        redisTemplate.opsForZSet().add("timeline:follow:" + tid, tuples);
    }

    public List<Integer> getFollowTimeline(Integer uid, Integer limit) {
        Map<String, Double> map = getZSetAsMapByScore("timeline:follow:" + uid, String.class, 0, System.currentTimeMillis(), limit);
        log.info("查询到TIMELINE：{}", map);
        return null;
    }


    public <V> Map<V, Double> getZSetAsMapByScore(
            String redisKey,
            Class<V> valueClass,
            double minScore,
            double maxScore,
            int limit) {

        Set<ZSetOperations.TypedTuple<String>> raw =
                redisTemplate.opsForZSet().reverseRangeByScoreWithScores(redisKey, minScore, maxScore - 0.1, 0, limit);

        Map<V, Double> result = new LinkedHashMap<>();
        if (raw != null) {
            for (ZSetOperations.TypedTuple<String> tuple : raw) {
                if (tuple.getValue() != null && tuple.getScore() != null) {
                    result.put(valueClass.cast(tuple.getValue()), tuple.getScore());
                }
            }
        }

        return result;
    }
}
