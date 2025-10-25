package com.megrez.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.megrez.client.UserServiceClient;
import com.megrez.dto.social_service.NextOffsetFollower;
import com.megrez.entity.UserFollow;
import com.megrez.mapper.UserFollowMapper;
import com.megrez.result.Response;
import com.megrez.result.Result;
import com.megrez.utils.PageTokenUtils;
import com.megrez.vo.social_service.Follower;
import com.megrez.vo.social_service.UserCursorLoadVO;
import com.megrez.vo.user_service.UsersVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListService {
    private static final Logger log = LoggerFactory.getLogger(ListService.class);

    private final UserFollowMapper userFollowMapper;
    private final UserServiceClient userServiceClient;

    public ListService(UserFollowMapper userFollowMapper, UserServiceClient userServiceClient) {
        this.userFollowMapper = userFollowMapper;
        this.userServiceClient = userServiceClient;
    }

    /**
     * 查询关注列表，支持游标滚动加载
     *
     * @param targetUid 目标用户id
     * @param userId    当前操作用户id
     * @param cursor    游标
     * @param mode      模式  1. 综合排序(目前先按照最近关注) 2. 最近关注 3. 最早关注
     * @return 用户视图列表
     * @throws Exception 如果解密游标发生错误，抛出异常。
     */
    public Result<UserCursorLoadVO> getFollowings(Integer targetUid, Integer userId, String cursor, Integer mode) throws Exception {

        // 先尝试获取游标对象。
        NextOffsetFollower follower = (cursor == null) ?
                null : PageTokenUtils.decryptState(cursor, NextOffsetFollower.class);
        int limit = 10;
        // 执行查询。
        List<UserFollow> userFollows = getUserFollowList(targetUid, mode, follower);

        // 没有查询到关注记录，返回空集合。
        if (userFollows.isEmpty()) {
            return Result.success(UserCursorLoadVO.builder().build());
        }

        // 有无更多数据的标记。
        boolean hasMore = false;
        if (userFollows.size() > limit) {
            hasMore = true;
            // 去掉多查的一条。
            userFollows = userFollows.subList(0, userFollows.size() - 1);
        }
        // 收集Ids，以查询用户信息。
        List<Integer> ids = userFollows.stream().map(UserFollow::getFollowingId).toList();

        // 调用远程服务获取用户信息。
        Result<List<UsersVO>> userinfoById = userServiceClient.getUserinfoByIdWithIfFollowed(userId, ids);
        if (!userinfoById.isSuccess()) {
            log.error("调用用户服务失败，原因：{}", userinfoById.getMsg());
            return Result.error(Response.UNKNOWN_WRONG);
        }
        // 查询到的用户信息列表集合
        List<UsersVO> data = userinfoById.getData();

        // 游标对象构建
        NextOffsetFollower nextOffset = NextOffsetFollower.builder()
                .userFollow(userFollows.get(userFollows.size() -1))
                .userId(userId)
                .mode(mode)
                .build();

        log.info("构建的游标：{}", nextOffset);

        // 构建最终内容
        UserCursorLoadVO build = UserCursorLoadVO.builder()
                .list(data)
                .hasMore(hasMore)
                .cursor(hasMore ?
                        PageTokenUtils.encryptState(nextOffset)
                        : null)
                .build();
        return Result.success(build);
    }

    private List<UserFollow> getUserFollowList(Integer targetUid, Integer mode, NextOffsetFollower nextOffsetFollower) {
        int limit = 10; // 每次加载十条。
        // 构建基础查询
        LambdaQueryWrapper<UserFollow> query = new LambdaQueryWrapper<UserFollow>().eq(UserFollow::getFollowerId, targetUid) // 关注者ID。
                .last("LIMIT " + (limit + 1)); // 多返回一条作为有无更多数据的判断依据。
        switch (mode) {
            case 1, 2 -> query.orderByDesc(UserFollow::getCreatedAt); // 综合排序
            case 3 -> query.orderByAsc(UserFollow::getCreatedAt); // 最早关注
        }
        // 如果有游标，加入附加条件。
        if (nextOffsetFollower != null) {
            // 获取游标对象中存储的关注记录信息，获取关注时间。
            UserFollow userFollow = nextOffsetFollower.getUserFollow();
            Long updatedAt = userFollow.getUpdatedAt();
            // 排除上次加载的记录。
            query.ne(UserFollow::getFollowingId, userFollow.getId());
            switch (mode) {
                case 1, 2 -> query.le(UserFollow::getCreatedAt, updatedAt); // 综合排序
                case 3 -> query.ge(UserFollow::getCreatedAt, updatedAt); // 最早关注
            }
        }
        // 执行查询
        return userFollowMapper.selectList(query);
    }

}

