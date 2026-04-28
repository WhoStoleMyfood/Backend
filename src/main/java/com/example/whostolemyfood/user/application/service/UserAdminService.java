package com.example.whostolemyfood.user.application.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.whostolemyfood.user.presentation.dto.request.ReqManagerCreateDtoV1;
import com.example.whostolemyfood.user.presentation.dto.response.ResGetUserByIdDtoV1;

public interface UserAdminService {

    //[MASTER 전용] 매니저 생성
    ResGetUserByIdDtoV1 registerManager(ReqManagerCreateDtoV1 requestDto);

    //[MASTER] 매니저 삭제 (회원 탈퇴 처리)
    void deleteManager(UUID userId);

    //[MASTER/MANAGER] 단일 사용자 상세 조회
    ResGetUserByIdDtoV1 getUserById(UUID userId);

    //[MASTER] 전체 사용자 목록 조회 (본인 제외 페이징)
    Page<ResGetUserByIdDtoV1> findAllUsers(Pageable validatedPageable, UUID myId);

}
