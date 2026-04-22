package com.example.whostolemyfood.global.config.security.jwt;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

        // 1. 요청 헤더에서 Bearer 토큰 추출
        String token = resolveToken(request);

        // 2. 토큰이 유효한지 검증
        if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            try {
                // 3. 토큰에서 사용자 정보 추출 (UUID와 Role)
                String userIdString = jwtUtil.extractSubject(token);
                String role = jwtUtil.extractRole(token); // JwtUtil에 해당 메서드가 있어야 함
                UUID userId = UUID.fromString(userIdString);

                log.info("인증 성공: userId={}, role={}", userId, role);

                // 4. 시큐리티 전용 권한 객체 생성 (ROLE_ 접두사 관례 준수)
                List<SimpleGrantedAuthority> authorities =
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));

                // 5. 인증 객체 생성 (Principal에 userId를 직접 넣거나 전용 DTO를 생성해서 넣음)
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 6. SecurityContextHolder에 인증 정보 저장 (이후 컨트롤러에서 꺼내 쓸 수 있음)
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                log.error("Security Context 인증 설정 실패: {}", e.getMessage());
            }
        }

        // 7. 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}