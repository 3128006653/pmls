package com.example1.controller;

import com.example1.common.BusinessException;
import com.example1.common.PageResult;
import com.example1.common.Result;
import com.example1.entity.Branch;
import com.example1.mapper.BranchMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchMapper branchMapper;

    @GetMapping("/page")
    public Result<PageResult<Branch>> page(@RequestParam(defaultValue = "1") long current,
                                           @RequestParam(defaultValue = "10") long size,
                                           @RequestParam(required = false) String keyword) {
        if (current < 1) current = 1;
        if (size < 1 || size > 100) size = 10;
        String kw = keyword == null ? "" : keyword.trim();

        long total = branchMapper.countPage(kw);
        List<Branch> records = total == 0
                ? List.of()
                : branchMapper.selectPageList(kw, (current - 1) * size, size);
        return Result.ok(new PageResult<>(total, current, size, records));
    }

    /** 给统计看板 / 前端下拉框用，不分页 */
    @GetMapping
    public Result<List<Branch>> list() {
        return Result.ok(branchMapper.selectPageList("", 0, 100));
    }

    @GetMapping("/{id}")
    public Result<Branch> detail(@PathVariable Long id) {
        Branch b = branchMapper.selectById(id);
        if (b == null || (b.getDeleted() != null && b.getDeleted() == 1)) {
            return Result.fail("网点不存在");
        }
        return Result.ok(b);
    }

    @PostMapping
    public Result<Branch> create(@RequestBody Branch b) {
        if (b.getName() == null || b.getName().isBlank()) {
            throw new BusinessException("网点名称不能为空");
        }
        b.setId(null);
        b.setDeleted(0);
        if (b.getStatus() == null) b.setStatus(1);
        branchMapper.insert(b);
        return Result.ok(b);
    }

    @PutMapping("/{id}")
    public Result<Branch> update(@PathVariable Long id, @RequestBody Branch b) {
        Branch exist = branchMapper.selectById(id);
        if (exist == null || (exist.getDeleted() != null && exist.getDeleted() == 1)) {
            throw new BusinessException("网点不存在");
        }
        b.setId(id);
        b.setDeleted(0);
        branchMapper.updateById(b);
        return Result.ok(branchMapper.selectById(id));
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        Branch exist = branchMapper.selectById(id);
        if (exist == null) throw new BusinessException("网点不存在");
        branchMapper.logicDelete(id);
        return Result.ok("已停用");
    }
}
