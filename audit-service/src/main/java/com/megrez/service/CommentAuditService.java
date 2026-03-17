package com.megrez.service;

import com.megrez.mongo_document.VideoComments;
import com.megrez.rabbit.exchange.CommentAddExchange;
import com.megrez.rabbit.message.CommentAddMessage;
import com.megrez.utils.JSONUtils;
import com.megrez.utils.SensitiveWordFilter;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class CommentAuditService {

    private final SensitiveWordFilter sensitiveWordFilter;
    private final MongoTemplate mongoTemplate;

    public CommentAuditService(SensitiveWordFilter sensitiveWordFilter, MongoTemplate mongoTemplate) {
        this.sensitiveWordFilter = sensitiveWordFilter;
        this.mongoTemplate = mongoTemplate;
    }

    @RabbitListener(queues = CommentAddExchange.QUEUE_COMMENT_ADD_AUDIT)
    public void commentAudit(String message) {
        CommentAddMessage commentAddMessage = JSONUtils.fromJSON(message, CommentAddMessage.class);
        VideoComments comment = commentAddMessage.getVideoComments();

        if (sensitiveWordFilter.containsSensitiveWord(comment.getContent())) {
            // 删除
            Query query = new Query(Criteria.where("_id").is(comment.getId()));
            Update update = new Update().set("isDeleted", true);
            mongoTemplate.updateFirst(query, update, VideoComments.class);
        }

    }
}
