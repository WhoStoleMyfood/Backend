package com.example.whostolemyfood.global.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class PageUtil {
    // 허용 사이즈 리스트
     private static final List<Integer> ALLOWED_PAGE_NUMBERS = List.of(10,30,50);
     private static final int DEFAULT_PAGE_SIZE = 10;

     public static Pageable validatePageSize(Pageable pageable) {
         int currentSize = pageable.getPageSize();

         // 허용된 사이즈가 아니면 사이즈만 기본으로 변경 나머지 유지
         if (!ALLOWED_PAGE_NUMBERS.contains(currentSize)) {
             return PageRequest.of(
                     pageable.getPageNumber(),
                     DEFAULT_PAGE_SIZE,
                     pageable.getSort()
             );
         }
         return pageable;
     }
}
