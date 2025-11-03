package com.megrez.controller;

import com.megrez.annotation.CurrentUser;
import com.megrez.mongo_document.Conversation;
import com.megrez.mongo_document.DirectMessage;
import com.megrez.mysql_entity.User;
import com.megrez.result.Response;
import com.megrez.result.Result;
import com.megrez.service.MessageService;
import com.megrez.vo.notification_dm_service.ConversationVO;
import com.megrez.vo.notification_dm_service.MessageVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 私信服务
 */
@RestController
@RequestMapping("/message")
public class MessageController {
    private static final Logger log = LoggerFactory.getLogger(MessageController.class);

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * 向指定用户创建一个会话
     *
     * @param userId 当前用户ID
     * @param tid    目标用户ID
     * @return 成功创建的会话对象
     */
    @GetMapping("/create")
    public Result<ConversationVO> createConversation(@CurrentUser Integer userId,
                                                     @RequestParam Integer tid) {

        if (userId.equals(tid)) {
            return Result.error(Response.MESSAGE_CANT_CREATE);
        }
        log.info("创建会话，UID:{},{}", userId, tid);

        List<Integer> ids = List.of(userId, tid);
        return messageService.createConversation(ids);
    }

    /**
     * 发送消息
     *
     * @param userId  用户ID
     * @param message 消息体
     * @return 创建的消息对象
     */
    @PostMapping("/send")
    public Result<MessageVO> send(@CurrentUser Integer userId,
                                  @RequestBody DirectMessage message) {
        log.info("用户ID：{}向会话发送消息：{}", userId, message);
        if (!userId.equals(message.getSenderId())) {
            return Result.error(Response.FORBIDDEN);
        }
        return messageService.createMessage(userId, message);
    }

    /**
     * 获取会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    @GetMapping("/getList")
    public Result<List<ConversationVO>> getList(@CurrentUser Integer userId) {
        log.info("用户ID：{}拉取聊天列表", userId);
        return messageService.getConversationsForUser(userId);
    }

    /**
     * 获取会话消息列表
     *
     * @param userId 用户id
     * @param cid    会话id
     * @return 消息列表
     */
    @GetMapping("/getMessage")
    public Result<List<MessageVO>> getMessages(@CurrentUser Integer userId,
                                               String cid) {
        log.info("用户ID：{}拉取消息，会话ID：{}", userId, cid);
        return messageService.getMessagesForConversation(userId, cid);
    }

    /**
     * 删除会话
     *
     * @param userId 用户id
     * @param cid    会话id
     * @return null
     */
    @DeleteMapping("/delete")
    public Result<Void> deleteConversation(@CurrentUser Integer userId,
                                           String cid) {
        log.info("用户ID：{}删除会话，会话ID：{}", userId, cid);
        return messageService.deleteConversationForUser(cid, userId);
    }

    /**
     * 获取用户私信未读数
     *
     * @param userId 用户id
     * @return 未读数
     */
    @GetMapping("/unread")
    public Result<Integer> getUnreadCount(@CurrentUser Integer userId) {
        return messageService.getUnreadCount(userId);
    }

    /**
     * 清空未读消息数
     *
     * @param userId 用户id
     * @param cid    会话id，不传入则清空所有未读数。
     * @return null
     */
    @DeleteMapping("/unread")
    public Result<Void> clearUnreadCount(@CurrentUser Integer userId,
                                         @RequestParam(value = "cid", required = false) String cid) {
        log.info("用户id：{} 清空未读数,会话id{}", userId, cid);
        return messageService.clearUnreadCount(userId, cid);
    }

}
