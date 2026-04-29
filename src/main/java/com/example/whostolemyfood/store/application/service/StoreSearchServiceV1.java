package com.example.whostolemyfood.store.application.service;

import com.example.whostolemyfood.global.exception.CustomException;
import com.example.whostolemyfood.global.exception.ErrorCode;
import com.example.whostolemyfood.global.response.PageResponse;
import com.example.whostolemyfood.store.domain.repository.StoreRepositoryCustom;
import com.example.whostolemyfood.store.presentation.dto.request.StoreSearchConditionV1;
import com.example.whostolemyfood.store.presentation.dto.response.StoreSearchResponseDtoV1;
import com.example.whostolemyfood.user.domain.entity.UserEntity;
import com.example.whostolemyfood.user.domain.entity.UserRole;
import com.example.whostolemyfood.user.domain.repository.UserRepository;
import com.example.whostolemyfood.user.domain.repository.UserRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreSearchServiceV1 {

    private final StoreRepositoryCustom storeRepositoryCustom;
    private final UserRepository userRepository;

    public PageResponse<StoreSearchResponseDtoV1> search(
            StoreSearchConditionV1 condition,
            Pageable pageable,
            UUID userid,
            String userRole
    ) {

        UserEntity loginUser = validateActiveUserAndRole(userid,userRole);

        // 1. 리포지토리에서 Page 객체 조회
        Page<StoreSearchResponseDtoV1> pageResult = storeRepositoryCustom.searchStore(condition, pageable);

        // 2. 공통 응답 객체인 PageResponse로 변환하여 반환
        return new PageResponse<>(pageResult);
    }

    private UserEntity validateActiveUserAndRole(UUID loginUserId, String tokenRole) {
        UserEntity user = userRepository.findById(loginUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        String Role = user.getUserRole().name();
        if (!Role.equals(tokenRole)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        return user;
    }



}