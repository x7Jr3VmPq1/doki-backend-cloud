package com.megrez.rabbit.exchange;

public class VideoPublishedExchange {
    // 视频发布后通知交换机 (FANOUT类型)
    public static final String FANOUT_EXCHANGE_VIDEO_PUBLISHED = "video.published.exchange";

    // 视频搜索队列
    public static final String QUEUE_VIDEO_SEARCH = "video.search.queue";

    // 通知队列
    public static final String QUEUE_NOTIFICATION = "notification.queue";

    // 统计分析队列
    public static final String QUEUE_ANALYTICS = "analytics.queue";
}
