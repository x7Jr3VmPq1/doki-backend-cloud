package com.megrez.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    // 交换机名称
    public static final String EXCHANGE_VIDEO_SUBMIT = "video.submit.exchange";
    // 审核队列名
    public static final String QUEUE_DRAFT_AUDIT = "draft.audit.queue";
    // 转码队列名
    public static final String QUEUE_VIDEO_PROCESSING = "video.processing.queue";
    // 发布视频队列
    public static final String QUEUE_VIDEO_PUBLISH = "video.publish.queue";

    // 发布通知队列

    // 定义交换机
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_VIDEO_SUBMIT);
    }

    // 定义队列
    @Bean
    public Queue queue() {
        return new Queue(QUEUE_DRAFT_AUDIT, true);
    }

    // 绑定队列到交换机
    @Bean
    public Binding binding(Queue myQueue, DirectExchange myExchange) {
        return BindingBuilder.bind(myQueue)
                .to(myExchange)
                .with("my.routing.key"); // 指定路由键
    }


}
