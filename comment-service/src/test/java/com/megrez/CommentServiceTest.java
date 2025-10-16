package com.megrez;

import com.megrez.entity.VideoComments;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;


@SpringBootTest

public class CommentServiceTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void updateColumn() {

        Query query = new Query();
        Update update = new Update();
        update.set("score", "1");
        mongoTemplate.updateMulti(query, update, VideoComments.class);

    }
}
