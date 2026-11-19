package com.example1.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example1.entity.Material;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MaterialMapper extends BaseMapper<Material> {

    int insertBatch(@Param("list") List<Material> list);
}