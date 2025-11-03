package com.megrez.service;

import com.megrez.client.VideoInfoClient;
import com.megrez.mongo_document.Notification;
import com.megrez.mysql_entity.Video;
import com.megrez.rabbit.exchange.VideoLikeExchange;

import com.megrez.rabbit.message.VideoLikeMessage;
import com.megrez.redis.NotifyAndDMRedisClient;
import com.megrez.result.Response;
import com.megrez.result.Result;
import com.megrez.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotifyService {

    private static final Logger log = LoggerFactory.getLogger(NotifyService.class);
    private final MongoTemplate mongoTemplate;
    private final VideoInfoClient videoInfoClient;
    private final NotifyAndDMRedisClient redisClient;

    public NotifyService(MongoTemplate mongoTemplate, VideoInfoClient videoInfoClient, NotifyAndDMRedisClient redisClient) {
        this.mongoTemplate = mongoTemplate;
        this.videoInfoClient = videoInfoClient;
        this.redisClient = redisClient;
    }


    @RabbitListener(queues = VideoLikeExchange.QUEUE_VIDEO_LIKE_NOTIFICATION)
    public void insertByLikeVideo(String message) {
        VideoLikeMessage videoLikeMessage = JSONUtils.fromJSON(message, VideoLikeMessage.class);

        Integer videoId = videoLikeMessage.getVideoId();
        //  查询视频发布者ID
        Result<Video> videoInfoById = videoInfoClient.getVideoInfoById(videoId);
        if (!videoInfoById.isSuccess()) {
            log.error("调用视频消息服务失败。{}", videoInfoById.getMsg());
            throw new RuntimeException();
        }
        Video videoInfo = videoInfoById.getData();

        // 如果是目标是自己，则不做任何处理。
        if (videoLikeMessage.getUserId().equals(videoInfo.getUploaderId())) {
            return;
        }

        // 创建分组key，对于视频点赞，格式：
        // receiverId-type-videoId
        String groupKey = videoInfo.getUploaderId() + "-" + "2" + "-" + videoLikeMessage.getVideoId();
        // 构建通知
        Notification notification = Notification.builder()
                .type(2)    // 类型2：视频点赞
                .groupKey(groupKey)// 设置合并键
                .operatorId(videoLikeMessage.getUserId()) // 通知触发者ID
                .sourceId(videoLikeMessage.getVideoId().toString()) // 源头ID，这里是视频
                .userId(videoInfo.getUploaderId()) // 被通知者ID
                .build();
        // 写入
        mongoTemplate.insert(notification);
        // 给被通知者的通知未读数 + 1
        redisClient.incNotifyUnread(videoInfo.getUploaderId());
    }
}
