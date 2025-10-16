package com.megrez.controller;

import com.megrez.annotation.CurrentUser;
import com.megrez.result.Result;
import com.megrez.service.ActionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 对评论的互动，调用此接口
 * 点赞，点踩，回复评论
 */
@RestController
@RequestMapping("/comment/action")
public class ActionController {

    private static final Logger log = LoggerFactory.getLogger(ActionController.class);
    private final ActionService actionService;

    public ActionController(ActionService actionService) {
        this.actionService = actionService;
    }

    /**
     * 评论点赞接口
     *
     * @param userId    用户id
     * @param commentId 评论id
     * @return 操作结果
     */
    @GetMapping("/like")
    public Result<Void> like(
            @CurrentUser Integer userId,
            @RequestParam String commentId
    ) {
        log.info("用户ID：{} 点赞了评论 ID：{}", userId, commentId);
        return actionService.like(userId, commentId);
    }
}
