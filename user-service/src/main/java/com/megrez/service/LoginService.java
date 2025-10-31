package com.megrez.service;

import com.megrez.mysql_entity.User;
import com.megrez.mysql_entity.UserStatistics;
import com.megrez.mapper.UserMapper;
import com.megrez.mapper.UserStatisticsMapper;
import com.megrez.result.Response;
import com.megrez.result.Result;
import com.megrez.utils.JWTUtil;
import com.megrez.utils.PasswordUtils;
import com.megrez.vo.user_service.LoginSuccessVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


import java.util.Random;

// 登录业务实现类
@Service
public class LoginService {
    private static final Logger log = LoggerFactory.getLogger(LoginService.class);
    private final SmsService smsService;
    private final UserMapper userMapper;
    private final UserStatisticsMapper userStatisticsMapper;

    public LoginService(SmsService smsService, UserMapper userMapper, UserStatisticsMapper userStatisticsMapper) {
        this.smsService = smsService;
        this.userMapper = userMapper;
        this.userStatisticsMapper = userStatisticsMapper;
    }

    /**
     * 短信登录逻辑
     *
     * @param phone 手机号
     * @param code  短信验证码
     * @return token/是否设置密码
     */
    public Result<LoginSuccessVO> loginBySms(String phone, String code) {
        // 1. 核对手机号和验证码是否有效
        boolean valid = smsService.verifyCode(phone, code);
        if (valid) {
            // 2. 有效，尝试获取用户信息判断是否注册
            User userByPhone = userMapper.getUserByPhone(phone);
            if (userByPhone == null) {
                // 2.1 没有注册，添加新用户，并发放token
                User newuser = new User();
                // 构建用户信息
                newuser.setPhoneNumber(phone);
                newuser.setUsername("新用户" + new Random().nextInt(100000000));
                newuser.setAvatarUrl("default.jpg");
                newuser.setCreatedAt(System.currentTimeMillis());
                newuser.setUpdatedAt(System.currentTimeMillis());
                // 写入
                userMapper.add(newuser);
                userStatisticsMapper.insert(
                        UserStatistics.builder()
                                .userId(newuser.getId())
                                .createdAt(System.currentTimeMillis())
                                .updatedAt(System.currentTimeMillis())
                                .build()
                );
                log.info("新用户：{}", phone);
                return Result.success(
                        new LoginSuccessVO(
                                JWTUtil.generateToken(newuser.getId()),
                                false
                        )
                );
            }
            // 2.2 已注册，直接发放token
            log.info("登录用户：{}", phone);
            return Result.success(
                    new LoginSuccessVO(
                            JWTUtil.generateToken(userByPhone.getId()),
                            userByPhone.getPasswordHash() != null  // 判断用户是否设置了密码
                    )
            );

        }
        // 3. 无效，返回验证码错误
        return Result.error(Response.USER_SMS_CODE_WRONG);
    }

    /**
     * 手机号密码登录
     *
     * @param phone    手机号
     * @param password 密码
     * @return token
     */
    public Result<String> loginByPassword(String phone, String password) {
        // 1. 根据手机号查询用户
        User userByPhone = userMapper.getUserByPhone(phone);
        // 2. 没有查询到用户、用户没有设置密码，或密码不匹配，返回错误
        if (userByPhone == null || userByPhone.getPasswordHash() == null
                || !PasswordUtils.matchPassword(password, userByPhone.getPasswordHash())) {
            return Result.error(Response.USER_LOGIN_WRONG);
        }
        // 3. 验证通过，返回token
        return Result.success(JWTUtil.generateToken(userByPhone.getId()));
    }
}
