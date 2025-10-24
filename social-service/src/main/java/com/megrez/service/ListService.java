package com.megrez.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.megrez.client.UserServiceClient;
import com.megrez.dto.social_service.NextOffsetFollower;
import com.megrez.entity.User;
import com.megrez.entity.UserFollow;
import com.megrez.mapper.UserFollowMapper;
import com.megrez.result.Response;
import com.megrez.result.Result;
import com.megrez.utils.PageTokenUtils;
import com.megrez.vo.social_service.FollowerVO;
import com.megrez.vo.social_service.UserCursorLoadVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ListService {
    private static final Logger log = LoggerFactory.getLogger(ListService.class);

    private final UserFollowMapper userFollowMapper;
    private final UserServiceClient userServiceClient;

    public ListService(UserFollowMapper userFollowMapper, UserServiceClient userServiceClient) {
        this.userFollowMapper = userFollowMapper;
        this.userServiceClient = userServiceClient;
    }

    public Result<UserCursorLoadVO> getFollowings(Integer userId, String cursor, Integer mode) throws Exception {
        // 三种查询模式：
        // 1. 综合排序(目前先按照最近关注) 2. 最近关注 3. 最早关注

        // 先尝试获取游标对象。
        NextOffsetFollower follower = (cursor == null) ?
                null : PageTokenUtils.decryptState(cursor, NextOffsetFollower.class);
        int limit = 10;
        // 执行查询。
        List<UserFollow> userFollows = getUserFollowList(userId, mode, follower);
        // 有无更多数据的标记。
        boolean hasMore = false;
        if (userFollows.size() > limit) {
            hasMore = true;
            // 去掉多查的一条。
            userFollows = userFollows.subList(0, userFollows.size() - 1);
        }
        // 收集Ids，以查询用户信息。
        List<Integer> ids = userFollows.stream().map(UserFollow::getFollowerId).toList();
        // 调用远程服务获取用户信息。
        Result<List<User>> userinfoById = userServiceClient.getUserinfoById(ids);
        if (!userinfoById.isSuccess()) {
            log.error("调用用户服务失败，原因：{}", userinfoById.getMsg());
            return Result.error(Response.UNKNOWN_WRONG);
        }
        // 创建视图对象并返回。
        List<User> data = userinfoById.getData();
        UserCursorLoadVO build = UserCursorLoadVO.builder()
                .list(data)
                .hasMore(hasMore)
                .cursor(hasMore ? PageTokenUtils.encryptState(data.get(data.size() - 1)) : null)
                .build();
        return Result.success(build);
    }

    private List<UserFollow> getUserFollowList(Integer userId, Integer mode, NextOffsetFollower nextOffsetFollower) {
        int limit = 10; // 每次加载十条。
        // 构建基础查询
        LambdaQueryWrapper<UserFollow> query = new LambdaQueryWrapper<UserFollow>().eq(UserFollow::getFollowingId, userId) // 关注者ID。
                .last("LIMIT " + (limit + 1)); // 多返回一条作为有无更多数据的判断依据。
        switch (mode) {
            case 1, 2 -> query.orderByDesc(UserFollow::getCreatedAt); // 综合排序
            case 3 -> query.orderByAsc(UserFollow::getCreatedAt); // 最早关注
        }
        // 如果有游标，加入附加条件。
        if (nextOffsetFollower != null) {
            // 获取游标对象中存储的用户信息，拿到其中的关注时间。
            FollowerVO follower = nextOffsetFollower.getFollowerVO();
            Long followedTime = follower.getFollowedTime();
            Integer id = follower.getId();
            query.ne(UserFollow::getFollowingId, id); // 排除上次加载的记录。
            switch (mode) {
                case 1, 2 -> query.le(UserFollow::getCreatedAt, followedTime); // 综合排序
                case 3 -> query.ge(UserFollow::getCreatedAt, followedTime); // 最早关注
            }
        }
        // 执行查询
        return userFollowMapper.selectList(query);
    }

}
