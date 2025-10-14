package com.megrez.exception;

/**
 * 校验token无效，抛出此异常
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
