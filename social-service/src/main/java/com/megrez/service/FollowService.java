package com.megrez.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.megrez.entity.UserFollow;
import com.megrez.mapper.UserFollowMapper;
import com.megrez.rabbit.exchange.SocialFollowExchange;
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


@Service
public class FollowService {

    private static final Logger log = LoggerFactory.getLogger(FollowService.class);
    private final UserFollowMapper userFollowMapper;
    private final RabbitMQUtils rabbitMQUtils;

    public FollowService(UserFollowMapper userFollowMapper, RabbitMQUtils rabbitMQUtils) {
        this.userFollowMapper = userFollowMapper;
        this.rabbitMQUtils = rabbitMQUtils;
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
        try {
            // 先尝试更新，如果没成功再插入新纪录。
            UserFollow build = UserFollow.builder().followerId(userId).followingId(targetId).build();
            LambdaUpdateWrapper<UserFollow> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(UserFollow::getFollowerId, userId)
                    .eq(UserFollow::getFollowingId, targetId)
                    .eq(UserFollow::getIsDeleted, true);
            int updated = userFollowMapper.update(build, updateWrapper);
            if (updated == 0) {
                userFollowMapper.insert(build);
            }
            // 发送消息
            rabbitMQUtils.sendMessage(SocialFollowExchange.FANOUT_EXCHANGE_SOCIAL_FOLLOW,
                    "",
                    JSONUtils.toJSON(build));
            // 返回操作结果
            return Result.success(null);
        } catch (DuplicateKeyException e) {
            // 唯一约束
            log.warn("用户 {} 已经关注用户 {}", userId, targetId, e);
            return Result.error(Response.SOCIAL_FORBID_REPEAT_FOLLOW);
        } catch (DataIntegrityViolationException e) {
            // 外键约束
            log.warn("用户 {} 关注 {} 失败：没找到这个用户。", userId, targetId, e);
            return Result.error(Response.USER_NOT_FOUND_WRONG);
        } catch (Exception e) {
            // 其它错误返回未知异常
            log.error("写入关注表数据库发生异常。", e);
            return Result.error(Response.UNKNOWN_WRONG);
        }
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
        );
        return Result.success(userFollows);
    }
}
