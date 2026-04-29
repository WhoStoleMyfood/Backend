package com.example.whostolemyfood.area.presentation.controller;

import com.example.whostolemyfood.area.application.service.AreaServiceV1;
import com.example.whostolemyfood.area.presentation.dto.response.ResCreateAreaDtoV1;
import com.example.whostolemyfood.area.presentation.dto.response.ResGetAreaDtoV1;
import com.example.whostolemyfood.global.config.security.SecurityConfig;
import com.example.whostolemyfood.global.config.security.jwt.JwtAuthenticationFilter;
import com.example.whostolemyfood.global.config.security.jwt.JwtUtil;
import com.example.whostolemyfood.user.application.security.AuthUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
	controllers = AreaControllerV1.class,
	excludeAutoConfiguration = {
		SecurityAutoConfiguration.class,
		SecurityFilterAutoConfiguration.class
	},
	excludeFilters = {
		@ComponentScan.Filter(
			type = FilterType.ASSIGNABLE_TYPE,
			classes = {
				SecurityConfig.class,
				JwtAuthenticationFilter.class
			}
		)
	}
)
@AutoConfigureMockMvc(addFilters = false)
class AreaControllerV1Test {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AreaServiceV1 areaServiceV1;

	@MockitoBean
	private JwtUtil jwtUtil;

	private final UUID areaId = UUID.randomUUID();

	@Test
	@DisplayName("지역 생성 성공")
	void createArea_success() throws Exception {
		ResCreateAreaDtoV1 response = ResCreateAreaDtoV1.builder()
			.areaId(areaId)
			.ukName("광화문")
			.city("서울특별시")
			.district("종로구")
			.isActive(true)
			.build();

		when(areaServiceV1.createArea(nullable(AuthUser.class), any()))
			.thenReturn(response);

		String body = """
                {
                  "ukName": "광화문",
                  "city": "서울특별시",
                  "district": "종로구"
                }
                """;

		mockMvc.perform(post("/api/v1/areas")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.areaId").value(areaId.toString()))
			.andExpect(jsonPath("$.ukName").value("광화문"))
			.andExpect(jsonPath("$.city").value("서울특별시"))
			.andExpect(jsonPath("$.district").value("종로구"))
			.andExpect(jsonPath("$.isActive").value(true));
	}

	@Test
	@DisplayName("지역 전체 조회 성공")
	void getAllAreas_success() throws Exception {
		ResGetAreaDtoV1 area = ResGetAreaDtoV1.builder()
			.areaId(areaId)
			.ukName("광화문")
			.city("서울특별시")
			.district("종로구")
			.isActive(true)
			.build();

		when(areaServiceV1.getAllAreas()).thenReturn(List.of(area));

		mockMvc.perform(get("/api/v1/areas"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].areaId").value(areaId.toString()))
			.andExpect(jsonPath("$[0].ukName").value("광화문"));
	}

	@Test
	@DisplayName("활성 지역 조회 성공")
	void getActiveAreas_success() throws Exception {
		ResGetAreaDtoV1 area = ResGetAreaDtoV1.builder()
			.areaId(areaId)
			.ukName("광화문")
			.city("서울특별시")
			.district("종로구")
			.isActive(true)
			.build();

		when(areaServiceV1.getActiveAreas()).thenReturn(List.of(area));

		mockMvc.perform(get("/api/v1/areas/active"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].areaId").value(areaId.toString()))
			.andExpect(jsonPath("$[0].ukName").value("광화문"))
			.andExpect(jsonPath("$[0].isActive").value(true));
	}

	@Test
	@DisplayName("지역 단건 조회 성공")
	void getArea_success() throws Exception {
		ResGetAreaDtoV1 area = ResGetAreaDtoV1.builder()
			.areaId(areaId)
			.ukName("광화문")
			.city("서울특별시")
			.district("종로구")
			.isActive(true)
			.build();

		when(areaServiceV1.getArea(areaId)).thenReturn(area);

		mockMvc.perform(get("/api/v1/areas/{areaId}", areaId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.areaId").value(areaId.toString()))
			.andExpect(jsonPath("$.ukName").value("광화문"));
	}

	@Test
	@DisplayName("지역 검색 성공")
	void searchAreas_success() throws Exception {
		ResGetAreaDtoV1 area = ResGetAreaDtoV1.builder()
			.areaId(areaId)
			.ukName("광화문")
			.city("서울특별시")
			.district("종로구")
			.isActive(true)
			.build();

		Page<ResGetAreaDtoV1> page = new PageImpl<>(
			List.of(area),
			PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")),
			1
		);

		when(areaServiceV1.searchAreas(
			eq("서울특별시"),
			eq("종로구"),
			eq(true),
			eq(0),
			eq(10),
			eq("desc")
		)).thenReturn(page);

		mockMvc.perform(get("/api/v1/areas/search")
				.param("city", "서울특별시")
				.param("district", "종로구")
				.param("isActive", "true")
				.param("page", "0")
				.param("size", "10")
				.param("sortDir", "desc"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].areaId").value(areaId.toString()))
			.andExpect(jsonPath("$.content[0].ukName").value("광화문"))
			.andExpect(jsonPath("$.content[0].city").value("서울특별시"))
			.andExpect(jsonPath("$.content[0].district").value("종로구"))
			.andExpect(jsonPath("$.totalElements").value(1));
	}

	@Test
	@DisplayName("지역 수정 성공")
	void updateArea_success() throws Exception {
		ResGetAreaDtoV1 response = ResGetAreaDtoV1.builder()
			.areaId(areaId)
			.ukName("종로")
			.city("서울특별시")
			.district("종로구")
			.isActive(true)
			.build();

		when(areaServiceV1.updateArea(nullable(AuthUser.class), eq(areaId), any()))
			.thenReturn(response);

		String body = """
                {
                  "ukName": "종로",
                  "city": "서울특별시",
                  "district": "종로구"
                }
                """;

		mockMvc.perform(put("/api/v1/areas/{areaId}", areaId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.areaId").value(areaId.toString()))
			.andExpect(jsonPath("$.ukName").value("종로"));
	}

	@Test
	@DisplayName("지역 활성화 상태 변경 성공")
	void updateAreaActive_success() throws Exception {
		ResGetAreaDtoV1 response = ResGetAreaDtoV1.builder()
			.areaId(areaId)
			.ukName("광화문")
			.city("서울특별시")
			.district("종로구")
			.isActive(false)
			.build();

		when(areaServiceV1.updateAreaActive(nullable(AuthUser.class), eq(areaId), eq(false)))
			.thenReturn(response);

		mockMvc.perform(patch("/api/v1/areas/{areaId}/active", areaId)
				.param("isActive", "false"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.areaId").value(areaId.toString()))
			.andExpect(jsonPath("$.isActive").value(false));
	}

	@Test
	@DisplayName("지역 삭제 성공")
	void deleteArea_success() throws Exception {
		mockMvc.perform(delete("/api/v1/areas/{areaId}", areaId))
			.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("지역 생성 DTO 유효성 검사 실패")
	void createArea_validation_fail() throws Exception {
		String body = """
                {
                  "ukName": "",
                  "city": "",
                  "district": ""
                }
                """;

		mockMvc.perform(post("/api/v1/areas")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isBadRequest());
	}
}