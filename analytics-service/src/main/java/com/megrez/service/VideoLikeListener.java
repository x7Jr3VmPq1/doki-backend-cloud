package com.megrez.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.megrez.entity.VideoStatistics;
import com.megrez.mapper.VideoStatisticsMapper;
import com.megrez.rabbit.message.VideoLikeMessage;
import com.megrez.rabbit.exchange.VideoLikeExchange;
import com.megrez.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class VideoLikeListener {

    private final VideoStatisticsMapper videoStatisticsMapper;
    private static final Logger log = LoggerFactory.getLogger(VideoLikeListener.class);

    public VideoLikeListener(VideoStatisticsMapper videoStatisticsMapper) {
        this.videoStatisticsMapper = videoStatisticsMapper;
    }

    /**
     * 点赞事件消费方法：给视频的点赞数量做变更
     *
     * @param videoLikeMessage 点赞消息
     */
    @RabbitListener(queues = VideoLikeExchange.QUEUE_VIDEO_LIKE_ANALYTICS)
    public void videoLikeListener(String videoLikeMessage) {
        log.info("收到消息：{}", videoLikeMessage);
        VideoLikeMessage likeMessage = JSONUtils.fromJSON(videoLikeMessage, VideoLikeMessage.class);

        // 执行更新点赞数操作
        videoStatisticsMapper.update(null,
                new LambdaUpdateWrapper<VideoStatistics>()
                        .eq(VideoStatistics::getVideoId, likeMessage.getVideoId())
                        .setSql("like_count = like_count + " + (likeMessage.getType() == 0 ? "-1" : "1"))
                        .setSql("updated_time = " + System.currentTimeMillis())
        );
    }
}
