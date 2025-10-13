package com.megrez.utils;

import io.jsonwebtoken.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JWTUtil {

    // JWT 密钥，用于签名
    private static final String SECRET_KEY = "1rS1Qur2XmrwIG9QgPwSc4sS89pZhaluU5hIX9feyA0";

    // 生成 JWT
    public static String generateToken(Integer userId) {
        Map<String, Object> claims = new HashMap<>();
        // 自定义载荷
        claims.put("id", userId);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10小时过期
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // 解析 JWT
    // 解析 JWT
    public static Claims extractClaims(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return null;
            }

            // 去掉前缀 "Bearer "（忽略大小写）
            String trim = token.replaceFirst("(?i)^bearer\\s+", "").trim();

            // 解析并返回 claims
            return Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(trim)
                    .getBody();
        } catch (Exception e) {
            // 解析失败，返回 null
            return null;
        }
    }


    // 检查 token 是否过期
    public static boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    // 判断给定用户ID是否和载荷相同
    public static boolean isSameUser(Integer id, String token) {
        return extractClaims(token).get("id").equals(id);
    }
}
