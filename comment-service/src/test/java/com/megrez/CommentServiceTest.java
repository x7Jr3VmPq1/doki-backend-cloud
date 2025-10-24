package com.megrez;

import com.megrez.dto.NextOffset;
import com.megrez.dto.VideoCommentDTO;
import com.megrez.entity.VideoComments;
import com.megrez.service.CommentService;
import com.megrez.utils.PageTokenUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;


@SpringBootTest

public class CommentServiceTest {

    private static final Logger log = LoggerFactory.getLogger(CommentServiceTest.class);
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private CommentService commentService;


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

}
