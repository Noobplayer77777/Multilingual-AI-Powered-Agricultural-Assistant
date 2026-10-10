package com.agri.query.client;

import com.agri.query.dto.QueryRequest;
import com.agri.query.dto.QueryResponse;
import com.agri.query.dto.Source;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Collections;

@Service
public class AdvisoryClient {

    private final RestClient restClient;
    private final String advisoryUrl;

    public AdvisoryClient(RestClient.Builder restClientBuilder, @Value("${advisory-service.url}") String advisoryUrl) {
        this.restClient = restClientBuilder.build();
        this.advisoryUrl = advisoryUrl;
    }

    public QueryResponse getAdvisory(QueryRequest request) {
        try {
            return restClient.post()
                    .uri(advisoryUrl + "/advise")
                    .body(request)
                    .retrieve()
                    .body(QueryResponse.class);
        } catch (Exception e) {
            // Fallback gracefully as required
            QueryResponse fallback = new QueryResponse();
            fallback.setAnswer("The advisory service is currently unavailable. Please try again later.");
            fallback.setLanguage(request.getLanguage() != null ? request.getLanguage() : "en");
            
            Source errorSource = new Source();
            errorSource.setName("System Fallback");
            errorSource.setType("INTERNAL");
            errorSource.setUrl("");
            fallback.setSources(Collections.singletonList(errorSource));
            
            return fallback;
        }
    }
}
