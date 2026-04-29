package com.example.whostolemyfood.global.config.ai;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(GeminiProperties.class)
public class GeminiConfig {

    @Bean
    public RestClient geminiRestClient(RestClient.Builder builder, GeminiProperties properties) {
        return builder
                .baseUrl(properties.getBaseUrl())
                .build();
    }
}
