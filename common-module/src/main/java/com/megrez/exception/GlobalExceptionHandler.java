package com.megrez.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * JWT token无效时的返回内容
     *
     * @param e UnauthorizedException
     * @return token无效提示
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(UnauthorizedException e) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", 401);
        body.put("msg", e.getMessage());
        body.put("data", null);
        body.put("timestamp", System.currentTimeMillis());
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }
}
