package com.example1.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_transfer")
public class Transfer {
    private Long id;
    private String transferNo;
    private Long fromBranchId;
    private Long toBranchId;
    private Long materialId;
    private Integer quantity;
    private Long operatorId;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
}