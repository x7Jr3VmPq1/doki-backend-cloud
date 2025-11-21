package com.megrez.result;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

// 统一响应结构
@Data
public class Result<T> {

    private static final Logger log = LoggerFactory.getLogger(Result.class);
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

    // 批量判断若干调用是否成功
    public static boolean allSuccess(List<Result<?>> results) {
        if (results == null || results.isEmpty()) {
            return false; // 没有结果，认为不成功
        }
        for (Result<?> result : results) {
            if (result == null) {
                return false;
            }
            if (!result.isSuccess()) {
                log.error("服务调用发生错误：{}", result.getMsg());
                return false;
            }
        }
        return true;
    }
}
