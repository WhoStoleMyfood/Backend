package com.example.whostolemyfood.store;

import com.example.whostolemyfood.area.domain.entity.AreaEntity;
import com.example.whostolemyfood.category.domain.entity.CategoryEntity;
import com.example.whostolemyfood.store.domain.entity.StoreEntity;
import com.example.whostolemyfood.store.domain.repository.StoreRepository;
import com.example.whostolemyfood.user.domain.entity.UserEntity;
import com.example.whostolemyfood.user.domain.entity.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@EnableJpaRepositories(basePackageClasses = StoreRepository.class)
@EntityScan(basePackages = "com.example.whostolemyfood")
class StoreRepositoryV1Test {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private TestEntityManager entityManager;


    @Test
    @DisplayName("[Repository] 삭제되지 않은 가게 이름 중복 여부를 확인할 수 있다")
    void existsByNameAndIsDeletedFalse() {
        // given
        CategoryEntity category = CategoryEntity.builder()
                .name("카테고리")
                .build();

        AreaEntity area = AreaEntity.builder()
                .city("서울")
                .district("강남")
                .ukName("서울-강남")
                .isActive(true)
                .build();

        category = entityManager.persist(category);
        area = entityManager.persist(area);

        StoreEntity store = StoreEntity.builder()
                .name("테스트 가게")
                .address("서울시 강남구")
                .minOrderPrice(10000)
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(22, 0))
                .phone("010-1234-5678")
                .content("설명")
                .category(category)
                .area(area)// 필요하면 생성해서 넣어야 함
                .build();

        storeRepository.save(store);

        // when
        Boolean exists = storeRepository.existsByNameAndIsDeletedFalse("테스트 가게");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("[Repository] 숨김 처리되지 않고 삭제되지 않은 가게를 단건 조회할 수 있다")
    void findByStoreIdAndIsHiddenFalseAndIsDeletedFalse() {
        // given
        CategoryEntity category = CategoryEntity.builder()
                .name("카테고리")
                .build();

        AreaEntity area = AreaEntity.builder()
                .city("서울")
                .district("강남")
                .ukName("서울-강남")
                .isActive(true)
                .build();

        category = entityManager.persist(category);
        area = entityManager.persist(area);

        StoreEntity store = StoreEntity.builder()
                .name("조회 가게")
                .address("서울시 강남구")
                .minOrderPrice(10000)
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(22, 0))
                .phone("010-1234-5678")
                .content("설명")
                .category(category)
                .area(area)// 필요하면 생성해서 넣어야 함
                .build();

        StoreEntity savedStore = storeRepository.save(store);

        // when
        Optional<StoreEntity> result =
                storeRepository.findByStoreIdAndIsHiddenFalseAndIsDeletedFalse(savedStore.getStoreId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("조회 가게");
    }

    @Test
    @DisplayName("[Repository] 삭제되지 않은 가게를 단건 조회할 수 있다")
    void findByStoreIdAndIsDeletedFalse() {
        // given
        CategoryEntity category = CategoryEntity.builder().build();

        StoreEntity store = StoreEntity.builder()
                .name("테스트 가게")
                .address("서울시 강남구")
                .minOrderPrice(10000)
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(22, 0))
                .phone("010-1234-5678")
                .content("설명")
                .category(category) // 필요하면 생성해서 넣어야 함
                .build();

        StoreEntity savedStore = storeRepository.save(store);

        // when
        Optional<StoreEntity> result =
                storeRepository.findByStoreIdAndIsDeletedFalse(savedStore.getStoreId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getIsHidden()).isTrue();
        assertThat(result.get().getIsDeleted()).isFalse();
    }

    @Test
    @DisplayName("[Repository] 숨김 처리되지 않고 삭제되지 않은 가게 목록을 조회할 수 있다")
    void findAllByIsHiddenFalseAndIsDeletedFalse() {
        // given
        CategoryEntity category = CategoryEntity.builder()
                .name("카테고리")
                .build();

        AreaEntity area = AreaEntity.builder()
                .city("서울")
                .district("강남")
                .ukName("서울-강남")
                .isActive(true)
                .build();

        category = entityManager.persist(category);
        area = entityManager.persist(area);

        StoreEntity activeStore = StoreEntity.builder()
                .name("활성 가게")
                .address("서울시 강남구")
                .minOrderPrice(10000)
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(22, 0))
                .phone("010-1234-5678")
                .content("설명")
                .category(category)
                .area(area)// 필요하면 생성해서 넣어야 함
                .build();

        StoreEntity hiddenStore = StoreEntity.builder()
                .name("숨김 가게")
                .address("서울시 강남구")
                .minOrderPrice(10000)
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(22, 0))
                .phone("010-1234-5678")
                .content("설명")
                .category(category)
                .area(area)// 필요하면 생성해서 넣어야 함
                .build();

        ReflectionTestUtils.setField(hiddenStore, "isHidden", true);

        storeRepository.save(activeStore);
        storeRepository.save(hiddenStore);

        // when
        Page<StoreEntity> result =
                storeRepository.findAllByIsHiddenFalseAndIsDeletedFalse(PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("활성 가게");
    }

    @Test
    @DisplayName("[Repository] 숨김 또는 삭제된 비활성 가게 목록을 조회할 수 있다")
    void findAllInactiveStores() {
        // given
        CategoryEntity category = CategoryEntity.builder()
                .name("카테고리")
                .build();

        AreaEntity area = AreaEntity.builder()
                .city("서울")
                .district("강남")
                .ukName("서울-강남")
                .isActive(true)
                .build();

        category = entityManager.persist(category);
        area = entityManager.persist(area);

        StoreEntity activeStore = StoreEntity.builder()
                .name("활성 가게")
                .address("서울시 강남구")
                .minOrderPrice(10000)
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(22, 0))
                .phone("010-1234-5678")
                .content("설명")
                .category(category)
                .area(area)// 필요하면 생성해서 넣어야 함
                .build();

        StoreEntity hiddenStore = StoreEntity.builder()
                .name("숨김 가게")
                .address("서울시 강남구")
                .minOrderPrice(10000)
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(22, 0))
                .phone("010-1234-5678")
                .content("설명")
                .category(category)
                .area(area)// 필요하면 생성해서 넣어야 함
                .build();

        StoreEntity deletedStore = StoreEntity.builder()
                .name("삭제 가게")
                .address("서울시 강남구")
                .minOrderPrice(10000)
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(22, 0))
                .phone("010-1234-5678")
                .content("설명")
                .category(category)
                .area(area)// 필요하면 생성해서 넣어야 함
                .build();

        ReflectionTestUtils.setField(hiddenStore, "isHidden", true);
        ReflectionTestUtils.setField(deletedStore, "isDeleted", true);

        storeRepository.save(activeStore);
        storeRepository.save(hiddenStore);
        storeRepository.save(deletedStore);

        // when
        Page<StoreEntity> result =
                storeRepository.findAllInactiveStores(PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(StoreEntity::getName)
                .containsExactlyInAnyOrder("숨김 가게", "삭제 가게");
    }

    @Test
    @DisplayName("[Repository] 특정 유저의 숨김 또는 삭제된 비활성 가게 목록을 조회할 수 있다")
    void findInactiveStoresByUserId() {
        // given
        CategoryEntity category = CategoryEntity.builder()
                .name("카테고리")
                .build();

        AreaEntity area = AreaEntity.builder()
                .city("서울")
                .district("강남")
                .ukName("서울-강남")
                .isActive(true)
                .build();

        category = entityManager.persist(category);
        area = entityManager.persist(area);

        UserEntity user = entityManager.persist(
                UserEntity.builder()
                        .role(UserRole.CUSTOMER)
                        .email("test@test.com")
                        .password("1234")
                        .name("테스트")
                        .address("서울")
                        .build()
        );

        StoreEntity myInactiveStore = StoreEntity.builder()
                .name("내 비활성 가게")
                .address("서울시 강남구")
                .minOrderPrice(10000)
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(22, 0))
                .phone("010-1234-5678")
                .content("설명")
                .category(category)
                .area(area)// 필요하면 생성해서 넣어야 함
                .build();

        StoreEntity myActiveStore = StoreEntity.builder()
                .name("내 활성 가게")
                .address("서울시 강남구")
                .minOrderPrice(10000)
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(22, 0))
                .phone("010-1234-5678")
                .content("설명")
                .category(category)
                .area(area)// 필요하면 생성해서 넣어야 함
                .build();

        ReflectionTestUtils.setField(myInactiveStore, "user", user);
        ReflectionTestUtils.setField(myActiveStore, "user", user);

        ReflectionTestUtils.setField(myInactiveStore, "isHidden", true);

        storeRepository.save(myInactiveStore);
        storeRepository.save(myActiveStore);

        // when
        Page<StoreEntity> result =
                storeRepository.findInactiveStoresByUserId(user.getId(), PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("내 비활성 가게");
    }
}