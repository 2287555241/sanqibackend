package sanqibackend.sanqibackend.utils;

import java.io.Serializable;

public class Result<T> implements Serializable {
    private String code;
    private String msg;
    private T data;

    public Result() {}

    public Result(String code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>("0", "成功", data);
    }

    public static <T> Result<T> success(T data, String msg) {
        return new Result<>("0", msg, data);
    }

    public static <T> Result<T> error(String code, String msg) {
        return new Result<>(code, msg, null);
    }

    // Getters and Setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}