package com.wjh.interviewbacked.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具：生成 / 解析 / 校验令牌
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    /** 有效期（秒） */
    @Value("${jwt.expiration:604800}")
    private long expiration;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** 生成令牌，载荷存放 userId 与 username */
    public String generateToken(String userId, String username) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + expiration * 1000L);
        return Jwts.builder()
                .subject(userId)
                .claim("username", username)
                .issuedAt(now)
                .expiration(expire)
                .signWith(key())
                .compact();
    }

    /** 解析令牌，返回 Claims */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 从令牌中取出 userId */
    public String getUserId(String token) {
        return parse(token).getSubject();
    }

    /** 校验令牌是否有效（未过期、签名正确） */
    public boolean validate(String token) {
        try {
            parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
