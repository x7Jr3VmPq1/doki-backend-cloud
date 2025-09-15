package com.megrez.result;

// 统一响应结构
public class Result {

    int code; // 状态码
    String msg; // 提示信息
    Object data; // 响应数据
    long timestamp; // 时间戳

    public Result() {
    }


    public Result(int code, String msg, Object data, long timestamp) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.timestamp = timestamp;
    }

    public static Result success(Object data) {
        return new Result(
                Response.SUCCESS.getCode(),
                Response.SUCCESS.getMessage(),
                data,
                System.currentTimeMillis()
        );
    }

    public static Result error(Response response) {
        return new Result(
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

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
