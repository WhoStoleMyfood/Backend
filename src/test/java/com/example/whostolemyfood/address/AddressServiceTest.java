package com.example.whostolemyfood.address;

import com.example.whostolemyfood.address.application.service.AddressServiceV1;
import com.example.whostolemyfood.address.domain.entity.AddressEntity;
import com.example.whostolemyfood.address.domain.repository.AddressRepository;
import com.example.whostolemyfood.address.presentation.dto.request.ReqCreateAddressDtoV1;
import com.example.whostolemyfood.address.presentation.dto.request.ReqUpdateAddressDtoV1;
import com.example.whostolemyfood.address.presentation.dto.response.ResCreateAddressDtoV1;
import com.example.whostolemyfood.address.presentation.dto.response.ResGetAddressDtoV1;
import com.example.whostolemyfood.global.exception.CustomException;
import com.example.whostolemyfood.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class AddressServiceTest {

    @InjectMocks
    private AddressServiceV1 addressService;

    @Mock
    private AddressRepository addressRepository;

    @Test
    @DisplayName("[성공] 배송지 생성 - 유효한 정보 입력 시 정상 저장")
    void createAddressSuccessTest() {
        // Given
        ReqCreateAddressDtoV1 request = ReqCreateAddressDtoV1.builder()
                .alias("우리집").address("서울시").isDefault(true).build();
        
        given(addressRepository.save(any())).willAnswer(invocation -> {
            AddressEntity address = invocation.getArgument(0);
            ReflectionTestUtils.setField(address, "id", UUID.randomUUID());
            return address;
        });

        // When
        ResCreateAddressDtoV1 response = addressService.createAddress(request);

        // Then
        assertThat(response.getAddressId()).isNotNull();
    }

    @Test
    @DisplayName("[성공] 기본 배송지 설정 - 새로운 기본지 생성 시 기존 기본지는 해제")
    void handleDefaultAddressTest() {
        // Given
        AddressEntity existingDefault = AddressEntity.builder().isDefault(true).build();
        given(addressRepository.findByUserIdAndIsDefaultTrueAndIsDeletedFalse(any()))
                .willReturn(Optional.of(existingDefault));
        
        ReqCreateAddressDtoV1 request = ReqCreateAddressDtoV1.builder().isDefault(true).address("새 주소").build();
        given(addressRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        // When
        addressService.createAddress(request);

        // Then
        assertThat(existingDefault.getIsDefault()).isFalse();
    }

    @Test
    @DisplayName("[성공] 배송지 목록 조회 - 별칭 검색어 포함 시 필터링 결과 반환")
    void getMyAddressesWithSearchTest() {
        // Given
        AddressEntity address = AddressEntity.builder().alias("회사").isDeleted(false).build();
        given(addressRepository.findAllByUserIdAndAliasContainingAndIsDeletedFalse(any(), any(), any()))
                .willReturn(new PageImpl<>(List.of(address)));

        // When
        Page<ResGetAddressDtoV1> result = addressService.getMyAddresses("회사", PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent().get(0).getAlias()).isEqualTo("회사");
    }

    @Test
    @DisplayName("[성공] 배송지 수정 - 유효한 데이터 입력 시 필드 정보 업데이트")
    void updateAddressSuccessTest() {
        // Given
        UUID addressId = UUID.randomUUID();
        AddressEntity address = AddressEntity.builder()
                .id(addressId)
                .userId(UUID.randomUUID()) // ENP 방지를 위해 userId 설정
                .alias("옛날집")
                .isDeleted(false)
                .build();
        
        given(addressRepository.findByIdAndIsDeletedFalse(addressId)).willReturn(Optional.of(address));

        ReqUpdateAddressDtoV1 request = ReqUpdateAddressDtoV1.builder().alias("새로운집").address("서울").build();

        // When
        ResGetAddressDtoV1 response = addressService.updateAddress(addressId, request);

        // Then
        assertThat(response.getAlias()).isEqualTo("새로운집");
    }

    @Test
    @DisplayName("[실패] 배송지 수정 - 존재하지 않는 ID 조회 시 ADDRESS_NOT_FOUND 예외 발생")
    void updateAddressNotFoundTest() {
        // Given
        UUID addressId = UUID.randomUUID();
        given(addressRepository.findByIdAndIsDeletedFalse(addressId)).willReturn(Optional.empty());

        // When
        CustomException exception = assertThrows(CustomException.class, () -> 
            addressService.updateAddress(addressId, ReqUpdateAddressDtoV1.builder().build())
        );

        // Then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ADDRESS_NOT_FOUND);
    }

    @Test
    @DisplayName("[실패] 배송지 삭제 - 존재하지 않는 ID 조회 시 ADDRESS_NOT_FOUND 예외 발생")
    void deleteAddressNotFoundTest() {
        // Given
        UUID addressId = UUID.randomUUID();
        given(addressRepository.findByIdAndIsDeletedFalse(addressId)).willReturn(Optional.empty());

        // When
        CustomException exception = assertThrows(CustomException.class, () -> addressService.deleteAddress(addressId));

        // Then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ADDRESS_NOT_FOUND);
    }
}
