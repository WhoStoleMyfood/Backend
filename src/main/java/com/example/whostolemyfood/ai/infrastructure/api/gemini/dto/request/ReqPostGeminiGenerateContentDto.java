package com.example.whostolemyfood.ai.infrastructure.api.gemini.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ReqPostGeminiGenerateContentDto {

    private List<Content> contents;

    public static ReqPostGeminiGenerateContentDto from(String prompt){
        return new ReqPostGeminiGenerateContentDto(
                List.of(new Content(List.of(new Part(prompt))))
        );
    }

    @Getter
    @AllArgsConstructor
    public static class Content {
        private List<Part> parts;
    }

    @Getter
    @AllArgsConstructor
    public static class Part {
        private String text;
    }
}
