package com.example.whostolemyfood.address.domain.entity;

import com.example.whostolemyfood.global.entity.BaseSoftDeleteEntity;
import com.example.whostolemyfood.user.domain.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@Table(name = "p_address")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AddressEntity extends BaseSoftDeleteEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "address_id", nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;

	@Column(name = "alias", length = 50)
	private String alias;

	@Column(name = "address", nullable = false, length = 255)
	private String address;

	@Column(name = "detail", length = 255)
	private String detail;

	@Column(name = "zip_code", length = 20)
	private String zipCode;

	@Column(name = "is_default", nullable = false)
	private Boolean isDefault = false;

	public AddressEntity(UserEntity user, String alias, String address, String detail, String zipCode) {
		this.user = user;
		this.alias = alias;
		this.address = address;
		this.detail = detail;
		this.zipCode = zipCode;
		this.isDefault = false;
	}
}