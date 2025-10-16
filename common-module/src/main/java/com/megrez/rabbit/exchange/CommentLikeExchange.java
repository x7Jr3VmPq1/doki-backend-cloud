package com.megrez.rabbit.exchange;

public class CommentLikeExchange {

    // 评论点赞交换机 (FANOUT类型)
    public static final String FANOUT_EXCHANGE_COMMENT_LIKE = "comment.like.exchange";

    // 视频点赞通知队列
    public static final String QUEUE_COMMENT_LIKE_NOTIFICATION = "comment.like.notification.queue";

    // 视频点赞统计队列
    public static final String QUEUE_COMMENT_LIKE_ANALYTICS = "comment.like.analytics.queue";
}
