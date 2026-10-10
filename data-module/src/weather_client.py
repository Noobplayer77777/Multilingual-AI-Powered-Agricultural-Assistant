import os
import re
import requests
from typing import Optional, Dict, Any, Tuple
from src.contracts import ContextItem, SourceType

def load_env_vars():
    """Loads .env file if WEATHER_API_KEY is not already in environment."""
    if "WEATHER_API_KEY" in os.environ and os.environ["WEATHER_API_KEY"]:
        return

    # Look for .env in current and parent directories
    base_dir = os.path.dirname(os.path.abspath(__file__))
    candidates = [
        os.path.join(base_dir, ".env"),
        os.path.join(os.path.dirname(base_dir), ".env"),
        os.path.join(os.path.dirname(os.path.dirname(base_dir)), ".env")
    ]
    for env_path in candidates:
        if os.path.exists(env_path):
            try:
                with open(env_path, "r", encoding="utf-8") as f:
                    for line in f:
                        line = line.strip()
                        if line and not line.startswith("#") and "=" in line:
                            k, v = line.split("=", 1)
                            k = k.strip()
                            v = v.strip().strip('"').strip("'")
                            if k not in os.environ:
                                os.environ[k] = v
                break
            except Exception:
                pass

load_env_vars()

# Common Indian location mappings
NAME_NORMALIZATIONS: Dict[str, str] = {
    # Tamil names
    "சென்னை": "Chennai",
    "மதுரை": "Madurai",
    "கோயம்புத்தூர்": "Coimbatore",
    "கோவை": "Coimbatore",
    "தஞ்சாவூர்": "Thanjavur",
    "திருச்சி": "Tiruchirappalli",
    "சேலம்": "Salem",
    "திருநெல்வேலி": "Tirunelveli",
    "ஈரோடு": "Erode",
    "வேலூர்": "Vellore",
    "திண்டுக்கல்": "Dindigul",
    "தேனி": "Theni",
    "காஞ்சிபுரம்": "Kanchipuram",
    # Hindi names
    "चेन्नई": "Chennai",
    "मदुरै": "Madurai",
    "कोयंबटूर": "Coimbatore",
    "तंजावूर": "Thanjavur",
    "दिल्ली": "New Delhi, India",
    "नई दिल्ली": "New Delhi, India",
    "लखनऊ": "Lucknow",
    "कानपुर": "Kanpur",
    "वाराणसी": "Varanasi",
    "पटना": "Patna",
    "भोपाल": "Bhopal",
    "जयपुर": "Jaipur",
    # Common English ambiguous names
    "delhi": "New Delhi, India"
}

def extract_location_from_text(text: str) -> Optional[str]:
    """
    Extracts a location mentioned in the text (English, Tamil, Hindi).
    """
    if not text:
        return None

    # Check direct script translations
    for non_en, en_name in NAME_NORMALIZATIONS.items():
        if non_en in text:
            return en_name

    # Check English city keywords
    english_cities = [
        "chennai", "madurai", "coimbatore", "thanjavur", "trichy", "tiruchirappalli",
        "salem", "tirunelveli", "erode", "vellore", "dindigul", "theni", "kanchipuram",
        "cuddalore", "villupuram", "dharmapuri", "karur", "namakkal", "pudukkottai",
        "lucknow", "kanpur", "varanasi", "delhi", "bengaluru", "bangalore", "mumbai",
        "pune", "hyderabad", "jaipur", "bhopal", "patna", "ludhiana", "amritsar"
    ]
    lower_text = text.lower()
    for city in english_cities:
        if re.search(r'\b' + re.escape(city) + r'\b', lower_text):
            return NAME_NORMALIZATIONS.get(city, city.capitalize())

    # Regex for "in <Location>", "at <Location>", "for <Location>"
    match = re.search(r'\b(?:in|at|for|near|around)\s+([A-Z][a-z]+)\b', text)
    if match:
        found = match.group(1)
        if found.lower() not in {"summer", "winter", "kharif", "rabi", "zaid", "today", "tomorrow"}:
            return found

    return None

class WeatherClient:
    WEATHER_API_URL = "http://api.weatherapi.com/v1/current.json"
    OPEN_METEO_URL = "https://api.open-meteo.com/v1/forecast"
    GEOCODING_URL = "https://geocoding-api.open-meteo.com/v1/search"

    def __init__(self):
        self.session = requests.Session()
        self.api_key = os.getenv("WEATHER_API_KEY", "").strip()

    def normalize_location(self, loc: str) -> str:
        if not loc:
            return loc
        return NAME_NORMALIZATIONS.get(loc.strip().lower(), NAME_NORMALIZATIONS.get(loc.strip(), loc.strip()))

    def get_weather_data(self, location_name: str) -> Optional[Dict[str, Any]]:
        """
        Fetches structured weather data using WeatherAPI.com (if key available)
        or falls back to Open-Meteo.
        """
        norm_location = self.normalize_location(location_name)
        if not norm_location:
            return None

        # 1. Try WeatherAPI.com if API key is present
        if self.api_key:
            try:
                resp = self.session.get(
                    self.WEATHER_API_URL,
                    params={"key": self.api_key, "q": norm_location, "aqi": "no"},
                    timeout=8
                )
                if resp.status_code == 200:
                    data = resp.json()
                    loc = data.get("location", {})
                    cur = data.get("current", {})

                    loc_display = f"{loc.get('name')}, {loc.get('region')}" if loc.get('region') else loc.get('name', norm_location)
                    temp_c = cur.get("temp_c", 0.0)
                    feels_c = cur.get("feelslike_c", temp_c)
                    humidity = cur.get("humidity", 0)
                    precip_mm = cur.get("precip_mm", 0.0)
                    cond = cur.get("condition", {}).get("text", "Clear")
                    wind_kph = cur.get("wind_kph", 0.0)
                    wind_dir = cur.get("wind_dir", "N")
                    uv = cur.get("uv", 0.0)

                    return {
                        "location": loc_display,
                        "temperature": temp_c,
                        "feels_like": feels_c,
                        "humidity": humidity,
                        "rainfall": precip_mm,
                        "condition": cond,
                        "wind_kph": wind_kph,
                        "wind_dir": wind_dir,
                        "uv": uv,
                        "source": "WeatherAPI.com",
                        "url": f"https://www.weatherapi.com/weather/q/{norm_location.replace(' ', '-')}"
                    }
            except Exception:
                pass

        # 2. Fallback to Open-Meteo
        try:
            geo_resp = self.session.get(
                self.GEOCODING_URL,
                params={"name": norm_location, "count": 1, "language": "en", "format": "json"},
                timeout=8
            )
            if geo_resp.status_code == 200:
                geo_data = geo_resp.json()
                if "results" in geo_data and geo_data["results"]:
                    r = geo_data["results"][0]
                    lat, lon = r["latitude"], r["longitude"]
                    loc_display = r.get("name", norm_location)

                    weather_resp = self.session.get(
                        self.OPEN_METEO_URL,
                        params={
                            "latitude": lat,
                            "longitude": lon,
                            "current": ["temperature_2m", "relative_humidity_2m", "precipitation", "rain", "wind_speed_10m"],
                            "timezone": "auto"
                        },
                        timeout=8
                    )
                    if weather_resp.status_code == 200:
                        w_data = weather_resp.json()
                        cur = w_data.get("current", {})
                        temp_c = cur.get("temperature_2m", 0.0)
                        humidity = cur.get("relative_humidity_2m", 0)
                        precip_mm = cur.get("precipitation", cur.get("rain", 0.0))
                        wind_kph = cur.get("wind_speed_10m", 0.0)

                        return {
                            "location": loc_display,
                            "temperature": temp_c,
                            "feels_like": temp_c,
                            "humidity": humidity,
                            "rainfall": precip_mm,
                            "condition": "Observation",
                            "wind_kph": wind_kph,
                            "wind_dir": "Local",
                            "uv": 0.0,
                            "source": "Open-Meteo",
                            "url": f"https://open-meteo.com/en/weather?lat={lat}&lon={lon}"
                        }
        except Exception:
            pass

        return None

    def get_weather_context(self, location_name: str) -> Optional[ContextItem]:
        """
        Fetches current weather and forecast for a location and returns it as a ContextItem.
        """
        data = self.get_weather_data(location_name)
        if not data:
            return None

        text = (
            f"Live Meteorological Weather for {data['location']} (Source: {data['source']}):\n"
            f"- Temperature: {data['temperature']}°C (Feels like: {data['feels_like']}°C)\n"
            f"- Relative Humidity: {data['humidity']}%\n"
            f"- Rainfall / Precipitation: {data['rainfall']} mm\n"
            f"- Weather Condition: {data['condition']}\n"
            f"- Wind Speed: {data['wind_kph']} km/h ({data['wind_dir']})\n"
            f"- UV Index: {data['uv']}"
        )

        source_name = (
            f"{data['source']} — {data['location']}: {data['temperature']}°C, "
            f"{data['humidity']}% Humidity, {data['rainfall']}mm Rain ({data['condition']})"
        )

        return ContextItem(
            text=text,
            sourceName=source_name,
            sourceUrl=data["url"],
            type=SourceType.API
        )
