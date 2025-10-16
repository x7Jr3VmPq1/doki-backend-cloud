package com.megrez.rabbit.config;

import com.megrez.rabbit.exchange.CommentLikeExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommentLikeExchangeConfig {
    
    // 定义评论点赞交换机 (FANOUT类型)
    @Bean(CommentLikeExchange.FANOUT_EXCHANGE_COMMENT_LIKE)
    public FanoutExchange commentLikeExchange() {
        return new FanoutExchange(CommentLikeExchange.FANOUT_EXCHANGE_COMMENT_LIKE);
    }

    // 定义评论点赞统计队列
    @Bean(CommentLikeExchange.QUEUE_COMMENT_LIKE_ANALYTICS)
    public Queue commentLikeAnalyticsQueue() {
        return new Queue(CommentLikeExchange.QUEUE_COMMENT_LIKE_ANALYTICS, true);
    }

    // 定义评论点赞通知队列
    @Bean(CommentLikeExchange.QUEUE_COMMENT_LIKE_NOTIFICATION)
    public Queue commentLikeNotificationQueue() {
        return new Queue(CommentLikeExchange.QUEUE_COMMENT_LIKE_NOTIFICATION, true);
    }

    // 绑定统计队列到交换机
    @Bean
    public Binding bindingCommentLikeAnalyticsQueueToExchange(
            @Qualifier(CommentLikeExchange.QUEUE_COMMENT_LIKE_ANALYTICS) Queue queue,
            @Qualifier(CommentLikeExchange.FANOUT_EXCHANGE_COMMENT_LIKE) FanoutExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange);
    }

    // 绑定通知队列到交换机
    @Bean
    public Binding bindingCommentLikeNotificationQueueToExchange(
            @Qualifier(CommentLikeExchange.QUEUE_COMMENT_LIKE_NOTIFICATION) Queue queue,
            @Qualifier(CommentLikeExchange.FANOUT_EXCHANGE_COMMENT_LIKE) FanoutExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange);
    }
}
