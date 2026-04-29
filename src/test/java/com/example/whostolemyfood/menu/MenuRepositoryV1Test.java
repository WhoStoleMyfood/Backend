package com.example.whostolemyfood.menu;

import com.example.whostolemyfood.area.domain.entity.AreaEntity;
import com.example.whostolemyfood.category.domain.entity.CategoryEntity;
import com.example.whostolemyfood.menu.domain.entity.MenuEntity;
import com.example.whostolemyfood.menu.domain.repository.MenuRepository;
import com.example.whostolemyfood.store.domain.entity.StoreEntity;
import com.example.whostolemyfood.store.domain.entity.StoreRatingSummaryEntity;
import com.example.whostolemyfood.store.domain.entity.StoreStatus;
import com.example.whostolemyfood.user.domain.entity.UserEntity;
import com.example.whostolemyfood.user.domain.entity.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.persistence.EntityManager;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@EnableJpaRepositories(
        basePackageClasses = MenuRepository.class
)
class MenuRepositoryV1Test {

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("숨김/삭제되지 않은 메뉴를 menuId와 storeId로 조회할 수 있다")
    void findByMenuIdAndStoreIdAndIsHiddenFalseAndIsDeletedFalse() {
        // given
        StoreEntity store = createStore();

        MenuEntity menu = createMenu(store, "후라이드", false, false);
        createMenu(store, "숨김 메뉴", true, false);
        createMenu(store, "삭제 메뉴", false, true);

        entityManager.flush();
        entityManager.clear();

        // when
        Optional<MenuEntity> result =
                menuRepository.findByMenuIdAndStore_StoreIdAndIsHiddenFalseAndIsDeletedFalse(
                        menu.getMenuId(),
                        store.getStoreId()
                );

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("후라이드");
    }

    @Test
    @DisplayName("삭제되지 않은 메뉴는 숨김 여부와 상관없이 조회할 수 있다")
    void findByMenuIdAndStoreIdAndIsDeletedFalse() {
        // given
        StoreEntity store = createStore();

        MenuEntity hiddenMenu = createMenu(store, "숨김 메뉴", true, false);

        entityManager.flush();
        entityManager.clear();

        // when
        Optional<MenuEntity> result =
                menuRepository.findByMenuIdAndStore_StoreIdAndIsDeletedFalse(
                        hiddenMenu.getMenuId(),
                        store.getStoreId()
                );

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("숨김 메뉴");
    }

    @Test
    @DisplayName("같은 가게에 삭제되지 않은 동일한 이름의 메뉴가 존재하는지 확인할 수 있다")
    void existsByStoreIdAndNameAndIsDeletedFalse() {
        // given
        StoreEntity store = createStore();
        createMenu(store, "양념치킨", false, false);
        createMenu(store, "삭제된 양념치킨", false, true);

        entityManager.flush();
        entityManager.clear();

        // when
        Boolean exists = menuRepository.existsByStore_StoreIdAndNameAndIsDeletedFalse(
                store.getStoreId(),
                "양념치킨"
        );

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("숨김/삭제되지 않은 메뉴 목록만 조회할 수 있다")
    void findAllByIsHiddenFalseAndIsDeletedFalse() {
        // given
        StoreEntity store = createStore();

        createMenu(store, "정상 메뉴", false, false);
        createMenu(store, "숨김 메뉴", true, false);
        createMenu(store, "삭제 메뉴", false, true);

        entityManager.flush();
        entityManager.clear();

        // when
        Page<MenuEntity> result =
                menuRepository.findAllByIsHiddenFalseAndIsDeletedFalse(PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("정상 메뉴");
    }

    @Test
    @DisplayName("특정 가게의 숨김 또는 삭제된 비활성 메뉴 목록을 조회할 수 있다")
    void findAllInactiveMenusByStoreId() {
        // given
        StoreEntity store = createStore();

        createMenu(store, "정상 메뉴", false, false);
        createMenu(store, "숨김 메뉴", true, false);
        createMenu(store, "삭제 메뉴", false, true);

        entityManager.flush();
        entityManager.clear();

        // when
        Page<MenuEntity> result =
                menuRepository.findAllInactiveMenusByStoreId(
                        store.getStoreId(),
                        PageRequest.of(0, 10)
                );

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(MenuEntity::getName)
                .containsExactlyInAnyOrder("숨김 메뉴", "삭제 메뉴");
    }

    private StoreEntity createStore() {
        CategoryEntity category = CategoryEntity.builder()
                .name("치킨")
                .build();

        AreaEntity area = AreaEntity.builder()
                .city("서울")
                .district("강남")
                .ukName("서울-강남")
                .isActive(true)
                .build();

        UserEntity user = UserEntity.builder()
                .role(UserRole.OWNER)
                .email("owner@test.com")
                .password("1234")
                .name("사장님")
                .address("서울")
                .build();

        StoreRatingSummaryEntity ratingSummary =
                StoreRatingSummaryEntity.builder().build();

        entityManager.persist(category);
        entityManager.persist(area);
        entityManager.persist(user);
        entityManager.persist(ratingSummary);

        StoreEntity store = StoreEntity.builder()
                .user(user)
                .category(category)
                .area(area)
                .storeRatingSummary(ratingSummary)
                .name("테스트 가게")
                .address("서울시 강남구")
                .phone("02-1234-5678")
                .content("테스트 가게입니다")
                .minOrderPrice(10000)
                .status(StoreStatus.OPEN)
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(22, 0))
                .build();

        entityManager.persist(store);
        return store;
    }

    private MenuEntity createMenu(
            StoreEntity store,
            String name,
            boolean isHidden,
            boolean isDeleted
    ) {
        MenuEntity menu = MenuEntity.builder()
                .store(store)
                .name(name)
                .price(10000)
                .description("테스트 메뉴")
                .build();

        ReflectionTestUtils.setField(menu, "isHidden", isHidden);
        ReflectionTestUtils.setField(menu, "isDeleted", isDeleted);

        entityManager.persist(menu);
        return menu;
    }
}
