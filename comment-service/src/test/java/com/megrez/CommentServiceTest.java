package com.megrez;

import com.megrez.dto.comment_service.VideoCommentDTO;
import com.megrez.mongo_document.VideoComments;
import com.megrez.rabbit.exchange.CommentLikeExchange;
import com.megrez.service.CommentService;
import com.megrez.utils.RabbitMQUtils;
import io.github.pigmesh.ai.deepseek.core.DeepSeekClient;
import io.github.pigmesh.ai.deepseek.core.chat.ChatCompletionResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import reactor.core.publisher.Flux;


@SpringBootTest

public class CommentServiceTest {

    private static final Logger log = LoggerFactory.getLogger(CommentServiceTest.class);
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private CommentService commentService;

    @Autowired
    private RabbitMQUtils rabbitMQUtils;

    @Autowired
    private DeepSeekClient deepSeekClient;

    @Test
    public void updateColumn() {

        Query query = new Query();
        Update update = new Update();
        update.set("score", "1");
        mongoTemplate.updateMulti(query, update, VideoComments.class);
    }

    @Test
    public void insertComment() {

        for (int i = 1; i <= 20; i++) {
            VideoCommentDTO videoCommentDTO = new VideoCommentDTO();
            videoCommentDTO.setVideoId(10);
            videoCommentDTO.setContent("第" + i + "条测试评论");
            System.out.println(videoCommentDTO.getContent());
            commentService.addComment(10001, videoCommentDTO);
        }

    }

    @Test
    public void testQueue() {
        while (true) {
            rabbitMQUtils.sendMessage(CommentLikeExchange.FANOUT_EXCHANGE_COMMENT_LIKE, "", "");
        }
    }




}
