package com.example.whostolemyfood.integration;

import com.example.whostolemyfood.address.domain.entity.AddressEntity;
import com.example.whostolemyfood.address.domain.repository.AddressRepository;
import com.example.whostolemyfood.ai.domain.entity.AiLogEntity;
import com.example.whostolemyfood.ai.domain.repository.AiLogRepository;
import com.example.whostolemyfood.ai.infrastructure.api.gemini.client.GeminiClient;
import com.example.whostolemyfood.area.domain.entity.AreaEntity;
import com.example.whostolemyfood.area.domain.repository.AreaRepository;
import com.example.whostolemyfood.category.domain.entity.CategoryEntity;
import com.example.whostolemyfood.category.domain.repository.CategoryRepository;
import com.example.whostolemyfood.menu.domain.entity.MenuEntity;
import com.example.whostolemyfood.menu.domain.repository.MenuRepository;
import com.example.whostolemyfood.order.domain.entity.OrderEntity;
import com.example.whostolemyfood.order.domain.entity.OrderStatus;
import com.example.whostolemyfood.order.domain.repository.OrderRepository;
import com.example.whostolemyfood.payment.infrastructure.PaymentRepository;
import com.example.whostolemyfood.review.application.service.ReviewRatingBatchService;
import com.example.whostolemyfood.store.domain.entity.StoreEntity;
import com.example.whostolemyfood.store.domain.entity.StoreRatingSummaryEntity;
import com.example.whostolemyfood.store.domain.entity.StoreStatus;
import com.example.whostolemyfood.store.domain.repository.StoreRatingSummaryRepository;
import com.example.whostolemyfood.store.domain.repository.StoreRepository;
import com.example.whostolemyfood.user.domain.entity.UserEntity;
import com.example.whostolemyfood.user.domain.entity.UserRole;
import com.example.whostolemyfood.user.domain.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class FullDomainIntegrationTest {

    private static final String AUTH_SIGNUP_URL = "/api/v1/auth/signup";
    private static final String AUTH_LOGIN_URL = "/api/v1/auth/login";
    private static final String STORE_BASE_URL = "/api/v1/stores";
    private static final String ADDRESS_BASE_URL = "/api/v1/addresses";
    private static final String ORDER_BASE_URL = "/api/v1/orders";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private StoreRepository storeRepository;
    @Autowired private MenuRepository menuRepository;
    @Autowired private AddressRepository addressRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private AiLogRepository aiLogRepository;
    @Autowired private AreaRepository areaRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private StoreRatingSummaryRepository storeRatingSummaryRepository;
    @Autowired private ReviewRatingBatchService reviewRatingBatchService; 
    @Autowired private EntityManager entityManager;

    @MockitoBean private GeminiClient geminiClient;

    @Test
    @DisplayName("주문 서비스 전체 흐름 테스트: 가입 -> 가게/메뉴 생성 -> 주문 -> 결제 -> 리뷰")
    void fullSystemFlowTest() throws Exception {
        
        // ---------------------------------------------------------
        log.info("--- [STEP 1] 회원가입 및 인증 테스트 ---");
        // ---------------------------------------------------------
        performSignupFailure("bad@test.com", "BadPass123", "user1234", "CUSTOMER", "비밀번호 규정 위반");
        performSignupFailure("short@test.com", "ValidPass123!", "abc", "CUSTOMER", "Username 길이 위반");

        String ownerToken = signupAndGetToken("owner_pro@test.com", "Owner123!@#", "owner1234", "OWNER");
        String customerToken = signupAndGetToken("customer_pro@test.com", "Customer123!@#", "cust1234", "CUSTOMER");

        // ---------------------------------------------------------
        log.info("--- [STEP 2] 초기 인프라 데이터 및 가게 생성 ---");
        // ---------------------------------------------------------
        AreaEntity area = areaRepository.save(AreaEntity.builder().ukName("Gwanghwamun_001").city("서울").district("종로구").build());
        CategoryEntity category = categoryRepository.save(CategoryEntity.builder().name("치킨").build());
        StoreRatingSummaryEntity ratingSummary = storeRatingSummaryRepository.save(StoreRatingSummaryEntity.builder().build());
        
        UserEntity ownerUser = userRepository.findByUserEmail("owner_pro@test.com")
                .orElseThrow(() -> new IllegalStateException("사장님 유저를 찾을 수 없습니다."));

        StoreEntity store = storeRepository.save(StoreEntity.builder()
                .user(ownerUser).area(area).category(category).storeRatingSummary(ratingSummary)
                .name("종로 바삭치킨").content("전통 방식으로 튀긴 바삭한 치킨 전문점").address("종로구 100").phone("02-1234").minOrderPrice(15000).status(StoreStatus.OPEN)
                .openTime(LocalTime.of(10, 0)).closeTime(LocalTime.of(22, 0)).isHidden(false).build());

        // ---------------------------------------------------------
        log.info("--- [STEP 3] 메뉴 등록 및 AI 설명 생성 검증 ---");
        // ---------------------------------------------------------
        String testPrompt = "후라이드 치킨의 바삭함을 강조해서 설명해줘";
        String aiResponse = "매일 교체하는 신선한 기름으로 튀겨 극강의 바삭함을 자랑합니다.";
        given(geminiClient.generateContent(anyString())).willReturn(aiResponse);
        
        long beforeLogCount = aiLogRepository.count();
        Map<String, Object> menuReq = Map.of("name", "오리지널 치킨", "price", 20000, "aiDescription", true, "aiPrompt", testPrompt);

        MvcResult menuResult = mockMvc.perform(post(STORE_BASE_URL + "/" + store.getStoreId() + "/menus").header("Authorization", ownerToken)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(menuReq)))
                .andExpect(status().isCreated()).andReturn();

        UUID menuId = UUID.fromString(objectMapper.readTree(menuResult.getResponse().getContentAsString()).get("menuId").asText());

        // AI 로그가 실제 DB에 기록되었는지 확인
        assertThat(aiLogRepository.count()).isEqualTo(beforeLogCount + 1);
        AiLogEntity latestLog = aiLogRepository.findAll().stream()
                .max(Comparator.comparing(AiLogEntity::getCreatedAt))
                .orElseThrow(() -> new IllegalStateException("AI 로그가 저장되지 않았습니다."));
        assertThat(latestLog.getRequestText()).isEqualTo(testPrompt);
        assertThat(latestLog.getResponseText()).isEqualTo(aiResponse);

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(geminiClient, atLeastOnce()).generateContent(promptCaptor.capture());
        assertThat(promptCaptor.getValue()).contains("50자 이내");

        // ---------------------------------------------------------
        log.info("--- [STEP 4] 가게 검색 (QueryDSL 필터링 및 페이징) ---");
        // ---------------------------------------------------------
        mockMvc.perform(get(STORE_BASE_URL + "/search").header("Authorization", customerToken)
                        .param("keyword", "바삭")
                        .param("categoryId", category.getCategoryId().toString())
                        .param("region", "종로구")
                        .param("minOrderPrice", "15000")
                        .param("sortBy", "createdAt")
                        .param("page", "0")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(10));

        // ---------------------------------------------------------
        log.info("--- [STEP 5] 배송지 등록 및 Audit 정보 확인 ---");
        // ---------------------------------------------------------
        Map<String, Object> addrReq = Map.of("alias", "오피스", "address", "세종대로 1", "zipCode", "03171", "isDefault", true);
        MvcResult aRes = mockMvc.perform(post(ADDRESS_BASE_URL).header("Authorization", customerToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(addrReq)))
                .andExpect(status().isOk()).andReturn();
        
        UUID addressId = UUID.fromString(objectMapper.readTree(aRes.getResponse().getContentAsString()).get("addressId").asText());
        UserEntity customerUser = userRepository.findByUserEmail("customer_pro@test.com")
                .orElseThrow(() -> new IllegalStateException("고객 유저를 찾을 수 없습니다."));
        AddressEntity savedAddr = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalStateException("저장된 배송지를 찾을 수 없습니다."));
        assertThat(savedAddr.getCreatedBy()).isEqualTo(customerUser.getId());

        // ---------------------------------------------------------
        log.info("--- [STEP 6] 주문 생성, 결제, 리뷰 작성 흐름 ---");
        // ---------------------------------------------------------
        Map<String, Object> oReq = Map.of("storeId", store.getStoreId(), "addressId", addressId, "orderItems", List.of(Map.of("menuId", menuId, "quantity", 1, "priceAtOrder", 20000)));
        MvcResult oRes = mockMvc.perform(post(ORDER_BASE_URL).header("Authorization", customerToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(oReq)))
                .andExpect(status().isOk()).andReturn();
        
        UUID orderId = UUID.fromString(objectMapper.readTree(oRes.getResponse().getContentAsString()).get("orderId").asText());
        
        // 결제 API 호출
        Map<String, Object> pReq = Map.of("orderId", orderId.toString(), "amount", 23000, "paymentKey", UUID.randomUUID().toString(), "payType", "CARD", "orderName", "후라이드 외");
        mockMvc.perform(post("/api/v1/pay").header("Authorization", customerToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(pReq)))
                .andExpect(status().isOk());

        assertThat(paymentRepository.findAll().stream().anyMatch(p -> p.getOrder().getOrderId().equals(orderId) && p.getAmount() == 23000L)).isTrue();

        // 주문 상태 변경 (ACCEPTED -> ... -> COMPLETED)
        for (OrderStatus st : List.of(OrderStatus.ACCEPTED, OrderStatus.COOKING, OrderStatus.DELIVERING, OrderStatus.DELIVERED, OrderStatus.COMPLETED)) {
            mockMvc.perform(patch(ORDER_BASE_URL + "/" + orderId + "/status").header("Authorization", ownerToken)
                    .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Map.of("status", st.name()))))
                    .andExpect(status().isOk());
        }

        // 리뷰 작성 및 배치 평점 갱신 확인
        mockMvc.perform(post(ORDER_BASE_URL + "/" + orderId + "/reviews").header("Authorization", customerToken).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("rating", 5, "content", "배달도 빠르고 치킨이 정말 바삭해요!"))))
                .andExpect(status().isOk());
        
        // 배치 서비스 호출 및 캐시 동기화
        reviewRatingBatchService.refreshAllStoreRatings();
        entityManager.flush();
        entityManager.clear();
        
        StoreRatingSummaryEntity updatedSummary = storeRatingSummaryRepository.findById(ratingSummary.getId())
                .orElseThrow(() -> new IllegalStateException("평점 요약 데이터를 찾을 수 없습니다."));
        assertThat(updatedSummary.getAverageRating()).isEqualByComparingTo(new BigDecimal("5.0"));
        assertThat(updatedSummary.getReviewCount()).isEqualTo(1);

        // ---------------------------------------------------------
        log.info("--- [STEP 7] 메뉴 및 가게 숨김 처리 검증 ---");
        // ---------------------------------------------------------
        // 1. 메뉴 숨김 처리 후 DB 상태 및 목록 노출 여부 확인
        mockMvc.perform(patch(STORE_BASE_URL + "/" + store.getStoreId() + "/menus/" + menuId + "/hide").header("Authorization", ownerToken)).andExpect(status().isOk());
        MenuEntity hiddenMenu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalStateException("메뉴를 찾을 수 없습니다."));
        assertThat(hiddenMenu.getIsHidden()).isTrue();
        
        mockMvc.perform(get(STORE_BASE_URL + "/" + store.getStoreId() + "/menus").header("Authorization", customerToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[?(@.menuId == '" + menuId + "')]").doesNotExist());

//        // 2. 가게 숨김 처리 후 검색 결과 제외 확인
//        mockMvc.perform(patch(STORE_BASE_URL + "/" + store.getStoreId() + "/hide").header("Authorization", ownerToken)).andExpect(status().isOk());
//        mockMvc.perform(get(STORE_BASE_URL + "/search").header("Authorization", customerToken)
//                        .param("keyword", "바삭").param("categoryId", category.getCategoryId().toString()))
//                .andExpect(status().isOk()).andExpect(jsonPath("$.content").isEmpty()); // 숨겨진 가게는 결과에 없어야 함

        // ---------------------------------------------------------
        log.info("--- [STEP 8] 실시간 권한 체크 및 취소 시간 제한 확인 ---");
        // ---------------------------------------------------------
        ReflectionTestUtils.setField(customerUser, "userRole", UserRole.MANAGER);
        userRepository.saveAndFlush(customerUser);
        mockMvc.perform(post(ORDER_BASE_URL).header("Authorization", customerToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(oReq)))
                .andExpect(status().isForbidden());

        ReflectionTestUtils.setField(customerUser, "userRole", UserRole.CUSTOMER);
        userRepository.saveAndFlush(customerUser);
        OrderEntity lateOrder = orderRepository.save(OrderEntity.builder().userId(customerUser.getId()).storeId(store.getStoreId()).addressId(addressId).status(OrderStatus.PENDING).deliveryFee(3000).totalPrice(23000).build());
        ReflectionTestUtils.setField(lateOrder, "createdAt", LocalDateTime.now().minusMinutes(6));
        orderRepository.saveAndFlush(lateOrder);
        mockMvc.perform(patch(ORDER_BASE_URL + "/" + lateOrder.getOrderId() + "/cancel").header("Authorization", customerToken))
                .andExpect(status().isBadRequest());

        // ---------------------------------------------------------
        log.info("--- [STEP 9] Soft Delete 물리 상태 검증 ---");
        // ---------------------------------------------------------
        mockMvc.perform(delete(ADDRESS_BASE_URL + "/" + addressId).header("Authorization", customerToken))
                .andExpect(status().isNoContent());
        AddressEntity deleted = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalStateException("삭제된 주소를 찾을 수 없습니다."));
        assertThat(deleted.getIsDeleted()).isTrue();
        assertThat(deleted.getDeletedAt()).isNotNull();
        assertThat(deleted.getDeletedBy()).isEqualTo(customerUser.getId());

        log.info("--- [SUCCESS] 모든 비즈니스 시나리오 통합 테스트 완료 ---");
    }

    private void performSignupFailure(String email, String password, String userName, String role, String description) throws Exception {
        Map<String, Object> signupData = Map.of("email", email, "password", password, "userName", userName, "userRole", role);
        mockMvc.perform(post(AUTH_SIGNUP_URL).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(signupData)))
                .andExpect(status().isBadRequest());
    }

    private String signupAndGetToken(String email, String password, String userName, String role) throws Exception {
        Map<String, Object> signupData = Map.of("email", email, "password", password, "userName", userName, "userRole", role);
        mockMvc.perform(post(AUTH_SIGNUP_URL).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(signupData))).andExpect(status().isCreated());

        MvcResult loginRes = mockMvc.perform(post(AUTH_LOGIN_URL).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isOk()).andReturn();

        return "Bearer " + objectMapper.readTree(loginRes.getResponse().getContentAsString()).get("accessToken").asText();
    }
}
