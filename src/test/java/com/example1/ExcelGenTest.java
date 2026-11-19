package com.example1;

import com.alibaba.excel.EasyExcel;
import com.example1.entity.MaterialExcelVO;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * 生成批量导入用的测试 Excel（用来测出"N 条耗时 M 秒"）
 * 跑之前把 PATH 改成你自己机器上的路径
 */
public class ExcelGenTest {

    private static final String PATH = "C:\\Users\\31280\\Downloads\\materials-test.xlsx";
    private static final int COUNT = 200;

    @Test
    void gen() {
        List<MaterialExcelVO> list = new ArrayList<>(COUNT);
        for (int i = 1; i <= COUNT; i++) {
            MaterialExcelVO v = new MaterialExcelVO();
            v.setName("测试物资" + i);
            v.setCategoryId((long) (i % 5 + 1));
            v.setSpec("规格" + i);
            v.setUnit("个");
            v.setSafeStock(i % 20);
            list.add(v);
        }
        EasyExcel.write(PATH, MaterialExcelVO.class).sheet("物资").doWrite(list);
        System.out.println("已生成 " + COUNT + " 条到 " + PATH);
    }
}
