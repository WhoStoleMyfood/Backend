package com.example.whostolemyfood.address;

import com.example.whostolemyfood.address.application.service.AddressServiceV1;
import com.example.whostolemyfood.address.presentation.controller.AddressControllerV1;
import com.example.whostolemyfood.address.presentation.dto.request.ReqCreateAddressDtoV1;
import com.example.whostolemyfood.address.presentation.dto.request.ReqUpdateAddressDtoV1;
import com.example.whostolemyfood.address.presentation.dto.response.ResCreateAddressDtoV1;
import com.example.whostolemyfood.address.presentation.dto.response.ResGetAddressDtoV1;
import com.example.whostolemyfood.global.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AddressControllerV1.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
public class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AddressServiceV1 addressService;

    @Test
    @DisplayName("[성공] 배송지 생성 API 호출 시 200 OK를 반환해야 함")
    void createAddressApiTest() throws Exception {
        ReqCreateAddressDtoV1 request = ReqCreateAddressDtoV1.builder()
                .alias("집").address("서울시").detail("101호").zipCode("12345").isDefault(true)
                .build();
        ResCreateAddressDtoV1 response = ResCreateAddressDtoV1.builder().addressId(UUID.randomUUID()).build();
        given(addressService.createAddress(any())).willReturn(response);

        mockMvc.perform(post("/api/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.addressId").exists());
    }

    @Test
    @DisplayName("[성공] 본인 배송지 목록 조회 API 호출 시 페이징 데이터가 반환되어야 함")
    void getMyAddressesApiTest() throws Exception {
        given(addressService.getMyAddresses(any(), any())).willReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/addresses?alias=집&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("[성공] 배송지 수정(PUT) API가 정상 호출되어야 함")
    void updateAddressApiTest() throws Exception {
        UUID addressId = UUID.randomUUID();
        ReqUpdateAddressDtoV1 request = ReqUpdateAddressDtoV1.builder()
                .alias("새집").address("경기도").build();
        ResGetAddressDtoV1 response = ResGetAddressDtoV1.builder()
                .addressId(addressId).alias("새집").build();
        
        given(addressService.updateAddress(any(), any())).willReturn(response);

        mockMvc.perform(put("/api/addresses/" + addressId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alias").value("새집"));
    }

    @Test
    @DisplayName("[성공] 배송지 삭제(DELETE) API 호출 시 204 No Content를 반환해야 함")
    void deleteAddressApiTest() throws Exception {
        UUID addressId = UUID.randomUUID();

        mockMvc.perform(delete("/api/addresses/" + addressId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("[실패] 필수값인 주소(address) 누락 시 400 에러를 반환해야 함")
    void validationErrorTest() throws Exception {
        ReqCreateAddressDtoV1 invalidRequest = ReqCreateAddressDtoV1.builder()
                .alias("집").address("").build(); // address가 빈 문자열

        mockMvc.perform(post("/api/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
