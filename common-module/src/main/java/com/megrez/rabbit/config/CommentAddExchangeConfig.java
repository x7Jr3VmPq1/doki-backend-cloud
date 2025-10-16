package com.megrez.rabbit.config;

import com.megrez.rabbit.exchange.CommentAddExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommentAddExchangeConfig {
    
    // 定义评论添加交换机 (FANOUT类型)
    @Bean(CommentAddExchange.FANOUT_EXCHANGE_COMMENT_ADD)
    public FanoutExchange commentAddExchange() {
        return new FanoutExchange(CommentAddExchange.FANOUT_EXCHANGE_COMMENT_ADD);
    }

    // 定义评论添加统计队列
    @Bean(CommentAddExchange.QUEUE_COMMENT_ADD_ANALYTICS)
    public Queue commentAddAnalyticsQueue() {
        return new Queue(CommentAddExchange.QUEUE_COMMENT_ADD_ANALYTICS, true);
    }

    // 定义评论添加通知队列
    @Bean(CommentAddExchange.QUEUE_COMMENT_ADD_NOTIFICATION)
    public Queue commentAddNotificationQueue() {
        return new Queue(CommentAddExchange.QUEUE_COMMENT_ADD_NOTIFICATION, true);
    }

    // 绑定统计队列到交换机
    @Bean
    public Binding bindingCommentAddAnalyticsQueueToExchange(
            @Qualifier(CommentAddExchange.QUEUE_COMMENT_ADD_ANALYTICS) Queue queue,
            @Qualifier(CommentAddExchange.FANOUT_EXCHANGE_COMMENT_ADD) FanoutExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange);
    }

    // 绑定通知队列到交换机
    @Bean
    public Binding bindingCommentAddNotificationQueueToExchange(
            @Qualifier(CommentAddExchange.QUEUE_COMMENT_ADD_NOTIFICATION) Queue queue,
            @Qualifier(CommentAddExchange.FANOUT_EXCHANGE_COMMENT_ADD) FanoutExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange);
    }
}
