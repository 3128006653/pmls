package com.example1.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example1.common.Result;
import com.example1.entity.InventoryCheck;
import com.example1.entity.InventoryCheckItem;
import com.example1.mapper.InventoryCheckItemMapper;
import com.example1.mapper.InventoryCheckMapper;
import com.example1.service.InventoryService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final InventoryCheckMapper checkMapper;
    private final InventoryCheckItemMapper itemMapper;

    @PostMapping("/create")
    public Result<Long> create(@RequestBody CreateRequest req) {
        return Result.ok(inventoryService.create(req.getBranchId(), req.getRemark()));
    }

    @PostMapping("/{id}/submit")
    public Result<String> submit(@PathVariable Long id, @RequestBody SubmitRequest req) {
        inventoryService.submit(id, req.getItems());
        return Result.ok("实盘数量已提交");
    }

    @PostMapping("/{id}/audit")
    public Result<String> audit(@PathVariable Long id) {
        inventoryService.audit(id);
        return Result.ok("盘点单已审核");
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        InventoryCheck check = checkMapper.selectById(id);
        if (check == null) return Result.fail("盘点单不存在");
        List<InventoryCheckItem> items = itemMapper.selectList(
                new LambdaQueryWrapper<InventoryCheckItem>().eq(InventoryCheckItem::getCheckId, id));
        Map<String, Object> data = new HashMap<>();
        data.put("check", check);
        data.put("items", items);
        return Result.ok(data);
    }

    @Data
    public static class CreateRequest {
        private Long branchId;
        private String remark;
    }

    @Data
    public static class SubmitRequest {
        private List<InventoryService.SubmitItem> items;
    }
}