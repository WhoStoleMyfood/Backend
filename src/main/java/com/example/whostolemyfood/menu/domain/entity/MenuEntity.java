package com.example.whostolemyfood.menu.domain.entity;

import com.example.whostolemyfood.global.entity.BaseSoftDeleteEntity;
import com.example.whostolemyfood.store.domain.entity.StoreEntity;
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
@SQLDelete(sql = "UPDATE p_menus SET is_deleted = true WHERE menu_id = ?")
@Table(name = "p_menus")
public class MenuEntity extends BaseSoftDeleteEntity {
    @Id
    @GeneratedValue
    @Column(name = "menu_id", nullable = false, updatable = false)
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false, updatable = false)
    private StoreEntity store;

    // ai_log_id

    @Column(name = "menuName")
    private String name;
    @Column(name = "menuPrice")
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
}
