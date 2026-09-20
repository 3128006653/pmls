package com.example1.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_material")
public class Material {
    private Long id;
    private String name;
    private Long categoryId;
    private String spec;
    private String unit;
    private Integer safeStock;
    private Integer status;
    private Integer deleted;
}