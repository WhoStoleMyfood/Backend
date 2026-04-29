package com.example.whostolemyfood.area.domain.repository;

import com.example.whostolemyfood.area.domain.entity.AreaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AreaRepository extends JpaRepository<AreaEntity, UUID> {

	boolean existsByUkName(String ukName);

	Optional<AreaEntity> findByAreaId(UUID areaId);

	List<AreaEntity> findAllByIsActiveTrue();

	@Query("""
        select a
        from AreaEntity a
        where (:city is null or a.city = :city)
          and (:district is null or a.district = :district)
          and (:isActive is null or a.isActive = :isActive)
    """)
	Page<AreaEntity> searchAreas(
		@Param("city") String city,
		@Param("district") String district,
		@Param("isActive") Boolean isActive,
		Pageable pageable
	);
}