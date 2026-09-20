package com.example1.common;

public record Result<T>(int code, String msg, T data) {
    public static <T> Result<T> ok(T data)     { return new Result<>(200, "success", data); }
    public static <T> Result<T> fail(String m) { return new Result<>(400, m, null); }
}