package com.example.whostolemyfood.global.config.security;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.example.whostolemyfood.global.config.security.jwt.JwtAuthenticationFilter;
import com.example.whostolemyfood.global.exception.ErrorCode;
import com.example.whostolemyfood.global.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper; // JSON 변환을 위해 주입

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/signup",
                                "/api/v1/auth/login",
                                "/api/v1/auth/reissue",
                                "/v3/api-docs/**",
                                "/swagger-ui/**"
                        ).permitAll()
                        .requestMatchers(
                                "/api/v1/auth/logout",
                                "/api/v1/auth/signout",
                                "/api/v1/user/me",
                                "/api/v1/users/**"
                        ).authenticated()
                        .anyRequest().authenticated()
                )
                //시큐리티 필터 단계 예외 처리 - 필터에 걸러져 서비스 단에서 오류가 처리안되는 사유
                .exceptionHandling(exception -> exception
                        // 1. 인가 실패 (401 Forbidden): 로그인 했으나 권한이 부족할 때 (예: CUSTOMER가 ADMIN API 호출)
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            sendErrorResponse(response, ErrorCode.ACCESS_DENIED);
                        })
                        // 2. 인증 실패 (40 Unauthorized): 토큰이 없거나 만료되어 누군지 모를 때
                        .authenticationEntryPoint((request, response, authException) -> {
                            sendErrorResponse(response, ErrorCode.ACCESS_DENIED);
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    //Filter 레벨 전용 에러 응답기
     /**
      *  @RestControllerAdvice는 Controller 이후 단계만 담당
      *  그 앞단인 Filter에서 발생하는 보안 에러는 여기서 직접 JSON 응답을 생성하여 클라이언트로 보냄
     */
    private void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws java.io.IOException {
        // 1. ErrorCode에 설정된 HttpStatus(401 또는 403)를 그대로 응답 상태 코드로 설정
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 2. ErrorResponse 객체 생성
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .errors(null) // 필터 에러는 FieldError가 없으므로 null 또는 빈 리스트
                .build();

        // 3. JSON으로 변환해서 출력
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}