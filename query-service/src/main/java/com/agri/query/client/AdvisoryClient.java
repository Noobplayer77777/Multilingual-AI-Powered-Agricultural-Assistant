package com.agri.query.client;

import com.agri.query.dto.QueryRequest;
import com.agri.query.dto.QueryResponse;
import com.agri.query.exception.AdvisoryServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Service
public class AdvisoryClient {

    private final RestClient restClient;
    private final String advisoryUrl;

    public AdvisoryClient(RestClient.Builder restClientBuilder, @Value("${advisory-service.url}") String advisoryUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) Duration.ofSeconds(10).toMillis());
        requestFactory.setReadTimeout((int) Duration.ofMinutes(4).toMillis());

        this.restClient = restClientBuilder
                .requestFactory(requestFactory)
                .build();
        this.advisoryUrl = advisoryUrl;
    }

    public QueryResponse getAdvisory(QueryRequest request) {
        try {
            QueryResponse response = restClient.post()
                    .uri(advisoryUrl + "/advise")
                    .body(request)
                    .retrieve()
                    .body(QueryResponse.class);

            if (response == null || response.getAnswer() == null) {
                throw new AdvisoryServiceUnavailableException("Advisory service returned an empty response.");
            }
            return response;
        } catch (Exception e) {
            throw new AdvisoryServiceUnavailableException("Advisory service is unavailable at " + advisoryUrl + ": " + e.getMessage(), e);
        }
    }
}
