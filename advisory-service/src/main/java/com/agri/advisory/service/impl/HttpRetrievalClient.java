package com.agri.advisory.service.impl;

import com.agri.advisory.dto.AdvisoryRequest;
import com.agri.advisory.dto.ContextItem;
import com.agri.advisory.service.RetrievalClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Collections;
import java.util.List;

@Service
public class HttpRetrievalClient implements RetrievalClient {

    private final WebClient webClient;
    private final String retrievalUrl;

    public HttpRetrievalClient(WebClient.Builder webClientBuilder, 
                               @Value("${retrieval.service.url:http://localhost:8083}") String retrievalUrl) {
        this.webClient = webClientBuilder.baseUrl(retrievalUrl).build();
        this.retrievalUrl = retrievalUrl;
    }

    @Override
    public List<ContextItem> retrieveContext(AdvisoryRequest request) {
        try {
            // Attempt to call Member C's Retrieval API
            return webClient.post()
                    .uri("/retrieve")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToFlux(ContextItem.class)
                    .collectList()
                    .block();
        } catch (Exception e) {
            System.err.println("Warning: Unable to connect to retrieval service at " + retrievalUrl + ": " + e.getMessage() + ". Proceeding with empty context.");
            return Collections.emptyList();
        }
    }
}
