package com.megrez.rabbit.exchange;

public class SocialFollowExchange {
    // 定义社交关系关注交换机（FANOUT类型）
    public static final String FANOUT_EXCHANGE_SOCIAL_FOLLOW = "social.follow.exchange";
    // 定义社交关系关注队列
    public static final String QUEUE_SOCIAL_FOLLOW_NOTIFICATION = "social.follow.notification.queue";
}
