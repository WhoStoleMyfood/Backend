package com.example.whostolemyfood.user.domain.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.*;

import com.example.whostolemyfood.address.domain.entity.AddressEntity;
import com.example.whostolemyfood.global.entity.BaseAuditEntity;
import com.example.whostolemyfood.user.presentation.dto.request.ReqUpdateUserDtoV1;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "authority", nullable = false)
    private UserRole userRole;

    @Column(name = "user_email", nullable = false, length = 255, unique = true)
    private String userEmail;

    @Column(name = "user_password", nullable = false, length = 255)
    private String userPassword;

    @Column(name = "user_name", nullable = false, length = 255)
    private String userName;

    @Column(name = "address", length = 255)
    private String address;

    /**
     * 리팩토링 핵심 포인트:
     * 1. @Column 제거: 이 필드 때문에 p_users에 컬럼이 생기지 않게 합니다.
     * 2. @JoinColumn: p_address 테이블에 있는 'user_id' 외래키를 연결 고리로 사용합니다.
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    private List<AddressEntity> addresses = new ArrayList<>();

    @Builder
    public UserEntity(UserRole role, String email, String password, String name, String address) {
        this.userRole = role;
        this.userEmail = email;
        this.userPassword = password;
        this.userName = name;
        this.address = address;
    }

    /**
     * 연관관계 편의 메서드
     * 이제 AddressEntity에 User객체가 없어도 이 메서드로 리스트 관리가 가능합니다.
     */
    public void addAddress(AddressEntity address) {
        this.addresses.add(address);
    }

    // 회원정보 수정 메서드
    public void updateUserInfo(ReqUpdateUserDtoV1 dto) {
        if (dto.getName() != null && !dto.getName().isBlank()) {
            this.userName = dto.getName();
        }
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            this.userPassword = dto.getPassword();
        }
        if (dto.getAddress() != null && !dto.getAddress().isBlank()) {
            this.address = dto.getAddress();
        }
    }
}