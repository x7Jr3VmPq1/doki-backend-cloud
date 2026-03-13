package com.megrez.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

@Component
public class FeedRedisClient {

    private final String USER_MODEL = "user:model:";

    private final StringRedisTemplate stringRedisTemplate;

    public FeedRedisClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    // 更新用户偏好模型
    public void refreshModel(Integer uid, String tag) {
        ZSetOperations<String, String> ops = stringRedisTemplate.opsForZSet();
        ops.add(USER_MODEL + uid, tag, System.currentTimeMillis());
    }
}
