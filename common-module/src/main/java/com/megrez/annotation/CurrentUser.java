package com.megrez.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 获取当前操作用户ID
 * 在controller层的参数中使用
 * 如： @CurrentUser Integer userId
 * required = true → 必须登录，false → 可选登录（允许游客）
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
    boolean required() default true;
}
