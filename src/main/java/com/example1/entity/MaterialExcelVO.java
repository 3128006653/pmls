package com.example1.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/** 物资导入模板对应的行对象 */
@Data
public class MaterialExcelVO {

    @ExcelProperty("物资名称")
    private String name;

    @ExcelProperty("分类ID")
    private Long categoryId;

    @ExcelProperty("规格")
    private String spec;

    @ExcelProperty("单位")
    private String unit;

    @ExcelProperty("安全库存")
    private Integer safeStock;
}
