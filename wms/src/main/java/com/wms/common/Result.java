package com.wms.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private int code;
    private String msg;
    private T data;

    public static <T> Result<T> success() {
        return restResult(null, 200, "操作成功");
    }

    public static <T> Result<T> success(T data) {
        return restResult(data, 200, "操作成功");
    }

    public static <T> Result<T> success(T data, String msg) {
        return restResult(data, 200, msg);
    }

    public static <T> Result<T> error() {
        return restResult(null, 500, "操作失败");
    }

    public static <T> Result<T> error(String msg) {
        return restResult(null, 500, msg);
    }

    public static <T> Result<T> error(int code, String msg) {
        return restResult(null, code, msg);
    }

    public static <T> Result<T> unauthorized() {
        return restResult(null, 401, "未登录或登录已过期");
    }

    public static <T> Result<T> forbidden() {
        return restResult(null, 403, "权限不足，无法执行该操作");
    }

    private static <T> Result<T> restResult(T data, int code, String msg) {
        Result<T> apiResult = new Result<>();
        apiResult.setCode(code);
        apiResult.setData(data);
        apiResult.setMsg(msg);
        return apiResult;
    }
}
