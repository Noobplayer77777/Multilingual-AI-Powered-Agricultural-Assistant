package com.agri.advisory.service;

import com.agri.advisory.dto.ContextItem;
import java.util.Optional;

public interface WeatherServiceClient {
    Optional<ContextItem> getWeatherContext(String location);
}
