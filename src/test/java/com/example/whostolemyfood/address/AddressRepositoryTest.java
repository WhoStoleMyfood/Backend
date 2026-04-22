package com.example.whostolemyfood.address;

import com.example.whostolemyfood.address.domain.entity.AddressEntity;
import com.example.whostolemyfood.address.domain.repository.AddressRepository;
import com.example.whostolemyfood.global.config.JpaAuditingConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaAuditingConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AddressRepositoryTest {

    @Autowired
    private AddressRepository addressRepository;

    @BeforeEach
    void setUp() {
        addressRepository.deleteAll();
    }

    @Test
    @DisplayName("사용자 ID로 조회 시 삭제되지 않은 배송지면 페이징되어 조회되어야 함")
    void findAllByUserIdAndIsDeletedFalseTest() {
        // Given
        UUID userId = UUID.randomUUID();
        addressRepository.save(createAddress(userId, "우리집"));
        AddressEntity deletedAddress = addressRepository.save(createAddress(userId, "옛날집"));
        deletedAddress.markAsDeleted(UUID.randomUUID());
        addressRepository.saveAndFlush(deletedAddress);

        // When
        Page<AddressEntity> result = addressRepository.findAllByUserIdAndIsDeletedFalse(userId, PageRequest.of(0, 10));

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1L);
        assertThat(result.getContent().get(0).getAlias()).isEqualTo("우리집");
    }

    @Test
    @DisplayName("별칭(alias) 검색 시 해당 키워드가 포함된 배송지만 조회되어야 함")
    void findAllByUserIdAndAliasContainingTest() {
        // Given
        UUID userId = UUID.randomUUID();
        addressRepository.save(createAddress(userId, "우리집"));
        addressRepository.save(createAddress(userId, "회사"));

        // When
        Page<AddressEntity> result = addressRepository.findAllByUserIdAndAliasContainingAndIsDeletedFalse(userId, "우리", PageRequest.of(0, 10));

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1L);
        assertThat(result.getContent().get(0).getAlias()).isEqualTo("우리집");
    }

    @Test
    @DisplayName("기본 배송지 설정 여부(isDefault)로 조회가 가능해야 함")
    void findByUserIdAndIsDefaultTrueTest() {
        // Given
        UUID userId = UUID.randomUUID();
        addressRepository.save(AddressEntity.builder()
                .userId(userId).address("서울").isDefault(true).build());
        addressRepository.save(AddressEntity.builder()
                .userId(userId).address("부산").isDefault(false).build());

        // When
        AddressEntity defaultAddress = addressRepository.findByUserIdAndIsDefaultTrueAndIsDeletedFalse(userId).orElseThrow();

        // Then
        assertThat(defaultAddress.getAddress()).isEqualTo("서울");
        assertThat(defaultAddress.getIsDefault()).isTrue();
    }

    @Test
    @DisplayName("삭제 시 markAsDeleted를 호출하면 Soft Delete가 적용되어야 함")
    void softDeleteTest() {
        // Given
        AddressEntity address = addressRepository.save(createAddress(UUID.randomUUID(), "집"));

        // When
        address.markAsDeleted(UUID.randomUUID());
        addressRepository.saveAndFlush(address);

        // Then
        AddressEntity found = addressRepository.findById(address.getId()).orElseThrow();
        assertThat(found.getIsDeleted()).isTrue();
        assertThat(found.getDeletedAt()).isNotNull();
    }

    private AddressEntity createAddress(UUID userId, String alias) {
        return AddressEntity.builder()
                .userId(userId)
                .alias(alias)
                .address("테스트 주소")
                .isDefault(false)
                .build();
    }
}
