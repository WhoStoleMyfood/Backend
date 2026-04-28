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

    // ---------------------------------------------------------
    // Helper Methods (Test Fixtures)
    // ---------------------------------------------------------

    private String signupAndGetToken(String email, String password, String userName, String role) throws Exception {
        Map<String, Object> signupData = Map.of("email", email, "password", password, "userName", userName, "userRole", role);
        mockMvc.perform(post(AUTH_SIGNUP_URL).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(signupData)))
                .andExpect(status().isCreated());

        MvcResult loginRes = mockMvc.perform(post(AUTH_LOGIN_URL).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isOk()).andReturn();

        return "Bearer " + objectMapper.readTree(loginRes.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private UserEntity getUserByEmail(String email) {
        return userRepository.findByUserEmail(email)
                .orElseThrow(() -> new IllegalStateException("유저를 찾을 수 없습니다: " + email));
    }

    private StoreEntity setupStore(UserEntity owner, String storeName) {
        AreaEntity area = areaRepository.save(AreaEntity.builder()
                .ukName("Area_" + UUID.randomUUID().toString().substring(0, 8))
                .city("서울").district("종로구")
                .isActive(true)
                .build());
        CategoryEntity category = categoryRepository.save(CategoryEntity.builder()
                .name("Cat_" + UUID.randomUUID().toString().substring(0, 8)).build());
        StoreRatingSummaryEntity ratingSummary = storeRatingSummaryRepository.save(StoreRatingSummaryEntity.builder().build());
        
        return storeRepository.save(StoreEntity.builder()
                .user(owner).area(area).category(category).storeRatingSummary(ratingSummary)
                .name(storeName).content("전통 방식으로 튀긴 바삭한 치킨 전문점").address("종로구 100")
                .phone("02-1234").minOrderPrice(15000).status(StoreStatus.OPEN)
                .openTime(LocalTime.of(10, 0)).closeTime(LocalTime.of(22, 0)).isHidden(false).build());
    }

    private UUID createMenuViaApi(String ownerToken, UUID storeId, String menuName, int price, String prompt) throws Exception {
        boolean useAi = prompt != null && !prompt.isEmpty();
        Map<String, Object> menuReq = Map.of(
                "name", menuName, 
                "price", price, 
                "aiDescription", useAi, 
                "aiPrompt", prompt != null ? prompt : ""
        );
        MvcResult result = mockMvc.perform(post(STORE_BASE_URL + "/" + storeId + "/menus")
                .header("Authorization", ownerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(menuReq)))
                .andExpect(status().isCreated()).andReturn();
        return UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("menuId").asText());
    }

    private UUID createAddressViaApi(String customerToken, String alias, String address) throws Exception {
        Map<String, Object> addrReq = Map.of("alias", alias, "address", address, "zipCode", "03171", "isDefault", true);
        MvcResult result = mockMvc.perform(post(ADDRESS_BASE_URL)
                .header("Authorization", customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addrReq)))
                .andExpect(status().isOk()).andReturn();
        return UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("addressId").asText());
    }

    private UUID createOrderViaApi(String customerToken, UUID storeId, UUID addressId, UUID menuId, int price) throws Exception {
        Map<String, Object> req = Map.of(
                "storeId", storeId, 
                "addressId", addressId, 
                "orderItems", List.of(Map.of("menuId", menuId, "quantity", 1, "priceAtOrder", price))
        );
        MvcResult result = mockMvc.perform(post(ORDER_BASE_URL)
                .header("Authorization", customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk()).andReturn();
        return UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("orderId").asText());
    }

    // ---------------------------------------------------------
    // Test Methods
    // ---------------------------------------------------------

    @Test
    @DisplayName("전체 흐름 검증: 회원가입 -> 가게/메뉴 생성 -> 주문 -> 결제 -> 리뷰 작성 -> 평점 갱신")
    void fullSystemFlowTest() throws Exception {
        // Given
        String ownerToken = signupAndGetToken("owner_flow@test.com", "Owner123!@#", "flowowner", "OWNER");
        String customerToken = signupAndGetToken("cust_flow@test.com", "Cust123!@#", "flowcust", "CUSTOMER");
        
        UserEntity ownerUser = getUserByEmail("owner_flow@test.com");
        StoreEntity store = setupStore(ownerUser, "종로 바삭치킨_Flow");
        
        given(geminiClient.generateContent(anyString())).willReturn("바삭한 치킨입니다.");
        UUID menuId = createMenuViaApi(ownerToken, store.getStoreId(), "오리지널 치킨", 20000, "바삭함을 강조해줘");
        UUID addressId = createAddressViaApi(customerToken, "집", "세종대로 1");
        
        // When: 주문 생성
        UUID orderId = createOrderViaApi(customerToken, store.getStoreId(), addressId, menuId, 20000);
        
        // When: 결제 진행
        Map<String, Object> pReq = Map.of("orderId", orderId.toString(), "amount", 23000, "paymentKey", UUID.randomUUID().toString(), "payType", "CARD", "orderName", "후라이드 외");
        mockMvc.perform(post("/api/v1/pay").header("Authorization", customerToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(pReq)))
                .andExpect(status().isOk());
                
        // Then: 결제 확인
        assertThat(paymentRepository.findAll().stream().anyMatch(p -> p.getOrder().getOrderId().equals(orderId) && p.getAmount() == 23000L)).isTrue();
        
        // When: 주문 상태 변경 (OWNER 권한)
        for (OrderStatus st : List.of(OrderStatus.ACCEPTED, OrderStatus.COOKING, OrderStatus.DELIVERING, OrderStatus.DELIVERED, OrderStatus.COMPLETED)) {
            mockMvc.perform(patch(ORDER_BASE_URL + "/" + orderId + "/status").header("Authorization", ownerToken)
                    .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Map.of("status", st.name()))))
                    .andExpect(status().isOk());
        }
        
        // When: 리뷰 작성 (CUSTOMER 권한)
        mockMvc.perform(post(ORDER_BASE_URL + "/" + orderId + "/reviews").header("Authorization", customerToken).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("rating", 5, "content", "배달도 빠르고 치킨이 정말 바삭해요!"))))
                .andExpect(status().isOk());
                
        // Then: 배치 서비스 호출 및 캐시 동기화 검증
        reviewRatingBatchService.refreshAllStoreRatings();
        entityManager.flush();
        entityManager.clear();
        
        StoreRatingSummaryEntity updatedSummary = storeRatingSummaryRepository.findById(store.getStoreRatingSummary().getId()).orElseThrow();
        assertThat(updatedSummary.getAverageRating()).isEqualByComparingTo(new BigDecimal("5.0"));
        assertThat(updatedSummary.getReviewCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("회원가입 실패 검증: 비밀번호 규칙 및 닉네임 길이 위반 시 400 Bad Request 반환")
    void signup_withInvalidData_shouldReturnBadRequest() throws Exception {
        // Given
        Map<String, Object> invalidPasswordReq = Map.of("email", "bad@test.com", "password", "BadPass123", "userName", "user1234", "userRole", "CUSTOMER");
        Map<String, Object> invalidUsernameReq = Map.of("email", "short@test.com", "password", "ValidPass123!", "userName", "abc", "userRole", "CUSTOMER");

        // When & Then: 비밀번호 규정 위반
        mockMvc.perform(post(AUTH_SIGNUP_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidPasswordReq)))
                .andExpect(status().isBadRequest());

        // When & Then: Username 길이 위반
        mockMvc.perform(post(AUTH_SIGNUP_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUsernameReq)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("AI 설명 생성: GeminiClient 호출 및 프롬프트 제한 확인, 로그 적재 검증")
    void createMenuWithAiDescription_shouldSaveAiLogAndLimitPrompt() throws Exception {
        // Given
        String ownerToken = signupAndGetToken("owner_ai@test.com", "Owner123!@#", "aiowner", "OWNER");
        UserEntity ownerUser = getUserByEmail("owner_ai@test.com");
        StoreEntity store = setupStore(ownerUser, "AI 치킨");
        
        String testPrompt = "후라이드 치킨의 바삭함을 강조해서 설명해줘";
        String aiResponse = "매일 교체하는 신선한 기름으로 튀겨 극강의 바삭함을 자랑합니다.";
        given(geminiClient.generateContent(anyString())).willReturn(aiResponse);
        
        long beforeLogCount = aiLogRepository.count();
        
        // When
        createMenuViaApi(ownerToken, store.getStoreId(), "AI 생성 치킨", 20000, testPrompt);
        
        // Then: DB 적재 확인
        assertThat(aiLogRepository.count()).isEqualTo(beforeLogCount + 1);
        AiLogEntity latestLog = aiLogRepository.findAll().stream()
                .max(Comparator.comparing(AiLogEntity::getCreatedAt)).orElseThrow();
        assertThat(latestLog.getRequestText()).isEqualTo(testPrompt);
        assertThat(latestLog.getResponseText()).isEqualTo(aiResponse);
        
        // Then: 프롬프트 제한 조건 삽입 여부 캡처 및 검증
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(geminiClient, atLeastOnce()).generateContent(promptCaptor.capture());
        assertThat(promptCaptor.getValue()).contains("50자 이내");
    }

    @Test
    @DisplayName("가게 검색: 조건별 필터링 및 사이즈 제한 검증")
    void searchStore_shouldApplyFiltersAndPaging() throws Exception {
        // Given
        String customerToken = signupAndGetToken("cust_srch@test.com", "Cust123!@#", "srchcust", "CUSTOMER");
        String ownerToken = signupAndGetToken("owner_srch@test.com", "Owner123!@#", "srchowner", "OWNER");
        UserEntity ownerUser = getUserByEmail("owner_srch@test.com");
        StoreEntity store = setupStore(ownerUser, "종로 바삭치킨 검색용");
        
        // When & Then: 사이즈 강제 보정 및 검색 결과 포함 검증
        mockMvc.perform(get(STORE_BASE_URL + "/search")
                        .header("Authorization", customerToken)
                        .param("keyword", "바삭")
                        .param("categoryId", store.getCategory().getCategoryId().toString())
                        .param("region", "종로구")
                        .param("minOrderPrice", "15000")
                        .param("sortBy", "createdAt")
                        .param("page", "0")
                        .param("size", "100")) 
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.content[0].storeName").value("종로 바삭치킨 검색용"));
    }

    @Test
    @DisplayName("실시간 권한 인가: CUSTOMER 권한을 MANAGER로 강제 변경 시 주문 생성 403 차단 검증")
    void changedCustomerRole_shouldBlockOrderCreationImmediately() throws Exception {
        // Given
        String ownerToken = signupAndGetToken("owner_role@test.com", "Owner123!@#", "roleowner", "OWNER");
        String customerToken = signupAndGetToken("cust_role@test.com", "Cust123!@#", "rolecust", "CUSTOMER");
        
        UserEntity ownerUser = getUserByEmail("owner_role@test.com");
        StoreEntity store = setupStore(ownerUser, "권한 차단 가게");
        UUID menuId = createMenuViaApi(ownerToken, store.getStoreId(), "일반 메뉴", 10000, null);
        UUID addressId = createAddressViaApi(customerToken, "자택", "서울시 강남구");
        
        // Given: 권한 강제 변경 (해킹 시뮬레이션)
        UserEntity customerUser = getUserByEmail("cust_role@test.com");
        ReflectionTestUtils.setField(customerUser, "userRole", UserRole.MANAGER);
        userRepository.saveAndFlush(customerUser);
        
        // When & Then
        Map<String, Object> req = Map.of("storeId", store.getStoreId(), "addressId", addressId, "orderItems", List.of(Map.of("menuId", menuId, "quantity", 1, "priceAtOrder", 10000)));
        mockMvc.perform(post(ORDER_BASE_URL)
                .header("Authorization", customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("주문 5분 취소 룰: 5분이 경과된 주문 취소 요청 시 400 Bad Request 검증")
    void cancelOrderAfterFiveMinutes_shouldFail() throws Exception {
        // Given
        String ownerToken = signupAndGetToken("own_cancel@test.com", "Owner123!@#", "canowner", "OWNER");
        String customerToken = signupAndGetToken("cus_cancel@test.com", "Cust123!@#", "cancust", "CUSTOMER");
        
        UserEntity ownerUser = getUserByEmail("own_cancel@test.com");
        StoreEntity store = setupStore(ownerUser, "취소 불가 가게");
        UUID addressId = createAddressViaApi(customerToken, "자택", "서울시 강남구");
        UserEntity customerUser = getUserByEmail("cus_cancel@test.com");
        
        OrderEntity lateOrder = orderRepository.save(OrderEntity.builder()
                .userId(customerUser.getId())
                .storeId(store.getStoreId())
                .addressId(addressId)
                .status(OrderStatus.PENDING)
                .deliveryFee(3000)
                .totalPrice(23000)
                .build());
                
        // Given: 시간 강제 조작 (6분 전으로)
        ReflectionTestUtils.setField(lateOrder, "createdAt", LocalDateTime.now().minusMinutes(6));
        orderRepository.saveAndFlush(lateOrder);
        
        // When & Then
        mockMvc.perform(patch(ORDER_BASE_URL + "/" + lateOrder.getOrderId() + "/cancel")
                .header("Authorization", customerToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("메뉴 숨김 처리: 숨김 처리 시 DB isHidden 상태 변경 및 고객 조회 시 노출 차단 검증")
    void hideMenu_shouldNotExposeMenuToCustomer() throws Exception {
        // Given
        String ownerToken = signupAndGetToken("own_hmenu@test.com", "Owner123!@#", "hmenuown", "OWNER");
        String customerToken = signupAndGetToken("cus_hmenu@test.com", "Cust123!@#", "hmenucust", "CUSTOMER");
        
        UserEntity ownerUser = getUserByEmail("own_hmenu@test.com");
        StoreEntity store = setupStore(ownerUser, "메뉴 숨김 테스트 가게");
        UUID menuId = createMenuViaApi(ownerToken, store.getStoreId(), "시크릿 메뉴", 15000, null);
        
        // When: 메뉴 숨김 API 호출
        mockMvc.perform(patch(STORE_BASE_URL + "/" + store.getStoreId() + "/menus/" + menuId + "/hide")
                .header("Authorization", ownerToken))
                .andExpect(status().isOk());
                
        // Then: DB isHidden 변경 확인
        MenuEntity hiddenMenu = menuRepository.findById(menuId).orElseThrow();
        assertThat(hiddenMenu.getIsHidden()).isTrue();
        
        // Then: 고객 권한 메뉴 목록에서 제외 검증
        mockMvc.perform(get(STORE_BASE_URL + "/" + store.getStoreId() + "/menus")
                .header("Authorization", customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.menuId == '" + menuId + "')]").doesNotExist());
    }

    @Test
    @DisplayName("가게 숨김 처리: DB isHidden 상태 변경 검증")
    void hideStore_shouldUpdateDbIsHidden() throws Exception {
        // Given
        String ownerToken = signupAndGetToken("own_hstore@test.com", "Owner123!@#", "hstoreown", "OWNER");
        String customerToken = signupAndGetToken("cus_hstore@test.com", "Cust123!@#", "hstorecust", "CUSTOMER");
        
        UserEntity ownerUser = getUserByEmail("own_hstore@test.com");
        StoreEntity store = setupStore(ownerUser, "숨긴가게 바삭치킨");
        
        // When: 가게 숨김 처리 API 호출
        mockMvc.perform(patch(STORE_BASE_URL + "/" + store.getStoreId() + "/hide")
                .header("Authorization", ownerToken))
                .andExpect(status().isOk());
                
        // Then: DB isHidden 변경 확인
        StoreEntity hiddenStore = storeRepository.findById(store.getStoreId()).orElseThrow();
        assertThat(hiddenStore.getIsHidden()).isTrue();

        /*
        // Then: 검색 결과 검증
        mockMvc.perform(get(STORE_BASE_URL + "/search")
                        .header("Authorization", customerToken)
                        .param("keyword", "바삭")
                        .param("categoryId", store.getCategory().getCategoryId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty()); // 숨겨진 가게는 결과에 노출되면 안 됨
        */
    }

    @Test
    @DisplayName("주소 삭제: DELETE 호출 시 Soft Delete 작동 확인 및 Audit 데이터 검증")
    void deleteAddress_shouldSoftDelete() throws Exception {
        // Given
        String customerToken = signupAndGetToken("cus_daddr@test.com", "Cust123!@#", "daddrcust", "CUSTOMER");
        UserEntity customerUser = getUserByEmail("cus_daddr@test.com");
        
        UUID addressId = createAddressViaApi(customerToken, "삭제할 집", "인천시 남동구");
        
        // When
        mockMvc.perform(delete(ADDRESS_BASE_URL + "/" + addressId)
                .header("Authorization", customerToken))
                .andExpect(status().isNoContent());
                
        // Then: 논리 삭제 및 Audit 확인
        AddressEntity deletedAddress = addressRepository.findById(addressId).orElseThrow();
        assertThat(deletedAddress.getIsDeleted()).isTrue();
        assertThat(deletedAddress.getDeletedAt()).isNotNull();
        assertThat(deletedAddress.getDeletedBy()).isEqualTo(customerUser.getId());
    }
}
