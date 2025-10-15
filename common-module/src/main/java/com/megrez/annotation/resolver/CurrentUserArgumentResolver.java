package com.megrez.annotation.resolver;

import com.megrez.annotation.CurrentUser;
import com.megrez.exception.UnauthorizedException;
import io.jsonwebtoken.*;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // 只处理 @CurrentUser 注解，参数类型是 Integer（userId）
        return parameter.hasParameterAnnotation(CurrentUser.class) &&
                parameter.getParameterType().equals(Integer.class);
    }

    @Override
    public Integer resolveArgument(MethodParameter parameter,
                                   ModelAndViewContainer mavContainer,
                                   NativeWebRequest webRequest,
                                   WebDataBinderFactory binderFactory) {
        // 获取参数上的注解信息
        CurrentUser user = parameter.getParameterAnnotation(CurrentUser.class);
        assert user != null;
        // 获取是否必须信息
        boolean required = user.required();
        try {
            // 获取请求头
            String authHeader = webRequest.getHeader("Authorization");
            // 从请求头中获取token并解析
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                Claims claims = Jwts.parser()
                        .setSigningKey("1rS1Qur2XmrwIG9QgPwSc4sS89pZhaluU5hIX9feyA0")
                        .parseClaimsJws(token)
                        .getBody();
                // 返回用户ID
                return claims.get("id", Integer.class);
            }
            // 未携带Authorization或格式有误的情况，抛出异常
            throw new UnauthorizedException("未登录");
        } catch (Exception e) {
            // 解析失败，判断是否为必须登录
            if (required) {
                throw new UnauthorizedException("未登录");
            }
        }
        // 不必须，返回一个代表游客的id
        return -1;
    }
}
