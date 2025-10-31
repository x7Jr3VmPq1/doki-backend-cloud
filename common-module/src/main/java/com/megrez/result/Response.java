package com.megrez.result;

import lombok.Getter;

@Getter
public enum Response {
    // 通用状态码
    SUCCESS(200, "操作成功"),
    FAILED(500, "操作失败"),
    VALIDATE_FAILED(400, "参数校验失败，请检查参数是否正确"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有相关权限"),
    UNKNOWN_WRONG(444, "未知错误"),
    PARAMS_WRONG(450, "参数有误"),
    TOO_MANY_REQUEST(410, "请求过于频繁，请稍后再试"),

    // 用户服务错误
    USER_SMS_CODE_WRONG(10001, "短信验证码错误"),
    USER_LOGIN_WRONG(10002, "账号或密码错误"),
    USER_NOT_FOUND_WRONG(10003, "不存在的用户"),
    USER_AVATAR_UPLOAD_WRONG(10004, "上传头像失败，请重试"),
    USER_UPDATE_PARAMS_WRONG(10005, "用户信息含有非法参数"),

    // 图片服务错误
    IMAGE_UPLOAD_DECODE_WRONG(20001, "图片解码失败"),
    IMAGE_UPLOAD_SAVE_WRONG(20002, "图片保存失败"),

    // 视频服务错误
    VIDEO_UPLOAD_EMPTY_VIDEO(30001, "上传视频不能为空"),
    VIDEO_UPLOAD_SAVE_FAILED(30002, "文件上传失败，请重试"),
    VIDEO_UPLOAD_REPEAT_DRAFT(30003, "已经存在一个草稿了!"),
    VIDEO_UPLOAD_UPDATE_DRAFT_WRONG(30003, "修改草稿失败"),
    VIDEO_DRAFT_NOT_FOUND(30004, "不存在的草稿"),
    VIDEO_NOT_UPLOAD(30100, "请先上传视频"),

    // 社交关系服务错误
    SOCIAL_FORBID_REPEAT_FOLLOW(40001, "禁止重复关注"),

    // 评论服务错误
    COMMENT_NOT_EMPTY(50000, "评论不能为空"),
    COMMENT_TOO_LONG(50001, "评论太长啦"),

    //私信/通知服务错误
    MESSAGE_CANT_CREATE(60000, "会话创建失败"),
    CONVERSATION_NOT_FOUND(60001, "会话不存在");

    private final int code;
    private final String message;

    Response(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
