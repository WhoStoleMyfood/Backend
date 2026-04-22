package com.example.whostolemyfood.global.config.security.jwt;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties properties;
    private Key key;

    @PostConstruct
    public void init() {
        // properties에서 가져온 secret string을 Key 객체로 변환
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public boolean hasCloverInfo(String token) {
        Claims claims = parseClaims(token);
        Object cloverId = claims.get("cloverId");
        Object cloverName = claims.get("cloverName");

        return cloverId instanceof Number && cloverName instanceof String;
    }

    public String createMiniToken(String userId, String role) {
        Date now = new Date();
        long duration = role.equals("Master")
                ? Duration.ofHours(12).toMillis()
                : Duration.ofMinutes(15).toMillis();

        Date expiry = new Date(now.getTime() + duration);

        return Jwts.builder()
                .subject(userId)         // setSubject -> subject
                .claim("role", role)
                .issuedAt(now)           // setIssuedAt -> issuedAt
                .expiration(expiry)      // setExpiration -> expiration
                .signWith(key)           // 알고리즘(HS512 등)은 key를 보고 자동 결정됨
                .compact();
    }

    public String generateAccessToken(String userId, String role, Long cloverId, String cloverName) {
        return Jwts.builder()
                .subject(userId)
                .claim("role", role)
                .claim("cloverId", cloverId)
                .claim("cloverName", cloverName)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + getAccessTokenExpireMillis()))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(String userId) {
        return Jwts.builder()
                .subject(userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + getRefreshTokenExpireMillis()))
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            // parserBuilder().setSigningKey() -> parser().verifyWith()
            Jwts.parser()
                    .verifyWith((javax.crypto.SecretKey) key)
                    .build()
                    .parseSignedClaims(token); // parseClaimsJws -> parseSignedClaims
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String extractSubject(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public Long extractCloverId(String token) {
        return parseClaims(token).get("cloverId", Long.class);
    }

    public String extractCloverName(String token) {
        return parseClaims(token).get("cloverName", String.class);
    }

    private long getAccessTokenExpireMillis() {
        return 1000L * 60 * properties.getToken().getAccess().getMinute();
    }

    private long getRefreshTokenExpireMillis() {
        return 1000L * 60 * properties.getToken().getRefresh().getMinute();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload(); // getBody() -> getPayload()
    }
}