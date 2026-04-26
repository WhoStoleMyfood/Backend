package com.example.whostolemyfood.store.infrastructure.repository;

import com.example.whostolemyfood.address.domain.entity.QAddressEntity;
import com.example.whostolemyfood.category.domain.entity.QCategoryEntity;
import com.example.whostolemyfood.store.domain.entity.QStoreEntity;
import com.example.whostolemyfood.store.domain.entity.QStoreRatingSummaryEntity;
import com.example.whostolemyfood.store.domain.repository.StoreRepositoryCustom;
import com.example.whostolemyfood.store.presentation.dto.request.StoreSearchConditionV1;
import com.example.whostolemyfood.store.presentation.dto.response.QStoreSearchResponseDtoV1;
import com.example.whostolemyfood.store.presentation.dto.response.StoreSearchResponseDtoV1;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class StoreRepositoryCustomImpl implements StoreRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private static final Logger logger = LoggerFactory.getLogger(StoreRepositoryCustomImpl.class);

    @Override
    public Page<StoreSearchResponseDtoV1> searchStore(StoreSearchConditionV1 cond, Pageable pageable) {

        QStoreEntity store = QStoreEntity.storeEntity;
        QCategoryEntity category = QCategoryEntity.categoryEntity;
        QAddressEntity address = QAddressEntity.addressEntity;
        QStoreRatingSummaryEntity storeRating = QStoreRatingSummaryEntity.storeRatingSummaryEntity;


        List<StoreSearchResponseDtoV1> content = queryFactory
                .select(new QStoreSearchResponseDtoV1(
                        store.id,            // 1. storeId
                        store.name,          // 2. storeName
                        store.address,       // 3. storeAddress
                        store.phone,         // 4. storePhone
                        store.content,       // 5. content
                        store.minOrderPrice, // 6. minOrderPrice
                        store.status,        // 7. status
                        store.openTime,      // 8. openTime
                        store.closeTime      // 9. closeTime
                ))
                .from(store)
                .join(store.category, category)
//                .leftJoin(store.address, address)
//                .leftJoin(store.storeRatingId, storeRating)
                .where(
                        keywordContains(cond.getKeyword()),
                        categoryEq(cond.getCategoryId()),
                        minOrderPriceLoe(cond.getMinOrderPrice()),
                        store.isDeleted.isFalse()
                )
                .orderBy(getOrderBy(cond.getSortBy()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        // 2. Count 쿼리
        Long total = queryFactory
                .select(store.count())
                .from(store)
                .leftJoin(store.category, category)
                .where(
                        this.keywordContains(cond.getKeyword()),
                        categoryEq(cond.getCategoryId()),
                        minOrderPriceLoe(cond.getMinOrderPrice()),
                        store.isDeleted.isFalse()
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanExpression keywordContains(String keyword) {
        // 가게 이름에 키워드가 포함되어 있는지 확인

        return StringUtils.hasText(keyword) ? QStoreEntity.storeEntity.name.contains(keyword) : null;
    }

    private BooleanExpression categoryEq(UUID categoryId) {
        // 카테고리 ID가 일치하는지 확인
        return categoryId != null ? QStoreEntity.storeEntity.category.categoryId.eq(categoryId) : null;
    }

    private BooleanExpression minOrderPriceLoe(Integer minOrderPrice) {
        // 입력받은 금액보다 '가게의 최소주문금액'이 작거나 같은 경우 (Loe: Less or Equal)
        return minOrderPrice != null ? QStoreEntity.storeEntity.minOrderPrice.loe(minOrderPrice) : null;
    }

    // 정렬 조건 처리 (필요에 따라 구현)
    private OrderSpecifier<?> getOrderBy(String sortBy) {
        if (!StringUtils.hasText(sortBy)) {
            return QStoreEntity.storeEntity.createdAt.desc(); // 기본 정렬: 최신순
        }
        // 예: "rating"이 들어오면 별점순 정렬 등 로직 추가 가능
        return QStoreEntity.storeEntity.createdAt.desc();
    }

}
