package com.agri.advisory.service.impl;

import com.agri.advisory.service.LlmProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.List;

@Service
@ConditionalOnProperty(name = "ai.provider", havingValue = "gemini")
public class GeminiLlmProvider implements LlmProvider {

    private final WebClient webClient;
    private final String apiKey;
    private final String model;

    public GeminiLlmProvider(WebClient.Builder webClientBuilder,
                             @Value("${gemini.api.key:}") String apiKey,
                             @Value("${gemini.model:gemini-1.5-flash}") String model) {
        this.webClient = webClientBuilder.build();
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public String generateResponse(String prompt, String language) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Gemini API key is not configured.");
        }

        String uriString = "https://generativelanguage.googleapis.com/v1beta/models/" + this.model + ":generateContent?key=" + this.apiKey;
        
        Map<String, Object> requestBody = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(
                    Map.of("text", prompt)
                ))
            )
        );

        try {
            Map<String, Object> response = webClient.post()
                    .uri(URI.create(uriString))
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(60))
                    .block();
            
            if (response != null && response.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    if (content != null && content.containsKey("parts")) {
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                        if (!parts.isEmpty()) {
                            return (String) parts.get(0).get("text");
                        }
                    }
                }
            }
            return "Unable to generate a grounded response at this time.";
        } catch (Exception e) {
            return "Error calling AI provider: " + e.getMessage();
        }
    }
}
