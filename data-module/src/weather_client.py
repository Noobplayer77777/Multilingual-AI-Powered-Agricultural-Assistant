import requests
from typing import Optional, Dict, Any
from src.contracts import ContextItem, SourceType

class WeatherClient:
    BASE_URL = "https://api.open-meteo.com/v1/forecast"
    GEOCODING_URL = "https://geocoding-api.open-meteo.com/v1/search"

    def __init__(self):
        self.session = requests.Session()

    def get_coordinates(self, location_name: str) -> Optional[tuple[float, float, str]]:
        """
        Geocodes a location name to coordinates.
        Returns a tuple of (latitude, longitude, formatted_name) or None if not found.
        """
        try:
            response = self.session.get(
                self.GEOCODING_URL,
                params={"name": location_name, "count": 1, "language": "en", "format": "json"},
                timeout=10
            )
            response.raise_for_status()
            data = response.json()
            if "results" in data and len(data["results"]) > 0:
                result = data["results"][0]
                return (result["latitude"], result["longitude"], result.get("name", location_name))
            return None
        except requests.RequestException:
            return None

    def get_weather_context(self, location_name: str) -> Optional[ContextItem]:
        """
        Fetches current weather and forecast for a location and returns it as a ContextItem.
        """
        coords = self.get_coordinates(location_name)
        if not coords:
            # Cannot validate location
            return None
            
        lat, lon, formatted_name = coords

        try:
            params = {
                "latitude": lat,
                "longitude": lon,
                "current_weather": True,
                "daily": ["temperature_2m_max", "temperature_2m_min", "precipitation_sum"],
                "timezone": "auto"
            }
            response = self.session.get(self.BASE_URL, params=params, timeout=10)
            response.raise_for_status()
            data = response.json()
            
            # Parse current (observation) and forecast
            current = data.get("current_weather", {})
            daily = data.get("daily", {})
            
            current_temp = current.get("temperature", "N/A")
            current_wind = current.get("windspeed", "N/A")
            
            # Safely get today's forecast
            max_temp = daily.get("temperature_2m_max", ["N/A"])[0] if daily.get("temperature_2m_max") else "N/A"
            min_temp = daily.get("temperature_2m_min", ["N/A"])[0] if daily.get("temperature_2m_min") else "N/A"
            precip = daily.get("precipitation_sum", ["N/A"])[0] if daily.get("precipitation_sum") else "N/A"
            
            text = (
                f"Weather for {formatted_name} (Lat: {lat}, Lon: {lon}):\n"
                f"- Current Observation: {current_temp}°C, Wind: {current_wind} km/h\n"
                f"- Today's Forecast: Max {max_temp}°C, Min {min_temp}°C, Precipitation: {precip} mm"
            )
            
            return ContextItem(
                text=text,
                sourceName="Open-Meteo",
                sourceUrl=f"https://open-meteo.com/en/weather?lat={lat}&lon={lon}",
                type=SourceType.API
            )

        except requests.RequestException:
            return None
