package com.megrez.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.megrez.mapper.UserStatisticsMapper;
import com.megrez.mysql_entity.User;
import com.megrez.mysql_entity.UserStatistics;
import com.megrez.rabbit.exchange.CountEventExchange;
import com.megrez.rabbit.message.CountMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class CountEventListener {

    private static final Logger log = LoggerFactory.getLogger(CountEventListener.class);
    private final UserStatisticsMapper userStatisticsMapper;

    public CountEventListener(UserStatisticsMapper userStatisticsMapper) {
        this.userStatisticsMapper = userStatisticsMapper;
    }


    private boolean check(CountMessage message) {
        if (message == null) {
            return false;
        }
        return message.getCount() != null && message.getUid() != null && message.getUid() > 0;
    }

    @RabbitListener(queues = CountEventExchange.FOLLOW)
    public void follow(CountMessage message) {
        if (!check(message))
            return;
        LambdaUpdateWrapper<UserStatistics> updateWrapper = new LambdaUpdateWrapper<UserStatistics>()
                .setSql("following_count = following_count + " + message.getCount())
                .eq(UserStatistics::getUserId, message.getUid());

        if (message.getCount() < 0) {
            updateWrapper.gt(UserStatistics::getFollowerCount, 0);
        }

        userStatisticsMapper.update(updateWrapper);
    }

    @RabbitListener(queues = CountEventExchange.FOLLOWED)
    public void followed(CountMessage message) {
        if (!check(message))
            return;

        LambdaUpdateWrapper<UserStatistics> updateWrapper = new LambdaUpdateWrapper<UserStatistics>()
                .setSql("follower_count = follower_count + " + message.getCount())
                .gt(UserStatistics::getFollowerCount, 0)
                .eq(UserStatistics::getUserId, message.getUid());
        userStatisticsMapper.update(updateWrapper);
    }
}
