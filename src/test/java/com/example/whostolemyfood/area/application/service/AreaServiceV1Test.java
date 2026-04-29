package com.example.whostolemyfood.area.application.service;

import com.example.whostolemyfood.area.application.service.AreaServiceV1;
import com.example.whostolemyfood.area.domain.entity.AreaEntity;
import com.example.whostolemyfood.area.domain.repository.AreaRepository;
import com.example.whostolemyfood.area.presentation.dto.request.ReqCreateAreaDtoV1;
import com.example.whostolemyfood.area.presentation.dto.request.ReqUpdateAreaDtoV1;
import com.example.whostolemyfood.global.exception.CustomException;
import com.example.whostolemyfood.global.exception.ErrorCode;
import com.example.whostolemyfood.user.application.security.AuthUser;
import com.example.whostolemyfood.user.domain.entity.UserEntity;
import com.example.whostolemyfood.user.domain.entity.UserRole;
import com.example.whostolemyfood.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AreaServiceV1Test {

	@Mock
	private AreaRepository areaRepository;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private AreaServiceV1 areaServiceV1;

	private final UUID managerId = UUID.randomUUID();
	private final UUID areaId = UUID.randomUUID();

	@Test
	@DisplayName("MANAGER는 지역을 생성할 수 있다")
	void createArea_success() {
		// given
		AuthUser loginUser = new AuthUser(managerId, "manager@test.com", UserRole.MANAGER);

		ReqCreateAreaDtoV1 reqDto = mock(ReqCreateAreaDtoV1.class);
		when(reqDto.getUkName()).thenReturn("광화문");
		when(reqDto.getCity()).thenReturn("서울특별시");
		when(reqDto.getDistrict()).thenReturn("종로구");

		UserEntity manager = UserEntity.builder()
			.role(UserRole.MANAGER)
			.email("manager@test.com")
			.password("password")
			.name("매니저")
			.build();

		AreaEntity savedArea = AreaEntity.builder()
			.areaId(areaId)
			.ukName("광화문")
			.city("서울특별시")
			.district("종로구")
			.isActive(true)
			.build();

		when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
		when(areaRepository.existsByUkName("광화문")).thenReturn(false);
		when(areaRepository.save(any(AreaEntity.class))).thenReturn(savedArea);

		// when
		var result = areaServiceV1.createArea(loginUser, reqDto);

		// then
		assertThat(result.getAreaId()).isEqualTo(areaId);
		assertThat(result.getUkName()).isEqualTo("광화문");
		assertThat(result.getCity()).isEqualTo("서울특별시");
		assertThat(result.getDistrict()).isEqualTo("종로구");
		assertThat(result.getIsActive()).isTrue();

		verify(areaRepository).save(any(AreaEntity.class));
	}

	@Test
	@DisplayName("중복된 지역명은 생성할 수 없다")
	void createArea_duplicateName_fail() {
		// given
		AuthUser loginUser = new AuthUser(managerId, "manager@test.com", UserRole.MANAGER);

		ReqCreateAreaDtoV1 reqDto = mock(ReqCreateAreaDtoV1.class);
		when(reqDto.getUkName()).thenReturn("광화문");

		UserEntity manager = UserEntity.builder()
			.role(UserRole.MANAGER)
			.email("manager@test.com")
			.password("password")
			.name("매니저")
			.build();

		when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
		when(areaRepository.existsByUkName("광화문")).thenReturn(true);

		// when & then
		assertThatThrownBy(() -> areaServiceV1.createArea(loginUser, reqDto))
			.isInstanceOf(CustomException.class)
			.hasMessage(ErrorCode.AREA_DUPLICATION.getMessage());

		verify(areaRepository, never()).save(any());
	}

	@Test
	@DisplayName("CUSTOMER는 지역을 생성할 수 없다")
	void createArea_customer_fail() {
		// given
		AuthUser loginUser = new AuthUser(managerId, "customer@test.com", UserRole.CUSTOMER);

		ReqCreateAreaDtoV1 reqDto = mock(ReqCreateAreaDtoV1.class);

		UserEntity customer = UserEntity.builder()
			.role(UserRole.CUSTOMER)
			.email("customer@test.com")
			.password("password")
			.name("고객")
			.build();

		when(userRepository.findById(managerId)).thenReturn(Optional.of(customer));

		// when & then
		assertThatThrownBy(() -> areaServiceV1.createArea(loginUser, reqDto))
			.isInstanceOf(CustomException.class)
			.hasMessage(ErrorCode.AREA_ACCESS_DENIED.getMessage());

		verify(areaRepository, never()).save(any());
	}

	@Test
	@DisplayName("토큰 role과 DB role이 다르면 지역 생성이 거부된다")
	void createArea_roleMismatch_fail() {
		// given
		AuthUser loginUser = new AuthUser(managerId, "manager@test.com", UserRole.MANAGER);

		ReqCreateAreaDtoV1 reqDto = mock(ReqCreateAreaDtoV1.class);

		UserEntity dbUser = UserEntity.builder()
			.role(UserRole.CUSTOMER)
			.email("manager@test.com")
			.password("password")
			.name("권한변경된유저")
			.build();

		when(userRepository.findById(managerId)).thenReturn(Optional.of(dbUser));

		// when & then
		assertThatThrownBy(() -> areaServiceV1.createArea(loginUser, reqDto))
			.isInstanceOf(CustomException.class)
			.hasMessage(ErrorCode.AREA_ACCESS_DENIED.getMessage());

		verify(areaRepository, never()).save(any());
	}

	@Test
	@DisplayName("지역 단건 조회 성공")
	void getArea_success() {
		// given
		AreaEntity area = AreaEntity.builder()
			.areaId(areaId)
			.ukName("광화문")
			.city("서울특별시")
			.district("종로구")
			.isActive(true)
			.build();

		when(areaRepository.findByAreaId(areaId)).thenReturn(Optional.of(area));

		// when
		var result = areaServiceV1.getArea(areaId);

		// then
		assertThat(result.getAreaId()).isEqualTo(areaId);
		assertThat(result.getUkName()).isEqualTo("광화문");
	}

	@Test
	@DisplayName("존재하지 않는 지역 조회 시 예외 발생")
	void getArea_notFound_fail() {
		// given
		when(areaRepository.findByAreaId(areaId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> areaServiceV1.getArea(areaId))
			.isInstanceOf(CustomException.class)
			.hasMessage(ErrorCode.AREA_NOT_FOUND.getMessage());
	}

	@Test
	@DisplayName("지역 수정 성공")
	void updateArea_success() {
		// given
		AuthUser loginUser = new AuthUser(managerId, "manager@test.com", UserRole.MANAGER);

		ReqUpdateAreaDtoV1 reqDto = mock(ReqUpdateAreaDtoV1.class);
		when(reqDto.getUkName()).thenReturn("종로");
		when(reqDto.getCity()).thenReturn("서울특별시");
		when(reqDto.getDistrict()).thenReturn("종로구");

		UserEntity manager = UserEntity.builder()
			.role(UserRole.MANAGER)
			.email("manager@test.com")
			.password("password")
			.name("매니저")
			.build();

		AreaEntity area = AreaEntity.builder()
			.areaId(areaId)
			.ukName("광화문")
			.city("서울특별시")
			.district("종로구")
			.isActive(true)
			.build();

		when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
		when(areaRepository.findByAreaId(areaId)).thenReturn(Optional.of(area));
		when(areaRepository.existsByUkName("종로")).thenReturn(false);

		// when
		var result = areaServiceV1.updateArea(loginUser, areaId, reqDto);

		// then
		assertThat(result.getUkName()).isEqualTo("종로");
		assertThat(result.getCity()).isEqualTo("서울특별시");
		assertThat(result.getDistrict()).isEqualTo("종로구");
	}

	@Test
	@DisplayName("지역 활성화 상태 변경 성공")
	void updateAreaActive_success() {
		// given
		AuthUser loginUser = new AuthUser(managerId, "manager@test.com", UserRole.MANAGER);

		UserEntity manager = UserEntity.builder()
			.role(UserRole.MANAGER)
			.email("manager@test.com")
			.password("password")
			.name("매니저")
			.build();

		AreaEntity area = AreaEntity.builder()
			.areaId(areaId)
			.ukName("광화문")
			.city("서울특별시")
			.district("종로구")
			.isActive(true)
			.build();

		when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
		when(areaRepository.findByAreaId(areaId)).thenReturn(Optional.of(area));

		// when
		var result = areaServiceV1.updateAreaActive(loginUser, areaId, false);

		// then
		assertThat(result.getIsActive()).isFalse();
	}

	@Test
	@DisplayName("지역 삭제 성공")
	void deleteArea_success() {
		// given
		AuthUser loginUser = new AuthUser(managerId, "manager@test.com", UserRole.MANAGER);

		UserEntity manager = UserEntity.builder()
			.role(UserRole.MANAGER)
			.email("manager@test.com")
			.password("password")
			.name("매니저")
			.build();

		AreaEntity area = AreaEntity.builder()
			.areaId(areaId)
			.ukName("광화문")
			.city("서울특별시")
			.district("종로구")
			.isActive(true)
			.build();

		when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
		when(areaRepository.findByAreaId(areaId)).thenReturn(Optional.of(area));

		// when
		areaServiceV1.deleteArea(loginUser, areaId);

		// then
		assertThat(area.getIsDeleted()).isTrue();
		assertThat(area.getDeletedAt()).isNotNull();
	}

	@Test
	@DisplayName("지역 검색 성공 - 기본 생성일 내림차순, size 10")
	void searchAreas_success() {
		// given
		AreaEntity area = AreaEntity.builder()
			.areaId(areaId)
			.ukName("광화문")
			.city("서울특별시")
			.district("종로구")
			.isActive(true)
			.build();

		Page<AreaEntity> pageResult = new PageImpl<>(List.of(area));

		when(areaRepository.searchAreas(
			eq("서울특별시"),
			eq("종로구"),
			eq(true),
			any(Pageable.class)
		)).thenReturn(pageResult);

		// when
		var result = areaServiceV1.searchAreas(
			"서울특별시",
			"종로구",
			true,
			0,
			10,
			"desc"
		);

		// then
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getUkName()).isEqualTo("광화문");
	}

	@Test
	@DisplayName("검색 size가 10, 30, 50이 아니면 10으로 보정된다")
	void searchAreas_invalidSize_toDefault10() {
		// given
		Page<AreaEntity> pageResult = new PageImpl<>(List.of());

		when(areaRepository.searchAreas(
			isNull(),
			isNull(),
			isNull(),
			any(Pageable.class)
		)).thenReturn(pageResult);

		// when
		areaServiceV1.searchAreas(null, null, null, 0, 20, "desc");

		// then
		verify(areaRepository).searchAreas(
			isNull(),
			isNull(),
			isNull(),
			argThat(pageable -> pageable.getPageSize() == 10)
		);
	}
}