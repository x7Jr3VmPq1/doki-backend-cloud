package com.megrez.controller;

import com.megrez.annotation.CurrentUser;
import com.megrez.mongo_document.Conversation;
import com.megrez.mongo_document.DirectMessage;
import com.megrez.mysql_entity.User;
import com.megrez.result.Result;
import com.megrez.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public Result<Conversation> createConversation(@CurrentUser Integer userId,
                                                   @RequestParam Integer tid) {
        log.info("创建会话，UID:{},{}", userId, tid);

        List<Integer> ids = List.of(userId, tid);
        return messageService.createConversation(ids);
    }
//
//    @PostMapping("/send")
//    public Result<DirectMessage> send(DirectMessage message) {
//        log.info("用户ID：{}向会话发送消息：{}", message.getId());
//    }
}
