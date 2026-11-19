package com.example1.mapper;

import com.example1.entity.StockExportVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 库存导出专用：联表查网点名与物资名 */
public interface StockExportMapper {

    List<StockExportVO> selectExportList(@Param("branchId") Long branchId);
}
