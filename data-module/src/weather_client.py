import os
import requests
from typing import Optional, Dict, Any, Tuple
from src.contracts import ContextItem, SourceType

def _load_env_fallback():
    """Lightweight loader for .env file if environment variables are not pre-loaded."""
    candidates = [
        os.path.join(os.getcwd(), ".env"),
        os.path.join(os.path.dirname(__file__), "..", "..", ".env"),
        os.path.join(os.path.dirname(__file__), "..", ".env"),
    ]
    for path in candidates:
        if os.path.isfile(path):
            try:
                with open(path, "r", encoding="utf-8") as f:
                    for line in f:
                        line = line.strip()
                        if line and not line.startswith("#") and "=" in line:
                            k, v = line.split("=", 1)
                            k = k.strip()
                            v = v.strip().strip("'\"")
                            if k not in os.environ:
                                os.environ[k] = v
            except Exception:
                pass
            break

class WeatherClient:
    WEATHER_API_BASE_URL = "https://api.weatherapi.com/v1/forecast.json"
    OPEN_METEO_BASE_URL = "https://api.open-meteo.com/v1/forecast"
    OPEN_METEO_GEOCODING_URL = "https://geocoding-api.open-meteo.com/v1/search"

    def __init__(self, api_key: Optional[str] = None):
        _load_env_fallback()
        self.api_key = api_key or os.getenv("WEATHER_API_KEY")
        self.session = requests.Session()

    def get_weather_context(self, location_name: str) -> Optional[ContextItem]:
        """
        Fetches current weather and forecast for a location and returns it as a ContextItem.
        Prioritizes WeatherAPI.com when an API key is available, falling back to Open-Meteo.
        """
        if not location_name or not location_name.strip():
            return None

        clean_location = location_name.strip()

        # 1. Try WeatherAPI.com if key is present
        if self.api_key:
            weather_ctx = self._fetch_weatherapi(clean_location)
            if weather_ctx:
                return weather_ctx

        # 2. Fallback to Open-Meteo
        return self._fetch_open_meteo(clean_location)

    def _fetch_weatherapi(self, location_name: str) -> Optional[ContextItem]:
        """Fetch current weather and 3-day forecast from WeatherAPI.com."""
        try:
            params = {
                "key": self.api_key,
                "q": location_name,
                "days": 3,
                "aqi": "no",
                "alerts": "no"
            }
            response = self.session.get(self.WEATHER_API_BASE_URL, params=params, timeout=10)
            response.raise_for_status()
            data = response.json()

            loc = data.get("location", {})
            name = loc.get("name", location_name)
            region = loc.get("region", "")
            country = loc.get("country", "")
            lat = loc.get("lat", "")
            lon = loc.get("lon", "")

            location_display = f"{name}"
            if region:
                location_display += f", {region}"
            if country:
                location_display += f" ({country})"

            current = data.get("current", {})
            temp_c = current.get("temp_c", "N/A")
            feelslike_c = current.get("feelslike_c", "N/A")
            humidity = current.get("humidity", "N/A")
            wind_kph = current.get("wind_kph", "N/A")
            precip_mm = current.get("precip_mm", 0.0)
            condition_text = current.get("condition", {}).get("text", "N/A")

            forecast_days = data.get("forecast", {}).get("forecastday", [])
            forecast_lines = []
            for fday in forecast_days:
                date = fday.get("date", "")
                day_info = fday.get("day", {})
                max_t = day_info.get("maxtemp_c", "N/A")
                min_t = day_info.get("mintemp_c", "N/A")
                cond = day_info.get("condition", {}).get("text", "")
                rain_chance = day_info.get("daily_chance_of_rain", 0)
                precip = day_info.get("totalprecip_mm", 0.0)
                forecast_lines.append(
                    f"  * {date}: Max {max_t}°C, Min {min_t}°C, Condition: {cond}, Rain Chance: {rain_chance}%, Precip: {precip} mm"
                )

            forecast_str = "\n".join(forecast_lines) if forecast_lines else "  * Forecast data unavailable."

            text = (
                f"Weather for {location_display} [Lat: {lat}, Lon: {lon}]:\n"
                f"- Current Observation: {temp_c}°C (Feels like: {feelslike_c}°C), {condition_text}, "
                f"Humidity: {humidity}%, Wind: {wind_kph} km/h, Precipitation: {precip_mm} mm\n"
                f"- Forecast (Next 3 Days):\n{forecast_str}"
            )

            return ContextItem(
                text=text,
                sourceName="WeatherAPI.com",
                sourceUrl=f"https://www.weatherapi.com/",
                type=SourceType.API
            )
        except requests.RequestException:
            return None

    def get_coordinates(self, location_name: str) -> Optional[Tuple[float, float, str]]:
        """
        Geocodes a location name to coordinates using Open-Meteo.
        Returns a tuple of (latitude, longitude, formatted_name) or None if not found.
        """
        try:
            response = self.session.get(
                self.OPEN_METEO_GEOCODING_URL,
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

    def _fetch_open_meteo(self, location_name: str) -> Optional[ContextItem]:
        """Fallback to Open-Meteo API when WeatherAPI is unavailable."""
        coords = self.get_coordinates(location_name)
        if not coords:
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
            response = self.session.get(self.OPEN_METEO_BASE_URL, params=params, timeout=10)
            response.raise_for_status()
            data = response.json()

            current = data.get("current_weather", {})
            daily = data.get("daily", {})

            current_temp = current.get("temperature", "N/A")
            current_wind = current.get("windspeed", "N/A")

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
