package com.example1.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example1.entity.Material;
import com.example1.mapper.MaterialMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialMapper materialMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String KEY_LIST = "pmls:material:list";

    /** 走缓存的物资列表：穿透用空值缓存，雪崩用随机过期 */
    @SuppressWarnings("unchecked")
    public List<Material> listCached() {
        List<Material> cache = (List<Material>) redisTemplate.opsForValue().get(KEY_LIST);
        if (cache != null) {
            return cache;
        }

        List<Material> db = materialMapper.selectList(new LambdaQueryWrapper<Material>()
                .eq(Material::getDeleted, 0)
                .orderByAsc(Material::getId));

        if (db.isEmpty()) {
            redisTemplate.opsForValue().set(KEY_LIST, new ArrayList<>(),
                    60 + ThreadLocalRandom.current().nextInt(30), TimeUnit.SECONDS);
        } else {
            redisTemplate.opsForValue().set(KEY_LIST, db,
                    10 + ThreadLocalRandom.current().nextInt(5), TimeUnit.MINUTES);
        }
        return db;
    }

    /** Cache Aside：改库后删缓存，不更新缓存 */
    public void evictListCache() {
        redisTemplate.delete(KEY_LIST);
    }
    public List<Material> listDirect() {
        return materialMapper.selectList(new LambdaQueryWrapper<Material>()
                .eq(Material::getDeleted, 0)
                .orderByAsc(Material::getId));
    }
    /** 批量落库（走 foreach 单条多值 INSERT） */
    public void saveBatch(List<Material> list) {
        if (list == null || list.isEmpty()) return;
        materialMapper.insertBatch(list);
    }
}
