package com.megrez.controller;

import com.megrez.annotation.CurrentUser;
import com.megrez.dto.VideoCommentDTO;
import com.megrez.entity.VideoComments;
import com.megrez.result.Result;
import com.megrez.service.CommentService;
import com.megrez.vo.VideoCommentsVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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
     * 加载下一页时，必须同时传递score和lastId作为分页依据，否则查询的结果会有误
     *
     * @param userId          用户Id（不必须）
     * @param videoId         视频Id
     * @param score           评论得分，作为排序依据(不必须)
     * @param lastId          上次拉取结果最后一条评论的Id，用于排除重复评论(不必须)
     * @param parentCommentId 父评论id（拉取回复列表时使用，不必须）
     * @return 获取的评论集合
     */
    @GetMapping("/get")
    public Result<Map<String, Object>> getComments(
            @CurrentUser(required = false) Integer userId,
            @RequestParam Integer videoId,
            @RequestParam(required = false) Double score,
            @RequestParam(required = false) String lastId,
            @RequestParam(required = false) String parentCommentId
    ) {
        log.info("用户ID：{} 拉取视频ID为：{}的评论 score <= {}", userId, videoId, score);
        return commentService.getComments(userId, videoId, score, lastId, parentCommentId);
    }

}
