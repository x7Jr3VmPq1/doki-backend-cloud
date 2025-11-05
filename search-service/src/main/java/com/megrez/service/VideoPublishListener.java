package com.megrez.service;

import com.megrez.client.UserServiceClient;
import com.megrez.es_document.VideoESDocument;
import com.megrez.mysql_entity.User;
import com.megrez.mysql_entity.Video;
import com.megrez.rabbit.exchange.VideoPublishedExchange;
import com.megrez.result.Result;
import com.megrez.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 处理视频上传消息，把新增视频添加进搜索引擎中。
 */

@Service
public class VideoPublishListener {

    private final UserServiceClient userServiceClient;
    private final ElasticsearchOperations operations;

    private static final Logger log = LoggerFactory.getLogger(VideoPublishListener.class);

    public VideoPublishListener(UserServiceClient userServiceClient, ElasticsearchOperations operations) {
        this.userServiceClient = userServiceClient;
        this.operations = operations;
    }

    @RabbitListener(queues = VideoPublishedExchange.QUEUE_VIDEO_SEARCH)
    public void createIndex(String message) {
        // 解析消息
        Video video = JSONUtils.fromJSON(message, Video.class);

        // 获取上传者的用户信息
        Result<List<User>> userinfoById = userServiceClient.getUserinfoById(List.of(video.getUploaderId()));
        if (!userinfoById.isSuccess()) {
            log.error("用户服务调用失败");
            throw new RuntimeException();
        }
        User user = userinfoById.getData().get(0);

        // 构建文档
        VideoESDocument document = new VideoESDocument();
        document.setId(video.getId().toString());
        document.setTitle(video.getTitle());
        document.setUserId(video.getUploaderId());
        document.setUsername(user.getUsername());
        document.setDescription(video.getDescription());
        document.setTags(video.getTags());

        operations.save(document);
        log.info("创建新索引：{}", document);
    }
}
