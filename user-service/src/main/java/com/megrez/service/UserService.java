package com.megrez.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.megrez.client.ImageServiceClient;
import com.megrez.client.SocialServiceClient;
import com.megrez.constant.GatewayHttpPath;
import com.megrez.dto.social_service.CheckFollowDTO;
import com.megrez.mysql_entity.User;
import com.megrez.mysql_entity.UserFollow;
import com.megrez.mapper.UserMapper;
import com.megrez.rabbit.exchange.UserUpdateExchange;
import com.megrez.result.Response;
import com.megrez.result.Result;
import com.megrez.utils.JWTUtil;
import com.megrez.utils.PasswordUtils;

import com.megrez.utils.RabbitMQUtils;
import com.megrez.vo.user_service.UsersVO;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final SmsService smsService;
    private final UserMapper userMapper;
    private final ImageServiceClient imageServiceClient;
    private final SocialServiceClient socialServiceClient;
    private final RabbitMQUtils rabbitMQUtils;

    public UserService(SmsService smsService, UserMapper userMapper, ImageServiceClient imageServiceClient, SocialServiceClient socialServiceClient, RabbitMQUtils rabbitMQUtils) {
        this.smsService = smsService;
        this.userMapper = userMapper;
        this.imageServiceClient = imageServiceClient;
        this.socialServiceClient = socialServiceClient;
        this.rabbitMQUtils = rabbitMQUtils;
    }

    /**
     * 根据用户名查询用户基本信息
     *
     * @param username 用户名
     * @return 用户基本信息
     */
    public Result<User> getByName(String username) {
        User user = userMapper.findUserByName(username);
        return user != null ? Result.success(user)
                : Result.error(Response.USER_NOT_FOUND_WRONG);
    }

    /**
     * 修改密码逻辑
     *
     * @param phone    手机号
     * @param code     短信验证码
     * @param password 密码
     * @return 操作结果
     */
    public Result<Void> setPassword(String phone, String code, String password) {
        // 1. 验证短信验证码
        boolean valid = smsService.verifyCode(phone, code);
        if (valid) {
            // 2. 没有这个用户，设置失败
            Integer updatedRow = userMapper.setPassword(phone, PasswordUtils.hashPassword(password));
            if (updatedRow == 0) {
                return Result.error(Response.USER_NOT_FOUND_WRONG);
            }
            // 2.1 验证通过，重置密码
            return Result.success(null);
        } else {
            // 3. 验证不通过，返回错误
            return Result.error(Response.USER_SMS_CODE_WRONG);
        }
    }


    public Result<User> update(User user) {
        // 1. 先判断有没有上传头像
        if (user.getAvatarUrl() != null) {
            // 调用图片上传服务，获取上传后的文件名。
            HashMap<String, String> base64 = new HashMap<>();
            base64.put("base64", user.getAvatarUrl());
            Result<String> result = imageServiceClient.uploadAvatar(base64);
            // 如果成功，把返回的文件名添加到对象上。
            if (result.isSuccess()) {
                user.setAvatarUrl(result.getData());
            } else {
                log.error("上传图片失败，原因：{}", result.getMsg());
                return Result.error(Response.UNKNOWN_WRONG);
            }
        }
        // 更新写入
        user.setUpdatedAt(System.currentTimeMillis());
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, user.getId()); // 用户ID
        wrapper.set(User::getUsername, user.getUsername()); // 用户名
        wrapper.set(User::getBio, user.getBio()); // 个人简介
        wrapper.set(User::getUpdatedAt, user.getUpdatedAt()); // 更新时间
        // 只有在头像不为空时才更新。
        if (user.getAvatarUrl() != null) {
            wrapper.set(User::getAvatarUrl, user.getAvatarUrl());
        }

        int updated = userMapper.update(wrapper);
        User updatedUser = userMapper.selectById(user.getId());
        if (updated == 1) {
            // 发送更新消息
            rabbitMQUtils.sendMessage(UserUpdateExchange.FANOUT_EXCHANGE_USER_UPDATE, "", updatedUser);
            // 成功返回更新后的user对象
            return Result.success(user);
        }
        // 没有更新任何用户信息，返回失败
        return Result.error(Response.USER_NOT_FOUND_WRONG);
    }

    /**
     * 根据用户ID批量获取用户信息
     *
     * @param targetIds 用户ID集合
     * @param userId    查询用户ID（不必须）
     * @return 用户信息
     */
    public Result<List<? extends User>> getByIds(Integer userId, List<Integer> targetIds) {
        // 1. 去重
        targetIds = targetIds.stream().distinct().toList();

        // 2. 查询
        List<? extends User> users = userMapper.selectBatchIds(targetIds);

        if (users.isEmpty()) {
            return Result.error(Response.USER_NOT_FOUND_WRONG);
        }

        // 3. 拼接头像地址
        users.forEach(e -> e.setAvatarUrl(GatewayHttpPath.AVATAR + e.getAvatarUrl()));

        // 4. 构建 VOs
        List<UsersVO> usersVOS = users.stream().map(e -> {
            UsersVO usersVO = new UsersVO();
            BeanUtils.copyProperties(e, usersVO);
            return usersVO;
        }).toList();

        // 5. 如果有 userId，补充关注状态
        if (userId != null) {
            List<Integer> targetUidList = users.stream().map(User::getId).toList();
            Result<List<UserFollow>> listResult = socialServiceClient.checkFollow(new CheckFollowDTO(userId, targetUidList));
            if (listResult.isSuccess()) {
                Map<Integer, UserFollow> followMap = listResult.getData().stream()
                        .collect(Collectors.toMap(UserFollow::getFollowingId, Function.identity()));
                usersVOS.forEach(e -> e.setFollowed(followMap.containsKey(e.getId())));
            }
        }

        // 6. 重新排序，让结果与传入的 targetIds 顺序一致
        Map<Integer, UsersVO> voMap = usersVOS.stream()
                .collect(Collectors.toMap(UsersVO::getId, Function.identity()));

        List<UsersVO> sorted = targetIds.stream()
                .map(voMap::get)
                .filter(Objects::nonNull)
                .toList();

        return Result.success(sorted);
    }

    /**
     * 根据token查询用户信息
     *
     * @param token token
     * @return 用户信息
     */
    public Result<User> getUserinfoByToken(String token) {
        // 1. 解析载荷
        Claims claims = JWTUtil.extractClaims(token);
        if (claims == null) {
            return Result.error(Response.FORBIDDEN);
        }
        Integer id = (Integer) claims.get("id");
        // 2. 查询用户信息
        User user = userMapper.selectById(id);
        // 拼接头像地址
        user.setAvatarUrl(GatewayHttpPath.AVATAR + user.getAvatarUrl());
        return Result.success(user);
    }
}
