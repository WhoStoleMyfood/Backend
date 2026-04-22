package com.example.whostolemyfood.global.config.security.jwt;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        // 1. 토큰이 없는 경우: 그냥 다음 필터로 넘김
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. 토큰이 유효하지 않은 경우: 로그 남기고 다음 필터로 넘김
        if (!jwtUtil.validateToken(token)) {
            log.warn("유효하지 않은 토큰입니다.");
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 토큰이 유효한 경우: 인증 정보 설정
        try {
            String subject = jwtUtil.extractSubject(token);
            String role = jwtUtil.extractRole(token);

            // subject(userId)가 숫자인지 확인 후 처리
            Long userId = Long.parseLong(subject);

            // 미니 토큰 구간 (클로버 정보가 없는 경우)
            if (!jwtUtil.hasCloverInfo(token)) {
                // MiniAuthUser 클래스가 적절히 정의되어 있어야 합니다.
                // 여기서는 예시로 일반적인 처리를 따릅니다.
                log.info("미니 토큰 인증 진행 중: userId={}", userId);
                setAuthentication(request, new MiniAuthUser(userId, role));

                // 풀 토큰 구간 (클로버 정보가 있는 경우)
            } else {
                Long cloverId = jwtUtil.extractCloverId(token);
                String cloverName = jwtUtil.extractCloverName(token);

                log.info("풀 토큰 인증 진행 중: userId={}, cloverId={}", userId, cloverId);
                setAuthentication(request, new AuthUser(userId, role, cloverId, cloverName));
            }
        } catch (Exception e) {
            log.error("Security Context에 인증 정보를 설정할 수 없습니다: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    // 인증 객체를 생성하여 SecurityContext에 저장하는 공통 메서드
    private void setAuthentication(HttpServletRequest request, Object authUser) {
        // authUser가 Authorities를 가지고 있다고 가정 (UserDetails 구현체 등)
        // 실제 프로젝트의 MiniAuthUser, AuthUser 구조에 맞게 캐스팅이 필요할 수 있습니다.
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(authUser, null, null); // 세 번째 인자는 권한 목록

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}