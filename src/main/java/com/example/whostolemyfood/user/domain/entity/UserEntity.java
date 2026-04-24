package com.example.whostolemyfood.user.domain.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.*;

import com.example.whostolemyfood.address.domain.entity.AddressEntity;
import com.example.whostolemyfood.global.entity.BaseAuditEntity;

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

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AddressEntity> addresses = new ArrayList<>();

    @Builder
    public UserEntity(UserRole role, String email, String password, String name) {
        this.userRole = role;
        this.userEmail = email;
        this.userPassword = password;
        this.userName = name;
    }

    /**
     * 연관관계 편의 메서드
     * 유저 객체에 주소를 추가할 때, 주소 객체에도 유저를 자동으로 연결해줍니다.
     */
    public void addAddress(AddressEntity address) {
        this.addresses.add(address);
        // AddressEntity 측에도 유저 정보를 세팅해줘야 양방향 정합성이 맞습니다.
        // 때문에 AddressEntity에 setUser(this) 같은 메서드가 필요합니다.
    }

    // 회원정보 수정 메서드
    public void updateUserInfo(String name, String password) {
        if (name != null && !name.isBlank()) {
            this.userName = name;
        }
        // 비밀번호는 수정용 데이터가 들어왔을 때만 변경하도록 방어 로직 추가
        if (password != null && !password.isBlank()) {
            this.userPassword = password;
        }
    }
}
