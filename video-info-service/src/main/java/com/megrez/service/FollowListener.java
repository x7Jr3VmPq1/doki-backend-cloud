package com.megrez.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.megrez.mapper.VideoMapper;
import com.megrez.mysql_entity.UserFollow;
import com.megrez.mysql_entity.Video;
import com.megrez.rabbit.exchange.SocialFollowExchange;
import com.megrez.redis.VideoInfoRedisClient;
import com.megrez.utils.CollectionUtils;
import com.megrez.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用来处理关注消息的方法，添加或移除操作用户的时间线内容。
 */
@Service
public class FollowListener {

    private static final Logger log = LoggerFactory.getLogger(FollowListener.class);
    private final VideoInfoRedisClient redisClient;
    private final VideoMapper videoMapper;

    public FollowListener(VideoInfoRedisClient redisClient, VideoMapper videoMapper) {
        this.redisClient = redisClient;
        this.videoMapper = videoMapper;
    }

    @RabbitListener(queues = SocialFollowExchange.QUEUE_SOCIAL_FOLLOW_TIMELINE)
    public void handleTimeline(String message) {

        UserFollow userFollow = JSONUtils.fromJSON(message, UserFollow.class);
        if (userFollow.getIsDeleted() == 0) {
            // 添加最近的五条动态到时间线
            Integer followingId = userFollow.getFollowingId();
            Integer followerId = userFollow.getFollowerId();

            LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<Video>()
                    .eq(Video::getUploaderId, followingId)
                    .orderByDesc(Video::getId)
                    .last("LIMIT 5");
            List<Video> videos = videoMapper.selectList(wrapper);


            redisClient.pushFollowTimeline(followerId, videos);
        }

    }
}
