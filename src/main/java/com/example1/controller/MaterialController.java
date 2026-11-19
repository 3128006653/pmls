package com.example1.controller;

import com.example1.common.BusinessException;
import com.example1.common.PageResult;
import com.example1.common.Result;
import com.example1.entity.Material;
import com.example1.mapper.MaterialMapper;
import com.example1.service.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;
    private final MaterialMapper materialMapper;

    /** 走 Redis 缓存 */
    @GetMapping
    public Result<List<Material>> list() {
        return Result.ok(materialService.listCached());
    }

    /** 直连数据库，用于压测对照 */
    @GetMapping("/nocache")
    public Result<List<Material>> listNoCache() {
        return Result.ok(materialService.listDirect());
    }

    /** 分页 + 关键词 + 分类过滤；分页直查库，不读缓存 */
    @GetMapping("/page")
    public Result<PageResult<Material>> page(@RequestParam(defaultValue = "1") long current,
                                             @RequestParam(defaultValue = "10") long size,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) Long categoryId) {
        if (current < 1) current = 1;
        if (size < 1 || size > 100) size = 10;
        String kw = keyword == null ? "" : keyword.trim();
        long cid = categoryId == null ? 0L : categoryId;

        long total = materialMapper.countPage(kw, cid);
        List<Material> records = total == 0
                ? List.of()
                : materialMapper.selectPageList(kw, cid, (current - 1) * size, size);
        return Result.ok(new PageResult<>(total, current, size, records));
    }

    @GetMapping("/{id}")
    public Result<Material> detail(@PathVariable Long id) {
        Material m = materialMapper.selectById(id);
        if (m == null || (m.getDeleted() != null && m.getDeleted() == 1)) {
            return Result.fail("物资不存在");
        }
        return Result.ok(m);
    }

    /** 新增后删缓存，保证 Cache Aside 一致性 */
    @PostMapping
    public Result<Material> create(@RequestBody Material m) {
        if (m.getName() == null || m.getName().isBlank()) {
            throw new BusinessException("物资名称不能为空");
        }
        m.setId(null);
        m.setDeleted(0);
        if (m.getStatus() == null) m.setStatus(1);
        if (m.getSafeStock() == null) m.setSafeStock(0);
        materialMapper.insert(m);
        materialService.evictListCache();
        return Result.ok(m);
    }

    @PutMapping("/{id}")
    public Result<Material> update(@PathVariable Long id, @RequestBody Material m) {
        Material exist = materialMapper.selectById(id);
        if (exist == null || (exist.getDeleted() != null && exist.getDeleted() == 1)) {
            throw new BusinessException("物资不存在");
        }
        m.setId(id);
        m.setDeleted(0);
        materialMapper.updateById(m);
        materialService.evictListCache();
        return Result.ok(materialMapper.selectById(id));
    }

    /** 逻辑删除，不物理删，避免破坏 t_stock / 盘点明细的外键语义 */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        Material exist = materialMapper.selectById(id);
        if (exist == null) throw new BusinessException("物资不存在");
        materialMapper.logicDelete(id);
        materialService.evictListCache();
        return Result.ok("已下架");
    }
}
