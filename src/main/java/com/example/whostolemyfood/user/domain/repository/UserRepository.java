package com.example.whostolemyfood.user.domain.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.whostolemyfood.user.domain.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

}