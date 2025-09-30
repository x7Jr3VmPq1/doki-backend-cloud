package com.megrez.service;

import com.megrez.constant.QueueConstants;
import com.megrez.entity.Video;
import com.megrez.mapper.VideoMapper;
import com.megrez.utils.JSONUtils;
import com.megrez.utils.RabbitMQUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * 当视频的审核和处理完毕后，进行新建视频记录操作
 */
@Service
public class VideoPublishService {

    private final VideoMapper videoMapper;
    private final RabbitMQUtils rabbitMQUtils;
    private static final Logger log = LoggerFactory.getLogger(VideoPublishService.class);

    public VideoPublishService(VideoMapper videoMapper, RabbitMQUtils rabbitMQUtils) {
        this.videoMapper = videoMapper;
        this.rabbitMQUtils = rabbitMQUtils;
    }

    @RabbitListener(queues = QueueConstants.QUEUE_VIDEO_PUBLISH)
    public void updateVideoState(String videoMessage) {
        // 解析消息体
        Video video = JSONUtils.fromJSON(videoMessage, Video.class);
        // 插入记录
        int insert = videoMapper.insert(video);
        if (insert > 0) {
            log.info("新增视频成功，ID：{}", video.getId());
            
            // 发送视频发布成功消息到VIDEO_PUBLISHED交换机
            // 消息会同时发送到搜索、通知、统计分析三个队列
            rabbitMQUtils.sendMessage(
                    QueueConstants.FANOUT_EXCHANGE_VIDEO_PUBLISHED,
                    "",
                    JSONUtils.toJSON(video)
            );
            log.info("视频发布通知已发送到FANOUT交换机，视频ID：{}", video.getId());
        }
    }
}
