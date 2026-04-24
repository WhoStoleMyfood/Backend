package com.example.whostolemyfood.user.application.security;

import com.example.whostolemyfood.user.domain.entity.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * 전역적으로 사용되는 인증된 사용자 전용 객체
 * SecurityContextHolder에 담겨 애플리케이션 전역에서 신분증 역할로 쓸예정 - 엔티티 아님
 */
public record AuthUser(
        UUID userId,
        String email,
        UserRole role
) implements UserDetails {

    // 권한 설정: Spring Security의 관례인 "ROLE_" 접두사를 사용합니다.
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    // JWT 기반 인증이므로 비밀번호 필드는 비워둡니다. (이미 필터에서 검증됨)
    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return email;
    }

    public UUID getUserId() { return userId; }

    // 계정 상태 정보 (커스텀 로직이 없다면 기본적으로 true 반환)
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}