package com.megrez.utils;

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
}
