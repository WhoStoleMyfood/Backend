package com.example.whostolemyfood.store.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter // 컨트롤러에서 파라미터 바인딩을 위해 필요할 수 있음
@Schema(description = "조건별 조회 조건 객체")
@NoArgsConstructor
public class StoreSearchConditionV1 {

    @Schema(description = "가게명 또는 키워드", example = "치킨")
    private String keyword;    // 가게명 또는 메뉴명 키워드
    @Schema(description = "카테고리", example = "")
    private UUID categoryId;   // 카테고리 UUID
    @Schema(description = "지역명", example = "종로")
    private String region;     // 지역명

    // Integer로 변경하여 null 체크가 가능하도록 함
    @Schema(description = "최소 주문 금액", example = "20000")
    private Integer minOrderPrice;

    @Schema(description = "정렬 기준", example = "createdAt")
    private String sortBy;     // 정렬 기준 (예: "createdAt", "rating")


    private Integer page = 0;
    private Integer size = 10;
}
