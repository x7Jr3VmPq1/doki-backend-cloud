package com.megrez.rabbit.exchange;

public class VideoLikeExchange {

    // 视频点赞交换机 (FANOUT类型)
    public static final String FANOUT_EXCHANGE_VIDEO_LIKE = "video.like.exchange";

    // 视频点赞通知队列
    public static final String QUEUE_VIDEO_LIKE_NOTIFICATION = "video.like.notification.queue";

    // 视频点赞统计队列
    public static final String QUEUE_VIDEO_LIKE_ANALYTICS = "video.like.analytics.queue";
}
