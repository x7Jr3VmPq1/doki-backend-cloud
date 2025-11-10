package com.megrez.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Configuration
public class RabbitMQUtils {


    private static final Logger log = LoggerFactory.getLogger(RabbitMQUtils.class);
    private final RabbitTemplate rabbitTemplate;

    public RabbitMQUtils(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发送消息到指定交换机和路由键
     *
     * @param exchange   交换机名称
     * @param routingKey 路由键
     * @param message    消息内容
     */
    public <T> void sendMessage(String exchange, String routingKey, T message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
        RabbitMQUtils.log.info("Sent: '{}' to exchange: '{}' with routing key: '{}'", message, exchange, routingKey);
    }
}
