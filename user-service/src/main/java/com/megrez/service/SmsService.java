package com.megrez.service;

import com.fasterxml.jackson.core.JsonProcessingException;

// 短信服务
public interface SmsService {

    // 获取验证码
    String sendCode(String phone) throws JsonProcessingException;

    // 校验验证码合法性
    boolean verifyCode(String phone, String code) throws JsonProcessingException;


}
