package com.example.whostolemyfood.store;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import com.example.whostolemyfood.global.config.security.jwt.JwtUtil;
import com.example.whostolemyfood.store.application.service.StoreSearchServiceV1;
import com.example.whostolemyfood.store.application.service.StoreServiceV1;
import com.example.whostolemyfood.store.presentation.controller.StoreControllerV1;
import com.example.whostolemyfood.store.presentation.dto.response.ResCreateStoreDtoV1;
import com.example.whostolemyfood.store.presentation.dto.response.ResGetStoreDtoV1;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StoreControllerV1.class)
@EnableMethodSecurity
public class StoreControllerV1Test {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StoreServiceV1 storeServiceV1;

    @MockitoBean
    private StoreSearchServiceV1 sStoreSearchServiceV1;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "OWNER")
    @DisplayName("[Controller] 가게 생성 성공 - OWNER 권한")
    void createStore_Success() throws Exception {
        ResCreateStoreDtoV1 response = ResCreateStoreDtoV1.builder()
                .storeId(UUID.randomUUID())
                .name("테스트 가게")
                .build();

        given(storeServiceV1.createStore(any(), any())).willReturn(response);

        mockMvc.perform(post("/api/v1/stores")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"name\":\"테스트 가게\", \"address\":\"주소\", \"minOrderPrice\":10000, \"openTime\": \"10:00\",\"closeTime\": \"23:00\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("테스트 가게"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("[Controller] 가게 생성 실패 - 권한 부족(CUSTOMER)")
    void createStore_Fail_Forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/stores")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"name\":\"테스트 가게\", \"address\":\"주소\", \"minOrderPrice\":10000, \"openTime\": \"10:00\",\"closeTime\": \"23:00\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    @DisplayName("[Controller] 가게 단건 조회 성공 - 권한 무관")
    void getStore_Success() throws Exception {
        UUID storeId = UUID.randomUUID();
        given(storeServiceV1.getStore(storeId)).willReturn(ResGetStoreDtoV1.builder().name("조회 성공").build());

        mockMvc.perform(get("/api/v1/stores/{storeId}", storeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("조회 성공"));
    }

    @Test
    @WithMockUser(roles = "MASTER")
    @DisplayName("[Controller] 비활성 가게 조회 - MASTER 권한")
    void getInactiveStores_Success() throws Exception {
        given(storeServiceV1.getInActiveStores(any(), any())).willReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/v1/stores/inactive"))
                .andExpect(status().isOk());
    }
}