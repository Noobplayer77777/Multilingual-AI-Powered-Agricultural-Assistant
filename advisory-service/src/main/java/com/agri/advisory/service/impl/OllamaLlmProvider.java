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
                             @Value("${ollama.model:gemma4:e4b}") String model) {
        this.webClient = webClientBuilder.baseUrl(ollamaUrl).build();
        this.model = model;
    }

    @Override
    public String generateResponse(String prompt, String language) {
        Map<String, Object> requestBody = Map.of(
            "model", this.model,
            "prompt", prompt,
            "stream", false
        );

        try {
            Map response = webClient.post()
                    .uri("/api/generate")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (response != null && response.containsKey("response")) {
                return (String) response.get("response");
            }
            return "Unable to generate a grounded response at this time.";
        } catch (Exception e) {
            return "Error calling Ollama provider: " + e.getMessage();
        }
    }
}
