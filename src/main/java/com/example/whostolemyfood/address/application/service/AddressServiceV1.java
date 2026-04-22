package com.example.whostolemyfood.address.application.service;

import com.example.whostolemyfood.address.domain.entity.AddressEntity;
import com.example.whostolemyfood.address.domain.repository.AddressRepository;
import com.example.whostolemyfood.address.presentation.dto.request.ReqCreateAddressDtoV1;
import com.example.whostolemyfood.address.presentation.dto.request.ReqUpdateAddressDtoV1;
import com.example.whostolemyfood.address.presentation.dto.response.ResCreateAddressDtoV1;
import com.example.whostolemyfood.address.presentation.dto.response.ResGetAddressDtoV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddressServiceV1 {

    private final AddressRepository addressRepository;

    /**
     * 배송지 생성
     */
    @Transactional
    public ResCreateAddressDtoV1 createAddress(ReqCreateAddressDtoV1 request) {
        // TODO: [인증/인가] SecurityContext 기반 사용자 ID 추출
        UUID mockUserId = UUID.randomUUID(); 
        log.info("Creating address for user: {}, alias: {}", mockUserId, request.getAlias());
        
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            handleDefaultAddress(mockUserId);
        }

        AddressEntity address = request.toEntity(mockUserId);
        AddressEntity savedAddress = addressRepository.save(address);
        
        return ResCreateAddressDtoV1.from(savedAddress);
    }

    /**
     * 본인의 배송지 목록 조회
     */
    public Page<ResGetAddressDtoV1> getMyAddresses(String alias, Pageable pageable) {
        // TODO: [인증/인가] SecurityContext 기반 사용자 ID 추출
        UUID mockUserId = UUID.randomUUID(); 
        log.info("Fetching addresses for user: {}, filter: {}", mockUserId, alias);
        
        Page<AddressEntity> addresses;
        if (alias != null && !alias.isBlank()) {
            addresses = addressRepository.findAllByUserIdAndAliasContainingAndIsDeletedFalse(mockUserId, alias, pageable);
        } else {
            addresses = addressRepository.findAllByUserIdAndIsDeletedFalse(mockUserId, pageable);
        }
        
        return addresses.map(ResGetAddressDtoV1::from);
    }

    /**
     * 배송지 수정
     */
    @Transactional
    public ResGetAddressDtoV1 updateAddress(UUID addressId, ReqUpdateAddressDtoV1 request) {
        // TODO: [인증/인가] SecurityContext 기반 사용자 ID 추출
        UUID mockUserId = UUID.randomUUID(); 
        log.info("Updating address: {} for user: {}", addressId, mockUserId);
        
        AddressEntity address = addressRepository.findByIdAndIsDeletedFalse(addressId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 배송지입니다. ID: " + addressId));

        // 본인 확인
        if (!address.getUserId().equals(mockUserId)) {
            throw new IllegalStateException("본인의 배송지만 수정할 수 있습니다.");
        }

        if (Boolean.TRUE.equals(request.getIsDefault()) && !address.getIsDefault()) {
            handleDefaultAddress(mockUserId);
        }

        address.updateAddress(
                request.getAlias(),
                request.getAddress(),
                request.getDetail(),
                request.getZipCode(),
                request.getIsDefault()
        );

        return ResGetAddressDtoV1.from(address);
    }

    /**
     * 배송지 삭제 (Soft Delete)
     */
    @Transactional
    public void deleteAddress(UUID addressId) {
        // TODO: [인증/인가] SecurityContext 기반 사용자 ID 추출
        UUID mockUserId = UUID.randomUUID(); 
        log.info("Deleting address: {} for user: {}", addressId, mockUserId);
        
        AddressEntity address = addressRepository.findByIdAndIsDeletedFalse(addressId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 배송지입니다. ID: " + addressId));

        if (!address.getUserId().equals(mockUserId)) {
            throw new IllegalStateException("본인의 배송지만 삭제할 수 있습니다.");
        }

        address.markAsDeleted(mockUserId);
    }

    private void handleDefaultAddress(UUID userId) {
        addressRepository.findByUserIdAndIsDefaultTrueAndIsDeletedFalse(userId)
                .ifPresent(existingDefault -> {
                    log.info("Unsetting default address: {}", existingDefault.getId());
                    existingDefault.setDefault(false);
                });
    }
}
