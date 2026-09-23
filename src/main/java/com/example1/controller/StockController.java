package com.example1.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example1.common.Result;
import com.example1.entity.Stock;
import com.example1.mapper.StockMapper;
import com.example1.security.LoginUser;
import com.example1.security.UserContext;
import com.example1.service.StockService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;
    private final StockMapper stockMapper;

    /** 库存查询：OPERATOR 强制限定本网点，传别的 branchId 也没用 */
    @GetMapping
    public Result<List<Stock>> list(@RequestParam(required = false) Long branchId) {
        LoginUser u = UserContext.get();
        if (u != null && !"ADMIN".equals(u.getRole())) {
            branchId = u.getBranchId();     // 服务端覆盖，不信任前端传参
        }
        LambdaQueryWrapper<Stock> q = new LambdaQueryWrapper<>();
        if (branchId != null) {
            q.eq(Stock::getBranchId, branchId);
        }
        return Result.ok(stockMapper.selectList(q));
    }

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