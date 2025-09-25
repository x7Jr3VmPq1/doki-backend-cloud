package com.megrez.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    // 交换机名称
    public static final String DIRECT_EXCHANGE_VIDEO_SUBMIT = "video.submit.exchange";

    // 审核队列名
    public static final String QUEUE_DRAFT_AUDIT = "draft.audit.queue";
    public static final String RK_DRAFT_AUDIT = "draft.audit.key";
    // 转码队列名
    public static final String QUEUE_VIDEO_PROCESSING = "video.processing.queue";
    public static final String RK_VIDEO_PROCESSING = "video.processing.key";
    // 发布视频队列
    public static final String QUEUE_VIDEO_PUBLISH = "video.publish.queue";
    public static final String RK_VIDEO_PUBLISH = "video.publish.key";

    // 定义草稿提交事件交换机
    @Bean("videoSubmitExchange")
    public DirectExchange videoSubmitExchange() {
        return new DirectExchange(DIRECT_EXCHANGE_VIDEO_SUBMIT);
    }

    // 定义草稿审核队列
    @Bean("draftAuditQueue")
    public Queue draftAuditQueue() {
        return new Queue(QUEUE_DRAFT_AUDIT, true);
    }

    // 定义视频转码队列
    @Bean("videoProcessingQueue")
    public Queue videoProcessingQueue() {
        return new Queue(QUEUE_VIDEO_PROCESSING, true);
    }

    // 定义视频发布队列
    @Bean("videoPublishQueue")
    public Queue videoPublishQueue() {
        return new Queue(QUEUE_VIDEO_PUBLISH, true);
    }

    // 绑定队列到交换机
    @Bean
    public Binding bindingSubmitExchangeToAudit(
            @Qualifier("draftAuditQueue") Queue queue,
            @Qualifier("videoSubmitExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(RK_DRAFT_AUDIT);
    }

    @Bean
    public Binding bindingSubmitExchangeToProcessing(
            @Qualifier("videoProcessingQueue") Queue queue,
            @Qualifier("videoSubmitExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(RK_VIDEO_PROCESSING);
    }

    @Bean
    public Binding bindingSubmitExchangeToPublish(
            @Qualifier("videoPublishQueue") Queue queue,
            @Qualifier("videoSubmitExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(RK_VIDEO_PUBLISH);
    }


}
