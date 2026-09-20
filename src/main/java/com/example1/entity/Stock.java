package com.example1.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_stock")
public class Stock {
    private Long id;
    private Long branchId;
    private Long materialId;
    private Integer quantity;
    private Integer version;
    private LocalDateTime updatedAt;
}