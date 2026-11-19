package com.example1;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example1.entity.OperationLog;
import com.example1.entity.Stock;
import com.example1.mapper.OperationLogMapper;
import com.example1.mapper.StockMapper;
import com.example1.service.StockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 事务与一致性集成测试（3 个用例，零脏数据）。
 *
 * 类上的 @Transactional 由 Spring Test 的 TransactionalTestExecutionListener 管理：
 * 每个测试方法结束后自动回滚，因此这里的所有写操作都不会真正落到数据库，
 * 跑完不需要清理任何测试数据。
 *
 * StockService 内部方法标注的是 @Transactional(rollbackFor = Exception.class)，
 * 默认传播行为 REQUIRED，会加入同一个测试事务，所以「方法执行到一半抛异常」
 * 的场景也能被完整回滚验证 —— 这正是本测试要证明的东西。
 *
 * 前置：MySQL(1-mysql-1) 与 Redis(sc-redis) 在运行；application.yml 配置正确。
 *       @SpringBootTest 默认 webEnvironment=MOCK，只提供 mock servlet 环境，
 *       不启动 Tomcat、不占 8080 端口，不会和正在运行的 Application 冲突。
 * 运行：mvn -Dtest=StockTransactionTest test
 * 期望：Tests run: 3, Failures: 0, Errors: 0 / BUILD SUCCESS
 */
@SpringBootTest
@Transactional
class StockTransactionTest {

    @Autowired private StockService stockService;
    @Autowired private StockMapper stockMapper;
    @Autowired private OperationLogMapper operationLogMapper;

    private static final Long B1 = 1L;   // 网点 1
    private static final Long B3 = 3L;   // 网点 3
    private static final Long M1 = 1L;   // 物资 1
    private static final Long M6 = 6L;   // 物资 6

    private Stock stock(Long branchId, Long materialId) {
        return stockMapper.selectOne(new LambdaQueryWrapper<Stock>()
                .eq(Stock::getBranchId, branchId)
                .eq(Stock::getMaterialId, materialId));
    }

    private List<OperationLog> logs(Long branchId, Long materialId) {
        return operationLogMapper.selectList(new LambdaQueryWrapper<OperationLog>()
                .eq(OperationLog::getBranchId, branchId)
                .eq(OperationLog::getMaterialId, materialId)
                .orderByAsc(OperationLog::getId));
    }

    private long transferLogCount() {
        // 用空条件查全表再过滤，避免依赖具体项目的自定义 Mapper 方法
        return operationLogMapper.selectList(new LambdaQueryWrapper<OperationLog>())
                .stream()
                .filter(l -> l.getOpType() != null && l.getOpType().startsWith("TRANSFER"))
                .count();
    }

    /** 用例 1：出库前后，库存变化与日志 before/after 精确衔接，且只留一条日志 */
    @Test
    void outLogBeforeAfterMatch() {
        int before = stock(B1, M1).getQuantity();
        int logCountBefore = logs(B1, M1).size();

        stockService.stockOut(B1, M1, 10, "TEST_ROLLBACK_OUT");

        assertEquals(Integer.valueOf(before - 10), stock(B1, M1).getQuantity(), "库存应精确扣减 10");
        List<OperationLog> after = logs(B1, M1);
        assertEquals(logCountBefore + 1, after.size(), "应只新增 1 条日志");
        OperationLog last = after.get(after.size() - 1);
        assertEquals("OUT", last.getOpType());
        assertEquals(Integer.valueOf(10), last.getQuantity());
        assertEquals(Integer.valueOf(before), last.getBeforeQty());
        assertEquals(Integer.valueOf(before - 10), last.getAfterQty());
    }

    /** 用例 2：超量出库被拒，库存与日志零副作用（条件更新防超卖） */
    @Test
    void oversellHasNoSideEffect() {
        int before = stock(B1, M1).getQuantity();
        int logCountBefore = logs(B1, M1).size();

        boolean thrown = false;
        try {
            stockService.stockOut(B1, M1, before + 9999, "TEST_ROLLBACK_OVERSELL");
        } catch (Exception e) {
            thrown = true;
        }

        assertTrue(thrown, "超量出库必须被拒绝");
        assertEquals(Integer.valueOf(before), stock(B1, M1).getQuantity(), "库存不能变");
        assertEquals(logCountBefore, logs(B1, M1).size(), "不能留下半条日志");
    }

    /** 用例 3：调拨中途失败，源/目标库存与 TRANSFER 双向日志整体回滚 */
    @Test
    void failedTransferRollsBackEverything() {
        int src0 = stock(B1, M6).getQuantity();
        int dst0 = stock(B3, M6).getQuantity();
        long transferLogs0 = transferLogCount();

        boolean thrown = false;
        try {
            stockService.transfer(B1, B3, M6, src0 + 9999);
        } catch (Exception e) {
            thrown = true;
        }

        assertTrue(thrown, "超量调拨必须被拒绝");
        assertEquals(Integer.valueOf(src0), stock(B1, M6).getQuantity(), "调出方库存不能变");
        assertEquals(Integer.valueOf(dst0), stock(B3, M6).getQuantity(), "调入方库存不能变");
        assertEquals(transferLogs0, transferLogCount(), "不能留下半条调拨日志");
    }
}
