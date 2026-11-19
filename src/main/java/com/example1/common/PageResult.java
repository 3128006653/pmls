package com.example1.common;

import java.util.List;

/** 手写分页的统一返回结构，避免依赖 MyBatis-Plus 的 Page 序列化 */
public record PageResult<T>(long total, long current, long size, List<T> records) {
}
