package com.example.whostolemyfood.area.domain.repository;

import com.example.whostolemyfood.area.domain.entity.AreaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnableJpaRepositories(basePackageClasses = AreaRepository.class)
@EntityScan(basePackageClasses = AreaEntity.class)
class AreaRepositoryTest {

	@Autowired
	private AreaRepository areaRepository;

	@Test
	@DisplayName("지역 저장 성공")
	void saveArea_success() {
		AreaEntity area = AreaEntity.builder()
			.ukName("광화문")
			.city("서울특별시")
			.district("종로구")
			.isActive(true)
			.build();

		AreaEntity saved = areaRepository.save(area);

		assertThat(saved.getAreaId()).isNotNull();
		assertThat(saved.getUkName()).isEqualTo("광화문");
		assertThat(saved.getCity()).isEqualTo("서울특별시");
		assertThat(saved.getDistrict()).isEqualTo("종로구");
		assertThat(saved.getIsActive()).isTrue();
	}

	@Test
	@DisplayName("지역명 중복 체크")
	void existsByUkName_success() {
		areaRepository.save(
			AreaEntity.builder()
				.ukName("광화문")
				.city("서울특별시")
				.district("종로구")
				.isActive(true)
				.build()
		);

		boolean exists = areaRepository.existsByUkName("광화문");

		assertThat(exists).isTrue();
	}

	@Test
	@DisplayName("areaId로 지역 단건 조회 성공")
	void findByAreaId_success() {
		AreaEntity saved = areaRepository.save(
			AreaEntity.builder()
				.ukName("광화문")
				.city("서울특별시")
				.district("종로구")
				.isActive(true)
				.build()
		);

		var result = areaRepository.findByAreaId(saved.getAreaId());

		assertThat(result).isPresent();
		assertThat(result.get().getUkName()).isEqualTo("광화문");
	}

	@Test
	@DisplayName("활성 지역만 조회")
	void findAllByIsActiveTrue_success() {
		areaRepository.save(
			AreaEntity.builder()
				.ukName("광화문")
				.city("서울특별시")
				.district("종로구")
				.isActive(true)
				.build()
		);

		areaRepository.save(
			AreaEntity.builder()
				.ukName("강남")
				.city("서울특별시")
				.district("강남구")
				.isActive(false)
				.build()
		);

		List<AreaEntity> result = areaRepository.findAllByIsActiveTrue();

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getUkName()).isEqualTo("광화문");
	}

	@Test
	@DisplayName("지역 검색 성공 - city, district, isActive 조건")
	void searchAreas_success() {
		areaRepository.save(
			AreaEntity.builder()
				.ukName("광화문")
				.city("서울특별시")
				.district("종로구")
				.isActive(true)
				.build()
		);

		areaRepository.save(
			AreaEntity.builder()
				.ukName("강남")
				.city("서울특별시")
				.district("강남구")
				.isActive(true)
				.build()
		);

		Pageable pageable = PageRequest.of(
			0,
			10,
			Sort.by(Sort.Direction.DESC, "createdAt")
		);

		Page<AreaEntity> result = areaRepository.searchAreas(
			"서울특별시",
			"종로구",
			true,
			pageable
		);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getUkName()).isEqualTo("광화문");
	}

	@Test
	@DisplayName("지역 검색 성공 - 조건 없이 전체 조회")
	void searchAreas_withoutCondition_success() {
		areaRepository.save(
			AreaEntity.builder()
				.ukName("광화문")
				.city("서울특별시")
				.district("종로구")
				.isActive(true)
				.build()
		);

		areaRepository.save(
			AreaEntity.builder()
				.ukName("강남")
				.city("서울특별시")
				.district("강남구")
				.isActive(true)
				.build()
		);

		Pageable pageable = PageRequest.of(0, 10);

		Page<AreaEntity> result = areaRepository.searchAreas(
			null,
			null,
			null,
			pageable
		);

		assertThat(result.getContent()).hasSize(2);
		assertThat(result.getTotalElements()).isEqualTo(2);
	}

	@Test
	@DisplayName("지역 검색 페이징 성공")
	void searchAreas_paging_success() {
		for (int i = 1; i <= 15; i++) {
			areaRepository.save(
				AreaEntity.builder()
					.ukName("지역" + i)
					.city("서울특별시")
					.district("종로구")
					.isActive(true)
					.build()
			);
		}

		Pageable pageable = PageRequest.of(
			0,
			10,
			Sort.by(Sort.Direction.DESC, "createdAt")
		);

		Page<AreaEntity> result = areaRepository.searchAreas(
			"서울특별시",
			"종로구",
			true,
			pageable
		);

		assertThat(result.getContent()).hasSize(10);
		assertThat(result.getTotalElements()).isEqualTo(15);
		assertThat(result.getTotalPages()).isEqualTo(2);
	}
}