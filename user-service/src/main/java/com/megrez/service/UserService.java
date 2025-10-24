package com.megrez.service;

import com.megrez.client.ImageServiceClient;
import com.megrez.client.SocialServiceClient;
import com.megrez.constant.GatewayHttpPath;
import com.megrez.dto.social_service.CheckFollowDTO;
import com.megrez.entity.User;
import com.megrez.entity.UserFollow;
import com.megrez.mapper.UserMapper;
import com.megrez.result.Response;
import com.megrez.result.Result;
import com.megrez.utils.JWTUtil;
import com.megrez.utils.PasswordUtils;

import com.megrez.vo.user_service.UsersVO;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final SmsService smsService;
    private final UserMapper userMapper;
    private final ImageServiceClient imageServiceClient;
    private final SocialServiceClient socialServiceClient;

    public UserService(SmsService smsService, UserMapper userMapper, ImageServiceClient imageServiceClient, SocialServiceClient socialServiceClient) {
        this.smsService = smsService;
        this.userMapper = userMapper;
        this.imageServiceClient = imageServiceClient;
        this.socialServiceClient = socialServiceClient;
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
        // 1. 调用图片上传服务，获取上传后的文件名。
        HashMap<String, String> base64 = new HashMap<>();
        base64.put("base64", user.getAvatarUrl());

        try {
            Result<String> result = imageServiceClient.uploadAvatar(base64);

            // 2. 如果成功，进行修改用户信息。
            if (result.isSuccess()) {
                // 把头像URL赋值为获取到的文件名
                user.setAvatarUrl(result.getData());
                // 更新写入
                int updated = userMapper.update(user);
                if (updated == 1) {
                    // 成功返回更新后的user对象
                    return Result.success(user);
                }
                // 没有更新任何用户信息，返回失败
                return Result.error(Response.USER_NOT_FOUND_WRONG);
            }
            // 3. 上传失败，打印错误信息
            log.error("上传图片失败，原因：{}", result.getMsg());
            return Result.error(Response.USER_AVATAR_UPLOAD_WRONG);
        } catch (Exception e) {
            // 4. 服务异常，打印错误信息
            log.error("上传服务异常：{}", e.getMessage());
            return Result.error(Response.USER_AVATAR_UPLOAD_WRONG);
        }
    }

    /**
     * 根据用户ID批量获取用户信息
     *
     * @param targetIds 用户ID集合
     * @param userId    查询用户ID（不必须）
     * @return 用户信息
     */
    public Result<List<? extends User>> getByIds(Integer userId, List<Integer> targetIds) {
        // 先对targetIds去重
        targetIds = targetIds.stream().distinct().toList();
        // 查询基础用户信息
        List<? extends User> users = userMapper.selectBatchIds(targetIds);
        // 1. 拼接头像地址
        users.forEach(e -> e.setAvatarUrl(GatewayHttpPath.AVATAR + e.getAvatarUrl()));
        // 2. 如果没有提供userId，直接返回这个结果。
        if (userId == null) {
            return Result.success(users);
        }
        // 3. 开始构建VOS集合。
        List<UsersVO> usersVOS = users.stream().map(e -> {
            UsersVO usersVO = new UsersVO();
            BeanUtils.copyProperties(e, usersVO);
            return usersVO;
        }).toList();
        // 4. 获取查询结果的ids
        List<Integer> targetUidList = users.stream().map(User::getId).toList();
        // 5. 调用关系服务，查询当前用户是否和这些用户有关注关系
        Result<List<UserFollow>> listResult = socialServiceClient.checkFollow(new CheckFollowDTO(userId, targetUidList));
        if (listResult.isSuccess()) {
            // 6. 把查询到的结果转换为一个形如<id,UserFollow>的map，方便收集数据
            Map<Integer, UserFollow> collect = listResult.getData().stream().collect(Collectors.toMap(
                    UserFollow::getFollowingId,
                    Function.identity()
            ));
            // 7. 设置结果，如果在结果集里找不到这个值，说明没有关注。
            usersVOS.forEach(e -> e.setFollowed(collect.get(e.getId()) != null));
        }
        // 8. 返回最终结果。
        return Result.success(usersVOS);
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
