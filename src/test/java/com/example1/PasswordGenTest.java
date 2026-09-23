package com.example1;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 临时工具类：生成真实 BCrypt 密文，跑一次拿到结果后更新 t_user.password，之后可删。
 */
public class PasswordGenTest {

    @Test
    void gen() {
        String hash = new BCryptPasswordEncoder().encode("123456");
        System.out.println("BCrypt hash = " + hash);
    }
}
