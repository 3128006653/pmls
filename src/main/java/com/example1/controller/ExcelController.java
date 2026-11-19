package com.example1.controller;

import com.alibaba.excel.EasyExcel;
import com.example1.common.Result;
import com.example1.entity.MaterialExcelVO;
import com.example1.entity.StockExportVO;
import com.example1.mapper.StockExportMapper;
import com.example1.security.LoginUser;
import com.example1.security.UserContext;
import com.example1.service.MaterialExcelListener;
import com.example1.service.MaterialService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/excel")
@RequiredArgsConstructor
public class ExcelController {

    private final MaterialService materialService;
    private final StockExportMapper stockExportMapper;

    /** 批量导入物资 */
    @PostMapping("/materials/import")
    public Result<Map<String, Object>> importMaterials(@RequestParam("file") MultipartFile file) {
        long start = System.currentTimeMillis();

        MaterialExcelListener listener = new MaterialExcelListener(materialService);  // 每次 new，绝不能单例
        try (InputStream in = file.getInputStream()) {
            EasyExcel.read(in, MaterialExcelVO.class, listener).sheet().doRead();
        } catch (Exception e) {
            log.error("Excel 导入失败", e);
            throw new RuntimeException("导入失败：" + e.getMessage());
        }

        // 导入后删掉列表缓存（Cache Aside）
        materialService.evictListCache();

        long cost = System.currentTimeMillis() - start;
        log.info("Excel 导入耗时 {} ms，成功 {} 条", cost, listener.getSuccessCount());

        Map<String, Object> data = new HashMap<>();
        data.put("successCount", listener.getSuccessCount());
        data.put("errorCount", listener.getErrors().size());
        data.put("errors", listener.getErrors());
        data.put("costMs", cost);
        return Result.ok(data);
    }

    /** 下载导入模板 */
    @GetMapping("/materials/template")
    public void downloadTemplate(HttpServletResponse response) throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("物资导入模板", StandardCharsets.UTF_8.name());
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

        // 写一个只有表头、无数据的文件
        EasyExcel.write(response.getOutputStream(), MaterialExcelVO.class)
                .sheet("物资模板")
                .doWrite(List.of());
    }

    /** 导出库存表（OPERATOR 自动限定本网点） */
    @GetMapping("/stocks/export")
    public void exportStocks(@RequestParam(required = false) Long branchId,
                             HttpServletResponse response) throws Exception {
        LoginUser u = UserContext.get();
        if (u != null && !"ADMIN".equals(u.getRole())) {
            branchId = u.getBranchId();
        }

        List<StockExportVO> list = stockExportMapper.selectExportList(branchId);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("库存盘点表", StandardCharsets.UTF_8.name());
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), StockExportVO.class)
                .sheet("库存")
                .doWrite(list);
    }
}
