package com.megrez.service;

import com.megrez.mongo_document.Conversation;
import com.megrez.mongo_document.DirectMessage;
import com.megrez.result.Result;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    private final MongoTemplate mongoTemplate;

    public MessageService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Result<Conversation> createConversation(List<Integer> members) {
        Conversation conversation = Conversation.builder()
                .members(members)
                .build();
        return Result.success(mongoTemplate.save(conversation));
    }

    public Result<List<Conversation>> getConversationsForUser(Integer userId) {
        Query query = new Query(Criteria.where("members").in(userId));
        List<Conversation> conversations = mongoTemplate.find(query, Conversation.class);
        // 过滤掉用户逻辑删除的会话
        conversations.removeIf(c -> c.getLastDeleteAt().getOrDefault(userId, 0L) > 0);
        return Result.success(conversations);
    }

    public Result<Void> updateLastMessage(String conversationId, DirectMessage message) {
        Query query = new Query(Criteria.where("_id").is(conversationId));
        Update update = new Update()
                .set("lastMessage", message)
                .set("updatedAt", System.currentTimeMillis());
        mongoTemplate.updateFirst(query, update, Conversation.class);
        return Result.success(null);
    }

    public Result<Void> deleteConversationForUser(String conversationId, Integer userId) {
        Query query = new Query(Criteria.where("_id").is(conversationId));
        Update update = new Update().set("lastDeleteAt." + userId, System.currentTimeMillis());
        mongoTemplate.updateFirst(query, update, Conversation.class);
        return Result.success(null);

    }


    public Result<DirectMessage> createMessage(DirectMessage message) {
        return Result.success(mongoTemplate.save(message));
    }

    public Result<List<DirectMessage>> getMessagesForConversation(String conversationId, int page, int size) {
        Query query = new Query(Criteria.where("conversationId").is(conversationId))
                .with(Sort.by(Sort.Direction.ASC, "timestamp"))
                .skip((long) page * size)
                .limit(size);
        return Result.success(mongoTemplate.find(query, DirectMessage.class));
    }

    public Result<Void> updateMessage(String messageId, String newContent, String imgUrl) {
        Query query = new Query(Criteria.where("_id").is(messageId));
        Update update = new Update()
                .set("content", newContent)
                .set("imgUrl", imgUrl)
                .set("timestamp", System.currentTimeMillis());
        mongoTemplate.updateFirst(query, update, DirectMessage.class);
        return Result.success(null);
    }

    public Result<Void> deleteMessage(String messageId) {
        Query query = new Query(Criteria.where("_id").is(messageId));
        mongoTemplate.remove(query, DirectMessage.class);

        return Result.success(null);
    }

    // 查询某用户在某会话的最新未删除消息
    public Result<List<DirectMessage>> getVisibleMessages(String conversationId, Integer userId) {
        Conversation conversation = mongoTemplate.findById(conversationId, Conversation.class);
        assert conversation != null;
        long lastDeleteTime = conversation.getLastDeleteAt().getOrDefault(userId, 0L);

        Query query = new Query(Criteria.where("conversationId").is(conversationId)
                .and("timestamp").gt(lastDeleteTime))
                .with(Sort.by(Sort.Direction.ASC, "timestamp"));

        return Result.success(mongoTemplate.find(query, DirectMessage.class));
    }
}
