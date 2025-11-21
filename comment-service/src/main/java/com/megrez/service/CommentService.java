package com.megrez.service;

import com.megrez.client.ImageServiceClient;
import com.megrez.client.UserServiceClient;
import com.megrez.constant.GatewayHttpPath;
import com.megrez.dto.comment_service.NextOffset;
import com.megrez.dto.comment_service.VideoCommentDTO;
import com.megrez.mysql_entity.*;
import com.megrez.mongo_document.CommentLike;
import com.megrez.mongo_document.VideoComments;
import com.megrez.rabbit.message.CommentAddMessage;
import com.megrez.rabbit.exchange.CommentAddExchange;
import com.megrez.rabbit.exchange.CommentDeleteExchange;
import com.megrez.result.Response;
import com.megrez.result.Result;
import com.megrez.utils.JSONUtils;
import com.megrez.utils.PageTokenUtils;
import com.megrez.utils.RabbitMQUtils;
import com.megrez.vo.CursorLoad;
import com.megrez.vo.comment_service.SingleCommentVO;
import com.megrez.vo.comment_service.VideoCommentsVO;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentService.class);
    private final MongoTemplate mongoTemplate;
    private final UserServiceClient userServiceClient;
    private final ImageServiceClient imageServiceClient;
    private final RabbitMQUtils rabbitMQUtils;

    public CommentService(MongoTemplate mongoTemplate, UserServiceClient userServiceClient, ImageServiceClient imageServiceClient, RabbitMQUtils rabbitMQUtils) {
        this.mongoTemplate = mongoTemplate;
        this.userServiceClient = userServiceClient;
        this.imageServiceClient = imageServiceClient;
        this.rabbitMQUtils = rabbitMQUtils;
    }

    public Result<VideoComments> addComment(Integer userId, VideoCommentDTO videoComment) {
        // 不能发空评论。
        if (videoComment.getContent() == null) {
            return Result.error(Response.COMMENT_NOT_EMPTY);
        }
        // 校验评论内容长度，不能超过200个字符。
        if (videoComment.getContent().length() > 200) {
            return Result.error(Response.COMMENT_TOO_LONG);
        }

        // TODO 这里缺少校验视频id合法性的逻辑，需要一个优雅的解决方案，总是调视频服务不科学！
        // 构建文档

        // 获取评论中可能存在的图片并上传至文件服务器
        String imgName = null;
        if (videoComment.getImage() != null && !videoComment.getImage().isEmpty()) {
            HashMap<String, String> map = new HashMap<>();
            map.put("base64", videoComment.getImage());
            try {
                Result<String> result = imageServiceClient.uploadCommentImg(map);
                if (result.isSuccess()) {
                    imgName = result.getData();
                }
            } catch (Exception e) {
                log.info("图片服务调用失败：{}", e.getMessage());
            }
        }
        VideoComments comment = VideoComments.builder().userId(userId).videoId(videoComment.getVideoId()).parentCommentId(videoComment.getParentCommentId()).replyTargetId(videoComment.getReplyTargetId()).isRoot(videoComment.getParentCommentId() == null) // 没有传递父级评论ID，说明是根评论
                .content(videoComment.getContent()).imgUrl(imgName).build();
        // 插入
        VideoComments inserted = mongoTemplate.insert(comment);

        // 如果是回复，则给被回复的根评论的回复总数 + 1。
        if (!inserted.getIsRoot()) {
            updateRootRepliesCount(inserted, 1);
        }

        // 发送消息
        CommentAddMessage commentAddMessage = new CommentAddMessage();
        commentAddMessage.setVideoComments(comment);
        rabbitMQUtils.sendMessage(CommentAddExchange.FANOUT_EXCHANGE_COMMENT_ADD, "", JSONUtils.toJSON(commentAddMessage));
        // 返回插入的文档
        // 转化一下评论图片的地址，以便前端显示
        inserted.setImgUrl(imgName != null ? GatewayHttpPath.COMMENT_IMG + imgName : null);
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
        Query query = new Query(Criteria.where("_id").is(commentId).and("userId").is(userId));
        Update update = new Update().set("isDeleted", true);

        mongoTemplate.updateFirst(query, update, VideoComments.class);

        // 5. 如果这是一条子回复，更新父评论的回复数量
        if (byId.getParentCommentId() != null) {
            updateRootRepliesCount(byId, -1);
        }

        // 6. 发送消息
        CommentAddMessage commentAddMessage = new CommentAddMessage();
        commentAddMessage.setVideoComments(byId);
        rabbitMQUtils.sendMessage(CommentDeleteExchange.FANOUT_EXCHANGE_COMMENT_DELETE, "", JSONUtils.toJSON(commentAddMessage));
        return Result.success(null);
    }


    /**
     * 辅助方法：增加或减少根评论的回复数量
     */
    private void updateRootRepliesCount(VideoComments comments, int delta) {
        Query query = new Query(Criteria.where("_id").is(comments.getParentCommentId()));
        Update update = new Update().inc("childCount", delta);
        mongoTemplate.updateFirst(query, update, VideoComments.class);
    }


    /**
     * 获取评论
     *
     * @param userId          用户id
     * @param videoId         视频id
     * @param nextOffsetCoded 加密后的游标
     * @param parentCommentId 父评论id
     * @return 评论列表
     */
    public Result<CursorLoad<VideoCommentsVO>> getComments(Integer userId, Integer videoId, String nextOffsetCoded, String parentCommentId) {

        // 未登录用户，禁止获取回复
        if (userId == -1 && parentCommentId != null) {
            return Result.error(Response.UNAUTHORIZED);
        }
        // 解码游标，以获取参数
        NextOffset nextOffset = new NextOffset();
        if (nextOffsetCoded != null) {
            try {
                nextOffset = PageTokenUtils.decryptState(nextOffsetCoded, NextOffset.class);
            } catch (Exception e) {
                log.error("解析游标参数失败！", e);
                return Result.error(Response.PARAMS_WRONG);
            }
            // 判断传入参数与游标中写入参数的一致性
            if (videoId != null && parentCommentId != null) {
                if (!(videoId.equals(nextOffset.getVideoId()) && userId.equals(nextOffset.getUserId()) && parentCommentId.equals(nextOffset.getParentCommentId()))) {
                    return Result.error(Response.PARAMS_WRONG);
                }
            }
            // 判断游标是否过期
            long difference = System.currentTimeMillis() - nextOffset.getTimestamp();
            if (difference > TimeUnit.DAYS.toMillis(1)) {
                return Result.error(Response.PARAMS_WRONG);
            }
        }
        // 1. 构建查询返回基础评论，如果传入了父评论id，则说明是拉取回复，否则拉取根评论
        int limit = (parentCommentId == null) ? 10 : 2; // 如果是拉取根评论，每次拉取10条，如果是回复，每次拉取2条
        // 执行查询
        List<VideoComments> comments = parentCommentId == null ?
                findRootComment(videoId, nextOffset.getScore(), nextOffset.getLastCommentId(), limit + 1)
                : findReplyComment(videoId, parentCommentId, nextOffset.getLastCommentId(), limit + 1);
        // 没有查询到任何评论，返回空集合
        if (comments.isEmpty()) {
            return Result.success(CursorLoad.empty());
        }
        boolean hasMore = false;    // 是否还有更多评论标记
        if (comments.size() > limit) {
            hasMore = true;
            comments = comments.subList(0, limit); // 去掉多查的一条
        }
        // 如果还有下一页，保存结果集的最后一条评论，作为游标
        String encryptedState = null;
        if (hasMore) {
            VideoComments cursor = comments.get(comments.size() - 1);
            nextOffset = NextOffset.builder().userId(userId).videoId(cursor.getVideoId()).lastCommentId(cursor.getId()).parentCommentId(cursor.getParentCommentId()).score(cursor.getScore()).build();
            try {
                encryptedState = PageTokenUtils.encryptState(nextOffset);
            } catch (Exception e) {
                log.error("加密偏移量时发生错误！", e);
            }
        }
        List<VideoCommentsVO> list = getVideoCommentsVOS(userId, comments);
        // 构建最终结果并返回
        // 对于未登录用户，不返回游标，以禁止翻页
        return Result.success(CursorLoad.of(
                list,
                userId != -1 && hasMore,
                userId != -1 ? encryptedState : null)
        );
    }

    private @NotNull List<VideoCommentsVO> getVideoCommentsVOS(Integer userId, List<VideoComments> comments) {
        // 获取的评论ID集合
        List<String> commentIdsCollect = comments.stream().map(VideoComments::getId).toList();
        // 对应评论的用户ID集合
        List<Integer> userIdslist = comments.stream().map(VideoComments::getUserId).toList();
        // 2. 查询用户对目前获取的评论的点赞记录
        List<CommentLike> likedList = List.of();
        if (userId != -1) {
            likedList = mongoTemplate.find(new Query(Criteria.where("commentId").in(commentIdsCollect).and("userId").is(userId)), CommentLike.class);
        }
        // 3. 调用用户服务，批量查询评论拥有者信息
        List<? extends User> users = new ArrayList<>();
        try {
            Result<List<User>> userinfoById = userServiceClient.getUserinfoById(userIdslist);
            if (userinfoById.isSuccess()) {
                users = userinfoById.getData();
            } else {
                log.error("用户服务调用失败，原因：{}", userinfoById.getMsg());
                throw new RuntimeException();
            }
        } catch (Exception e) {
            log.error("用户服务不可用，原因：{}", e.getMessage());
            throw new RuntimeException();
        }
        // 4. 组装完整评论信息并返回
        // 先构建 userId -> User 的 Map
        Map<Integer, User> userMap = users.stream().collect(Collectors.toMap(User::getId, Function.identity()));

        // 构建likedList -> CommentLike 的 Map
        Map<String, CommentLike> likedCommentsMap = likedList.stream().collect(Collectors.toMap(CommentLike::getCommentId, Function.identity()));

        // 构建 VO List 组装数据
        return comments.stream().map(c -> {
            VideoCommentsVO vo = new VideoCommentsVO();
            // 把文件名转为URL
            if (c.getImgUrl() != null && !c.getImgUrl().isEmpty()) {
                c.setImgUrl(GatewayHttpPath.COMMENT_IMG + c.getImgUrl());
            }
            vo.setComments(c);
            // 判断是否点赞
            vo.setLiked(likedCommentsMap.get(c.getId()) != null);
            // 获取原始用户信息
            vo.setUser(userMap.get(c.getUserId()));
            return vo;
        }).toList();
    }


    /**
     * 查询根评论方法
     *
     * @param videoId      视频id
     * @param score        热度值，对于根评论，作为排序依据
     * @param lasCommentId 上次加载的最后一条评论id，排除掉这一条
     * @return 评论列表
     */
    private List<VideoComments> findRootComment(Integer videoId, Double score, String lasCommentId, int limit) {
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
            c2 = Criteria.where("_id").gt(new ObjectId(lasCommentId)).and("score").is(score);
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
    private List<VideoComments> findReplyComment(Integer videoId, String parentCommentId, String lastCommentId, int limit) {
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

    public Result<List<VideoCommentsVO>> findReplyComment(Integer userId, String pid, Integer page) {

        Query query = new Query();
        // 排序
        query.with(PageRequest.of(page, 5));
        query.with(Sort.by(Sort.Direction.ASC, "_id"));
        query.addCriteria(Criteria.where("parentCommentId").is(pid));
        query.addCriteria(Criteria.where("isRoot").is(false));
        query.addCriteria(Criteria.where("isDeleted").is(false));

        List<VideoComments> videoComments = mongoTemplate.find(query, VideoComments.class);

        if (videoComments.isEmpty()) {
            return Result.success(List.of());
        }

        List<VideoCommentsVO> videoCommentsVOS = getVideoCommentsVOS(userId, videoComments);

        return Result.success(videoCommentsVOS);

    }

    public Result<SingleCommentVO> getSingle(Integer userId, String cid) {

        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(cid)
                .and("isDeleted").is(false));  // 只查询未被逻辑删除的记录

        VideoComments videoComment = mongoTemplate.findOne(query, VideoComments.class);

        if (videoComment == null) {
            return Result.success(null);
        }

        // 如果它不是一条根评论，查询一下它在父评论中页码的位置
        int pageNum = -1;
        if (!videoComment.getIsRoot()) {
            String pid = videoComment.getParentCommentId();

            query = new Query();
            query.addCriteria(Criteria.where("parentCommentId").is(pid)
                    .and("createdAt").lt(videoComment.getCreatedAt()));
            long countBefore = mongoTemplate.count(query, VideoComments.class);

            int pageSize = 5;
            pageNum = (int) (countBefore / pageSize) + 1;

            // 如果是次级评论，则最后返回的是它的根评论。
            query = new Query();
            query.addCriteria(Criteria.where("_id").is(pid)
                    .and("isDeleted").is(false));  // 只查询未被逻辑删除的记录
            videoComment = mongoTemplate.findOne(query, VideoComments.class);
        }

        if (videoComment == null) {
            return Result.success(null);
        }

        query = new Query(Criteria.where("commentId")
                .is(cid).and("userId").is(userId));
        CommentLike liked = mongoTemplate.findOne(query, CommentLike.class);

        Integer commentUserId = videoComment.getUserId();

        Result<List<User>> userinfoById = userServiceClient.getUserinfoById(List.of(commentUserId));
        if (!userinfoById.isSuccess()) {
            log.error("用户服务调用失败.");
            throw new RuntimeException();
        }

        SingleCommentVO vo = new SingleCommentVO();
        vo.setComments(videoComment);
        vo.setUser(userinfoById.getData().get(0));
        vo.setLiked(liked != null);
        vo.setPage(pageNum);

        return Result.success(vo);
    }
}
