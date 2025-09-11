package com.megrez.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.megrez.entity.User;

import java.util.Map;

public interface LoginAndRegisterService {

    // 短信验证码登录
    Map<String, String> loginBySms(String phone, String code) throws JsonProcessingException;

    // 密码登录
    String loginByPassword(String phone, String rawPassword);

    // 添加新用户
    User addNewUser(String phone);

    // 设置密码
    void setPassword(Long userId, String password);

    // 重置密码
    String resetPassword(String phone, String code, String newPassword);


}
