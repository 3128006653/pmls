package com.example1.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_operation_log")
public class OperationLog {
    private Long id;
    private Long branchId;
    private Long materialId;
    private String opType;      // IN/OUT/TRANSFER_OUT/TRANSFER_IN
    private Integer quantity;
    private Integer beforeQty;
    private Integer afterQty;
    private Long relatedId;
    private String relatedType;
    private Long operatorId;
    private String operatorName;
    private String remark;
    private LocalDateTime createdAt;
}