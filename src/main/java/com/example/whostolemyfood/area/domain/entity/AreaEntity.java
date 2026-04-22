package com.example.whostolemyfood.area.domain.entity;

import com.example.whostolemyfood.global.entity.BaseSoftDeleteEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "p_areas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE p_areas SET is_deleted = true, deleted_at = now() WHERE area_id = ?")
@SQLRestriction("is_deleted = false")
public class AreaEntity extends BaseSoftDeleteEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "area_id", nullable = false, updatable = false)
	private UUID areaId;

	@Column(name = "uk_name", nullable = false, unique = true, length = 100)
	private String ukName;

	@Column(name = "city", nullable = false, length = 50)
	private String city;

	@Column(name = "district", nullable = false, length = 50)
	private String district;

	@Column(name = "is_active", nullable = false)
	@Builder.Default
	private Boolean isActive = true;

	public void update(String ukName, String city, String district) {
		this.ukName = ukName;
		this.city = city;
		this.district = district;
	}

	public void updateActive(Boolean isActive) {
		this.isActive = isActive;
	}
}