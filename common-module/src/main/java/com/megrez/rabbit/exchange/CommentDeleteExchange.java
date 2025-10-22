package com.megrez.rabbit.exchange;

public class CommentDeleteExchange {

    // 评论删除交换机 (FANOUT类型)
    public static final String FANOUT_EXCHANGE_COMMENT_DELETE = "comment.delete.exchange";

    // 评论删除通知队列
    // public static final String QUEUE_COMMENT_DELETE_NOTIFICATION = "comment.delete.notification.queue";

    // 评论删除统计队列
    public static final String QUEUE_COMMENT_DELETE_ANALYTICS = "comment.delete.analytics.queue";

    // 评论删除图片服务队列
    public static final String QUEUE_COMMENT_DELETE_IMAGE = "comment.delete.image.queue";
}
