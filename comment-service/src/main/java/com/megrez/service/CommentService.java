package com.megrez.service;

import com.megrez.client.UserServiceClient;
import com.megrez.dto.VideoCommentDTO;
import com.megrez.entity.*;
import com.megrez.repository.CommentRepository;
import com.megrez.result.Response;
import com.megrez.result.Result;
import com.megrez.vo.VideoCommentsVO;
import com.mongodb.client.result.DeleteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentService.class);
    private final MongoTemplate mongoTemplate;
    private final UserServiceClient userServiceClient;

    public CommentService(MongoTemplate mongoTemplate, UserServiceClient userServiceClient) {
        this.mongoTemplate = mongoTemplate;
        this.userServiceClient = userServiceClient;
    }

    public Result<VideoComments> addComment(Integer userId, VideoCommentDTO videoComment) {
        // 构建文档
        VideoComments comment = VideoComments.builder()
                .userId(userId)
                .videoId(videoComment.getVideoId())
                .parentCommentId(videoComment.getParentCommentId())
                .content(videoComment.getContent()).build();
        // 插入
        VideoComments inserted = mongoTemplate.insert(comment);

        // 返回插入的文档
        return Result.success(inserted);
    }

    public Result<Void> delComment(Integer userId, String commentId) {
        // 1. 先查询评论
        VideoComments byId = mongoTemplate.findById(commentId, VideoComments.class);
        // 2. 没有查询到，直接返回成功
        if (byId == null) {
            return Result.success(null);
        }
        // 3. 判断是否有无权限删除
        if (!byId.getUserId().equals(userId)) {
            return Result.error(Response.FORBIDDEN);
        }
        // 4. 有权限，执行删除
        mongoTemplate.remove(byId);

        return Result.success(null);
    }

    public Result<List<VideoCommentsVO>> getComments(Integer userId, Integer videoId, Double score, String commentId) {

        // 1. 构建查询返回基础评论
        List<VideoComments> comments = mongoTemplate.find(
                new Query(
                        Criteria.where("videoId").is(videoId)
                                .and("score").lte(String.valueOf(score == null ? 1 : score)) // 没有传递热度值，默认为1
                                .and("id").ne(commentId == null ? "" : commentId) // 没有传递上一条评论ID，传递一个空串
                                .and("isDeleted").ne(true)
                )
                        .with(Sort.by(Sort.Direction.DESC, "score"))
                        .limit(10),
                VideoComments.class
        );
        // 评论ID集合
        List<String> commentIdsCollect = comments.stream().map(VideoComments::getId).toList();
        log.info("{}", commentIdsCollect);
        // 用户ID集合
        List<Integer> userIdslist = comments.stream().map(VideoComments::getUserId).toList();
        // 2. 判断用户是否点赞过
        // 用户对目前获取的评论的点赞情况
        List<CommentLike> likedList = mongoTemplate.find(
                new Query(
                        Criteria.where("commentId").in(commentIdsCollect).and("userId").is(userId)
                ), CommentLike.class
        );
        log.info("获取用户点赞评论列表：{}", likedList);
        // 3. 调用用户服务，批量查询评论拥有者信息
        List<User> users = new ArrayList<>();
        try {
            Result<List<User>> userinfoById = userServiceClient.getUserinfoById(userIdslist);
            if (userinfoById.isSuccess()) {
                users = userinfoById.getData();
            }
        } catch (Exception e) {
            log.error("用户服务调用失败！");
            return Result.error(Response.UNKNOWN_WRONG);
        }
        // 4. 组装完整评论信息并返回
        // 先构建 userId -> User 的 Map
        Map<Integer, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // 构建likedList -> CommentLike 的 Map
        Map<String, CommentLike> commentsMap = likedList.stream()
                .collect(Collectors.toMap(CommentLike::getCommentId, Function.identity()));

        List<VideoCommentsVO> list = comments.stream().map(c -> {
            VideoCommentsVO videoCommentsVO = new VideoCommentsVO();
            videoCommentsVO.setVideoComments(c);
            // 判断是否点赞
            videoCommentsVO.setLiked(commentsMap.get(c.getId()) != null);
            // 设置评论对应用户信息
            videoCommentsVO.setUser(userMap.get(c.getUserId()));
            return videoCommentsVO;
        }).toList();

        return Result.success(list);
    }
}
