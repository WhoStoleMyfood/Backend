package com.example.whostolemyfood.menu;

import com.example.whostolemyfood.global.config.security.jwt.JwtUtil;
import com.example.whostolemyfood.global.exception.GlobalExceptionHandler;
import com.example.whostolemyfood.menu.application.service.MenuServiceV1;
import com.example.whostolemyfood.menu.presentation.controller.MenuControllerV1;
import com.example.whostolemyfood.menu.presentation.dto.response.ResCreateMenuDtoV1;
import com.example.whostolemyfood.menu.presentation.dto.response.ResGetMenuDtoV1;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MenuControllerV1.class)
@EnableMethodSecurity
@Import(GlobalExceptionHandler.class)
class MenuControllerV1Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MenuServiceV1 menuServiceV1;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private RedisTemplate<String, Object> redisTemplate;

    private static final UUID STORE_ID = UUID.randomUUID();
    private static final UUID MENU_ID = UUID.randomUUID();

    @Test
    @WithMockUser(roles = "OWNER")
    @DisplayName("[Controller] 메뉴 생성 성공 - OWNER 권한")
    void addMenu_success() throws Exception {
        ResCreateMenuDtoV1 response = ResCreateMenuDtoV1.builder()
                .menuId(MENU_ID)
                .build();

        given(menuServiceV1.addMenu(any(), any(), any())).willReturn(response);

        mockMvc.perform(post("/api/v1/stores/{storeId}/menus", STORE_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "후라이드 치킨",
                                  "price": 20000,
                                  "description": "바삭한 치킨",
                                  "aiDescription": false,
                                  "aiPrompt": ""
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.menuId").value(MENU_ID.toString()));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("[Controller] 메뉴 생성 실패 - 권한 부족")
    void addMenu_fail_forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/stores/{storeId}/menus", STORE_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "후라이드 치킨",
                                  "price": 20000,
                                  "description": "바삭한 치킨",
                                  "aiDescription": false,
                                  "aiPrompt": ""
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    @DisplayName("[Controller] 메뉴 단건 조회 성공")
    void getMenu_success() throws Exception {
        ResGetMenuDtoV1 response = ResGetMenuDtoV1.builder()
                .menuId(MENU_ID)
                .name("후라이드 치킨")
                .price(20000)
                .description("바삭한 치킨")
                .build();

        given(menuServiceV1.getMenu(STORE_ID, MENU_ID)).willReturn(response);

        mockMvc.perform(get("/api/v1/stores/{storeId}/menus/{menuId}", STORE_ID, MENU_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.menuId").value(MENU_ID.toString()))
                .andExpect(jsonPath("$.name").value("후라이드 치킨"))
                .andExpect(jsonPath("$.price").value(20000));
    }

    @Test
    @WithMockUser
    @DisplayName("[Controller] 메뉴 목록 조회 성공")
    void getMenus_success() throws Exception {
        given(menuServiceV1.getMenus(any())).willReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/v1/stores/{storeId}/menus", STORE_ID))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "OWNER")
    @DisplayName("[Controller] 메뉴 수정 성공")
    void updateMenu_success() throws Exception {
        ResGetMenuDtoV1 response = ResGetMenuDtoV1.builder()
                .menuId(MENU_ID)
                .name("양념 치킨")
                .price(21000)
                .description("매콤한 치킨")
                .build();

        given(menuServiceV1.updateMenu(any(), any(), any(), any())).willReturn(response);

        mockMvc.perform(put("/api/v1/stores/{storeId}/menus/{menuId}", STORE_ID, MENU_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "양념 치킨",
                                  "price": 21000,
                                  "description": "매콤한 치킨"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("양념 치킨"))
                .andExpect(jsonPath("$.price").value(21000));
    }

    @Test
    @WithMockUser(roles = "OWNER")
    @DisplayName("[Controller] 메뉴 숨김/해제 성공")
    void hideMenu_success() throws Exception {
        doNothing().when(menuServiceV1).hiddenMenu(any(), any(), any());

        mockMvc.perform(patch("/api/v1/stores/{storeId}/menus/{menuId}/hide", STORE_ID, MENU_ID)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MASTER")
    @DisplayName("[Controller] 비활성 메뉴 조회 성공")
    void getInactiveMenus_success() throws Exception {
        given(menuServiceV1.getInActiveMenus(any(), any(), any()))
                .willReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/v1/stores/{storeId}/menus/inactive", STORE_ID))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "OWNER")
    @DisplayName("[Controller] 메뉴 삭제 성공")
    void deleteMenu_success() throws Exception {
        doNothing().when(menuServiceV1).deleteMenu(any(), any(), any());

        mockMvc.perform(delete("/api/v1/stores/{storeId}/menus/{menuId}", STORE_ID, MENU_ID)
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}