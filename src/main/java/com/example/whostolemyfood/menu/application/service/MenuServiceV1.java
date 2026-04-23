package com.example.whostolemyfood.menu.application.service;

import com.example.whostolemyfood.menu.domain.entity.MenuEntity;
import com.example.whostolemyfood.menu.domain.repository.MenuRepository;
import com.example.whostolemyfood.menu.presentation.dto.request.ReqCreateMenuDtoV1;
import com.example.whostolemyfood.menu.presentation.dto.request.ReqUpdateMenuDtoV1;
import com.example.whostolemyfood.menu.presentation.dto.response.ResCreateMenuDtoV1;
import com.example.whostolemyfood.menu.presentation.dto.response.ResGetMenuDtoV1;
import com.example.whostolemyfood.store.domain.entity.StoreEntity;
import com.example.whostolemyfood.store.domain.repository.StoreRepository;
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

    // 메뉴 생성
    @Transactional
    public ResCreateMenuDtoV1 addMenu(UUID storeId, ReqCreateMenuDtoV1 request) {
        // 생성 권한 확인로직

        // 스토어 존재 확인
        StoreEntity store = storeRepository.findById(storeId).orElseThrow(
                        ()-> new IllegalArgumentException("해당 스토어를 찾을 수 없습니다")
        );

        // 스토어 내 메뉴 이름 중복 확인
        if (menuRepository.existsByStore_StoreIdAndNameAndIsDeletedFalse(store.getStoreId(), request.getName())) {
            throw new IllegalArgumentException("해당 스토어에 이미 존재하는 메뉴 이름입니다");
        }
        MenuEntity menu = MenuEntity.builder()
                .store(store)
                .name(request.getName())
                .price(request.getPrice())
                .description(request.getDescription())
                .build();

        MenuEntity savedMenu = menuRepository.save(menu);
        return ResCreateMenuDtoV1.from(savedMenu);
    }

    // 메뉴 조회
    @Transactional(readOnly = true)
    public ResGetMenuDtoV1 getMenu(UUID storeId, UUID menuId) {
        MenuEntity menu = menuRepository.findByMenuIdAndStore_StoreIdAndIsHiddenFalseAndIsDeletedFalse(menuId, storeId)
                .orElseThrow(()-> new IllegalArgumentException("해당 스토어가 존재하지 않거나 메뉴가 존재하지 않습니다"));

        return ResGetMenuDtoV1.from(menu);
    }

    // 메뉴 목록 조회
    @Transactional(readOnly = true)
    public Page<ResGetMenuDtoV1> getMenus(Pageable pageable) {
        Page<MenuEntity> menus = menuRepository.findAllByIsHiddenFalseAndIsDeletedFalse(pageable);

        return menus.map(ResGetMenuDtoV1::from);
    }

    // 메뉴 수정
    @Transactional
    public ResGetMenuDtoV1 updateMenu(UUID storeId, UUID menuId, ReqUpdateMenuDtoV1 request) {
        MenuEntity menu = menuRepository.findByMenuIdAndStore_StoreIdAndIsDeletedFalse(menuId, storeId)
                .orElseThrow(()-> new IllegalArgumentException("해당 스토어에 메뉴를 찾을 수 없습니다"));
        // 유저 권한 확인

        // 이름이 변경될때만 체크
        if(!menu.getName().equals(request.getName())) {
            if (menuRepository.existsByStore_StoreIdAndNameAndIsDeletedFalse(storeId, request.getName())) {
                throw new IllegalArgumentException("해당 스토어에 이미 존재하는 메뉴 이름입니다");
            }
        }
        menu.updateMenu(request);

        return ResGetMenuDtoV1.from(menu);
    }

    // 메뉴 삭제
    @Transactional
    public void deleteMenu(UUID storeId, UUID menuId) {
        MenuEntity menu = menuRepository.findByMenuIdAndStore_StoreIdAndIsDeletedFalse(menuId, storeId)
                .orElseThrow(()-> new IllegalArgumentException("해당 스토어가 존재하지 않거나 메뉴가 존재하지 않습니다"));

        menu.deleteMenu(storeId);
    }

    // 메뉴 숨김, 노출
    @Transactional
    public void hiddenMenu(UUID storeId, UUID menuId) {
        // isHiddenFalse가 없어야지 숨김 상태를 찾아 노출로 변경가능
        MenuEntity menu = menuRepository.findByMenuIdAndStore_StoreIdAndIsDeletedFalse(menuId, storeId)
                .orElseThrow(()-> new IllegalArgumentException("해당 스토어 혹은 메뉴가 존재하지 않습니다"));

        menu.toggleIsHidden();
    }
}
