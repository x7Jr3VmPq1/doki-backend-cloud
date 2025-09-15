package com.megrez.utils;

import com.megrez.entity.User;

public class Validator {

    // 校验手机号规则
    public static boolean checkPhone(String phone) {
        if (phone == null) {
            return false;
        }
        String regex = "^1[3-9]\\d{9}$";
        return phone.matches(regex);
    }

    // 校验验证码规则
    public static boolean checkCode(String code) {
        if (code == null) {
            return false;
        }
        String regex = "^\\d{6}$";
        return code.matches(regex);
    }

    // 校验用户信息规则（允许字母、数字、下划线、中文）
    public static boolean checkUser(User user) {
        // 用户名规则：3-16位，字母/数字/下划线/中文，不能以下划线开头或结尾
        String regex = "^(?!_)[a-zA-Z0-9_\\u4e00-\\u9fa5]{3,16}(?<!_)$";

        // 没有传参数
        if (user == null) {
            return false;
        }
        // 没传用户名
        if (user.getUsername() == null) {
            return false;
        }
        // 用户名不符合规则
        if (!regex.matches(user.getUsername())) {
            return false;
        }

        // 个人简介必须小于200长度
        return user.getBio().length() <= 200;
    }

}
