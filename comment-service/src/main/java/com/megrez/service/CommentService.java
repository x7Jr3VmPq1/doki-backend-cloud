package com.megrez.service;

import com.megrez.dto.VideoCommentDTO;
import com.megrez.entity.Video;
import com.megrez.entity.VideoComments;
import com.megrez.repository.CommentRepository;
import com.megrez.result.Response;
import com.megrez.result.Result;
import com.mongodb.client.result.DeleteResult;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    private final MongoTemplate mongoTemplate;

    public CommentService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
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

    public Result<List<VideoComments>> getComments(Integer userId, Integer videoId) {

        List<VideoComments> comments = mongoTemplate.find(
                new Query(Criteria.where("videoId").is(videoId))
                        .with(Sort.by(Sort.Direction.DESC, "likeCount")) // 按创建时间倒序
                        .limit(10),
                VideoComments.class
        );
        return Result.success(comments);
    }
}
