package com.megrez.rabbitConfig;

import com.megrez.constant.QueueConstants;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 视频发布后通知相关RabbitMQ配置
 * 使用FANOUT交换机，实现一对多消息分发
 */
@Configuration
public class VideoPublishedExchangeConfig {

    // 定义视频发布后通知交换机 (FANOUT类型)
    @Bean("videoPublishedExchange")
    public FanoutExchange videoPublishedExchange() {
        return new FanoutExchange(QueueConstants.FANOUT_EXCHANGE_VIDEO_PUBLISHED);
    }

    // 定义视频搜索队列
    @Bean("videoSearchQueue")
    public Queue videoSearchQueue() {
        return new Queue(QueueConstants.QUEUE_VIDEO_SEARCH, true);
    }

    // 定义通知队列
    @Bean("notificationQueue")
    public Queue notificationQueue() {
        return new Queue(QueueConstants.QUEUE_NOTIFICATION, true);
    }

    // 定义统计分析队列
    @Bean("analyticsQueue")
    public Queue analyticsQueue() {
        return new Queue(QueueConstants.QUEUE_ANALYTICS, true);
    }

    // 绑定视频搜索队列到交换机 (FANOUT不需要路由键)
    @Bean
    public Binding bindingVideoSearchToPublished(
            @Qualifier("videoSearchQueue") Queue queue,
            @Qualifier("videoPublishedExchange") FanoutExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange);
    }

    // 绑定通知队列到交换机
    @Bean
    public Binding bindingNotificationToPublished(
            @Qualifier("notificationQueue") Queue queue,
            @Qualifier("videoPublishedExchange") FanoutExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange);
    }

    // 绑定统计分析队列到交换机
    @Bean
    public Binding bindingAnalyticsToPublished(
            @Qualifier("analyticsQueue") Queue queue,
            @Qualifier("videoPublishedExchange") FanoutExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange);
    }
}
