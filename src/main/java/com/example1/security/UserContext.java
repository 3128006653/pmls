package com.example1.security;

/**
 * 请求级用户上下文。
 * 必须在 finally 里 remove()，否则线程池复用会导致用户串号甚至内存泄漏。
 */
public class UserContext {

    private static final ThreadLocal<LoginUser> LOCAL = new ThreadLocal<>();

    public static void set(LoginUser u) { LOCAL.set(u); }

    public static LoginUser get()       { return LOCAL.get(); }

    public static void remove()         { LOCAL.remove(); }
}
