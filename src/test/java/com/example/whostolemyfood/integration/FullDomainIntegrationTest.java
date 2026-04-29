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
import com.example.whostolemyfood.review.domain.repository.ReviewRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
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
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private AiLogRepository aiLogRepository;
    @Autowired private AreaRepository areaRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private StoreRatingSummaryRepository storeRatingSummaryRepository;
    @Autowired private ReviewRatingBatchService reviewRatingBatchService; 
    @Autowired private EntityManager entityManager;
    @Autowired private PasswordEncoder passwordEncoder;

    @MockitoBean private GeminiClient geminiClient;

    // ---------------------------------------------------------
    // Helper Methods (Test Fixtures)
    // ---------------------------------------------------------

    /**
     * Repository를 통해 유저를 직접 생성합니다. (Given 최적화용)
     */
    private UserEntity createUserFixture(String email, String password, String userName, UserRole role) {
        return userRepository.save(UserEntity.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .name(userName)
                .role(role)
                .build());
    }

    /**
     * Repository를 통해 가게를 직접 생성합니다.
     */
    private StoreEntity createStoreFixture(UserEntity owner, String storeName) {
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
                .openTime(LocalTime.of(10, 0)).closeTime(LocalTime.of(22, 0)).build());
    }

    /**
     * Repository를 통해 메뉴를 직접 생성합니다.
     */
    private MenuEntity createMenuFixture(StoreEntity store, String name, int price) {
        return menuRepository.save(MenuEntity.builder()
                .store(store).name(name).price(price).description("설명").build());
    }

    /**
     * Repository를 통해 배송지를 직접 생성합니다.
     */
    private AddressEntity createAddressFixture(UserEntity user, String alias, String address) {
        return addressRepository.save(AddressEntity.builder()
                .userId(user.getId()).alias(alias).address(address)
                .detail("상세주소").zipCode("12345").isDefault(false).build());
    }

    /**
     * Repository를 통해 주문을 직접 생성합니다.
     */
    private OrderEntity createOrderFixture(UserEntity customer, StoreEntity store, AddressEntity address, OrderStatus status, int totalPrice) {
        return orderRepository.save(OrderEntity.builder()
                .userId(customer.getId())
                .storeId(store.getStoreId())
                .addressId(address.getId())
                .status(status)
                .totalPrice(totalPrice)
                .deliveryFee(3000)
                .isHidden(false)
                .build());
    }

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
        StoreEntity store = createStoreFixture(ownerUser, "종로 바삭치킨_Flow");
        
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
        StoreEntity store = createStoreFixture(ownerUser, "AI 치킨");
        
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
        // [Refactor] Given: 검색 API 검증을 위해 customerToken만 유지, 사장님/가게는 Fixture로 단순화
        String customerToken = signupAndGetToken("cust_srch@test.com", "Cust123!@#", "srchcust", "CUSTOMER");
        UserEntity ownerUser = createUserFixture("owner_srch@test.com", "Owner123!@#", "srchowner", UserRole.OWNER);
        StoreEntity store = createStoreFixture(ownerUser, "종로 바삭치킨 검색용");
        
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
        // [Refactor] Given: 사장님은 Fixture로 생성 (토큰 불필요)
        UserEntity owner = createUserFixture("owner_role@test.com", "Owner123!@#", "roleowner", UserRole.OWNER);

        // Given: 고객은 토큰 발급 후 엔티티 확보 (보안 컨텍스트와 데이터 정합성 보장)
        String customerToken = signupAndGetToken("cust_role@test.com", "Cust123!@#", "rolecust", "CUSTOMER");
        UserEntity customerUser = getUserByEmail("cust_role@test.com");

        // Given: 가게/메뉴/배송지 Fixture 생성
        StoreEntity store = createStoreFixture(owner, "권한 차단 가게");
        MenuEntity menu = createMenuFixture(store, "일반 메뉴", 10000);
        AddressEntity address = createAddressFixture(customerUser, "자택", "서울시 강남구");

        // Given: 권한 강제 변경 (해킹 시뮬레이션 - DB 상태 직접 조작)
        ReflectionTestUtils.setField(customerUser, "userRole", UserRole.MANAGER);
        userRepository.saveAndFlush(customerUser);
        
        // When & Then: MANAGER로 변한 고객이 주문 생성 시도 시 403 Forbidden 확인
        Map<String, Object> req = Map.of(
                "storeId", store.getStoreId(),
                "addressId", address.getId(),
                "orderItems", List.of(Map.of("menuId", menu.getMenuId(), "quantity", 1, "priceAtOrder", 10000))
        );

        mockMvc.perform(post(ORDER_BASE_URL)
                .header("Authorization", customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("주문 5분 취소 룰: 5분이 경과된 주문 취소 요청 시 400 Bad Request 검증")
    void cancelOrderAfterFiveMinutes_shouldFail() throws Exception {
        // [Refactor] Given: 취소 API 검증을 위해 customerToken만 유지, 사장님은 Fixture로 신속 생성
        UserEntity owner = createUserFixture("own_cancel@test.com", "Owner123!@#", "canowner", UserRole.OWNER);
        String customerToken = signupAndGetToken("cus_cancel@test.com", "Cust123!@#", "cancust", "CUSTOMER");
        UserEntity customer = getUserByEmail("cus_cancel@test.com");

        StoreEntity store = createStoreFixture(owner, "취소 불가 가게");
        AddressEntity address = createAddressFixture(customer, "자택", "서울시 강남구");
        
        // Given: 5분이 경과한 주문 생성 (과거 시간 강제 주입)
        OrderEntity lateOrder = createOrderFixture(customer, store, address, OrderStatus.PENDING, 23000);
        ReflectionTestUtils.setField(lateOrder, "createdAt", LocalDateTime.now().minusMinutes(6));
        orderRepository.saveAndFlush(lateOrder);
        
        // When: 취소 API 호출 (MockMvc)
        mockMvc.perform(patch(ORDER_BASE_URL + "/" + lateOrder.getOrderId() + "/cancel")
                .header("Authorization", customerToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("메뉴 숨김 처리: 숨김 처리 시 DB isHidden 상태 변경 및 고객 조회 시 노출 차단 검증")
    void hideMenu_shouldNotExposeMenuToCustomer() throws Exception {
        // [Hybrid] Given: 유저 생성 및 토큰 확보
        String ownerToken = signupAndGetToken("own_hmenu@test.com", "Owner123!@#", "hmenuown", "OWNER");
        String customerToken = signupAndGetToken("cus_hmenu@test.com", "Cust123!@#", "hmenucust", "CUSTOMER");
        UserEntity owner = getUserByEmail("own_hmenu@test.com");

        // [Hybrid] Given: 메뉴 데이터 Fixture 생성 (가게/메뉴 API 연쇄 호출 제거)
        StoreEntity store = createStoreFixture(owner, "메뉴 숨김 테스트 가게");
        MenuEntity menu = createMenuFixture(store, "시크릿 메뉴", 15000);
        UUID menuId = menu.getMenuId();
        
        // When: 메뉴 숨김 API 호출 (MockMvc)
        mockMvc.perform(patch(STORE_BASE_URL + "/" + store.getStoreId() + "/menus/" + menuId + "/hide")
                .header("Authorization", ownerToken))
                .andExpect(status().isOk());
                
        // Then: DB 상태 검증
        assertThat(menuRepository.findById(menuId).orElseThrow().getIsHidden()).isTrue();
        
        // Then: 고객 메뉴 목록 조회 시 제외 검증 (MockMvc)
        mockMvc.perform(get(STORE_BASE_URL + "/" + store.getStoreId() + "/menus")
                .header("Authorization", customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.menuId == '" + menuId + "')]").doesNotExist());
    }

    @Test
    @DisplayName("가게 숨김 처리: DB isHidden 상태 변경 검증")
    void hideStore_shouldUpdateDbIsHidden() throws Exception {
        // [Hybrid] Given: 유저/가게 데이터 Fixture 생성 및 토큰 확보
        String ownerToken = signupAndGetToken("own_hstore@test.com", "Owner123!@#", "hstoreown", "OWNER");
        String customerToken = signupAndGetToken("cus_hstore@test.com", "Cust123!@#", "hstorecust", "CUSTOMER");
        UserEntity owner = getUserByEmail("own_hstore@test.com");

        StoreEntity store = createStoreFixture(owner, "숨긴가게 바삭치킨");
        
        // When: 가게 숨김 처리 API 호출
        mockMvc.perform(patch(STORE_BASE_URL + "/" + store.getStoreId() + "/hide")
                .header("Authorization", ownerToken))
                .andExpect(status().isOk());
                
        // Then: DB 상태 확인
        assertThat(storeRepository.findById(store.getStoreId()).orElseThrow().getIsHidden()).isTrue();

        // Then: 검색 결과 노출 차단 검증 (MockMvc)
        mockMvc.perform(get(STORE_BASE_URL + "/search")
                        .header("Authorization", customerToken)
                        .param("keyword", "바삭")
                        .param("categoryId", store.getCategory().getCategoryId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @DisplayName("주소 삭제: DELETE 호출 시 Soft Delete 작동 확인 및 Audit 데이터 검증")
    void deleteAddress_shouldSoftDelete() throws Exception {
        // [Hybrid] Given: 토큰 발급 및 주인공 엔티티 확보
        String customerToken = signupAndGetToken("cus_daddr@test.com", "Cust123!@#", "daddrcust", "CUSTOMER");
        UserEntity customer = getUserByEmail("cus_daddr@test.com");
        
        // [Hybrid] Given: 주소 소유자가 토큰 사용자와 일치하도록 Fixture 생성 (정합성 보장)
        AddressEntity address = createAddressFixture(customer, "삭제할 집", "인천시 남동구");
        UUID addressId = address.getId();
        
        // When: 삭제 API 호출 (MockMvc - customerToken 유지)
        mockMvc.perform(delete(ADDRESS_BASE_URL + "/" + addressId)
                .header("Authorization", customerToken))
                .andExpect(status().isNoContent());
                
        // Then: Soft Delete 및 Audit(deletedBy) 검증
        AddressEntity deletedAddress = addressRepository.findById(addressId).orElseThrow();
        assertThat(deletedAddress.getIsDeleted()).isTrue();
        assertThat(deletedAddress.getDeletedAt()).isNotNull();
        assertThat(deletedAddress.getDeletedBy()).isEqualTo(customer.getId());
    }

    @Test
    @DisplayName("리뷰 작성 실패: COMPLETED 상태가 아닌 주문(PENDING)에 리뷰 작성 시도 시 400 에러 및 DB 저장 안됨 확인")
    void review_shouldFail_whenOrderNotCompleted() throws Exception {
        // [Refactor] Given: 기초 데이터 Fixture 생성 및 토큰 확보
        UserEntity owner = createUserFixture("own_revfail@test.com", "Owner123!@#", "revown", UserRole.OWNER);
        String customerToken = signupAndGetToken("cus_revfail@test.com", "Cust123!@#", "revcus", "CUSTOMER");
        UserEntity customerUser = getUserByEmail("cus_revfail@test.com");

        StoreEntity store = createStoreFixture(owner, "리뷰 실패 테스트 가게");
        AddressEntity address = createAddressFixture(customerUser, "우리집", "서울시 강남구");

        // Given: PENDING 상태의 주문 생성
        OrderEntity pendingOrder = createOrderFixture(customerUser, store, address, OrderStatus.PENDING, 10000);
        UUID orderId = pendingOrder.getOrderId();

        long beforeReviewCount = reviewRepository.count();

        // When: PENDING 주문에 대해 리뷰 작성 시도
        Map<String, Object> revReq = Map.of("rating", 5, "content", "미리 쓰는 리뷰");
        mockMvc.perform(post(ORDER_BASE_URL + "/" + orderId + "/reviews")
                .header("Authorization", customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(revReq)))
                // Then: 400 Bad Request 확인
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("R005"));

        // Then: DB에 리뷰가 실제로 저장되지 않았음을 검증
        assertThat(reviewRepository.count()).isEqualTo(beforeReviewCount);
    }
}

