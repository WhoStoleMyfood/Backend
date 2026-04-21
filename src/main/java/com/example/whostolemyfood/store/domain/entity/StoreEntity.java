package com.example.whostolemyfood.store.domain.entity;

import com.example.whostolemyfood.global.entity.BaseSoftDeleteEntity;
import com.example.whostolemyfood.store.presentation.dto.request.ReqUpdateStoreDtoV1;
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


    // owner용 업데이트
    public void updateAllFields(ReqUpdateStoreDtoV1 request) {
        this.name = request.getStoreName();
        this.address = request.getAddress();
        this.phone = request.getPhone();
        this.content = request.getContent();
        this.minOrderPrice = request.getMinOrderPrice();
        this.openTime = request.getOpenTime();
        this.closeTime = request.getCloseTime();

        if (request.getStatus() != null) {
            this.status = request.getStatus();
        }
        // 상태, 숨김
    }

    // manager / master용 업데이트
//    public void updateOptionalFields(ReqUpdateStoreDtoV1 request) {
//        this.status = request.getStatus();
//        this.isHidden = request.getIsHidden();
//    }

    public void deleteByOwnerAndMaster(UUID deletedBy) {
        this.isDeleted = true;
        super.delete(deletedBy);
        this.status = StoreStatus.SHUTDOWN;
    }
}
