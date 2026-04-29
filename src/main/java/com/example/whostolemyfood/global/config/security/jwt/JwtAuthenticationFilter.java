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

import com.example.whostolemyfood.user.application.security.AuthUser; // 패키지 경로 확인!
import com.example.whostolemyfood.user.domain.entity.UserRole;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate; // 주입 추가

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            try {
                String userIdString = jwtUtil.extractSubject(token);


                String redisKey = "logout:" + userIdString;
                if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
                    log.warn("이미 로그아웃된 토큰입니다: userId={}", userIdString);
                    filterChain.doFilter(request, response);
                    return;
                }

                String roleName = jwtUtil.extractRole(token);
                UUID userId = UUID.fromString(userIdString);
                UserRole role = UserRole.valueOf(roleName);

                log.info("인증 성공: userId={}, role={}", userId, role);

                // 권한 객체 생성
                List<SimpleGrantedAuthority> authorities =
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));

                // Principal에 UUID 대신 'AuthUser' 신분증 객체를 생성해서 넣기
                // 이메일 정보가 토큰에 없다면 우선 임시값("N/A")을 넣거나, JwtUtil을 고쳐서 이메일도 추출하세요!
                AuthUser authUser = new AuthUser(userId, "N/A", role);

                // 이제 첫 번째 인자로 UUID가 아닌 authUser(신분증)가 들어감
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(authUser, null, authorities);

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // SecurityContextHolder에 인증 정보 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                log.error("Security Context 인증 설정 실패: {}", e.getMessage());
            }
        }
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