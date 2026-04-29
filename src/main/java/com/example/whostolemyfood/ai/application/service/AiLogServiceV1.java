package com.example.whostolemyfood.ai.application.service;

import com.example.whostolemyfood.ai.domain.entity.AiLogEntity;
import com.example.whostolemyfood.ai.domain.repository.AiLogRepository;
import com.example.whostolemyfood.ai.infrastructure.api.gemini.client.GeminiClient;
import com.example.whostolemyfood.ai.presentation.dto.response.ResGetAiLogDtoV1;
import com.example.whostolemyfood.global.exception.CustomException;
import com.example.whostolemyfood.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiLogServiceV1 {

    private final AiLogRepository aiLogRepository;
    private final GeminiClient geminiClient;

    @Transactional
    public ResGetAiLogDtoV1 generateMenuDescription(UUID userId, String aiPrompt) {
        if (aiPrompt == null || aiPrompt.isBlank()) {
            throw new CustomException(ErrorCode.AI_PROMPT_REQUIRE);
        }
        if (aiPrompt.length() > 100) {
            throw new CustomException(ErrorCode.AI_PROMPT_TOO_LONG);
        }

        String prompt = String.format("""
                [사용자 요청]: %s
                
                [제약 사항]:
                - 메뉴 설명 문구로 작성할 것
                - 한국어로 작성할 것
                - 이모티콘이나 문장부호를 입력하지 않을 것
                - 답변은 반드시 띄어쓰기 포함 50자 이내로 간결하게 문장만 출력할 것 (초과시 LLM교체 할 예정)
                """, aiPrompt);

        String response = geminiClient.generateContent(prompt);

        if (response.length() > 100) {
            response = response.substring(0, 80);
        }

        AiLogEntity ai = AiLogEntity.builder()
                .userId(userId)
                .requestText(aiPrompt)
                .responseText(response)
                .build();

        AiLogEntity savedAi = aiLogRepository.save(ai);
        return new ResGetAiLogDtoV1(response, savedAi.getAiLogId());
    }

//    @Transactional(readOnly = true)
//    public ResGenerateMenuDescriptionDtoV1 generateDescription (
//            UUID storeId,
//            ReqGenerateMenuDescriptionDtoV1 request,
//            AuthUser authUser) {
//        StoreEntity store = storeRepository.findByStoreIdAndIsDeletedFalse(storeId)
//                .orElseThrow(()-> new CustomException(ErrorCode.STORE_NOT_FOUND));
//        validateMenuAiAccess(store, authUser);
//
//        String prompt = """
//                가게 이름은 '%s'입니다.
//                메뉴 이름은 '%s'입니다.
//                손님에게 보여줄 메뉴 설명을 50자 이하로 작성해주세요.
//                답변은 설명 문장만 반환해주세요.
//                """.formatted(store.getName(), request.getMenuName());
//
//        String description = geminiClient.generateContent(prompt);
//
//        if (description.length() > 50) {
//            description = description.substring(0, 50);
//        }
//        return new ResGenerateMenuDescriptionDtoV1(description);
//    }

    // TODO : AI 호출을 메뉴 생성때만 하지말고 단독으로 호출하게 할꺼면 만들기

//    private void validateMenuAiAccess(StoreEntity store, AuthUser authUser) {
//        if (authUser.role() == UserRole.CUSTOMER) {
//            throw new CustomException(ErrorCode.ACCESS_DENIED);
//        }
//
//        if (authUser.role() == UserRole.OWNER && !store.getUser().getId().equals(authUser.getUserId())) {
//            throw new CustomException(ErrorCode.ACCESS_DENIED);
//        }
//    }
}
