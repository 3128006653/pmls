package com.example1.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_inventory_check_item")
public class InventoryCheckItem {
    private Long id;
    private Long checkId;
    private Long materialId;
    private Integer bookQty;
    private Integer actualQty;
    private Integer auditBookQty;
    private Integer diffQty;
    private String remark;
}