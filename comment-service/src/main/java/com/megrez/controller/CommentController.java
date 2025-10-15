package com.megrez.controller;

import com.megrez.annotation.CurrentUser;
import com.megrez.dto.VideoCommentDTO;
import com.megrez.entity.VideoComments;
import com.megrez.result.Result;
import com.megrez.service.CommentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/comment")
public class CommentController {

    private static final Logger log = LoggerFactory.getLogger(CommentController.class);
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * 向视频添加评论
     *
     * @param userId       用户ID
     * @param videoComment 评论体，包括视频ID，评论内容，图片BASE64
     * @return 被添加的评论
     */
    @PostMapping
    public Result<VideoComments> addComment(
            @CurrentUser Integer userId,
            @RequestBody VideoCommentDTO videoComment
    ) {
        log.info("用户ID：{} 向视频：{} 添加评论：{}",
                userId,
                videoComment.getVideoId(),
                videoComment.getContent());
        return commentService.addComment(userId, videoComment);
    }


    @DeleteMapping
    public Result<Void> delComment(
            @CurrentUser Integer userId,
            @RequestParam String commentId
    ) {
        log.info("用户ID：{} 删除评论ID：{}", userId, commentId);
        return commentService.delComment(userId, commentId);
    }

    @GetMapping("/get")
    public Result<List<VideoComments>> getComments(
            @CurrentUser(required = false) Integer userId,
            @RequestParam Integer videoId
    ) {
        log.info("用户ID：{} 拉取视频ID为：{}的评论", userId, videoId);
        return commentService.getComments(userId, videoId);
    }
}
