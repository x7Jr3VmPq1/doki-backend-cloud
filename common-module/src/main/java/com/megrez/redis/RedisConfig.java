package com.megrez.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Integer> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Integer> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        // key序列化
        template.setKeySerializer(template.getStringSerializer());
        // value序列化为 Integer
        template.setValueSerializer(new GenericToStringSerializer<>(Integer.class));
        template.afterPropertiesSet();
        return template;
    }
}
