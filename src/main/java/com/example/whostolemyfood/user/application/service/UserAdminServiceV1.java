package com.example.whostolemyfood.user.application.service;

import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.whostolemyfood.auth.application.service.AuthService;
import com.example.whostolemyfood.auth.presentation.dto.request.ReqSignUpDtoV1;
import com.example.whostolemyfood.auth.presentation.dto.response.ResSignUpDtoV1;
import com.example.whostolemyfood.global.exception.CustomException;
import com.example.whostolemyfood.global.exception.ErrorCode;
import com.example.whostolemyfood.user.domain.entity.UserEntity;
import com.example.whostolemyfood.user.domain.entity.UserRole;
import com.example.whostolemyfood.user.domain.repository.UserRepository;
import com.example.whostolemyfood.user.presentation.dto.request.ReqManagerCreateDtoV1;
import com.example.whostolemyfood.user.presentation.dto.response.ResGetUserByIdDtoV1;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAdminServiceV1 implements UserAdminService {

    private final UserRepository userRepository;
    private final AuthService authService;

    // [MASTER 전용] 매니저 생성
    @Override
    @Transactional
    @CacheEvict(cacheNames = "userListCache", allEntries = true)
    public ResGetUserByIdDtoV1 registerManager(ReqManagerCreateDtoV1 requestDto) {
        // 1. 매니저 생성용 회원가입 DTO로 변환
        ReqSignUpDtoV1 signUpDto = ReqSignUpDtoV1.builder()
                .email(requestDto.getEmail())
                .password(requestDto.getPassword())
                .userName(requestDto.getName())
                .userRole(UserRole.MANAGER)
                .adminToken(requestDto.getAdminToken())
                .build();

        // 2. AuthService.signup 재사용 (여기서 토큰 검증 로직이 실행됨)
        ResSignUpDtoV1 res = authService.signup(signUpDto);

        // 3. 반환 타입에 맞춰 변환해서 응답
        UserEntity manager = userRepository.findByUserEmail(res.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return new ResGetUserByIdDtoV1(manager);
    }

    // [MASTER] 매니저 삭제 로직
    @Override
    @Transactional
    @CacheEvict(cacheNames = {"userCache", "userListCache"}, allEntries = true)
    public void deleteManager(UUID userId) {
        // AuthService의 signout 기능을 재사용!
        authService.signout(userId);
    }

    // [MASTER/MANAGER] 단일 사용자 상세 조회
    @Override
    @Cacheable(cacheNames = "userCache", key = "#id")// 조회 시 캐시 적용
    public ResGetUserByIdDtoV1 getUserById(UUID id) {
        UserEntity user = findActiveUser(id);
        return new ResGetUserByIdDtoV1(user.getUserEmail(), user.getUserName(), user.getUserRole());
    }

    // [MASTER] 전체 사용자 목록 조회 (본인 제외 페이징)

    @Override
    @Transactional
    @Cacheable(cacheNames = "userListCache", key = "#validatedPageable.pageNumber + '_' + #myId")
    public Page<ResGetUserByIdDtoV1> findAllUsers(Pageable validatedPageable, UUID myId) { // 👈 UUID myId 파라미터 추가!

        Page<UserEntity> userPage = userRepository.findAllExceptMe(validatedPageable, myId);
        return userPage.map(ResGetUserByIdDtoV1::new);
    }

//    // [MASTER] 사용자 상태 변경 (ACTIVE, DELETED 등)
//    @Transactional
//    public void updateUserStatus(UUID userId, @Valid ReqUpdateUserStatusDtoV1 requestDto) {
//        UserEntity user = userRepository.findById(userId)
//                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
//
//        // 엔티티 내부의 비즈니스 메서드 호출
//        user.updateStatus(requestDto.getStatus());
//    }

    private UserEntity findActiveUser(UUID id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

}
