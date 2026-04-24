package com.example.whostolemyfood.address.application.service;

import com.example.whostolemyfood.address.domain.entity.AddressEntity;
import com.example.whostolemyfood.address.domain.repository.AddressRepository;
import com.example.whostolemyfood.address.presentation.dto.request.ReqCreateAddressDtoV1;
import com.example.whostolemyfood.address.presentation.dto.request.ReqUpdateAddressDtoV1;
import com.example.whostolemyfood.address.presentation.dto.response.ResCreateAddressDtoV1;
import com.example.whostolemyfood.address.presentation.dto.response.ResGetAddressDtoV1;
import com.example.whostolemyfood.global.exception.CustomException;
import com.example.whostolemyfood.global.exception.ErrorCode;
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
    public ResCreateAddressDtoV1 createAddress(ReqCreateAddressDtoV1 request, UUID userId) {
        log.info("[Address] Creating new address for User: {}, Alias: {}", userId, request.getAlias());
        
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            handleDefaultAddress(userId);
        }

        AddressEntity address = request.toEntity(userId);
        
        // 🚨 [Audit 필수사항] 생성자 정보 기록
        address.markCreatedBy(userId);

        AddressEntity savedAddress = addressRepository.save(address);
        
        log.info("[Address] Address created successfully. AddressId: {}, User: {}", savedAddress.getId(), userId);
        return ResCreateAddressDtoV1.from(savedAddress, "배송지가 성공적으로 생성되었습니다.");
    }

    /**
     * 본인의 배송지 목록 조회
     */
    public Page<ResGetAddressDtoV1> getMyAddresses(UUID userId, String alias, Pageable pageable) {
        log.info("[Address] Fetching addresses for User: {}, Filter: {}", userId, alias);
        
        Page<AddressEntity> addresses;
        if (alias != null && !alias.isBlank()) {
            addresses = addressRepository.findAllByUserIdAndAliasContainingAndIsDeletedFalse(userId, alias, pageable);
        } else {
            addresses = addressRepository.findAllByUserIdAndIsDeletedFalse(userId, pageable);
        }
        
        log.info("[Address] Successfully fetched {} addresses for User: {}", addresses.getTotalElements(), userId);
        return addresses.map(ResGetAddressDtoV1::from);
    }

    /**
     * 배송지 수정
     */
    @Transactional
    public ResGetAddressDtoV1 updateAddress(UUID addressId, ReqUpdateAddressDtoV1 request, UUID userId) {
        log.info("[Address] Updating address info. AddressId: {}, User: {}", addressId, userId);
        
        AddressEntity address = addressRepository.findByIdAndIsDeletedFalse(addressId)
                .orElseThrow(() -> {
                    log.warn("[Address] Update failed. Address not found. AddressId: {}", addressId);
                    return new CustomException(ErrorCode.ADDRESS_NOT_FOUND);
                });

        // [인가] 본인 확인 로직
        if (!address.getUserId().equals(userId)) {
            log.warn("[Address] Update unauthorized. User {} tried to update address owned by {}", userId, address.getUserId());
            throw new CustomException(ErrorCode.ADDRESS_NOT_OWNER);
        }

        if (Boolean.TRUE.equals(request.getIsDefault()) && !address.getIsDefault()) {
            handleDefaultAddress(userId);
        }

        address.updateAddress(
                request.getAlias(),
                request.getAddress(),
                request.getDetail(),
                request.getZipCode(),
                request.getIsDefault()
        );
        
        // 🚨 [Audit 필수사항] 수정자 정보 기록
        address.markUpdatedBy(userId);

        log.info("[Address] Address updated successfully. AddressId: {}", addressId);
        return ResGetAddressDtoV1.from(address, "배송지 정보가 성공적으로 수정되었습니다.");
    }

    /**
     * 배송지 삭제 (Soft Delete)
     */
    @Transactional
    public void deleteAddress(UUID addressId, UUID userId) {
        log.info("[Address] Requesting soft-delete. AddressId: {}, User: {}", addressId, userId);
        
        AddressEntity address = addressRepository.findByIdAndIsDeletedFalse(addressId)
                .orElseThrow(() -> {
                    log.warn("[Address] Delete failed. Address not found. AddressId: {}", addressId);
                    return new CustomException(ErrorCode.ADDRESS_NOT_FOUND);
                });

        // [인가] 본인 확인 로직
        if (!address.getUserId().equals(userId)) {
            log.warn("[Address] Delete unauthorized. User {} tried to delete address owned by {}", userId, address.getUserId());
            throw new CustomException(ErrorCode.ADDRESS_NOT_OWNER);
        }

        // [Audit 필수사항] 삭제자 정보 기록 및 Soft Delete 수행
        address.softDelete(userId);
        log.info("[Address] Address soft-deleted successfully. AddressId: {}", addressId);
    }

    private void handleDefaultAddress(UUID userId) {
        addressRepository.findByUserIdAndIsDefaultTrueAndIsDeletedFalse(userId)
                .ifPresent(existingDefault -> {
                    log.info("[Address] Unsetting existing default address. AddressId: {}, User: {}", existingDefault.getId(), userId);
                    existingDefault.setDefault(false);
                });
    }
}
