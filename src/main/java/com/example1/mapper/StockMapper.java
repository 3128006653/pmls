package com.example1.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example1.entity.Stock;
import org.apache.ibatis.annotations.Param;

public interface StockMapper extends BaseMapper<Stock> {

    /** 条件更新：库存不足或并发冲突都表现为返回 0 */
    int deductIfEnough(@Param("branchId") Long branchId,
                       @Param("materialId") Long materialId,
                       @Param("quantity") int quantity);

    /** 不存在则插入，存在则累加 */
    int upsertStock(@Param("branchId") Long branchId,
                    @Param("materialId") Long materialId,
                    @Param("quantity") int quantity);
    int addQuantity(@Param("branchId") Long branchId,
                    @Param("materialId") Long materialId,
                    @Param("quantity") int quantity);
    int setQuantity(@Param("branchId") Long branchId,
                    @Param("materialId") Long materialId,
                    @Param("quantity") int quantity);
}
