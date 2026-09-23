package com.example1.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JJWT 0.12.x 写法：signWith(key) / verifyWith(key).parseSignedClaims
 * 0.11.5 依赖 JDK 9 起就移除的 javax.xml.bind，在 JDK 17 下会报
 * NoClassDefFoundError: javax/xml/bind/DatatypeConverter
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expireMs;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expire-minutes:120}") long minutes) {
        // HS256 要求密钥至少 32 字节
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireMs = minutes * 60 * 1000;
    }

    public String generate(LoginUser user) {
        return Jwts.builder()
                .subject(String.valueOf(user.getUserId()))
                .claim("username", user.getUsername())
                .claim("role", user.getRole())
                .claim("branchId", user.getBranchId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expireMs))
                .signWith(key)
                .compact();
    }

    /** 解析失败（过期/篡改）返回 null，由上层决定 401 */
    public Claims parse(String token) {
        try {
            return Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token).getPayload();
        } catch (Exception e) {
            return null;
        }
    }
}
