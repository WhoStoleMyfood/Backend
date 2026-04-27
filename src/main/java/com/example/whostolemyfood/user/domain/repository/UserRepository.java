package com.example.whostolemyfood.user.domain.repository;

import java.util.Optional;
import java.util.UUID;

import jakarta.security.auth.message.AuthException;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.example.whostolemyfood.user.domain.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, UUID>, UserRepositoryCustom {

    Optional<UserEntity> findByUserEmail(String email);

    boolean existsByUserEmail(String email);

    // 소프트 딜리트 대응: ID 타입을 UUID로 변경
    Optional<UserEntity> findByIdAndIsDeletedFalse(UUID id);

    // 기본 제공 메서드 오버라이드 (선택 사항)
    @Override
    @Query("select u from UserEntity u where u.id = :id and u.isDeleted = false")
    Optional<UserEntity> findById(UUID id);

    // 예외 처리를 포함한 기본 메서드 (Java 8+ default 활용)
    default UserEntity findByIdOrElseThrow(UUID id) {
        return findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));
    }
}