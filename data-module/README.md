# Data Module - Multilingual AI-Powered Agricultural Assistant

**Owner:** Member C

This module is responsible for the persistent data layer, vector embeddings (PostgreSQL + pgvector), document ingestion, and external API integrations (weather).

## Architecture & Deployment Notes

- **Database**: PostgreSQL 16 with `pgvector` extension.
- **Data Persistence**: Uses a Docker volume (`pgdata`) to persist data across container restarts.
- **Configuration**: Connection strings and credentials are injected via environment variables. See `../.env.example` for details. Do not hardcode `localhost` for container-to-container communication; use the service name `db`.
- **Port Mapping**: For local host access, use port `5432`. Inside the Docker network, use `db:5432`.

## Database Schema Documentation

Refer to `../init.sql` for the complete initial schema.
Key tables:
- `users`: Tracks user profiles.
- `queries` / `responses`: Stores conversation history.
- `sources`: Tracks references used in responses.
- `documents`: Knowledge base root table storing titles, sources, and URLs.
- `document_chunks`: Stores chunked text and its vector embedding (`VECTOR`). *Note: Dimension size is pending the selection of the embedding model.*

## Data-Source Inventory

1. **ICAR (Indian Council of Agricultural Research)**: General crop guidelines.
2. **TNAU (Tamil Nadu Agricultural University)**: Regional and crop-specific pest management.
3. **Open-Meteo API**: Free, open-source weather API for current observations and forecasts.

## Ingestion and Retrieval Instructions

### Ingestion

To ingest initial documents into the database:
1. Ensure the database is running: `docker-compose up -d db`
2. Install Python dependencies: `pip install -r requirements.txt`
3. Run the script: `python src/ingestion.py`

This will read `INITIAL_DOCS` and populate `documents` and `document_chunks` securely, avoiding duplicates.

### Retrieval (Contracts)

Member B can consume the unified `RetrievalInterface` (see `src/contracts.py`).
- Use `mock_retrieval.py` for early integration without a live database. Mock items clearly indicate they are `[MOCK CONTENT]`.
- For weather, use `WeatherClient` in `src/weather_client.py` which returns `ContextItem` elements.

## Quality Assurance & Testing

Refer to `tests/test_cases.md` for the comprehensive integration test sheet. Includes 18 distinct scenarios covering languages, external APIs, graceful fallbacks, and DB health.

## Final Demo Checklist

- [ ] Docker Compose correctly starts DB and mounts volume.
- [ ] `pgvector` extension is successfully enabled (`CREATE EXTENSION vector;`).
- [ ] Sample agricultural documents (10-20) are ingested successfully.
- [ ] Weather API returns live forecast and validates locations correctly.
- [ ] Mock data is easily distinguishable.
- [ ] Members A and B have verified integration with `RetrievalInterface`.
