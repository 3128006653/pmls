package com.example1.controller;

import com.example1.common.Result;
import com.example1.entity.Material;
import com.example1.service.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    /** 走 Redis 缓存 */
    @GetMapping
    public Result<List<Material>> list() {
        return Result.ok(materialService.listCached());
    }

    /** 直连数据库，用于压测对照 —— 必须在类的 } 之前 */
    @GetMapping("/nocache")
    public Result<List<Material>> listNoCache() {
        return Result.ok(materialService.listDirect());
    }
}