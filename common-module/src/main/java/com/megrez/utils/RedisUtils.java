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
