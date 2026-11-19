package com.example1.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_branch")
public class Branch {
    private Long id;
    private String name;
    private String city;
    private String address;
    private String manager;
    private String phone;
    private Integer status;
    private Integer deleted;
}
