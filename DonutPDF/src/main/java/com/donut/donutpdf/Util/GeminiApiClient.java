package com.donut.donutpdf.Util;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class GeminiApiClient {

    private static final Dotenv dotenv = Dotenv.load();
    private final String geminiApiUrl = dotenv.get("GEMINI_API_URL");
    private final String apiKey = dotenv.get("GEMINI_API_KEY");

    private final RestTemplate restTemplate = new RestTemplate();

    public String sendPrompt(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey); // API 키 헤더에 포함

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(geminiApiUrl, request, Map.class);

        try {
            // Gemini 응답 파싱 (응답 구조에 따라 수정 필요)
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

            return parts.get(0).get("text").toString();

        } catch (Exception e) {
            throw new RuntimeException("Gemini 요약 응답 파싱 실패", e);
        }
    }
}

