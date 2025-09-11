package com.megrez.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.megrez.entity.User;
import com.megrez.exception.InvalidCodeException;
import com.megrez.exception.PasswordWrongException;
import com.megrez.mapper.LoginAndRegisterMapper;
import com.megrez.service.LoginAndRegisterService;
import com.megrez.service.SmsService;
import com.megrez.utils.JWTUtil;
import com.megrez.utils.PasswordUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 用户登录/注册相关接口
 * 包括以下功能：
 * 1. 手机号注册
 * 2. 短信登录
 * 3. 密码登录
 * 4. 添加新用户
 * 5. 重置密码
 */
@Service
public class LoginAndRegisterServiceImpl implements LoginAndRegisterService {
    private final SmsService smsService;
    private final LoginAndRegisterMapper loginAndRegisterMapper;

    public LoginAndRegisterServiceImpl(SmsService smsService, LoginAndRegisterMapper loginAndRegisterMapper) {
        this.smsService = smsService;
        this.loginAndRegisterMapper = loginAndRegisterMapper;
    }

    @Override
    public Map<String, String> loginBySms(String phone, String code) throws JsonProcessingException {
        // 1. 核对手机号和验证码是否有效
        boolean result = smsService.verifyCode(phone, code);
        Map<String, String> data = new HashMap<>();
        if (result) {
            // 2. 有效，判断用户是否已经注册，如果没有注册，添加新用户，添加token
            User userByPhone = loginAndRegisterMapper.getUserByPhone(phone);
            if (userByPhone == null) {
                User newUser = addNewUser(phone);
                data.put("token", JWTUtil.generateToken(newUser.getUserName(), newUser.getId()));
            } else {
                data.put("token", JWTUtil.generateToken(userByPhone.getUserName(), userByPhone.getId()));
            }
            // 3. 判断用户是否设置了密码
            data.put("hasPassword", loginAndRegisterMapper.hasPassword(phone) != null ? "1" : "0");

            // 返回结果
            return data;
        } else {
            // 无效，抛出验证码错误异常
            throw new InvalidCodeException();
        }
    }

    @Override
    public String loginByPassword(String phone, String rawPassword) {
        // 1. 手机号为空，返回
        if (phone == null) {
            throw new PasswordWrongException("手机号或密码错误");
        }
        // 2. 查询用户信息
        User userByPhone = loginAndRegisterMapper.getUserByPhone(phone);
        // 3. 没有查询到用户，或者该用户没有设置密码，返回
        if (userByPhone == null || userByPhone.getPasswordHash() == null) {
            throw new PasswordWrongException("手机号或密码错误");
        }
        // 4. 比对密码
        boolean matched = PasswordUtils.matchPassword(rawPassword, userByPhone.getPasswordHash());
        if (matched) {
            // 成功返回token
            return JWTUtil.generateToken(userByPhone.getUserName(), userByPhone.getId());
        } else {
            // 比对失败，抛出密码错误异常
            throw new PasswordWrongException("手机号或密码错误");
        }
    }

    @Override
    public User addNewUser(String phone) {
        // 创建用户对象，给予默认值
        User newUser = new User();
        newUser.setUserName("用户" + new Random().nextInt(10000000));
        newUser.setPhoneNumber(phone);
        newUser.setAvatarUrl("http://localhost:8081/avatars/defaultAvatar.png");
        // 添加到表中
        loginAndRegisterMapper.addNewUser(newUser);
        // 把创建好的用户返回给调用处
        return newUser;
    }

    @Override
    public void setPassword(Long userId, String password) {
        // 加密处理
        String hashPassword = PasswordUtils.hashPassword(password);
        loginAndRegisterMapper.setPassword(userId, hashPassword);
    }

    @Override
    public String resetPassword(String phone, String code, String newPassword) {
        return "";
    }
}
