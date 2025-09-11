package com.megrez.utils;

import io.jsonwebtoken.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JWTUtil {

    // JWT 密钥，用于签名
    private static final String SECRET_KEY = "1rS1Qur2XmrwIG9QgPwSc4sS89pZhaluU5hIX9feyA0";

    // 生成 JWT
    public static String generateToken(String username, Integer userId) {
        Map<String, Object> claims = new HashMap<>();
        // 自定义载荷
        claims.put("username", username);
        claims.put("userId", userId);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10小时过期
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // 解析 JWT
    public static Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    // 获取用户名（或者其它用户信息）从 JWT 中
    public static String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    // 检查 token 是否过期
    public static boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    // 验证 token 是否有效
    public static boolean validateToken(String token, String username) {
        return (username.equals(extractUsername(token)) && !isTokenExpired(token));
    }
}
