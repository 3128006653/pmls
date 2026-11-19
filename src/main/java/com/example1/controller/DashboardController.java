package com.example1.controller;

import com.example1.common.Result;
import com.example1.mapper.DashboardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardMapper dashboardMapper;

    /** 分类占比 */
    @GetMapping("/category-share")
    public Result<List<Map<String, Object>>> categoryShare() {
        return Result.ok(dashboardMapper.categoryShare());
    }

    /** 网点库存排行 */
    @GetMapping("/branch-rank")
    public Result<List<Map<String, Object>>> branchRank() {
        return Result.ok(dashboardMapper.branchRank());
    }

    /** 月度出入库趋势 */
    @GetMapping("/monthly-trend")
    public Result<List<Map<String, Object>>> monthlyTrend() {
        return Result.ok(dashboardMapper.monthlyTrend());
    }
}
