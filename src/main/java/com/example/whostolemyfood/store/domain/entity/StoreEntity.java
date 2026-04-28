package com.example.whostolemyfood.store.domain.entity;

import com.example.whostolemyfood.area.domain.entity.AreaEntity;
import com.example.whostolemyfood.category.domain.entity.CategoryEntity;
import com.example.whostolemyfood.global.entity.BaseSoftDeleteEntity;
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
@AllArgsConstructor
@Builder
@SQLRestriction("is_deleted = false")
@SQLDelete(sql = "UPDATE p_stores SET is_deleted = true WHERE store_id = ?")
@Table(name = "p_stores")
public class StoreEntity extends BaseSoftDeleteEntity {
    @Id
    @Column(name = "store_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID storeId;

    // user_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id")
    private AreaEntity area;

    //store_rating_id 이걸로 통일
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_rating_id")
    private StoreRatingSummaryEntity storeRatingSummary;

    @Column(nullable = false, unique = true)
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
    private StoreStatus status = StoreStatus.OPEN;

    @JsonFormat(pattern = "HH:mm")
    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;
    @JsonFormat(pattern = "HH:mm")
    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;

    @Column(name = "is_hidden")
    @Builder.Default
    private Boolean isHidden = false;
    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    // 스토어 수정
    public void updateStore(ReqUpdateStoreDtoV1 request, CategoryEntity category) {
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

    public void deleteByOwnerAndMaster(UUID deletedBy) {
        this.isDeleted = true;
        super.delete(deletedBy);
        this.status = StoreStatus.SHUTDOWN;
    }

    public void updateStoreRatingSummary(StoreRatingSummaryEntity storeRatingSummary) {
        this.storeRatingSummary = storeRatingSummary;
    }

    public void toggleIsHidden() {
        this.isHidden = !this.isHidden;
    }
}
