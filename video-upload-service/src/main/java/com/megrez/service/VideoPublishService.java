package com.megrez.service;

import com.megrez.config.RabbitConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class VideoPublishService {

    @RabbitListener(queues = RabbitConfig.QUEUE_VIDEO_PUBLISH)
    public void updateVideoState() {

    }
}
