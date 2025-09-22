package com.megrez.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
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
    public void sendMessage(String exchange, String routingKey, String message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
        RabbitMQUtils.log.info("Sent: '{}' to exchange: '{}' with routing key: '{}'", message, exchange, routingKey);
    }
}
