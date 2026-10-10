package com.agri.advisory.service.impl;

import com.agri.advisory.dto.ContextItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class WeatherServiceClientImplTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testGetWeatherContext_EmptyApiKey_ReturnsEmpty() {
        WeatherServiceClientImpl client = new WeatherServiceClientImpl(
                WebClient.builder(),
                "",
                objectMapper
        );

        Optional<ContextItem> result = client.getWeatherContext("Thanjavur");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetWeatherContext_NullLocation_ReturnsEmpty() {
        WeatherServiceClientImpl client = new WeatherServiceClientImpl(
                WebClient.builder(),
                "test-key",
                objectMapper
        );

        Optional<ContextItem> result = client.getWeatherContext(null);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetWeatherContext_BlankLocation_ReturnsEmpty() {
        WeatherServiceClientImpl client = new WeatherServiceClientImpl(
                WebClient.builder(),
                "test-key",
                objectMapper
        );

        Optional<ContextItem> result = client.getWeatherContext("   ");
        assertTrue(result.isEmpty());
    }
}
