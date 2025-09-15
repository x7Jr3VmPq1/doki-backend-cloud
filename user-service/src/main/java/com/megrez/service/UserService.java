package com.megrez.service;

import com.megrez.client.ImageServiceClient;
import com.megrez.entity.User;
import com.megrez.mapper.UserMapper;
import com.megrez.result.Response;
import com.megrez.result.Result;
import com.megrez.utils.PasswordUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


import java.util.HashMap;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final SmsService smsService;
    private final UserMapper userMapper;
    private final ImageServiceClient imageServiceClient;

    public UserService(SmsService smsService, UserMapper userMapper, ImageServiceClient imageServiceClient) {
        this.smsService = smsService;
        this.userMapper = userMapper;
        this.imageServiceClient = imageServiceClient;
    }

    /**
     * 根据用户名查询用户基本信息
     *
     * @param username 用户名
     * @return 用户基本信息
     */
    public Result getByName(String username) {
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
    public Result setPassword(String phone, String code, String password) {
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


    public Result update(User user) {
        // 1. 调用图片上传服务，获取上传后的文件名。
        HashMap<String, String> base64 = new HashMap<>();
        base64.put("base64", user.getAvatarUrl());

        try {
            Result result = imageServiceClient.uploadAvatar(base64);

            // 2. 如果成功，进行修改用户信息。
            if (result.isSuccess()) {
                // 把头像URL赋值为获取到的文件名
                user.setAvatarUrl((String) result.getData());
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
            log.error("上传服务异常：{}",e.getMessage());
            return Result.error(Response.USER_AVATAR_UPLOAD_WRONG);
        }
    }
}
