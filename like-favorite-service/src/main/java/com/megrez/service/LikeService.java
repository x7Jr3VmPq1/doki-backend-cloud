package com.megrez.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.megrez.mapper.StatMapper;
import com.megrez.mysql_entity.VideoLikes;
import com.megrez.mapper.LikeMapper;
import com.megrez.mysql_entity.VideoStatistics;
import com.megrez.rabbit.message.VideoLikeMessage;
import com.megrez.rabbit.exchange.VideoLikeExchange;
import com.megrez.redis.FeedRedisClient;
import com.megrez.result.Result;
import com.megrez.utils.JSONUtils;
import com.megrez.utils.RabbitMQUtils;
import com.megrez.utils.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LikeService {
    private static final Logger log = LoggerFactory.getLogger(LikeService.class);
    private final LikeMapper likeMapper;
    private final RabbitMQUtils rabbitMQUtils;
    private final FeedRedisClient feedRedisClient;
    private final StatMapper statMapper;

    public LikeService(LikeMapper likeMapper, RabbitMQUtils rabbitMQUtils, RedisUtils redisUtils, FeedRedisClient feedRedisClient, StatMapper statMapper) {
        this.likeMapper = likeMapper;
        this.rabbitMQUtils = rabbitMQUtils;
        this.feedRedisClient = feedRedisClient;
        this.statMapper = statMapper;
    }


    /**
     * 给视频添加点赞记录方法
     *
     * @param userId  用户ID
     * @param videoId 视频ID
     * @return
     */
    public Result<Void> addLikeRecord(Integer userId, Integer videoId) {
        // 先查询是否存在了点赞记录
        List<VideoLikes> videoLikes = likeMapper.selectList(
                new LambdaQueryWrapper<VideoLikes>()
                        .eq(VideoLikes::getUserId, userId)
                        .eq(VideoLikes::getVideoId, videoId)
        );
        // 构建消息对象
        VideoLikeMessage videoLikeMessage = new VideoLikeMessage();
        videoLikeMessage.setVideoId(videoId);
        videoLikeMessage.setUserId(userId);
        if (videoLikes.isEmpty()) {
            VideoLikes likes = VideoLikes.builder()
                    .videoId(videoId)
                    .userId(userId)
                    .createdAt(System.currentTimeMillis())
                    .build();
            likeMapper.insert(likes);
            // 消息类型：点赞
            videoLikeMessage.setType(1);

            // 更新用户偏好
            // 先查询对应的标签
            LambdaQueryWrapper<VideoStatistics> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(VideoStatistics::getVideoId, videoId).last("LIMIT 1");
            VideoStatistics statistics = statMapper.selectOne(queryWrapper);
            // 更新模型
            if (!statistics.getTags().isEmpty()) {
                feedRedisClient.refreshModel(userId, statistics.getTags());
            }
        } else {
            // 存在，删除记录
            likeMapper.delete(
                    new LambdaQueryWrapper<VideoLikes>()
                            .eq(VideoLikes::getUserId, userId)
                            .eq(VideoLikes::getVideoId, videoId)
            );
            // 消息类型：取消点赞
            videoLikeMessage.setType(0);
        }
        // 发送消息
        rabbitMQUtils.sendMessage(
                VideoLikeExchange.FANOUT_EXCHANGE_VIDEO_LIKE,
                "",
                JSONUtils.toJSON(videoLikeMessage)
        );
        return Result.success(null);
    }

    /**
     * 判断用户是否点赞了某个视频
     *
     * @param userId  用户ID
     * @param videoId 视频ID
     * @return 判断结果
     */
    public Result<Boolean> existLikeRecord(Integer userId, Integer videoId) {
        // 构建查询
        boolean exists = likeMapper.exists(
                new LambdaQueryWrapper<VideoLikes>()
                        .eq(VideoLikes::getUserId, userId)
                        .eq(VideoLikes::getVideoId, videoId)
        );
        return Result.success(exists);
    }
}
