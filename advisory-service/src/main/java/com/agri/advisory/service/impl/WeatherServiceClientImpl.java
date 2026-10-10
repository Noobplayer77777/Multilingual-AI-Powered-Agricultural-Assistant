package com.agri.advisory.service.impl;

import com.agri.advisory.dto.ContextItem;
import com.agri.advisory.service.WeatherServiceClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Optional;

@Service
public class WeatherServiceClientImpl implements WeatherServiceClient {

    private static final Logger log = LoggerFactory.getLogger(WeatherServiceClientImpl.class);
    private static final String BASE_URL = "https://api.weatherapi.com";

    private final WebClient webClient;
    private final String apiKey;
    private final ObjectMapper objectMapper;

    public WeatherServiceClientImpl(WebClient.Builder webClientBuilder,
                                    @Value("${weather.api.key:}") String apiKey,
                                    ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl(BASE_URL).build();
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<ContextItem> getWeatherContext(String location) {
        if (apiKey == null || apiKey.trim().isEmpty() || location == null || location.trim().isEmpty()) {
            return Optional.empty();
        }

        try {
            String responseBody = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/forecast.json")
                            .queryParam("key", apiKey)
                            .queryParam("q", location.trim())
                            .queryParam("days", 3)
                            .queryParam("aqi", "no")
                            .queryParam("alerts", "no")
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            if (responseBody == null || responseBody.isBlank()) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode loc = root.path("location");
            String name = loc.path("name").asText(location);
            String region = loc.path("region").asText();
            String country = loc.path("country").asText();
            double lat = loc.path("lat").asDouble();
            double lon = loc.path("lon").asDouble();

            String locDisplay = name + (region.isEmpty() ? "" : ", " + region) + (country.isEmpty() ? "" : " (" + country + ")");

            JsonNode current = root.path("current");
            double tempC = current.path("temp_c").asDouble();
            double feelsLike = current.path("feelslike_c").asDouble();
            String condition = current.path("condition").path("text").asText();
            int humidity = current.path("humidity").asInt();
            double windKph = current.path("wind_kph").asDouble();
            double precipMm = current.path("precip_mm").asDouble();

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Weather for %s [Lat: %.2f, Lon: %.2f]:\n", locDisplay, lat, lon));
            sb.append(String.format("- Current: %.1f°C (Feels like: %.1f°C), %s, Humidity: %d%%, Wind: %.1f km/h, Precip: %.1f mm\n",
                    tempC, feelsLike, condition, humidity, windKph, precipMm));

            JsonNode forecastDays = root.path("forecast").path("forecastday");
            if (forecastDays.isArray() && !forecastDays.isEmpty()) {
                sb.append("- 3-Day Forecast:\n");
                for (JsonNode dayNode : forecastDays) {
                    String date = dayNode.path("date").asText();
                    JsonNode day = dayNode.path("day");
                    double maxT = day.path("maxtemp_c").asDouble();
                    double minT = day.path("mintemp_c").asDouble();
                    String dayCond = day.path("condition").path("text").asText();
                    int rainChance = day.path("daily_chance_of_rain").asInt();
                    double dayPrecip = day.path("totalprecip_mm").asDouble();
                    sb.append(String.format("  * %s: Max %.1f°C, Min %.1f°C, %s, Rain Chance: %d%%, Precip: %.1f mm\n",
                            date, maxT, minT, dayCond, rainChance, dayPrecip));
                }
            }

            ContextItem item = new ContextItem();
            item.setText(sb.toString().trim());
            item.setSourceName("WeatherAPI.com");
            item.setSourceUrl("https://www.weatherapi.com/");
            item.setType("API");

            return Optional.of(item);
        } catch (Exception e) {
            log.warn("Failed to fetch weather for {}: {}", location, e.getMessage());
            return Optional.empty();
        }
    }
}
