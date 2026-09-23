package com.example1.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_user")
public class User {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String role;          // ADMIN / OPERATOR
    private Long branchId;        // ADMIN 可为 NULL
    private Integer status;
    private Integer deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
