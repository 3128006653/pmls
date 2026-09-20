package com.example1;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example1.mapper")   // ← 必须有，少了这行会报找不到 MaterialMapper
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}