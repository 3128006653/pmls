package com.example1.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_inventory_check")
public class InventoryCheck {
    private Long id;
    private String checkNo;
    private Long branchId;
    /** DRAFT / SUBMITTED / AUDITED / VOID */
    private String status;
    private Long operatorId;
    private Long auditorId;
    private Integer totalDiff;
    private Integer changedCount;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime auditedAt;
}