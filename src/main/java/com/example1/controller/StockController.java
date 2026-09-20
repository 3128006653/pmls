package com.example1.controller;

import com.example1.common.Result;
import com.example1.service.StockService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @PostMapping("/in")
    public Result<String> in(@RequestBody StockOpRequest req) {
        stockService.stockIn(req.getBranchId(), req.getMaterialId(), req.getQuantity(), req.getRemark());
        return Result.ok("入库成功");
    }

    @PostMapping("/out")
    public Result<String> out(@RequestBody StockOpRequest req) {
        stockService.stockOut(req.getBranchId(), req.getMaterialId(), req.getQuantity(), req.getRemark());
        return Result.ok("出库成功");
    }

    @PostMapping("/transfer")
    public Result<String> transfer(@RequestBody TransferRequest req) {
        Long id = stockService.transfer(req.getFromBranchId(), req.getToBranchId(),
                req.getMaterialId(), req.getQuantity());
        return Result.ok("调拨成功，调拨单号：" + id);
    }

    @Data
    public static class StockOpRequest {
        private Long branchId;
        private Long materialId;
        private Integer quantity;
        private String remark;
    }

    @Data
    public static class TransferRequest {
        private Long fromBranchId;
        private Long toBranchId;
        private Long materialId;
        private Integer quantity;
    }
}