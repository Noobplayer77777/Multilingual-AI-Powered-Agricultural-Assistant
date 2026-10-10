package com.agri.advisory.service.impl;

import com.agri.advisory.service.LlmProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@ConditionalOnProperty(name = "ai.provider", havingValue = "ollama")
public class OllamaLlmProvider implements LlmProvider {

    private final WebClient webClient;
    private final String model;

    public OllamaLlmProvider(WebClient.Builder webClientBuilder,
                             @Value("${ollama.url:http://localhost:11434}") String ollamaUrl,
                             @Value("${ollama.model:qwen2.5:7b-instruct-q4_K_M}") String model) {
        this.webClient = webClientBuilder.baseUrl(ollamaUrl).build();
        this.model = model;
    }

    @Override
    public String generateResponse(String prompt, String language) {
        Map<String, Object> requestBody = Map.of(
            "model", this.model,
            "prompt", prompt,
            "stream", false,
            "options", Map.of("num_predict", 200)
        );

        try {
            Map response = webClient.post()
                    .uri("/api/generate")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(java.time.Duration.ofMinutes(4))
                    .block();
            
            if (response != null) {
                String answer = (String) response.get("response");
                if (answer == null || answer.trim().isEmpty()) {
                    answer = (String) response.get("thinking");
                }
                if (answer != null && !answer.trim().isEmpty()) {
                    return answer.trim();
                }
            }
            throw new IllegalStateException("Ollama returned an empty response.");
        } catch (Exception e) {
            throw new IllegalStateException("AI provider execution failed: " + e.getMessage(), e);
        }
    }
}
