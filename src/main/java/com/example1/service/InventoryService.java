package com.example1.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example1.common.BusinessException;
import com.example1.entity.*;
import com.example1.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryCheckMapper checkMapper;
    private final InventoryCheckItemMapper itemMapper;
    private final StockMapper stockMapper;
    private final OperationLogMapper operationLogMapper;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    /** 创建盘点单：快照当前账面数 */
    @Transactional(rollbackFor = Exception.class)
    public Long create(Long branchId, String remark) {
        List<Stock> stocks = stockMapper.selectList(
                new LambdaQueryWrapper<Stock>().eq(Stock::getBranchId, branchId));
        if (stocks.isEmpty()) {
            throw new BusinessException("该网点暂无库存记录");
        }

        InventoryCheck c = new InventoryCheck();
        c.setCheckNo("PD" + LocalDateTime.now().format(FMT));
        c.setBranchId(branchId);
        c.setStatus("DRAFT");
        c.setTotalDiff(0);
        c.setChangedCount(0);
        c.setRemark(remark);
        checkMapper.insert(c);

        for (Stock s : stocks) {
            InventoryCheckItem item = new InventoryCheckItem();
            item.setCheckId(c.getId());
            item.setMaterialId(s.getMaterialId());
            item.setBookQty(s.getQuantity());   // 快照，仅用于展示对比
            itemMapper.insert(item);
        }
        return c.getId();
    }

    /** 提交实盘数量 */
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long checkId, List<SubmitItem> items) {
        InventoryCheck c = checkMapper.selectById(checkId);
        if (c == null) throw new BusinessException("盘点单不存在");
        if (!"DRAFT".equals(c.getStatus())) throw new BusinessException("当前状态不可录入，status=" + c.getStatus());

        for (SubmitItem si : items) {
            InventoryCheckItem item = itemMapper.selectOne(new LambdaQueryWrapper<InventoryCheckItem>()
                    .eq(InventoryCheckItem::getCheckId, checkId)
                    .eq(InventoryCheckItem::getMaterialId, si.getMaterialId()));
            if (item == null) throw new BusinessException("盘点明细不存在，物资ID：" + si.getMaterialId());
            item.setActualQty(si.getActualQty());
            item.setRemark(si.getRemark());
            itemMapper.updateById(item);
        }
        c.setStatus("SUBMITTED");
        checkMapper.updateById(c);
    }

    /**
     * 审核：以审核时刻的账面数重算差异（不是用创建时的快照），
     * 这样盘点期间的正常出入库不会被覆盖掉
     */
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long checkId) {
        InventoryCheck c = checkMapper.selectById(checkId);
        if (c == null) throw new BusinessException("盘点单不存在");
        if (!"SUBMITTED".equals(c.getStatus())) throw new BusinessException("当前状态不可审核，status=" + c.getStatus());

        List<InventoryCheckItem> items = itemMapper.selectList(
                new LambdaQueryWrapper<InventoryCheckItem>().eq(InventoryCheckItem::getCheckId, checkId));

        int totalDiff = 0;
        for (InventoryCheckItem item : items) {
            if (item.getActualQty() == null) continue;   // 没盘到的跳过

            Stock stock = stockMapper.selectOne(new LambdaQueryWrapper<Stock>()
                    .eq(Stock::getBranchId, c.getBranchId())
                    .eq(Stock::getMaterialId, item.getMaterialId()));
            int auditBook = stock == null ? 0 : stock.getQuantity();  // 审核时刻真实账面数
            int diff = item.getActualQty() - auditBook;

            item.setAuditBookQty(auditBook);
            item.setDiffQty(diff);
            itemMapper.updateById(item);

            if (diff == 0) continue;
            totalDiff += Math.abs(diff);

            if (stock == null) {
                stockMapper.upsertStock(c.getBranchId(), item.getMaterialId(), item.getActualQty());
            } else {
                stockMapper.setQuantity(c.getBranchId(), item.getMaterialId(), item.getActualQty());
            }

            OperationLog log = new OperationLog();
            log.setBranchId(c.getBranchId());
            log.setMaterialId(item.getMaterialId());
            log.setOpType(diff > 0 ? "CHECK_GAIN" : "CHECK_LOSS");
            log.setQuantity(Math.abs(diff));
            log.setBeforeQty(auditBook);
            log.setAfterQty(item.getActualQty());
            log.setRelatedId(checkId);
            log.setRelatedType("INVENTORY_CHECK");
            log.setRemark("盘点差异");
            operationLogMapper.insert(log);
        }

        // 记录盘点期间该网点发生过多少次出入库，供审核人追溯
        Long changed = operationLogMapper.selectCount(new LambdaQueryWrapper<OperationLog>()
                .eq(OperationLog::getBranchId, c.getBranchId())
                .ge(OperationLog::getCreatedAt, c.getCreatedAt()));

        c.setChangedCount(changed == null ? 0 : changed.intValue());
        c.setTotalDiff(totalDiff);
        c.setStatus("AUDITED");
        c.setAuditedAt(LocalDateTime.now());
        checkMapper.updateById(c);
    }

    @lombok.Data
    public static class SubmitItem {
        private Long materialId;
        private Integer actualQty;
        private String remark;
    }
}