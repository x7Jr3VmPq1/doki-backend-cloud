package com.megrez.service;

import com.megrez.client.UserServiceClient;
import com.megrez.mongo_document.Conversation;
import com.megrez.mongo_document.DirectMessage;
import com.megrez.mysql_entity.User;
import com.megrez.redis.NotifyAndDMRedisClient;
import com.megrez.result.Response;
import com.megrez.result.Result;
import com.megrez.utils.CollectionUtils;
import com.megrez.vo.notification_dm_service.ConversationVO;
import com.megrez.vo.notification_dm_service.MessageVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);
    private final MongoTemplate mongoTemplate;
    private final UserServiceClient userServiceClient;
    private final NotifyAndDMRedisClient redisClient;

    public MessageService(MongoTemplate mongoTemplate, UserServiceClient userServiceClient, NotifyAndDMRedisClient redisClient) {
        this.mongoTemplate = mongoTemplate;
        this.userServiceClient = userServiceClient;
        this.redisClient = redisClient;
    }

    public Result<ConversationVO> createConversation(List<Integer> members) {
        // 成员列表排序，确保查询一致性
        List<Integer> sortedMembers = new ArrayList<>(members);
        Collections.sort(sortedMembers);

        // 查询条件：members 包含所有这些成员，并且成员数量相等
        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(Criteria.where("members").all(sortedMembers), Criteria.where("members").size(sortedMembers.size())));

        Result<List<User>> userinfoById = userServiceClient.getUserinfoById(List.of(members.get(1)));
        if (!userinfoById.isSuccess()) {
            return Result.error(Response.UNKNOWN_WRONG);
        }
        List<User> data = userinfoById.getData();
        User user = data.get(0);

        Conversation existing = mongoTemplate.findOne(query, Conversation.class);
        if (existing != null) {
            // 已存在会话，直接返回
            ConversationVO vo = new ConversationVO();
            BeanUtils.copyProperties(existing, vo);
            vo.setUserinfo(user);
            return Result.success(vo);
        }
        // 不存在则创建新会话
        Conversation conversation = Conversation.builder().members(sortedMembers).build();
        Conversation saved = mongoTemplate.save(conversation);
        ConversationVO vo = new ConversationVO();
        BeanUtils.copyProperties(saved, vo);
        vo.setUserinfo(user);
        return Result.success(vo);
    }

    public Result<List<ConversationVO>> getConversationsForUser(Integer userId) {
        Query query = new Query(Criteria.where("members").in(userId));
        query.with(Sort.by(Sort.Direction.DESC, "updatedAt"));
        List<Conversation> conversations = mongoTemplate.find(query, Conversation.class);
        if (conversations.isEmpty()) {
            return Result.success(List.of());
        }
        // 过滤掉用户逻辑删除的会话
        // 如果上次删除的时间大于最后更新的时间，则说明没有新消息，排除这个会话
        conversations.removeIf(c -> c.getLastDeleteAt().getOrDefault(userId, 0L) > c.getUpdatedAt());
        // 获取全部用户的ID
        List<List<Integer>> uidList = CollectionUtils.toList(conversations, Conversation::getMembers);
        // 把大集合摊平，变成一个普通的List
        List<Integer> result = uidList.stream()
                .flatMap(List::stream)
                .filter(id -> !Objects.equals(id, userId))
                .distinct()
                .toList();
        // 获取用户信息
        Result<List<User>> userinfoById = userServiceClient.getUserinfoById(result);
        if (!userinfoById.isSuccess()) {
            return Result.error(Response.UNKNOWN_WRONG);
        }
        Map<Integer, User> userMap = CollectionUtils.toMap(userinfoById.getData(), User::getId);

        // 获取未读数量
        List<String> cIds = CollectionUtils.toList(conversations, Conversation::getId);
        List<Object> cIdsList = new ArrayList<>(cIds);
        Map<String, Integer> unreadMap = redisClient.getUnread(userId, cIdsList);

        // 把会话集合转为一个以会话另一方为key的map，方便聚合。
        Map<Integer, Conversation> conversationMap = conversations.stream()
                .collect(Collectors.toMap(c -> c.getMembers()
                        .stream().filter(id -> !Objects.equals(id, userId)).toList().get(0), Function.identity()));


        // 组装最终数据
        List<ConversationVO> list = result.stream().map(id -> {
            User userinfo = userMap.get(id);
            Conversation conversation = conversationMap.get(id);
            ConversationVO conversationVO = new ConversationVO();
            BeanUtils.copyProperties(conversation, conversationVO);
            conversationVO.setUserinfo(userinfo);
            conversationVO.setUnread(Objects.requireNonNullElse(unreadMap.get(conversation.getId()), 0));
            return conversationVO;
        }).toList();

        return Result.success(list);
    }

    public void updateLastMessage(String conversationId, DirectMessage message) {
        Query query = new Query(Criteria.where("_id").is(conversationId));
        Update update = new Update().set("lastMessage", message).set("updatedAt", System.currentTimeMillis());
        mongoTemplate.updateFirst(query, update, Conversation.class);
    }

    public Result<Void> deleteConversationForUser(String conversationId, Integer userId) {
        Query query = new Query(Criteria.where("_id").is(conversationId));
        Conversation c = mongoTemplate.findOne(query, Conversation.class);
        if (c == null || !c.getMembers().contains(userId)) {
            return Result.error(Response.FORBIDDEN);
        }
        Update update = new Update().set("lastDeleteAt." + userId, System.currentTimeMillis());
        mongoTemplate.updateFirst(query, update, Conversation.class);
        return Result.success(null);
    }


    public Result<MessageVO> createMessage(Integer userId, DirectMessage message) {
        Conversation c = getConversation(message.getConversationId());
        if (c == null || !c.getMembers().contains(userId)) {
            return Result.error(Response.CONVERSATION_NOT_FOUND);
        }
        DirectMessage save = mongoTemplate.save(message);
        updateLastMessage(c.getId(), save);

        Result<List<User>> userinfoById = userServiceClient.getUserinfoById(List.of(save.getSenderId()));

        if (!userinfoById.isSuccess()) {
            log.error("用户服务异常。");
            return Result.error(Response.UNKNOWN_WRONG);
        }

        MessageVO vo = new MessageVO();
        BeanUtils.copyProperties(save, vo);
        vo.setUserinfo(userinfoById.getData().get(0));

        // 给会话的另一方增加未读数
        Integer another = c.getMembers().stream().filter(id -> !id.equals(userId)).toList().get(0);
        redisClient.incDMUnread(another, c.getId(), 1);

        return Result.success(vo);
    }

    public Result<List<MessageVO>> getMessagesForConversation(Integer userId, String conversationId) {
        // 1. 先查询会话有效性和判断权限。
        Conversation c = getConversation(conversationId);
        if (c == null || !c.getMembers().contains(userId)) {
            return Result.error(Response.FORBIDDEN);
        }
        // 2. 查询消息
        Long lastDelete = c.getLastDeleteAt().get(userId);
        Query query = new Query(Criteria.where("conversationId").is(c.getId()));
        if (lastDelete != null) {
            query.addCriteria(Criteria.where("timestamp").gt(lastDelete));
        }
        List<DirectMessage> directMessages = mongoTemplate.find(query, DirectMessage.class);
        List<Integer> uIds = directMessages.stream().map(DirectMessage::getSenderId).toList();

        Result<List<User>> userinfoById = userServiceClient.getUserinfoById(uIds);
        if (!userinfoById.isSuccess()) {
            log.error("用户服务调用失败:{}", userinfoById.getMsg());
            return Result.error(Response.UNKNOWN_WRONG);
        }
        List<User> userList = userinfoById.getData();
        Map<Integer, User> userMap = CollectionUtils.toMap(userList, User::getId);

        List<MessageVO> list = directMessages.stream().map(message -> {
            MessageVO vo = new MessageVO();
            BeanUtils.copyProperties(message, vo);
            vo.setUserinfo(userMap.get(message.getSenderId()));
            return vo;
        }).toList();
        return Result.success(list);
    }

    // 根据会话ID查询一个会话对象
    private Conversation getConversation(String conversationId) {
        return mongoTemplate.findById(conversationId, Conversation.class);
    }

    /**
     * 获取全部未读数
     *
     * @param userId 用户id
     * @return 未读数
     */
    public Result<Integer> getUnreadCount(Integer userId) {
        return Result.success(redisClient.getUnreadTotal(userId));
    }

    /**
     * 清空未读数
     *
     * @param userId 用户id
     * @param cid    会话id，不传入则清空所有未读数。
     * @return null
     */
    public Result<Void> clearUnreadCount(Integer userId, String cid) {
        if (cid != null && !cid.isEmpty()) {
            redisClient.clearSingleUnread(userId, cid);
        } else {
            redisClient.clearAllUnread(userId);
        }
        return Result.success(null);
    }
}
