package com.example.whostolemyfood.menu.application.service;

import com.example.whostolemyfood.ai.application.service.AiLogServiceV1;
import com.example.whostolemyfood.ai.presentation.dto.response.ResGetAiLogDtoV1;
import com.example.whostolemyfood.global.exception.CustomException;
import com.example.whostolemyfood.global.exception.ErrorCode;
import com.example.whostolemyfood.menu.domain.entity.MenuEntity;
import com.example.whostolemyfood.menu.domain.repository.MenuRepository;
import com.example.whostolemyfood.menu.presentation.dto.request.ReqCreateMenuDtoV1;
import com.example.whostolemyfood.menu.presentation.dto.request.ReqUpdateMenuDtoV1;
import com.example.whostolemyfood.menu.presentation.dto.response.ResCreateMenuDtoV1;
import com.example.whostolemyfood.menu.presentation.dto.response.ResGetInActiveMenuDtoV1;
import com.example.whostolemyfood.menu.presentation.dto.response.ResGetMenuDtoV1;
import com.example.whostolemyfood.store.domain.entity.StoreEntity;
import com.example.whostolemyfood.store.domain.repository.StoreRepository;
import com.example.whostolemyfood.user.application.security.AuthUser;
import com.example.whostolemyfood.user.domain.entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuServiceV1 {

    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;
    private final AiLogServiceV1 aiLogServiceV1;

    // Owner Only
    // 메뉴 생성
    @Transactional
    public ResCreateMenuDtoV1 addMenu(UUID storeId, ReqCreateMenuDtoV1 request, AuthUser authUser) {
        // 스토어 존재 확인
        StoreEntity store = storeRepository.findByStoreIdAndIsHiddenFalseAndIsDeletedFalse(storeId).orElseThrow(
                ()-> new CustomException(ErrorCode.STORE_NOT_FOUND));

        // 권한 확인
        validateMenuAccess(store, authUser);

        // 스토어 내 메뉴 이름 중복 확인
        if (menuRepository.existsByStore_StoreIdAndNameAndIsDeletedFalse(store.getStoreId(), request.getName())) {
            throw new CustomException(ErrorCode.MENU_DUPLICATION_NAME);
        }

        ResGetAiLogDtoV1 aiLogDto = resolveDescription(request, authUser);

        MenuEntity menu = MenuEntity.builder()
                .store(store)
                .name(request.getName())
                .price(request.getPrice())
                .description(aiLogDto.description())
                .aiLogId(aiLogDto.aiLogId())
                .build();

        MenuEntity savedMenu = menuRepository.save(menu);

        return ResCreateMenuDtoV1.from(savedMenu);
    }

    // All
    // 메뉴 조회
    @Transactional(readOnly = true)
    public ResGetMenuDtoV1 getMenu(UUID storeId, UUID menuId) {
        StoreEntity store = storeRepository.findByStoreIdAndIsHiddenFalseAndIsDeletedFalse(storeId)
                .orElseThrow(()-> new CustomException(ErrorCode.STORE_NOT_FOUND));
        MenuEntity menu = menuRepository.findByMenuIdAndStore_StoreIdAndIsHiddenFalseAndIsDeletedFalse(menuId,storeId)
                .orElseThrow(()-> new CustomException(ErrorCode.MENU_NOT_FOUND));

        return ResGetMenuDtoV1.from(menu);
    }

    // All
    // 메뉴 목록 조회
    @Transactional(readOnly = true)
    public Page<ResGetMenuDtoV1> getMenus(Pageable pageable) {
        Page<MenuEntity> menus = menuRepository.findAllByIsHiddenFalseAndIsDeletedFalse(pageable);

        return menus.map(ResGetMenuDtoV1::from);
    }

    // Owner, Manager, Master
    // 메뉴 수정
    @Transactional
    public ResGetMenuDtoV1 updateMenu(UUID storeId, UUID menuId, ReqUpdateMenuDtoV1 request, AuthUser authUser) {
        StoreEntity store = storeRepository.findByStoreIdAndIsDeletedFalse(storeId)
                .orElseThrow(()-> new CustomException(ErrorCode.STORE_NOT_FOUND));
        MenuEntity menu = menuRepository.findByMenuIdAndStore_StoreIdAndIsDeletedFalse(menuId,storeId)
                .orElseThrow(()-> new CustomException(ErrorCode.MENU_NOT_FOUND));
        // 유저 권한 확인
        validateMenuAccess(store, authUser);

        // 이름이 변경될때만 체크
        if(!menu.getName().equals(request.getName())) {
            if (menuRepository.existsByStore_StoreIdAndNameAndIsDeletedFalse(storeId, request.getName())) {
                throw new CustomException(ErrorCode.MENU_DUPLICATION_NAME);
            }
        }

        // 설명 변경 처리
        ResGetAiLogDtoV1 aiResult = resolveDescription(request, authUser);

        menu.updateMenu(request, aiResult);

        return ResGetMenuDtoV1.from(menu);
    }

    // Owner, Manager, Master
    // 메뉴 삭제
    @Transactional
    public void deleteMenu(UUID storeId, UUID menuId,AuthUser authUser) {
        StoreEntity store = storeRepository.findByStoreIdAndIsDeletedFalse(storeId)
                .orElseThrow(()-> new CustomException(ErrorCode.STORE_NOT_FOUND));
        MenuEntity menu = menuRepository.findByMenuIdAndStore_StoreIdAndIsDeletedFalse(menuId,storeId)
                .orElseThrow(()-> new CustomException(ErrorCode.MENU_NOT_FOUND));

        validateMenuAccess(store, authUser);
        menu.deleteMenu(authUser.userId());
    }

    // Owner, Manager, Master
    // 메뉴 숨김, 노출
    @Transactional
    public void hiddenMenu(UUID storeId, UUID menuId, AuthUser authUser) {
        StoreEntity store = storeRepository.findByStoreIdAndIsDeletedFalse(storeId)
                .orElseThrow(()-> new CustomException(ErrorCode.STORE_NOT_FOUND));
        // isHiddenFalse가 없어야지 숨김 상태를 찾아 노출로 변경가능
        MenuEntity menu = menuRepository.findByMenuIdAndStore_StoreIdAndIsDeletedFalse(menuId,storeId)
                .orElseThrow(()-> new CustomException(ErrorCode.MENU_NOT_FOUND));

        validateMenuAccess(store, authUser);
        menu.toggleIsHidden();
    }

    @Transactional(readOnly = true)
    public Page<ResGetInActiveMenuDtoV1> getInActiveMenus(UUID storeId, AuthUser authUser, Pageable pageable) {
        StoreEntity store = storeRepository.findByStoreIdAndIsDeletedFalse(storeId)
                .orElseThrow(()-> new CustomException(ErrorCode.STORE_NOT_FOUND));

        validateMenuAccess(store, authUser);

        Page<MenuEntity> menus =  menuRepository.findAllInactiveMenusByStoreId(storeId, pageable);
        return menus.map(ResGetInActiveMenuDtoV1::from);
    }

    // 권한 확인
    private void validateMenuAccess(StoreEntity store, AuthUser authUser) {
        if (authUser.role() == UserRole.CUSTOMER) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        if (authUser.role() == UserRole.OWNER && !store.getUser().getId().equals(authUser.userId())) {
            throw new CustomException(ErrorCode.STORE_NOT_OWNER);
        }
    }

    // description 예외처리
    private ResGetAiLogDtoV1 resolveDescription(ReqCreateMenuDtoV1 request, AuthUser authUser) {
        return resolveDescriptionLogic(request.getDescription(),request.getAiDescription(), request.getAiPrompt(), authUser);
    }

    private ResGetAiLogDtoV1 resolveDescription(ReqUpdateMenuDtoV1 request, AuthUser authUser) {
        return resolveDescriptionLogic(request.getDescription(),request.getAiDescription(),request.getAiPrompt(), authUser);
    }

    private ResGetAiLogDtoV1 resolveDescriptionLogic(String description, Boolean aiDescription, String aiPrompt, AuthUser authUser) {
        boolean useAi = Boolean.TRUE.equals(aiDescription);
        boolean hasDescription = hasText(description);
        boolean hasAiPrompt = hasText(aiPrompt);

        // 1. 설명 없음 +  AI false + 프롬프트 없음 >> null
        if (!hasDescription && !useAi && !hasAiPrompt) {
            return new ResGetAiLogDtoV1(null,null);
        }

        // 2. AI false + 프롬프트 있음 >> 예외처리
        if (!useAi && hasAiPrompt) {
            throw new CustomException(ErrorCode.MENU_AI_PROMPT_NOT_ALLOWED);
        }

        // 3. 설명 있고 AI true >> 예외
        if (hasDescription && useAi) {
            throw new CustomException(ErrorCode.MENU_DESCRIPTION_DUPLICATE);
        }

        // 4. AI true 면 AI서비스로 이동하여 prompt 검증후 description 생성
        if (useAi) {
            return aiLogServiceV1.generateMenuDescription(authUser.userId(), aiPrompt);
        }

        // 5. AI false + 설명만 있으면 설명 저장
        return new ResGetAiLogDtoV1(description,null);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }


}
