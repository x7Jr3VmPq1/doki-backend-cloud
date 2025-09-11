package com.megrez.common;

@SuppressWarnings("unused")
public class ResponseCode {
    public static final int SUCCESS = 200; // 请求成功
    public static final int CREATED = 201; // 资源创建成功
    public static final int ACCEPTED = 202; // 请求已被接受但未处理完成
    public static final int NO_CONTENT = 204; // 请求成功，但无返回内容

    public static final int BAD_REQUEST = 400; // 请求参数错误
    public static final int UNAUTHORIZED = 401; // 未授权访问
    public static final int FORBIDDEN = 403; // 禁止访问
    public static final int NOT_FOUND = 404; // 资源未找到
    public static final int METHOD_NOT_ALLOWED = 405; // 请求方法不被允许
    public static final int CONFLICT = 409; // 资源冲突
    public static final int GONE = 410; // 资源已永久删除
    public static final int UNPROCESSABLE_ENTITY = 422; // 请求格式正确，但语义错误

    public static final int INTERNAL_SERVER_ERROR = 500; // 服务器内部错误
    public static final int NOT_IMPLEMENTED = 501; // 服务器不支持请求的功能
    public static final int SERVICE_UNAVAILABLE = 503; // 服务不可用
    public static final int GATEWAY_TIMEOUT = 504; // 网关超时
}
