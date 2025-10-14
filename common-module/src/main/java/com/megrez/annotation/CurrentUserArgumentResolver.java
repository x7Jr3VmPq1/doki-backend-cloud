package com.megrez.annotation;

import com.megrez.exception.UnauthorizedException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
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
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {

        String authHeader = webRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            Claims claims = null;
            try {
                claims = Jwts.parser()
                        .setSigningKey("1rS1Qur2XmrwIG9QgPwSc4sS89pZhaluU5hIX9feyA0")
                        .parseClaimsJws(token)
                        .getBody();
            } catch (Exception e) {
                throw new UnauthorizedException("未登录或Token无效");
            }
            return claims.get("id", Integer.class); // 直接返回用户ID
        }
        throw new UnauthorizedException("未登录或Token无效");
    }
}
