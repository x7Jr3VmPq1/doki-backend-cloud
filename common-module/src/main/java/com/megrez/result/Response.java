package com.megrez.result;

public enum Response {
    // 通用状态码
    SUCCESS(200, "操作成功"),
    FAILED(500, "操作失败"),
    VALIDATE_FAILED(400, "参数校验失败，请检查参数是否正确"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有相关权限"),
    TOO_MANY_REQUEST(410, "请求过于频繁，请稍后再试"),

    // 用户服务错误
    USER_SMS_CODE_WRONG(10001, "短信验证码错误"),
    USER_LOGIN_WRONG(10002, "账号或密码错误");

    private final int code;
    private final String message;

    Response(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
