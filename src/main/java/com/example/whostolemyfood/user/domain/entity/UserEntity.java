package com.example.whostolemyfood.user.domain.entity;

import java.util.UUID;
import jakarta.persistence.*;

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

//    @Enumerated(EnumType.STRING)
//    @Column(name = "status", nullable = false)
//    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "user_email", nullable = false, length = 255, unique = true)
    private String userEmail;

    @Column(name = "user_password", nullable = false, length = 255)
    private String userPassword;

    @Column(name = "user_name", nullable = false, length = 255)
    private String userName;

    @Column(name = "address", length = 255)
    private String address;

    @Builder
    public UserEntity(UserRole role, String email, String password, String name, String address) {
        this.userRole = role;
        this.userEmail = email;
        this.userPassword = password;
        this.userName = name;
        this.address = address;
//        this.status = UserStatus.ACTIVE;
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

    public void updateStatus(UserStatus status) {
//        this.status = status;
    }
}
