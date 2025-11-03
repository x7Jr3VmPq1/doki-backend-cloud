package com.megrez.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtils {
    private final StringRedisTemplate stringRedisTemplate;

    public RedisUtils(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    // 设置一个永久KV
    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    // 设置一个带过期时间的KV
    public void set(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    // 设置一个锁
    public Boolean setLock(String key, String value, long timeout, TimeUnit unit) {
        return stringRedisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
    }

    /**
     * 如果 key 不存在就设置 value，否则返回已有的 value
     *
     * @param key   Redis key
     * @param value 要设置的 value
     * @return 如果 key 已存在返回原 value，否则返回 null
     */
    public String setIfAbsentReturnExisting(String key, String value, long timeout, TimeUnit unit) {
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
        if (success != null && success) {
            // key 不存在，设置成功
            return null;
        } else {
            // key 已存在，返回原 value
            return stringRedisTemplate.opsForValue().get(key);
        }
    }


    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public void del(String key) {
        stringRedisTemplate.delete(key);
    }

    // 基本计数器
    public void incr(String key) {
        stringRedisTemplate.opsForValue().increment(key);
    }

}
