package com.example.whostolemyfood.user.domain.repository;

import com.example.whostolemyfood.user.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
}