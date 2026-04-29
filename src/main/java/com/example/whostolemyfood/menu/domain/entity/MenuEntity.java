package com.example.whostolemyfood.menu.domain.entity;

import com.example.whostolemyfood.ai.presentation.dto.response.ResGetAiLogDtoV1;
import com.example.whostolemyfood.global.entity.BaseAuditEntity;
import com.example.whostolemyfood.global.entity.BaseSoftDeleteEntity;
import com.example.whostolemyfood.menu.presentation.dto.request.ReqUpdateMenuDtoV1;
import com.example.whostolemyfood.store.domain.entity.StoreEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@SQLRestriction("is_deleted = false")
//@SQLDelete(sql = "UPDATE p_menus SET is_deleted = true WHERE menu_id = ?")
@Table(name = "p_menus")
public class MenuEntity extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "menu_id", nullable = false, updatable = false)
    private UUID menuId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false, updatable = false)
    private StoreEntity store;

    @Column(name = "ai_log_id")
    private UUID aiLogId;

    @Column(nullable = false, name = "name")
    private String name;
    @Column(nullable = false, name = "price")
    private Integer price;
    @Column(name = "description")
    private String description;

    @Column(name = "is_hidden")
    private Boolean isHidden = false;

    @Builder
    public MenuEntity(UUID menuId, StoreEntity store, UUID aiLogId, String name, Integer price, String description) {
        validatePrice(price);
        this.menuId = menuId;
        this.store = store;
        this.aiLogId = aiLogId;
        this.name = name;
        this.price = price;
        this.description = description;
    }

    public void updateMenu(ReqUpdateMenuDtoV1 request, ResGetAiLogDtoV1 aiResult) {
        validatePrice(request.getPrice());

        this.name = request.getName();
        this.price = request.getPrice();
        this.description = aiResult.description();
        this.aiLogId = aiResult.aiLogId();
    }

    public void deleteMenu(UUID userId) {
        super.softDelete(userId);
    }

    public void toggleIsHidden() {
        this.isHidden = !this.isHidden;
    }

    private void validatePrice(Integer price) {
        if (price == null || price < 0) {
            throw new IllegalArgumentException("메뉴가격 이상 에러코드 작성하세여 십련아");
        }
    }
}
