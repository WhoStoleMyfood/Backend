package com.example.whostolemyfood.area.infrastructure.initializer;

import com.example.whostolemyfood.area.domain.entity.AreaEntity;
import com.example.whostolemyfood.area.domain.repository.AreaRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * area 초기값으로 광화문 자동 삽입
 */
@Component
@RequiredArgsConstructor
public class AreaInitializer {

	private final AreaRepository areaRepository;

	@PostConstruct
	@Transactional
	public void init() {
		if (!areaRepository.existsByUkName("광화문")) {
			AreaEntity area = AreaEntity.builder()
				.ukName("광화문")
				.city("서울특별시")
				.district("종로구")
				.isActive(true)
				.build();

			areaRepository.save(area);
		}
	}
}