package com.megrez.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.megrez.entity.VideoComments;
import com.megrez.entity.VideoStatistics;
import com.megrez.mapper.VideoStatisticsMapper;
import com.megrez.rabbit.message.CommentAddMessage;
import com.megrez.rabbit.message.CommentDelMessage;
import com.megrez.rabbit.exchange.CommentAddExchange;
import com.megrez.rabbit.exchange.CommentDeleteExchange;
import com.megrez.utils.JSONUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * 评论事件消费者方法
 */
@Service
public class VideoCommentListener {

    private final VideoStatisticsMapper videoStatisticsMapper;

    public VideoCommentListener(VideoStatisticsMapper videoStatisticsMapper) {
        this.videoStatisticsMapper = videoStatisticsMapper;
    }

    /**
     * 当发生添加评论时，更新视频的评论数
     *
     * @param message 评论消息
     */
    @RabbitListener(queues = CommentAddExchange.QUEUE_COMMENT_ADD_ANALYTICS)
    public void commentAddListener(String message) {
        // 1. 解析消息
        CommentAddMessage commentAddMessage = JSONUtils.fromJSON(message, CommentAddMessage.class);
        // 2. 获取评论本体
        VideoComments videoComments = commentAddMessage.getVideoComments();
        // 3. 执行自增
        videoStatisticsMapper.update(
                new LambdaUpdateWrapper<VideoStatistics>()
                        .eq(VideoStatistics::getVideoId, videoComments.getVideoId())
                        .setSql("comment_count = comment_count + 1")
        );
    }

    @RabbitListener(queues = CommentDeleteExchange.QUEUE_COMMENT_DELETE_ANALYTICS)
    public void commentDelListener(String message) {
        // 1. 解析消息
        CommentDelMessage commentDelMessage = JSONUtils.fromJSON(message, CommentDelMessage.class);
        // 2. 获取评论本体
        VideoComments videoComments = commentDelMessage.getVideoComments();
        // 3. 执行自增
        videoStatisticsMapper.update(
                new LambdaUpdateWrapper<VideoStatistics>()
                        .eq(VideoStatistics::getVideoId, videoComments.getVideoId())
                        .setSql("comment_count = comment_count - 1")
        );
    }
}
