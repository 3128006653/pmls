package com.example1.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example1.entity.Material;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface MaterialMapper extends BaseMapper<Material> {

    /** 保留：EasyExcel 批量导入用，对应 resources/mapper/MaterialMapper.xml */
    int insertBatch(@Param("list") List<Material> list);

    @Select("SELECT id,name,category_id,spec,unit,safe_stock,status,deleted FROM t_material " +
            "WHERE deleted=0 " +
            "AND (#{keyword} = '' OR name LIKE CONCAT('%',#{keyword},'%')) " +
            "AND (#{categoryId} = 0 OR category_id = #{categoryId}) " +
            "ORDER BY id ASC LIMIT #{offset},#{size}")
    List<Material> selectPageList(@Param("keyword") String keyword,
                                  @Param("categoryId") long categoryId,
                                  @Param("offset") long offset,
                                  @Param("size") long size);

    @Select("SELECT COUNT(*) FROM t_material WHERE deleted=0 " +
            "AND (#{keyword} = '' OR name LIKE CONCAT('%',#{keyword},'%')) " +
            "AND (#{categoryId} = 0 OR category_id = #{categoryId})")
    long countPage(@Param("keyword") String keyword,
                   @Param("categoryId") long categoryId);

    @Update("UPDATE t_material SET deleted=1 WHERE id=#{id}")
    int logicDelete(@Param("id") Long id);
}
