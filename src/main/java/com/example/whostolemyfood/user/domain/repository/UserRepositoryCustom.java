package com.example.whostolemyfood.user.domain.repository;

import com.example.whostolemyfood.user.domain.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface UserRepositoryCustom {
    Page<UserEntity> findAllExceptMe(Pageable pageable, UUID myId);
}