package com.example.whostolemyfood.store.domain.entity;

import com.example.whostolemyfood.area.domain.entity.AreaEntity;
import com.example.whostolemyfood.category.domain.entity.CategoryEntity;
import com.example.whostolemyfood.global.entity.BaseAuditEntity;
import com.example.whostolemyfood.store.presentation.dto.request.ReqUpdateStoreDtoV1;
import com.example.whostolemyfood.user.domain.entity.UserEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@SQLRestriction("is_deleted = false")
//@SQLDelete(sql = "UPDATE p_stores SET is_deleted = true WHERE store_id = ?")
@Table(name = "p_stores")
public class StoreEntity extends BaseAuditEntity {
    @Id
    @Column(name = "store_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID storeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id")
    private AreaEntity area;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_rating_id")
    private StoreRatingSummaryEntity storeRatingSummary;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String address;
    @Column(nullable = false)
    private String phone;
    @Column(nullable = false)
    private String content;

    @Column(name = "min_order_price")
    private Integer minOrderPrice;

    @Enumerated(EnumType.STRING)
    private StoreStatus status;

    @JsonFormat(pattern = "HH:mm")
    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;
    @JsonFormat(pattern = "HH:mm")
    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;

    @Column(name = "is_hidden")
    private Boolean isHidden = false;


    @Builder
    public StoreEntity(UUID storeId, UserEntity user, CategoryEntity category, AreaEntity area, StoreRatingSummaryEntity storeRatingSummary, String name, String address, String phone, String content, Integer minOrderPrice, StoreStatus status, LocalTime openTime, LocalTime closeTime) {
        validateMinOrderPrice(minOrderPrice);
        this.storeId = storeId;
        this.user = user;
        this.category = category;
        this.area = area;
        this.storeRatingSummary = storeRatingSummary;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.content = content;
        this.minOrderPrice = minOrderPrice;
        this.status = status;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    // 스토어 수정
    public void updateStore(ReqUpdateStoreDtoV1 request, CategoryEntity category) {
        validateMinOrderPrice(request.getMinOrderPrice());

        this.name = request.getName();
        this.address = request.getAddress();
        this.phone = request.getPhone();
        this.content = request.getContent();
        this.category = category;
        this.minOrderPrice = request.getMinOrderPrice();
        this.openTime = request.getOpenTime();
        this.closeTime = request.getCloseTime();

        if (request.getStatus() != null) {
            this.status = request.getStatus();
        }
    }

    public void deleteByOwnerAndMaster(UUID userId) {
        super.softDelete(userId);
        this.status = StoreStatus.SHUTDOWN;
    }

    public void updateStoreRatingSummary(StoreRatingSummaryEntity storeRatingSummary) {
        this.storeRatingSummary = storeRatingSummary;
    }

    public void toggleIsHidden() {
        this.isHidden = !this.isHidden;
    }

    private void validateMinOrderPrice(Integer minOrderPrice) {
        if (minOrderPrice != null && minOrderPrice < 0) {
            throw new IllegalArgumentException("최소주문 가격은 0이상이여하 한다");
        }
    }

    public StoreStatus getCalculatedStatus() {
        if (this.status == StoreStatus.SHUTDOWN) {
            return StoreStatus.SHUTDOWN;
        }

        if (this.isHidden) {
            return StoreStatus.CLOSED;
        }
        return StoreStatus.calculateStatus(LocalTime.now(), this.openTime, this.closeTime);
    }
}
