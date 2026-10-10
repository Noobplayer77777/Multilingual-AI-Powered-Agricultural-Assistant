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
            // Fallback to dummy data if Member C's service is not running yet
            System.err.println("Failed to connect to Member C's retrieval service at " + retrievalUrl + ". Using mock context.");
            ContextItem dummyContext = new ContextItem();
            dummyContext.setText("Sample context regarding " + request.getCrop() + " in " + request.getLocation());
            dummyContext.setSourceName("Development Retrieval Interface");
            dummyContext.setType("DOCUMENT");
            dummyContext.setSourceUrl("http://localhost:8083/docs/mock");
            return Collections.singletonList(dummyContext);
        }
    }
}
