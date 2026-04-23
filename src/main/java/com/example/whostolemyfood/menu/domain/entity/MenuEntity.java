package com.example.whostolemyfood.menu.domain.entity;

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
@SQLRestriction("is_deleted = false")
@SQLDelete(sql = "UPDATE p_menus SET is_deleted = true WHERE menu_id = ?")
@Table(name = "p_menus")
public class MenuEntity extends BaseSoftDeleteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "menu_id", nullable = false, updatable = false)
    private UUID menuId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false, updatable = false)
    private StoreEntity store;

    // ai_log_id

    @Column(nullable = false, name = "name")
    private String name;
    @Column(nullable = false, name = "price")
    private Integer price;
    @Column(name = "description")
    private String description;

    @Column(name = "is_hidden")
    private Boolean isHidden = false;
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Builder
    public MenuEntity(StoreEntity store, String name, Integer price, String description) {
        this.store = store;
        this.name = name;
        this.price = price;
        this.description = description;
    }

    public void updateMenu(ReqUpdateMenuDtoV1 request) {
        this.name = request.getName();
        this.price = request.getPrice();
        this.description = request.getDescription();
    }

    public void deleteMenu(UUID deletedBy) {
        this.isDeleted = true;
        super.delete(deletedBy);
    }

    public void toggleIsHidden() {
        this.isHidden = !this.isHidden;
    }
}
