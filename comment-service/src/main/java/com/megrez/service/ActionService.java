package com.megrez.service;

import com.megrez.entity.CommentLike;
import com.megrez.entity.VideoComments;
import com.megrez.rabbit.message.CommentLikeMessage;
import com.megrez.rabbit.exchange.CommentLikeExchange;
import com.megrez.result.Result;
import com.megrez.utils.JSONUtils;
import com.megrez.utils.RabbitMQUtils;
import com.mongodb.client.MongoClient;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class ActionService {

    private final MongoTemplate mongoTemplate;
    private final RabbitMQUtils rabbitMQUtils;
    private final MongoClient mongo;

    public ActionService(MongoTemplate mongoTemplate, RabbitMQUtils rabbitMQUtils, MongoClient mongo) {
        this.mongoTemplate = mongoTemplate;
        this.rabbitMQUtils = rabbitMQUtils;
        this.mongo = mongo;
    }

    public Result<Void> like(Integer userId, String commentId) {
        // 1. 先查询是否存在这个评论
        boolean existsComment = mongoTemplate.exists(
                new Query(
                        Criteria.where("id").is(commentId)
                                .and("isDeleted").ne(true)
                ), VideoComments.class
        );
        // 2.如果存在，查询是否存在点赞记录
        int type = 0; // 类型：1 点赞 0 取消点赞
        if (existsComment) {
            // 构建查询条件
            Query query = new Query(
                    Criteria.where("userId").is(userId)
                            .and("commentId").is(commentId)
            );
            boolean existsRecord = mongoTemplate.exists(query, CommentLike.class);
            // 3. 存在，删除记录
            if (existsRecord) {
                mongoTemplate.remove(query, CommentLike.class);
            } else {
                // 不存在，插入点赞记录
                mongoTemplate.insert(
                        CommentLike.builder()
                                .commentId(commentId)
                                .userId(userId).build()
                );
                type = 1;
            }
            // 4. 更新点赞数
            Query query1 = new Query(Criteria.where("_id").is(commentId));
            Update update = new Update().inc("likeCount", type == 1 ? 1 : -1);
            mongoTemplate.updateFirst(query1, update, VideoComments.class);

            // 5. 发送消息
            rabbitMQUtils.sendMessage(
                    CommentLikeExchange.FANOUT_EXCHANGE_COMMENT_LIKE,
                    null,
                    JSONUtils.toJSON(
                            CommentLikeMessage.builder()
                                    .type(type)
                                    .commentId(commentId)
                                    .userId(userId)
                                    .build()
                    )
            );
        }
        // 返回结果
        return Result.success(null);
    }
}
