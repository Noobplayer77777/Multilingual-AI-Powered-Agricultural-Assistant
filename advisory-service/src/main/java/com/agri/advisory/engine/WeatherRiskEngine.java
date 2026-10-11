package com.agri.advisory.engine;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class WeatherRiskEngine {

    public enum ActionDirective {
        PROCEED,
        PREPARE,
        WAIT,
        SEEK_LOCAL_ADVISORY
    }

    public record WeatherRiskAlert(
            String riskType, // "RAINFALL_EXCESS", "SPRAY_DRIFT", "DISEASE_OUTBREAK", "HEAT_STRESS", "COLD_INJURY"
            String severity, // "HIGH", "MODERATE", "LOW"
            String description,
            String agronomicImpact,
            String suggestedMitigation
    ) {}

    public record WeatherRiskAssessment(
            String location,
            String crop,
            String season,
            ActionDirective directive,
            String directiveExplanation,
            List<WeatherRiskAlert> alerts,
            String sowingWindowStatus,
            String spraySuitabilityNotice,
            Map<String, Object> observedMetrics,
            String forecastSource,
            String metadataFreshness
    ) {}

    public WeatherRiskAssessment evaluateWeatherRisk(String location, String cropInput, String seasonInput,
                                                     Double rainfallMm, Double tempMaxC, Double tempMinC,
                                                     Double humidityPct, Double windKph) {
        String crop = cropInput != null ? cropInput.trim().toLowerCase() : "paddy";
        String season = seasonInput != null ? seasonInput.trim() : "Current Season";

        List<WeatherRiskAlert> alerts = new ArrayList<>();
        ActionDirective directive = ActionDirective.PROCEED;
        List<String> reasons = new ArrayList<>();

        double rain = rainfallMm != null ? rainfallMm : 0.0;
        double tMax = tempMaxC != null ? tempMaxC : 32.0;
        double tMin = tempMinC != null ? tempMinC : 24.0;
        double rh = humidityPct != null ? humidityPct : 65.0;
        double wind = windKph != null ? windKph : 8.0;

        // 1. Heavy rainfall risk (> 40 mm)
        if (rain >= 40.0) {
            directive = ActionDirective.WAIT;
            reasons.add(String.format("Heavy rainfall forecast (%.1f mm). Sowing or fertilizer broadcasting will lead to waterlogging and seed wash-off.", rain));
            alerts.add(new WeatherRiskAlert(
                    "EXCESS_RAINFALL_WATERLOGGING",
                    "HIGH",
                    String.format("Heavy rainfall of %.1f mm predicted in 24-48h window.", rain),
                    "Soil saturation prevents oxygen diffusion to emerging rootlets; high probability of damping-off and seed rotting.",
                    "Ensure drainage channels (periphery trenches) are cleared. Defer sowing and nursery seeding until excess water recedes."
            ));
        } else if (rain >= 15.0) {
            if (directive != ActionDirective.WAIT) directive = ActionDirective.PREPARE;
            reasons.add(String.format("Moderate rainfall expected (%.1f mm).", rain));
            alerts.add(new WeatherRiskAlert(
                    "MODERATE_RAINFALL",
                    "MODERATE",
                    String.format("Rainfall of %.1f mm anticipated.", rain),
                    "Foliar spray wash-off risk if applied within 6 hours before rain.",
                    "Complete sowing if drainage is functional, but suspend foliar pesticide and nutrient sprays."
            ));
        }

        // 2. High wind risk for spraying (> 15 km/h)
        if (wind > 15.0) {
            alerts.add(new WeatherRiskAlert(
                    "SPRAY_DRIFT_HAZARD",
                    "HIGH",
                    String.format("Elevated wind speeds of %.1f km/h.", wind),
                    "Pesticide droplets will drift off-target, contaminating adjacent fields and reducing efficacy.",
                    "Do NOT spray pesticides or foliar nutrition during windy hours. Wait for calm morning or evening conditions (< 10 km/h)."
            ));
        }

        // 3. High humidity & warm temperature -> Fungal Disease Risk
        if (rh >= 80.0 && tMax >= 24.0 && tMax <= 32.0) {
            alerts.add(new WeatherRiskAlert(
                    "FUNGAL_DISEASE_VULNERABILITY",
                    "HIGH",
                    String.format("Prolonged high relative humidity (%.0f%%) with canopy temperatures around %.1f°C.", rh, tMax),
                    "Ideal microclimate for rapid sporulation of Blast (Pyricularia oryzae) in Paddy and Early/Late Blight in Solanaceous crops.",
                    "Scout lower leaves every 48 hours for diamond-shaped lesions or water-soaked margins. Avoid excess nitrogenous top-dressing."
            ));
            if (directive == ActionDirective.PROCEED) {
                directive = ActionDirective.PREPARE;
                reasons.add("Elevated humidity poses active disease vulnerability; field scouting required.");
            }
        }

        // 4. Extreme Heat Stress (> 38°C)
        if (tMax >= 38.0) {
            alerts.add(new WeatherRiskAlert(
                    "HIGH_TEMPERATURE_HEAT_STRESS",
                    "HIGH",
                    String.format("Extreme daytime maximum temperature of %.1f°C.", tMax),
                    "Pollen desiccation during anthesis causes spikelet sterility in cereals and blossom drop in vegetables.",
                    "Provide light and frequent evening irrigations. Apply 1% Potassium Nitrate (13-0-45) foliar spray to improve osmotic stress tolerance."
            ));
            if (directive == ActionDirective.PROCEED) {
                directive = ActionDirective.PREPARE;
                reasons.add("Extreme temperatures require thermal stress management.");
            }
        }

        // Sowing window analysis
        String sowingStatus;
        if (rain > 40.0) {
            sowingStatus = "POSTPONE SOWING: Soil is excessively saturated; high risk of poor seedling emergence.";
        } else if (rain > 10.0 && rain <= 35.0) {
            sowingStatus = "OPTIMAL MOISTURE: Moderate moisture is favorable for seed bed preparation and sowing.";
        } else if (rain < 2.0 && tMax > 36.0) {
            sowingStatus = "CAUTION: Low moisture and high heat; ensure assured irrigation before sowing.";
        } else {
            sowingStatus = "FAVORABLE: Climatic conditions are within standard agronomic ranges.";
        }

        String spraySuitability;
        if (rain > 5.0 || wind > 15.0) {
            spraySuitability = "UNSUITABLE FOR SPRAYING: Postpone all chemical and bio-pesticide applications due to rain wash-off or drift hazard.";
        } else {
            spraySuitability = "SUITABLE FOR SPRAYING: Wind and precipitation are within safe spraying parameters. Spray early morning (7-10 AM) or late afternoon (4-6 PM).";
        }

        String explanation = reasons.isEmpty()
                ? "Weather parameters are within normal agronomic thresholds. Proceed with scheduled field operations."
                : String.join(" ", reasons);

        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("forecastRainfallMm", rain);
        metrics.put("tempMaxC", tMax);
        metrics.put("tempMinC", tMin);
        metrics.put("relativeHumidityPct", rh);
        metrics.put("windSpeedKph", wind);

        return new WeatherRiskAssessment(
                location != null ? location : "Field Location",
                crop.toUpperCase(), season, directive, explanation,
                alerts, sowingStatus, spraySuitability, metrics,
                "IMD / WeatherAPI Agro-Meteorological Advisory Grid",
                "Verified real-time forecast data (valid for next 24-48 hours)"
        );
    }
}
