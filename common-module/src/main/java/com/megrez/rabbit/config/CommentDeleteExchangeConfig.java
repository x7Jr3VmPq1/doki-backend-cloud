package com.megrez.rabbit.config;

import com.megrez.rabbit.exchange.CommentDeleteExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommentDeleteExchangeConfig {

    // 定义评论删除交换机 (FANOUT类型)
    @Bean(CommentDeleteExchange.FANOUT_EXCHANGE_COMMENT_DELETE)
    public FanoutExchange commentDeleteExchange() {
        return new FanoutExchange(CommentDeleteExchange.FANOUT_EXCHANGE_COMMENT_DELETE);
    }

    // 定义评论删除统计队列
    @Bean(CommentDeleteExchange.QUEUE_COMMENT_DELETE_ANALYTICS)
    public Queue commentDeleteAnalyticsQueue() {
        return new Queue(CommentDeleteExchange.QUEUE_COMMENT_DELETE_ANALYTICS, true);
    }

    // 定义评论删除图片队列
    @Bean(CommentDeleteExchange.QUEUE_COMMENT_DELETE_IMAGE)
    public Queue commentDeleteImageQueue() {
        return new Queue(CommentDeleteExchange.QUEUE_COMMENT_DELETE_IMAGE);
    }

    // 绑定统计队列到交换机
    @Bean
    public Binding bindingCommentDeleteAnalyticsQueueToExchange(
            @Qualifier(CommentDeleteExchange.QUEUE_COMMENT_DELETE_ANALYTICS) Queue queue,
            @Qualifier(CommentDeleteExchange.FANOUT_EXCHANGE_COMMENT_DELETE) FanoutExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange);
    }

    // 绑定图片服务队列到交换机
    @Bean
    public Binding bindingCommentDeleteImageQueueToExchange(
            @Qualifier(CommentDeleteExchange.QUEUE_COMMENT_DELETE_IMAGE) Queue queue,
            @Qualifier(CommentDeleteExchange.FANOUT_EXCHANGE_COMMENT_DELETE) FanoutExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange);
    }

}
