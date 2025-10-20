package com.megrez.service;

import com.megrez.client.UserServiceClient;
import com.megrez.dto.VideoCommentDTO;
import com.megrez.entity.*;
import com.megrez.rabbit.dto.CommentAddMessage;
import com.megrez.rabbit.exchange.CommentAddExchange;
import com.megrez.result.Response;
import com.megrez.result.Result;
import com.megrez.utils.JSONUtils;
import com.megrez.utils.RabbitMQUtils;
import com.megrez.vo.VideoCommentsVO;
import org.bouncycastle.cms.PasswordRecipient;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentService.class);
    private final MongoTemplate mongoTemplate;
    private final UserServiceClient userServiceClient;
    private final RabbitMQUtils rabbitMQUtils;

    public CommentService(MongoTemplate mongoTemplate, UserServiceClient userServiceClient, RabbitMQUtils rabbitMQUtils) {
        this.mongoTemplate = mongoTemplate;
        this.userServiceClient = userServiceClient;
        this.rabbitMQUtils = rabbitMQUtils;
    }

    public Result<VideoComments> addComment(Integer userId, VideoCommentDTO videoComment) {
        // 构建文档
        VideoComments comment = VideoComments.builder()
                .userId(userId)
                .videoId(videoComment.getVideoId())
                .parentCommentId(videoComment.getParentCommentId())
                .replyTargetId(videoComment.getReplyTargetId())
                .isRoot(videoComment.getParentCommentId() == null) // 没有传递父级评论ID，说明是根评论
                .content(videoComment.getContent()).build();
        // 插入
        VideoComments inserted = mongoTemplate.insert(comment);

        // 如果是回复，则给被回复的根评论的回复总数 + 1。
        if (!inserted.getIsRoot()) {
            Query query = new Query(Criteria.where("_id").is(inserted.getParentCommentId()));
            Update update = new Update().inc("childCount", 1);
            mongoTemplate.updateFirst(query, update, VideoComments.class);
        }

        // 发送消息
        CommentAddMessage commentAddMessage = new CommentAddMessage();
        commentAddMessage.setVideoComments(comment);
        rabbitMQUtils.sendMessage(
                CommentAddExchange.FANOUT_EXCHANGE_COMMENT_ADD,
                "",
                JSONUtils.toJSON(commentAddMessage));
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

        // 5. 如果这是一条子回复，更新父评论的回复数量
        if (!byId.getParentCommentId().isEmpty()) {
            Query query = new Query(Criteria.where("_id").is(byId.getParentCommentId()));
            Update update = new Update().inc("childCount", -1);
            mongoTemplate.updateFirst(query, update, VideoComments.class);
        }
        return Result.success(null);
    }

    /**
     * 获取评论
     *
     * @param userId          用户id
     * @param videoId         视频id
     * @param score           热度
     * @param lastCommentId   上次加载的最后一条评论id
     * @param parentCommentId 父评论id
     * @return 评论列表
     */
    public Result<Map<String, Object>> getComments(Integer userId, Integer videoId, Double score, String lastCommentId, String parentCommentId) {

        // 1. 构建查询返回基础评论，如果传入了父评论id，则说明是拉取回复，否则拉取根评论
        int limit; // 如果是拉取根评论，每次拉取10条，如果是回复，每次拉取2条
        if (parentCommentId == null) {
            limit = 10;
        } else {
            limit = 2;
        }
        // 执行查询
        List<VideoComments> comments = parentCommentId == null ?
                findRootComment(videoId, score, lastCommentId, limit + 1) :
                findReplyComment(videoId, parentCommentId, lastCommentId, limit + 1);
        // 没有查询到任何评论，返回空
        if (comments.isEmpty()) {
            return Result.success(null);
        }
        boolean hasMore = false;    // 是否还有更多评论标记
        if (comments.size() > limit) {
            hasMore = true;
            comments = comments.subList(0, limit); // 去掉多查的一条
        }
        // 获取的评论ID集合
        List<String> commentIdsCollect = comments.stream().map(VideoComments::getId).toList();
        // 对应评论的用户ID集合
        List<Integer> userIdslist = comments.stream().map(VideoComments::getUserId).toList();
        // 2. 查询用户对目前获取的评论的点赞记录
        List<CommentLike> likedList = mongoTemplate.find(new Query(Criteria.where("commentId").in(commentIdsCollect).and("userId").is(userId)), CommentLike.class);
        // 3. 调用用户服务，批量查询评论拥有者信息
        List<User> users = new ArrayList<>();
        try {
            Result<List<User>> userinfoById = userServiceClient.getUserinfoById(userIdslist);
            if (userinfoById.isSuccess()) {
                users = userinfoById.getData();
            } else {
                log.error("用户服务调用失败，原因：{}", userinfoById.getMsg());
                return Result.error(Response.UNKNOWN_WRONG);
            }
        } catch (Exception e) {
            log.error("用户服务不可用，原因：{}", e.getMessage());
            return Result.error(Response.UNKNOWN_WRONG);
        }
        // 4. 组装完整评论信息并返回
        // 先构建 userId -> User 的 Map
        Map<Integer, User> userMap = users.stream().collect(Collectors.toMap(User::getId, Function.identity()));

        // 构建likedList -> CommentLike 的 Map
        Map<String, CommentLike> likedCommentsMap = likedList.stream().collect(Collectors.toMap(CommentLike::getCommentId, Function.identity()));

        // 构建 VO List 组装数据
        List<VideoCommentsVO> list = comments.stream().map(c -> {
            VideoCommentsVO videoCommentsVO = new VideoCommentsVO();
            videoCommentsVO.setComments(c);
            // 判断是否点赞
            videoCommentsVO.setLiked(likedCommentsMap.get(c.getId()) != null);
            // 设置评论对应用户信息
            videoCommentsVO.setUser(userMap.get(c.getUserId()));
            return videoCommentsVO;
        }).toList();
        HashMap<String, Object> map = new HashMap<>();
        map.put("list", list); // 评论数据
        map.put("hasMore", hasMore); // 是否还有更多标记
        return Result.success(map);
    }

    /**
     * 查询根评论方法
     *
     * @param videoId      视频id
     * @param score        热度值，对于根评论，作为排序依据
     * @param lasCommentId 上次加载的最后一条评论id，排除掉这一条
     * @return 评论列表
     */
    public List<VideoComments> findRootComment(Integer videoId, Double score, String lasCommentId, int limit) {
        Query query = new Query();

        // 基本条件
        query.addCriteria(Criteria.where("videoId").is(videoId));
        query.addCriteria(Criteria.where("isRoot").is(true));
        query.addCriteria(Criteria.where("isDeleted").is(false));

        // 热度条件
        // 默认1
        Criteria c1 = Criteria.where("score").lt(Objects.requireNonNullElse(score, 1.0));

        Criteria c2 = null;
        if (lasCommentId != null && !lasCommentId.isEmpty()) {
            c2 = Criteria.where("_id").gt(new ObjectId(lasCommentId))
                    .and("score").is(score);
        }

        // 构造 OR 条件
        if (c2 != null) {
            query.addCriteria(new Criteria().orOperator(c1, c2));
        } else {
            query.addCriteria(c1);
        }


        // 排序：先按热度，再按时间升序
        query.with(Sort.by(Sort.Order.desc("score"), Sort.Order.asc("_id"))).limit(limit);

        return mongoTemplate.find(query, VideoComments.class);
    }


    /**
     * 查询回复方法
     *
     * @param videoId         视频id
     * @param parentCommentId 父评论id
     * @param lastCommentId   上次加载的最后一条评论id
     * @return 评论列表
     */
    public List<VideoComments> findReplyComment(Integer videoId, String parentCommentId, String lastCommentId, int limit) {
        Query query = new Query();

        // 基本条件
        query.addCriteria(Criteria.where("videoId").is(videoId));
        query.addCriteria(Criteria.where("parentCommentId").is(parentCommentId));
        query.addCriteria(Criteria.where("isRoot").is(false));
        query.addCriteria(Criteria.where("isDeleted").is(false));

        // 分页条件：只有 lastCommentId 不为空时才添加 _id 条件
        if (lastCommentId != null && !lastCommentId.isEmpty()) {
            query.addCriteria(Criteria.where("_id").gt(new ObjectId(lastCommentId)));
        }

        // 排序 + 限制条数
        query.with(Sort.by(Sort.Direction.ASC, "_id")).limit(limit);

        return mongoTemplate.find(query, VideoComments.class);
    }

}
