package com.megrez.constant;

/**
 * RabbitMQ队列和交换机常量
 */
public class QueueConstants {

    // 草稿交换机
    public static final String DIRECT_EXCHANGE_VIDEO_SUBMIT = "video.submit.exchange";

    // 审核队列
    public static final String QUEUE_DRAFT_AUDIT = "draft.audit.queue";
    public static final String RK_DRAFT_AUDIT = "draft.audit.key";

    // 转码队列
    public static final String QUEUE_VIDEO_PROCESSING = "video.processing.queue";
    public static final String RK_VIDEO_PROCESSING = "video.processing.key";

    // 发布视频队列
    public static final String QUEUE_VIDEO_PUBLISH = "video.publish.queue";
    public static final String RK_VIDEO_PUBLISH = "video.publish.key";

    // 视频发布后通知交换机 (FANOUT类型)
    public static final String FANOUT_EXCHANGE_VIDEO_PUBLISHED = "video.published.exchange";

    // 视频搜索队列
    public static final String QUEUE_VIDEO_SEARCH = "video.search.queue";

    // 通知队列
    public static final String QUEUE_NOTIFICATION = "notification.queue";

    // 统计分析队列
    public static final String QUEUE_ANALYTICS = "analytics.queue";
}
