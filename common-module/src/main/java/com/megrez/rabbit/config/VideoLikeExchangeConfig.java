package com.megrez.rabbit.config;

import com.megrez.rabbit.exchange.VideoLikeExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VideoLikeExchangeConfig {
    // 定义视频点赞交换机 (FANOUT类型)
    @Bean(VideoLikeExchange.FANOUT_EXCHANGE_VIDEO_LIKE)
    public FanoutExchange videoLikeExchange() {
        return new FanoutExchange(VideoLikeExchange.FANOUT_EXCHANGE_VIDEO_LIKE);
    }

    // 定义点赞统计队列
    @Bean(VideoLikeExchange.QUEUE_VIDEO_LIKE_ANALYTICS)
    public Queue videoLikeAnalyticsQueue() {
        return new Queue(VideoLikeExchange.QUEUE_VIDEO_LIKE_ANALYTICS, true);
    }

    // 定义点赞通知队列
    @Bean(VideoLikeExchange.QUEUE_VIDEO_LIKE_NOTIFICATION)
    public Queue VideoLikeNotificationQueue() {
        return new Queue(VideoLikeExchange.QUEUE_VIDEO_LIKE_NOTIFICATION, true);
    }

    // 绑定队列到交换机
    @Bean
    public Binding bindingAnalyticsQueueToExchange(
            @Qualifier(VideoLikeExchange.QUEUE_VIDEO_LIKE_ANALYTICS) Queue queue,
            @Qualifier(VideoLikeExchange.FANOUT_EXCHANGE_VIDEO_LIKE) FanoutExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange);
    }
    @Bean
    public Binding bindingNotificationQueueToExchange(
            @Qualifier(VideoLikeExchange.QUEUE_VIDEO_LIKE_NOTIFICATION) Queue queue,
            @Qualifier(VideoLikeExchange.FANOUT_EXCHANGE_VIDEO_LIKE) FanoutExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange);
    }

}
