package com.megrez.exception;

public class CodeNotConsumedException extends SmsCodeException {
    public CodeNotConsumedException() {
        super("发送过于频繁，请稍后再试");
    }
}