package com.megrez.service;

import com.megrez.result.Response;
import com.megrez.result.Result;
import com.megrez.utils.JWTUtil;
import com.megrez.utils.RedisUtils;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {
    private final RedisUtils redisUtils;

    public AuthService(RedisUtils redisUtils) {
        this.redisUtils = redisUtils;
    }

    public Result<String> getAuthCode(Integer userId) {
        // 1. 生成一个随机的授权码
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String code = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        // 2. 写入Redis，设置一分钟有效期
        String existing = redisUtils.setIfAbsentReturnExisting("auth:user:" + userId, code, 10, TimeUnit.SECONDS);
        // 3. 返回
        return Result.success(Objects.requireNonNullElse(existing, code));
    }

    public Result<String> getTokenByAuthCode(Integer userId, String code) {
        // 1. 查询这个code是否有效
        String result = redisUtils.get("auth:user:" + userId);
        // 2. 判断存在情况
        if (result == null) {
            return Result.error(Response.UNAUTHORIZED);
        }
        // 3. 存在，判断是否和提供的code一致
        if (result.equals(code)) {
            // 3. 存在，即授权码有效，立刻使这个授权码失效。
            redisUtils.del("auth:user:" + userId);
            // 4. 生成JWT并返回。
            String token = JWTUtil.generateToken(userId);
            return Result.success(token);
        }
        // 4. 不一致，返回错误
        return Result.error(Response.UNAUTHORIZED);
    }
}
