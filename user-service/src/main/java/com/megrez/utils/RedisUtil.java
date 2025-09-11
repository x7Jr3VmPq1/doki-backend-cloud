package com.megrez.utils;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisTemplate redisTemplate;

    public RedisUtil(StringRedisTemplate stringRedisTemplate, RedisTemplate redisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisTemplate = redisTemplate;
    }

    // 设置一个永久KV
    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    // 设置一个带过期时间的KV
    public void set(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
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
    // 搜索记录计数器

    public void recordSearch(String keyword) {
        String redisKey = RedisConstant.SEARCH_COUNT_KEY + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE); // e.g., 20250528
        stringRedisTemplate.opsForHash().increment(redisKey, keyword, 1);
    }

}
