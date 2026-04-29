package com.example.whostolemyfood.ai.infrastructure.api.gemini.client;

import com.example.whostolemyfood.global.config.ai.GeminiProperties;
import com.example.whostolemyfood.ai.infrastructure.api.gemini.dto.request.ReqPostGeminiGenerateContentDto;
import com.example.whostolemyfood.ai.infrastructure.api.gemini.dto.response.ResPostGeminiGenerateContentDto;
import com.example.whostolemyfood.global.exception.CustomException;
import com.example.whostolemyfood.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class GeminiClient {

    private final RestClient restClient;
    private final GeminiProperties properties;

    public String generateContent(String prompt) {
        ResPostGeminiGenerateContentDto response = restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/models/{model}:generateContent")
                        .queryParam("key", properties.getApiKey())
                        .build(properties.getModel()))
                .body(ReqPostGeminiGenerateContentDto.from(prompt))
                .retrieve()
                .body(ResPostGeminiGenerateContentDto.class);

        if (response == null || response.getText() == null || response.getText().isEmpty()) {
            throw new CustomException(ErrorCode.AI_RESPONSE_EMPTY);
        }
        return response.getText().trim();
    }

}
