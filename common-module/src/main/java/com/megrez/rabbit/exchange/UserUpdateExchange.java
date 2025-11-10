package com.megrez.rabbit.exchange;

public class UserUpdateExchange {
    // 用户资料更新交换机(FANOUT)
    public static final String FANOUT_EXCHANGE_USER_UPDATE = "user.update.exchange";

    /**
     * 更新搜索引擎队列
     * 当用户修改个人资料时，从该队列中消费消息
     * 更新搜索引擎中的数据
     */
    public static final String QUEUE_USER_UPDATE_SEARCH = "user.update.search.queue";
}
