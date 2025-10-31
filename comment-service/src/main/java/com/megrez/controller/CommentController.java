package com.megrez.controller;

import com.megrez.annotation.CurrentUser;
import com.megrez.dto.comment_service.VideoCommentDTO;
import com.megrez.mongo_document.VideoComments;
import com.megrez.result.Response;
import com.megrez.result.Result;
import com.megrez.service.CommentService;
import com.megrez.vo.CursorLoad;
import com.megrez.vo.comment_service.VideoCommentsVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * 评论增删改查接口
 */
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


    /**
     * 删除评论
     *
     * @param userId    用户Id
     * @param commentId 评论Id
     * @return 操作结果
     */
    @DeleteMapping
    public Result<Void> delComment(
            @CurrentUser Integer userId,
            @RequestParam String commentId
    ) {
        log.info("用户ID：{} 删除评论ID：{}", userId, commentId);
        return commentService.delComment(userId, commentId);
    }

    /**
     * 拉取视频评论，无限滚动加载，按热度排序
     *
     * @param userId          用户Id（不必须）
     * @param videoId         视频Id
     * @param parentCommentId 父评论id（拉取回复列表时使用，不必须）
     * @param cursor          加密游标
     * @return 获取的评论集合
     */
    @GetMapping("/get")
    public Result<CursorLoad<VideoCommentsVO>> getComments(
            @CurrentUser(required = false) Integer userId,
            @RequestParam Integer videoId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String parentCommentId
    ) {
        // 未登录用户只能查看前十条根评论
        if (userId == -1 && (cursor != null || parentCommentId != null)) {
            return Result.error(Response.UNAUTHORIZED);
        }
        log.info("用户ID：{} 拉取视频ID为：{}的评论", userId, videoId);
        return commentService.getComments(userId, videoId, cursor, parentCommentId);
    }

}
