package com.megrez.result;

import lombok.Data;

// 统一响应结构
@Data
public class Result<T> {

    int code; // 状态码
    String msg; // 提示信息
    T data; // 响应数据
    long timestamp; // 时间戳

    public Result() {
    }


    public Result(int code, String msg, T data, long timestamp) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.timestamp = timestamp;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(
                Response.SUCCESS.getCode(),
                Response.SUCCESS.getMessage(),
                data,
                System.currentTimeMillis()
        );
    }

    public static <T> Result<T> error(Response response) {
        return new Result<>(
                response.getCode(),
                response.getMessage(),
                null,
                System.currentTimeMillis()
        );
    }

    // 判断操作是否成功方法
    public boolean isSuccess() {
        return this.code == Response.SUCCESS.getCode();
    }
}
