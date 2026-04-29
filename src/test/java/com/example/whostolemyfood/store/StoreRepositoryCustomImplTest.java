package com.example.whostolemyfood.store;

import com.example.whostolemyfood.address.domain.entity.AddressEntity;
import com.example.whostolemyfood.area.domain.entity.AreaEntity;
import com.example.whostolemyfood.category.domain.entity.CategoryEntity;
import com.example.whostolemyfood.global.config.JpaAuditingConfig;
import com.example.whostolemyfood.global.config.search.QueryDSLConfig;
import com.example.whostolemyfood.menu.domain.entity.MenuEntity;
import com.example.whostolemyfood.store.domain.entity.StoreEntity;
import com.example.whostolemyfood.store.domain.repository.StoreRepositoryCustom;
import com.example.whostolemyfood.store.infrastructure.repository.StoreRepositoryCustomImpl;
import com.example.whostolemyfood.store.presentation.dto.request.StoreSearchConditionV1;
import com.example.whostolemyfood.store.presentation.dto.response.StoreSearchResponseDtoV1;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


//@JPATest 시 bean Context 문제 발생으로 SpringBootTest로 진행
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class StoreRepositoryCustomImplTest {

    @Autowired
    private StoreRepositoryCustom storeRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("가게명 또는 메뉴명으로 검색 시 중복 없이 페이징 결과가 반환된다")
    @Transactional
    void searchStore_KeywordTest() {
        // given: 가게 및 메뉴 세팅 (가게 1개에 메뉴 3개)
        CategoryEntity category = CategoryEntity.builder()
                .name("중식")
                .build();
        em.persist(category);

        AreaEntity area = AreaEntity.builder()
                .ukName("대구_중구_성내동_01")
                .city("대구")
                .district("중구")
                .isActive(true)
                .build();
        em.persist(area);

        AddressEntity address = AddressEntity.builder()
                .userId(UUID.randomUUID())
                .address("광화문")
                .isDefault(false)
                .build();

        StoreEntity store = StoreEntity.builder()
                .name("짜장백개")
                .address("대구 중구 종로 100")
                .phone("053-123-4567")
                .content("정통 중식 전문점")
                .category(category)
                .area(area)
                .openTime(LocalTime.of(11, 0))
                .closeTime(LocalTime.of(22, 0))
                .minOrderPrice(10000)
                .build();
        em.persist(store);

        em.persist(MenuEntity.builder()
                .store(store)
                .price(7000)
                .name("짜장면")
                .build());

        em.persist(MenuEntity.builder()
                .store(store)
                .price(10000)
                .name("짬뽕")
                .build());

        em.persist(MenuEntity.builder()
                .store(store)
                .price(20000)
                .name("탕수육")
                .build());

        em.flush();
        em.clear();

        StoreSearchConditionV1 cond = new StoreSearchConditionV1();
        cond.setKeyword("짜장"); // 가게명에도 있고 메뉴명에도 있음
        PageRequest pageable = PageRequest.of(0, 10);

        // when
        Page<StoreSearchResponseDtoV1> result = storeRepository.searchStore(cond, pageable);

        // then
        System.out.println("========== 검색 결과 ==========");
        System.out.println("총 개수: " + result.getTotalElements());
        System.out.println("현재 페이지 개수: " + result.getContent().size());
        System.out.println("검색 결과:");
        result.getContent().forEach(storeDto -> {
            System.out.println("  - Store ID: " + storeDto.getStoreId());
            System.out.println("    Store Name: " + storeDto.getStoreName());
            System.out.println("    Matched Menu: " + storeDto.getStoreAddress());
        });
        System.out.println("==============================");
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}