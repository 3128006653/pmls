package com.example1.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example1.entity.Branch;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface BranchMapper extends BaseMapper<Branch> {

    /** 手写分页：LIMIT + 独立 COUNT，不依赖 MyBatis-Plus 分页插件 */
    @Select("SELECT id,name,city,address,manager,phone,status,deleted FROM t_branch " +
            "WHERE deleted=0 " +
            "AND (#{keyword} = '' OR name LIKE CONCAT('%',#{keyword},'%')) " +
            "ORDER BY id ASC LIMIT #{offset},#{size}")
    List<Branch> selectPageList(@Param("keyword") String keyword,
                                @Param("offset") long offset,
                                @Param("size") long size);

    @Select("SELECT COUNT(*) FROM t_branch WHERE deleted=0 " +
            "AND (#{keyword} = '' OR name LIKE CONCAT('%',#{keyword},'%'))")
    long countPage(@Param("keyword") String keyword);

    @Update("UPDATE t_branch SET deleted=1 WHERE id=#{id}")
    int logicDelete(@Param("id") Long id);
}
