package com.megrez.exception;

// 基础异常：所有验证码相关异常的父类

public class SmsCodeException extends RuntimeException {
    public SmsCodeException(String message) {
        super(message);
    }
}

