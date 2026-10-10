# Integration Test Cases - Data Module

This test sheet covers the quality assurance requirements for the knowledge retrieval and external API (weather) integration.
Run these tests manually or via automation after bringing up the `docker-compose` environment.

| ID | Category | Language | Query / Scenario | Expected Behavior | Actual Result | Status |
|---|---|---|---|---|---|---|
| TC01 | Crop Recommendation | English | "What are the best practices for tomato cultivation in summer?" | Retrieves ICAR/TNAU docs on tomatoes, correctly labeled as `DOCUMENT`. | | PENDING |
| TC02 | Crop Recommendation | Tamil | "கோடையில் தக்காளி சாகுபடிக்கான சிறந்த நடைமுறைகள் யாவை?" | Retrieves same/similar documents in Tamil/English (depending on embedding). Labeled as `DOCUMENT`. | | PENDING |
| TC03 | Crop Recommendation | Hindi | "गर्मियों में टमाटर की खेती के लिए सबसे अच्छे तरीके क्या हैं?" | Retrieves appropriate documents. Matches Tamil/English semantic space. | | PENDING |
| TC04 | Weather | English | "What is the weather in Chennai today?" | Retrieves weather forecast from Open-Meteo for Chennai. Source `API`. | | PENDING |
| TC05 | Weather | Tamil | "இன்று கோயம்புத்தூர் வானிலை எப்படி இருக்கும்?" | Retrieves weather for Coimbatore, translates query or directly fetches coordinates. | | PENDING |
| TC06 | Missing Location | English | "What is the weather like?" | System should prompt for location or fallback gracefully. No API crash. | | PENDING |
| TC07 | Missing Evidence | English | "How to grow truffles in space?" | Empty retrieval or low confidence. Should not hallucinate evidence. | | PENDING |
| TC08 | Unsupported Crop | English | "Give me recommendations for growing dragon fruit." | Returns "No specific data found in ICAR/TNAU DB" or empty retrieval list. | | PENDING |
| TC09 | Empty Retrieval | English | "asdfghjkl" (gibberish) | Returns 0 `ContextItem`s. System handles empty list correctly. | | PENDING |
| TC10 | API Failure | English | Simulate network drop / API rate limit for Open-Meteo. | Weather client returns `None`. System gracefully informs user. | | PENDING |
| TC11 | Source Attribution | English | "Tell me about Rice Stem Borer." | Context clearly indicates source (e.g., TNAU) and URL in `ContextItem`. | | PENDING |
| TC12 | Language Consistency | Tamil | "நெல் பற்றிய தகவல்கள்" | Ensures response leverages the retrieved context but correctly responds in Tamil. | | PENDING |
| TC13 | Weather History | English | "What was the weather yesterday in Delhi?" | Client distinguishes forecast from history (or states history unsupported). | | PENDING |
| TC14 | Invalid Location | English | "Weather in Atlantis" | Open-Meteo returns no coordinates, gracefully handled as `None`. | | PENDING |
| TC15 | DB Health | N/A | Check `docker-compose` PostgreSQL healthcheck | DB should be `healthy`, `pgvector` extension should be created successfully. | | PENDING |
| TC16 | Duplicate Ingestion | N/A | Run `ingestion.py` twice | Second run should detect existing titles/URLs and skip duplicates. | | PENDING |
| TC17 | Malformed Docs | N/A | Ingest document with null title or content | Database constraints or ingestion script should reject and log the error. | | PENDING |
| TC18 | Mock Data Isolation | N/A | Trigger `mock_retrieval.py` | Content should clearly state `[MOCK CONTENT - NOT VERIFIED EVIDENCE]`. | | PENDING |
