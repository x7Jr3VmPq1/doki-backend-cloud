package com.megrez.rabbit.exchange;

public class CommentAddExchange {

    // 评论添加交换机 (FANOUT类型)
    public static final String FANOUT_EXCHANGE_COMMENT_ADD = "comment.add.exchange";

    // 评论通知队列
    public static final String QUEUE_COMMENT_ADD_NOTIFICATION = "comment.add.notification.queue";

    // 评论统计队列
    public static final String QUEUE_COMMENT_ADD_ANALYTICS = "comment.add.analytics.queue";
}
