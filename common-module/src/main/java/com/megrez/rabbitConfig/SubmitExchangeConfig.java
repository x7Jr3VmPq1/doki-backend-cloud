package com.megrez.rabbitConfig;

import com.megrez.constant.QueueConstants;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 视频相关RabbitMQ配置
 */
@Configuration
public class SubmitExchangeConfig {

    // 定义草稿提交事件交换机
    @Bean("videoSubmitExchange")
    public DirectExchange videoSubmitExchange() {
        return new DirectExchange(QueueConstants.DIRECT_EXCHANGE_VIDEO_SUBMIT);
    }

    // 定义草稿审核队列
    @Bean("draftAuditQueue")
    public Queue draftAuditQueue() {
        return new Queue(QueueConstants.QUEUE_DRAFT_AUDIT, true);
    }

    // 定义视频转码队列
    @Bean("videoProcessingQueue")
    public Queue videoProcessingQueue() {
        return new Queue(QueueConstants.QUEUE_VIDEO_PROCESSING, true);
    }

    // 定义视频发布队列
    @Bean("videoPublishQueue")
    public Queue videoPublishQueue() {
        return new Queue(QueueConstants.QUEUE_VIDEO_PUBLISH, true);
    }

    // 绑定队列到交换机
    @Bean
    public Binding bindingSubmitExchangeToAudit(
            @Qualifier("draftAuditQueue") Queue queue,
            @Qualifier("videoSubmitExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(QueueConstants.RK_DRAFT_AUDIT);
    }

    @Bean
    public Binding bindingSubmitExchangeToProcessing(
            @Qualifier("videoProcessingQueue") Queue queue,
            @Qualifier("videoSubmitExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(QueueConstants.RK_VIDEO_PROCESSING);
    }

    @Bean
    public Binding bindingSubmitExchangeToPublish(
            @Qualifier("videoPublishQueue") Queue queue,
            @Qualifier("videoSubmitExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(QueueConstants.RK_VIDEO_PUBLISH);
    }
}
