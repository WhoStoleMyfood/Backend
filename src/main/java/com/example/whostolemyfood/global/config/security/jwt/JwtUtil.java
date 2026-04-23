package com.example.whostolemyfood.global.config.security.jwt;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.example.whostolemyfood.user.domain.entity.UserRole;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT(JSON Web Token) 생성, 검증 및 정보 추출을 담당하는 유틸리티 클래스입니다.
 * * - Access Token 및 Refresh Token 발급
 * - 토큰의 유효성(만료 여부, 서명 등) 검증
 * - 토큰 내부의 Payload(Subject, Role 등) 추출
 * - UUID 기반의 사용자 식별 체계 지원
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties properties;
    private Key key;

    @PostConstruct
    public void init() {
        // secret string을 Key 객체로 변환
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }


    //Access Token 생성

    public String createToken(UUID userId, UserRole role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + getAccessTokenExpireMillis());

        return Jwts.builder()
                .subject(userId.toString()) // UUID를 String으로 저장
                .claim("role", role.name())  // 권한 정보 추가
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    //Refresh Token 생성
    public String generateRefreshToken(UUID userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + getRefreshTokenExpireMillis());

        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    //토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith((javax.crypto.SecretKey) key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    public String extractSubject(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    private long getAccessTokenExpireMillis() {
        return 1000L * 60 * properties.getToken().getAccess().getMinute();
    }

    private long getRefreshTokenExpireMillis() {
        return 1000L * 60 * properties.getToken().getRefresh().getMinute();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}