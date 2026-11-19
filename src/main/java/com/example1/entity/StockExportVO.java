package com.example1.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/** 库存导出对应的行对象 */
@Data
public class StockExportVO {

    @ExcelProperty("网点")
    private String branchName;

    @ExcelProperty("物资")
    private String materialName;

    @ExcelProperty("规格")
    private String spec;

    @ExcelProperty("当前库存")
    private Integer quantity;

    @ExcelProperty("安全库存")
    private Integer safeStock;

    @ExcelProperty("是否预警")
    private String warning;
}
