package com.megrez.service;

import com.megrez.client.UserServiceClient;
import com.megrez.client.VideoInfoClient;
import com.megrez.mongo_document.Notification;
import com.megrez.mongo_document.VideoComments;
import com.megrez.mysql_entity.User;
import com.megrez.mysql_entity.UserFollow;
import com.megrez.mysql_entity.Video;
import com.megrez.rabbit.exchange.CommentAddExchange;
import com.megrez.rabbit.exchange.CommentLikeExchange;
import com.megrez.rabbit.exchange.SocialFollowExchange;
import com.megrez.rabbit.exchange.VideoLikeExchange;

import com.megrez.rabbit.message.CommentAddMessage;
import com.megrez.rabbit.message.CommentLikeMessage;
import com.megrez.rabbit.message.VideoLikeMessage;
import com.megrez.redis.NotifyAndDMRedisClient;
import com.megrez.result.Response;
import com.megrez.result.Result;
import com.megrez.utils.CollectionUtils;
import com.megrez.utils.JSONUtils;
import com.megrez.vo.notification_dm_service.NotificationVO;
import com.megrez.vo.video_info_service.VideoVO;
import com.mongodb.client.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class NotifyService {

    private static final Logger log = LoggerFactory.getLogger(NotifyService.class);
    private final MongoTemplate mongoTemplate;
    private final VideoInfoClient videoInfoClient;
    private final NotifyAndDMRedisClient redisClient;
    private final UserServiceClient userServiceClient;

    public NotifyService(MongoTemplate mongoTemplate, VideoInfoClient videoInfoClient, NotifyAndDMRedisClient redisClient, UserServiceClient userServiceClient, MongoClient mongo) {
        this.mongoTemplate = mongoTemplate;
        this.videoInfoClient = videoInfoClient;
        this.redisClient = redisClient;
        this.userServiceClient = userServiceClient;
    }


    /**
     * 获取通知列表
     *
     * @param userId 用户id
     * @param type   类型，0：全部通知，1：粉丝，2：获赞, 3：评论
     * @return 通知列表
     */
    public Result<List<NotificationVO>> getNotifications(Integer userId, Integer type) {
        Criteria criteria = Criteria.where("userId").is(userId);

        switch (type) {
            case 0 -> {
            }
            case 1 -> criteria.and("type").is(1);
            case 2 -> criteria.and("type").in(2, 3);
            case 3 -> criteria.and("type").in(4, 5);
            default -> {
                return Result.success(List.of());
            }
        }

        Query query = new Query(criteria);

        query.with(Sort.by(Sort.Direction.DESC, "_id"));

        List<Notification> notifications = mongoTemplate.find(query, Notification.class);

        if (notifications.isEmpty()) {
            return Result.success(List.of());
        }

        List<Integer> uIds = CollectionUtils.toList(notifications, Notification::getOperatorId);
        List<Integer> vIds = CollectionUtils.toList(notifications, Notification::getSourceVideoId);

        Result<List<User>> userinfoById = userServiceClient.getUserinfoById(uIds);
        Result<List<Video>> videoInfoByIds = videoInfoClient.getVideoInfoByIds(vIds);
        if (!userinfoById.isSuccess() || !videoInfoByIds.isSuccess()) {
            return Result.error(Response.UNKNOWN_WRONG);
        }

        Map<Integer, User> userMap = CollectionUtils.toMap(userinfoById.getData(), User::getId);
        Map<Integer, Video> videoMap = CollectionUtils.toMap(videoInfoByIds.getData(), Video::getId);


        List<NotificationVO> list = notifications.stream().map(n -> {
            NotificationVO vo = new NotificationVO();
            BeanUtils.copyProperties(n, vo);
            vo.setUser(userMap.get(n.getOperatorId()));
            vo.setSourceInfo(videoMap.get(n.getSourceVideoId()));
            return vo;
        }).toList();

        return Result.success(list);
    }


    /**
     * 消费视频点赞消息
     *
     * @param message
     */
    @RabbitListener(queues = VideoLikeExchange.QUEUE_VIDEO_LIKE_NOTIFICATION)
    public void insertByLikeVideo(String message) {
        VideoLikeMessage videoLikeMessage = JSONUtils.fromJSON(message, VideoLikeMessage.class);

        Integer videoId = videoLikeMessage.getVideoId();
        //  查询视频发布者ID
        Result<Video> videoInfoById = videoInfoClient.getVideoInfoById(videoId);
        if (!videoInfoById.isSuccess()) {
            log.error("调用视频消息服务失败。{}", videoInfoById.getMsg());
            throw new RuntimeException();
        }
        Video videoInfo = videoInfoById.getData();

        // 如果是目标是自己，则不做任何处理。
        if (videoLikeMessage.getUserId().equals(videoInfo.getUploaderId())) {
            return;
        }

        // 创建分组key，对于视频点赞，格式：
        // receiverId-type-videoId
        String groupKey = videoInfo.getUploaderId() + "-" + "2" + "-" + videoLikeMessage.getVideoId();
        // 构建通知
        Notification notification = Notification.builder()
                .type(2)    // 类型2：视频点赞
                .groupKey(groupKey)// 设置合并键
                .operatorId(videoLikeMessage.getUserId()) // 通知触发者ID
                .sourceVideoId(videoLikeMessage.getVideoId()) // 源头ID，这里是视频
                .userId(videoInfo.getUploaderId()) // 被通知者ID
                .build();
        // 写入
        mongoTemplate.insert(notification);
        // 给被通知者的通知未读数 + 1
        redisClient.incNotifyUnread(videoInfo.getUploaderId());
    }

    /**
     * 消费关注消息
     *
     * @param message
     */
    @RabbitListener(queues = SocialFollowExchange.QUEUE_SOCIAL_FOLLOW_NOTIFICATION)
    public void insertByFollow(String message) {
        UserFollow userFollow = JSONUtils.fromJSON(message, UserFollow.class);
        // 获取推送目标ID
        if (userFollow.getIsDeleted() == 0) {
            Integer tid = userFollow.getFollowingId();
            Integer cid = userFollow.getFollowerId();
            String groupKey = tid + "-" + "1";

            Notification notification = Notification.builder()
                    .userId(tid)
                    .operatorId(cid)
                    .groupKey(groupKey)
                    .type(1)
                    .build();
            // 写入
            mongoTemplate.insert(notification);
            // 给被通知者的通知未读数 + 1
            redisClient.incNotifyUnread(tid);
        }
    }

    @RabbitListener(queues = CommentLikeExchange.QUEUE_COMMENT_LIKE_NOTIFICATION)
    public void insertByCommentLike(String message) {

        CommentLikeMessage commentLikeMessage = JSONUtils.fromJSON(message, CommentLikeMessage.class);

        if (commentLikeMessage.getUserId().equals(commentLikeMessage.getCommentSender())) {
            return;
        }
        //
        Integer targetUserId = commentLikeMessage.getCommentSender();
        Notification notification = Notification.builder()
                .userId(targetUserId)
                .content(commentLikeMessage.getContent())
                .operatorId(commentLikeMessage.getUserId())
                .type(3)
                .sourceCommentId(commentLikeMessage.getCommentId())
                .sourceVideoId(commentLikeMessage.getVideoId())
                .groupKey(targetUserId + "-3-" + commentLikeMessage.getCommentId()).build();
        mongoTemplate.insert(notification);
        // 给被通知者的通知未读数 + 1
        redisClient.incNotifyUnread(commentLikeMessage.getCommentSender());
    }


    @RabbitListener(queues = CommentAddExchange.QUEUE_COMMENT_ADD_NOTIFICATION)
    public void insertByCommentAdd(String message) {
        CommentAddMessage commentAddMessage = JSONUtils.fromJSON(message, CommentAddMessage.class);

        VideoComments comment = commentAddMessage.getVideoComments();

        Integer videoId = comment.getVideoId();

        Integer targetUID = null;
        int type = 0;

        // 查询评论的视频信息
        Result<VideoVO> videoInfo = videoInfoClient.getVideoInfoByIdV2(videoId);
        if (!videoInfo.isSuccess()) {
            log.info("视频信息服务调用失败...");
            throw new RuntimeException();
        }
        VideoVO videoInfoData = videoInfo.getData();

        // 如果是根评论，通知目标为视频作者
        if (comment.getIsRoot()) {
            targetUID = videoInfoData.getUploaderId();
            type = 4;
        } else {
            // 如果是回复，则通知目标为目标评论作者
            VideoComments targetComment = mongoTemplate.findById(comment.getReplyTargetId(), VideoComments.class);
            if (targetComment != null) {
                targetUID = targetComment.getUserId();
                type = 5;
            }
        }

        // 如果最终通知目标就是这个评论的作者，则不作任何处理。
        if (comment.getUserId().equals(targetUID)) {
            return;
        }

        // 构建消息
        Notification notification = Notification.builder()
                .userId(targetUID)
                .content(comment.getContent())
                .operatorId(comment.getUserId())
                .type(type)
                .sourceCommentId(comment.getId())
                .sourceVideoId(comment.getVideoId())
                .groupKey(targetUID + "-" + type + "-" + comment.getId()).build();
        mongoTemplate.insert(notification);
        // 给被通知者的通知未读数 + 1
        redisClient.incNotifyUnread(targetUID);
    }

    public Result<Integer> getUnreadCount(Integer uid) {
        Integer unreadTotal = redisClient.getNotifyUnreadTotal(uid);
        return Result.success(unreadTotal);
    }


    public Result<Void> delUnreadCount(Integer uid) {
        redisClient.delNotifyUnreadCount(uid);
        return Result.success(null);
    }
}
