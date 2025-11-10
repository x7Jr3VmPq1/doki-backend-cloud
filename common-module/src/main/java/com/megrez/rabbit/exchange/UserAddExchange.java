package com.megrez.rabbit.exchange;

public class UserAddExchange {
    // 用户资料更新交换机(FANOUT)
    public static final String FANOUT_EXCHANGE_USER_ADD = "user.add.exchange";

    /**
     * 更新搜索引擎队列
     * 当新增用户时，从该队列中消费消息
     * 更新搜索引擎中的数据
     */
    public static final String QUEUE_USER_ADD_SEARCH = "user.update.add.queue";
}
