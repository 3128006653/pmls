package com.example1.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DashboardMapper {

    /** 分类占比：按 category_id 聚合物资数量 */
    @Select("SELECT category_id AS category_id, COUNT(*) AS material_count " +
            "FROM t_material WHERE deleted=0 GROUP BY category_id ORDER BY category_id")
    List<Map<String, Object>> categoryShare();

    /** 网点排行：按库存总量降序，LEFT JOIN 保证 0 库存网点也在榜上 */
    @Select("SELECT b.id AS branch_id, b.name AS branch_name, " +
            "COALESCE(SUM(s.quantity),0) AS total_qty, COUNT(s.id) AS sku_count " +
            "FROM t_branch b LEFT JOIN t_stock s ON s.branch_id=b.id " +
            "WHERE b.deleted=0 GROUP BY b.id,b.name ORDER BY total_qty DESC")
    List<Map<String, Object>> branchRank();

    /** 月度趋势：按出入库方向汇总操作日志，近 12 个月 */
    @Select("SELECT DATE_FORMAT(created_at,'%Y-%m') AS ym, " +
            "SUM(CASE WHEN op_type IN ('IN','TRANSFER_IN','CHECK_GAIN') THEN quantity ELSE 0 END) AS in_qty, " +
            "SUM(CASE WHEN op_type IN ('OUT','TRANSFER_OUT','CHECK_LOSS') THEN quantity ELSE 0 END) AS out_qty, " +
            "COUNT(*) AS op_count " +
            "FROM t_operation_log GROUP BY DATE_FORMAT(created_at,'%Y-%m') " +
            "ORDER BY ym DESC LIMIT 12")
    List<Map<String, Object>> monthlyTrend();
}
