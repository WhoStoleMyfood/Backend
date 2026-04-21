package com.example.whostolemyfood.area.domain.entity;

import com.example.whostolemyfood.global.entity.BaseSoftDeleteEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@Table(name = "p_areas")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AreaEntity extends BaseSoftDeleteEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "area_id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "uk_name", nullable = false, length = 100)
	private String ukName;

	@Column(name = "city", nullable = false, length = 50)
	private String city;

	@Column(name = "district", nullable = false, length = 50)
	private String district;

	@Column(name = "is_active", nullable = false)
	private Boolean isActive = true;

	public AreaEntity(String ukName, String city, String district) {
		this.ukName = ukName;
		this.city = city;
		this.district = district;
		this.isActive = true;
	}
}