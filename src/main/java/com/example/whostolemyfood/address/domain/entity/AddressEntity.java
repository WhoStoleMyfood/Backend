package com.example.whostolemyfood.address.domain.entity;

import com.example.whostolemyfood.global.entity.BaseSoftDeleteEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "p_address")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AddressEntity extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "address_id", nullable = false, updatable = false)
    private UUID id;

    // OrderEntity와 동일하게 Entity가 아닌 UUID로 관리
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "alias", length = 50)
    private String alias;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "detail", length = 255)
    private String detail;

    @Column(name = "zip_code", length = 255)
    private String zipCode;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    public void updateAddress(String alias, String address, String detail, String zipCode, Boolean isDefault) {
        this.alias = alias;
        this.address = address;
        this.detail = detail;
        this.zipCode = zipCode;
        if (isDefault != null) {
            this.isDefault = isDefault;
        }
    }

    public void setDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public void markAsDeleted(UUID deletedBy) {
        this.isDeleted = true;
        super.delete(deletedBy);
    }
}
