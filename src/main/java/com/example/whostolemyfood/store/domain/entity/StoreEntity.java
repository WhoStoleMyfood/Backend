package com.example.whostolemyfood.store.domain.entity;

import com.example.whostolemyfood.global.entity.BaseSoftDeleteEntity;
import com.example.whostolemyfood.store.presentation.dto.request.ReqUpdateStoreDtoV1;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("is_deleted = false")
@SQLDelete(sql = "UPDATE store_entity SET is_deleted = true WHERE store_id = ?")
@Table(name = "p_stores")
public class StoreEntity extends BaseSoftDeleteEntity {
    @Id
    @Column(name = "store_id")
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    // store_rating_id
    // user_id
//    @ManyToOne
//    @JoinColumn(name = "category_id")
//    private Category category;

//    @ManyToOne
//    @Column(name = "area_id")
//    private Area area;
    // store_status_id

    private String name;
    private String address;
    private String phone;
    private String content;

    @Column(name = "min_order_price")
    private Integer minOrderPrice;

    @Enumerated(EnumType.STRING)
    private StoreStatus status = StoreStatus.OPEN;

    @Column(name = "is_hidden")
    private Boolean isHidden = false;
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Builder
    public StoreEntity(String name, String address, String phone, String content, Integer minOrderPrice) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.content = content;
        this.minOrderPrice = minOrderPrice;
    }

    // owner용 업데이트
    public void updateAllFields(ReqUpdateStoreDtoV1 request) {
        this.name = request.getStoreName();
        this.address = request.getAddress();
        this.phone = request.getPhone();
        this.content = request.getContent();
        this.minOrderPrice = request.getMinOrderPrice();
        this.status = request.getStatus();
        // 상태, 숨김
    }

    // manager / master용 업데이트
//    public void updateOptionalFields(ReqUpdateStoreDtoV1 request) {
//        this.status = request.getStatus();
//        this.isHidden = request.getIsHidden();
//    }

    public void deleteByOwnerAndMaster(UUID storeId) {
        this.isDeleted = true;
        this.delete(storeId);
    }
}
