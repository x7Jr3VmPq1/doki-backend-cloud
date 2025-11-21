package com.megrez.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class SocialRedisClient {

    private static final Logger log = LoggerFactory.getLogger(SocialRedisClient.class);
    private final RedisTemplate<String, Integer> redisTemplate;

    public SocialRedisClient(RedisTemplate<String, Integer> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    public void addFollowing(Integer uid, Integer tid) {
        redisTemplate.opsForSet().add("user:follow:" + uid, tid);
    }

    public void removeFollowing(Integer uid, Integer tid) {
        redisTemplate.opsForSet().remove("user:follow:" + uid, tid);
    }
}
