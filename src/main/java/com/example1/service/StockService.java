package com.example1.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example1.common.BusinessException;
import com.example1.entity.OperationLog;
import com.example1.entity.Stock;
import com.example1.entity.Transfer;
import com.example1.mapper.OperationLogMapper;
import com.example1.mapper.StockMapper;
import com.example1.mapper.TransferMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import org.springframework.dao.DuplicateKeyException;
@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockMapper stockMapper;
    private final OperationLogMapper operationLogMapper;
    private final TransferMapper transferMapper;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    // ==================== 入库 ====================
    @Transactional(rollbackFor = Exception.class)
    public void stockIn(Long branchId, Long materialId, int quantity, String remark) {
        if (quantity <= 0) {
            throw new BusinessException("入库数量必须大于 0");
        }
        upsertStock(branchId, materialId, quantity);

        Stock after = selectStock(branchId, materialId);
        // 入库后 = 原值 + q，所以变动前 = 现值 - q
        writeLog(branchId, materialId, "IN", quantity,
                after.getQuantity() - quantity, after.getQuantity(), null, null, remark);
    }

    // ==================== 出库 ====================
    @Transactional(rollbackFor = Exception.class)
    public void stockOut(Long branchId, Long materialId, int quantity, String remark) {
        if (quantity <= 0) {
            throw new BusinessException("出库数量必须大于 0");
        }

        int rows = stockMapper.deductIfEnough(branchId, materialId, quantity);
        if (rows == 0) {
            // 再查一次只为给用户准确提示：到底是不够，还是被并发抢了
            Stock cur = stockMapper.selectOne(new LambdaQueryWrapper<Stock>()
                    .eq(Stock::getBranchId, branchId)
                    .eq(Stock::getMaterialId, materialId));
            if (cur == null || cur.getQuantity() < quantity) {
                throw new BusinessException("库存不足，当前库存：" + (cur == null ? 0 : cur.getQuantity()));
            }
            throw new BusinessException("库存并发冲突，请重试");
        }

        Stock after = selectStock(branchId, materialId);
        // 出库后 = 原值 - q，所以变动前 = 现值 + q
        writeLog(branchId, materialId, "OUT", quantity,
                after.getQuantity() + quantity, after.getQuantity(), null, null, remark);
    }

    // ==================== 调拨 ====================
    @Transactional(rollbackFor = Exception.class)
    public Long transfer(Long fromBranchId, Long toBranchId, Long materialId, int quantity) {
        if (quantity <= 0) {
            throw new BusinessException("调拨数量必须大于 0");
        }
        if (Objects.equals(fromBranchId, toBranchId)) {
            throw new BusinessException("源网点与目标网点不能相同");
        }

        Transfer t = new Transfer();
        t.setTransferNo("DB" + LocalDateTime.now().format(FMT));
        t.setFromBranchId(fromBranchId);
        t.setToBranchId(toBranchId);
        t.setMaterialId(materialId);
        t.setQuantity(quantity);
        t.setStatus("DONE");
        t.setRemark("网点调拨");
        transferMapper.insert(t);

        outInternal(fromBranchId, materialId, quantity,
                "调出至网点#" + toBranchId, t.getId(), "TRANSFER");
        inInternal(toBranchId, materialId, quantity,
                "由网点#" + fromBranchId + "调入", t.getId(), "TRANSFER");

        return t.getId();
    }

    // ==================== 内部方法 ====================
    private void outInternal(Long branchId, Long materialId, int quantity,
                             String remark, Long relatedId, String relatedType) {
        int rows = stockMapper.deductIfEnough(branchId, materialId, quantity);
        if (rows == 0) {
            throw new BusinessException("调出失败：网点#" + branchId + " 库存不足或并发冲突");
        }
        Stock after = selectStock(branchId, materialId);
        writeLog(branchId, materialId, "TRANSFER_OUT", quantity,
                after.getQuantity() + quantity, after.getQuantity(), relatedId, relatedType, remark);
    }

    private void inInternal(Long branchId, Long materialId, int quantity,
                            String remark, Long relatedId, String relatedType) {
        upsertStock(branchId, materialId, quantity);
        Stock after = selectStock(branchId, materialId);
        writeLog(branchId, materialId, "TRANSFER_IN", quantity,
                after.getQuantity() - quantity, after.getQuantity(), relatedId, relatedType, remark);
    }
    private void upsertStock(Long branchId, Long materialId, int quantity) {
        int rows = stockMapper.addQuantity(branchId, materialId, quantity);
        if (rows == 0) {
            // 库存行还不存在，插入一条
            Stock s = new Stock();
            s.setBranchId(branchId);
            s.setMaterialId(materialId);
            s.setQuantity(quantity);
            s.setVersion(0);
            try {
                stockMapper.insert(s);
            } catch (DuplicateKeyException e) {
                // 并发下已被别的线程插入，改为累加
                stockMapper.addQuantity(branchId, materialId, quantity);
            }
        }
    }
    private Stock selectStock(Long branchId, Long materialId) {
        Stock s = stockMapper.selectOne(new LambdaQueryWrapper<Stock>()
                .eq(Stock::getBranchId, branchId)
                .eq(Stock::getMaterialId, materialId));
        if (s == null) {
            throw new BusinessException("库存记录不存在");
        }
        return s;
    }

    private void writeLog(Long branchId, Long materialId, String opType, int quantity,
                          int beforeQty, int afterQty, Long relatedId, String relatedType, String remark) {
        OperationLog log = new OperationLog();
        log.setBranchId(branchId);
        log.setMaterialId(materialId);
        log.setOpType(opType);
        log.setQuantity(quantity);
        log.setBeforeQty(beforeQty);
        log.setAfterQty(afterQty);
        log.setRelatedId(relatedId);
        log.setRelatedType(relatedType);
        log.setRemark(remark);
        operationLogMapper.insert(log);
    }
}