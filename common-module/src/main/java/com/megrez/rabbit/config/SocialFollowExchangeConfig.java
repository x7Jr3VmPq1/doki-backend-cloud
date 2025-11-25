package com.megrez.rabbit.config;

import com.megrez.rabbit.exchange.SocialFollowExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SocialFollowExchangeConfig {
    
    // 定义社交关注交换机 (FANOUT类型)
    @Bean(SocialFollowExchange.FANOUT_EXCHANGE_SOCIAL_FOLLOW)
    public FanoutExchange socialFollowExchange() {
        return new FanoutExchange(SocialFollowExchange.FANOUT_EXCHANGE_SOCIAL_FOLLOW);
    }

    // 定义社交关注通知队列
    @Bean(SocialFollowExchange.QUEUE_SOCIAL_FOLLOW_NOTIFICATION)
    public Queue socialFollowNotificationQueue() {
        return new Queue(SocialFollowExchange.QUEUE_SOCIAL_FOLLOW_NOTIFICATION, true);
    }

    // 定义时间线队列
    @Bean(SocialFollowExchange.QUEUE_SOCIAL_FOLLOW_TIMELINE)
    public Queue socialFollowTimelineQueue() {
        return new Queue(SocialFollowExchange.QUEUE_SOCIAL_FOLLOW_TIMELINE, true);
    }

    // 绑定通知队列到交换机
    @Bean
    public Binding bindingSocialFollowNotificationQueueToExchange(
            @Qualifier(SocialFollowExchange.QUEUE_SOCIAL_FOLLOW_NOTIFICATION) Queue queue,
            @Qualifier(SocialFollowExchange.FANOUT_EXCHANGE_SOCIAL_FOLLOW) FanoutExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange);
    }

    // 绑定时间线队列到交换机
    @Bean
    public Binding bindingSocialFollowTimelineQueueToExchange(
            @Qualifier(SocialFollowExchange.QUEUE_SOCIAL_FOLLOW_TIMELINE) Queue queue,
            @Qualifier(SocialFollowExchange.FANOUT_EXCHANGE_SOCIAL_FOLLOW) FanoutExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange);
    }
}
