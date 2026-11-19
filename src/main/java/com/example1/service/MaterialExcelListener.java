package com.example1.service;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.example1.entity.Material;
import com.example1.entity.MaterialExcelVO;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * EasyExcel 监听器
 *
 * 两个必须注意的坑：
 * 1. 不能被 Spring 管理 —— @Autowired 注入进来是 null，必须用构造器传依赖
 * 2. 不能做成单例 Bean —— 多线程下数据会串，每次 import 请求都要 new 一个新的
 */
@Slf4j
public class MaterialExcelListener extends AnalysisEventListener<MaterialExcelVO> {

    private static final int BATCH_SIZE = 100;

    private final MaterialService materialService;   // 构造器传入
    private final List<Material> buffer;
    private final List<String> errors;
    private int successCount = 0;

    public MaterialExcelListener(MaterialService materialService) {
        this.materialService = materialService;
        this.buffer = new ArrayList<>();
        this.errors = new ArrayList<>();
    }

    @Override
    public void invoke(MaterialExcelVO vo, AnalysisContext ctx) {
        int row = ctx.readRowHolder().getRowIndex() + 1;

        // 校验：错误行收集后回显，不抛异常中断整批
        if (vo.getName() == null || vo.getName().isBlank()) {
            errors.add("第" + row + "行：物资名称为空");
            return;
        }
        if (vo.getSafeStock() != null && vo.getSafeStock() < 0) {
            errors.add("第" + row + "行：安全库存不能为负");
            return;
        }

        Material m = new Material();
        m.setName(vo.getName().trim());
        m.setCategoryId(vo.getCategoryId());
        m.setSpec(vo.getSpec());
        m.setUnit(vo.getUnit() == null ? "个" : vo.getUnit());
        m.setSafeStock(vo.getSafeStock() == null ? 0 : vo.getSafeStock());
        m.setStatus(1);
        m.setDeleted(0);
        buffer.add(m);
        successCount++;

        if (buffer.size() >= BATCH_SIZE) {
            flush();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext ctx) {
        if (!buffer.isEmpty()) {
            flush();
        }
        log.info("Excel 导入完成，成功 {} 条，失败 {} 条", successCount, errors.size());
    }

    @Override
    public void onException(Exception e, AnalysisContext ctx) {
        int row = ctx.readRowHolder() == null ? -1 : ctx.readRowHolder().getRowIndex() + 1;
        errors.add("第" + row + "行解析失败：" + e.getMessage());
    }

    /** 批量落库：减少网络往返与事务开销 */
    private void flush() {
        materialService.saveBatch(buffer);
        buffer.clear();
    }

    public List<String> getErrors() {
        return errors;
    }

    public int getSuccessCount() {
        return successCount;
    }
}
