package com.example.mi.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class Result<T> implements Serializable {
    private Integer status;
    private String message;
    private T data;

    // 手动定义构造函数
    public Result(Integer code, String message, T data) {
        this.status = code;
        this.message = message;
        this.data = data;
    }

    // 成功响应
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "ok", data);
    }

    // 失败响应
    public static <T> Result<T> error(String message) {
        return new Result<>(0, message, null);
    }
}