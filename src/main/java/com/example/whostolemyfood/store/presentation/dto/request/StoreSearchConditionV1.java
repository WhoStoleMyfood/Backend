package com.example.whostolemyfood.store.presentation.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter // 컨트롤러에서 파라미터 바인딩을 위해 필요할 수 있음
@NoArgsConstructor
public class StoreSearchConditionV1 {

    private String keyword;    // 가게명 또는 메뉴명 키워드
    private UUID categoryId;   // 카테고리명 (또는 UUID로 변경 권장)
    private String region;     // 지역명

    // Integer로 변경하여 null 체크가 가능하도록 함
    private Integer minOrderPrice;

    private String sortBy;     // 정렬 기준 (예: "createdAt", "rating")

    private Integer page = 0;
}
