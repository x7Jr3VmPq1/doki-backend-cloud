package com.megrez.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.megrez.mysql_entity.User;
import com.megrez.mysql_entity.UserFollow;
import com.megrez.mapper.UserFollowMapper;
import com.megrez.rabbit.exchange.CountEventExchange;
import com.megrez.rabbit.exchange.SocialFollowExchange;
import com.megrez.rabbit.message.CountMessage;
import com.megrez.redis.SocialRedisClient;
import com.megrez.result.Response;
import com.megrez.result.Result;
import com.megrez.utils.JSONUtils;
import com.megrez.utils.RabbitMQUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class FollowService {

    private static final Logger log = LoggerFactory.getLogger(FollowService.class);
    private final UserFollowMapper userFollowMapper;
    private final RabbitMQUtils rabbitMQUtils;
    private final SocialRedisClient redisClient;

    public FollowService(UserFollowMapper userFollowMapper, RabbitMQUtils rabbitMQUtils, SocialRedisClient redisClient) {
        this.userFollowMapper = userFollowMapper;
        this.rabbitMQUtils = rabbitMQUtils;
        this.redisClient = redisClient;
    }

    /**
     * 关注用户方法。
     *
     * @param userId   当前用户
     * @param targetId 目标用户
     * @return 操作结果
     */
    public Result<Void> follow(Integer userId, Integer targetId) {
        // 不允许自己关注自己。
        if (userId.equals(targetId)) {
            return Result.success(null);
        }

        // 先尝试更新，如果没成功再插入新纪录。
        UserFollow build = UserFollow.builder().followerId(userId).followingId(targetId).build();
        LambdaUpdateWrapper<UserFollow> updateWrapper = new LambdaUpdateWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, userId)
                .eq(UserFollow::getFollowingId, targetId)
                .set(UserFollow::getUpdatedAt, System.currentTimeMillis())
                .set(UserFollow::getIsDeleted, false);
        int updated = userFollowMapper.update(updateWrapper);
        if (updated == 0) {
            // 写入主库
            userFollowMapper.insert(build);
        }

        // 写入缓存
        redisClient.addFollowing(userId, targetId);

        // 发送消息
        rabbitMQUtils.sendMessage(SocialFollowExchange.FANOUT_EXCHANGE_SOCIAL_FOLLOW,
                "",
                JSONUtils.toJSON(build));
        log.info("发送消息：{},{}", userId, targetId);
        rabbitMQUtils.sendMessage(CountEventExchange.DIRECT_EXCHANGE_COUNT,
                CountEventExchange.RK_FOLLOW,
                new CountMessage(userId, 1));
        rabbitMQUtils.sendMessage(CountEventExchange.DIRECT_EXCHANGE_COUNT,
                CountEventExchange.RK_FOLLOWED,
                new CountMessage(targetId, 1));
        // 返回操作结果
        return Result.success(null);

    }

    /**
     * 取消关注方法。
     *
     * @param userId   用户id
     * @param targetId 目标用户id
     * @return 操作结果
     */
    public Result<Void> unFollow(Integer userId, Integer targetId) {
        // 不允许自己取消关注自己。
        if (userId.equals(targetId)) {
            return Result.success(null);
        }
        // 尝试更新记录。
        LambdaUpdateWrapper<UserFollow> updateWrapper = new LambdaUpdateWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, userId)
                .eq(UserFollow::getFollowingId, targetId)
                .set(UserFollow::getUpdatedAt, System.currentTimeMillis())
                .set(UserFollow::getIsDeleted, true);
        int updated = userFollowMapper.update(updateWrapper);
        if (updated > 0) {
            // 清理缓存
            redisClient.removeFollowing(userId, targetId);
            // 重新查询关注数据
            UserFollow userFollow = userFollowMapper.selectOne(new LambdaQueryWrapper<UserFollow>()
                    .eq(UserFollow::getFollowerId, userId)
                    .eq(UserFollow::getFollowingId, targetId));
            // 发送消息
            rabbitMQUtils.sendMessage(SocialFollowExchange.FANOUT_EXCHANGE_SOCIAL_FOLLOW, "", JSONUtils.toJSON(userFollow));

            rabbitMQUtils.sendMessage(CountEventExchange.DIRECT_EXCHANGE_COUNT,
                    CountEventExchange.RK_FOLLOW,
                    new CountMessage(userId, -1));
            rabbitMQUtils.sendMessage(CountEventExchange.DIRECT_EXCHANGE_COUNT,
                    CountEventExchange.RK_FOLLOWED,
                    new CountMessage(targetId, -1));
            return Result.success(null);
        }
        return Result.error(Response.UNKNOWN_WRONG);
    }


    /**
     * 批量查询当前用户是否对目标用户有关注关系。
     * 提供一个ID列表，如果存在对这些用户的关注，则把关注的信息返回。
     *
     * @param userId    用户ID
     * @param targetIds 目标用户ID集合
     * @return 结果集，包含当前用户的关注列表。
     */
    public Result<List<UserFollow>> checkFollow(Integer userId, List<Integer> targetIds) {
        List<UserFollow> userFollows = userFollowMapper.selectList(
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getFollowerId, userId)
                        .in(UserFollow::getFollowingId, targetIds)
                        .eq(UserFollow::getIsDeleted, false)
        );
        // 根据传入的targetIds重新排序
        Map<Integer, UserFollow> collect = userFollows.stream().collect(Collectors.toMap(UserFollow::getFollowingId, Function.identity()));

        List<UserFollow> sorted = targetIds.stream().map(collect::get).filter(Objects::nonNull).toList();

        return Result.success(sorted);
    }
}
