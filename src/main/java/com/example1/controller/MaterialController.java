package com.example1.controller;

import com.example1.common.Result;
import com.example1.entity.Material;
import com.example1.mapper.MaterialMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialMapper materialMapper;

    @GetMapping
    public Result<List<Material>> list() {
        return Result.ok(materialMapper.selectList(null));
    }
}