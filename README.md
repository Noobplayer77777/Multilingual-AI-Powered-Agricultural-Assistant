

# 🌾 Multilingual AI-Powered Agricultural Assistant (Kisan AI)

> An AI assistant that gives farmers practical, multilingual crop and farming advice, grounded in a local knowledge base (RAG) and live weather data.

<img width="9556" height="8153" alt="diagram (4)" src="https://github.com/user-attachments/assets/6563ea42-d189-4c22-aa84-9c15a833b54b" />


---

## 📖 About the Project

Farmers often struggle to get timely, reliable advice in their own language. **Kisan AI** is a multilingual assistant where a farmer asks a question in their preferred language and gets an answer that is:

- **Grounded**: answers are generated from a curated knowledge base (PDF, TXT, MD and JSON files) using **Retrieval-Augmented Generation (RAG)**, which reduces hallucinations.
- **Context-aware**: combined with **live weather data** so advice reflects current conditions.
- **Private and offline-friendly**: runs on a **local LLM through Ollama**, so no internet scraping and no data sent to third-party AI APIs (Gemini is supported as an optional alternative).
- **Easy to use**: a simple web frontend served by the query service.

## ✨ Features

- 🌐 Multilingual question answering : English, Hindi, Tamil
- 📚 RAG pipeline with vector similarity search over your own agricultural documents
- 🌦️ Weather-aware advisory via WeatherAPI
- 🧠 Local LLM inference with Ollama (`qwen2.5:7b-instruct-q4_K_M`) and embeddings (`nomic-embed-text`)
- 🔌 Pluggable AI provider: `ollama` (default) or `gemini`
- 🧩 Microservice architecture: query, advisory and retrieval services
- 🖥️ Multi-device deployment: run the RAG/Ollama stack on a separate GPU machine over LAN
- 🐳 One-command local database setup with Docker (PostgreSQL + pgvector)
- 🚀 PowerShell scripts to start and stop everything at once

## 🏗️ Architecture

The system is split into three services plus a vector database:

| Service | Folder | Tech | Port | Role |
|---|---|---|---|---|
| **Query Service** | `query-service/` | Spring Boot (Java) | `8081` | Entry point: serves the frontend, receives user questions, orchestrates the pipeline |
| **Advisory Service** | `advisory-service/` | Spring Boot (Java) | `8082` | Builds the final advice using retrieved context, weather data and the LLM |
| **Retrieval Service** | `data-module/` | Python | `8083` | Ingests knowledge files, creates embeddings and performs similarity search |
| **Database** | `docker-compose.yml` / Neon | PostgreSQL + pgvector | `5433` (local) | Stores documents and vector embeddings |
| **LLM Engine** | n/a | Ollama | `11434` | Runs the chat model and embedding model locally |

Other folders: `contracts/` (shared API contracts between services), `docs/` (documentation), `scripts/` (helper scripts).

```
User ──▶ Query Service (8081) ──▶ Advisory Service (8082) ──▶ Ollama / Gemini
                                         │
                                         ├──▶ Retrieval Service (8083) ──▶ PostgreSQL + pgvector
                                         └──▶ WeatherAPI
```

## 🛠️ Tech Stack

**Backend**
- Java, Spring Boot (Maven Wrapper included)
- Python (retrieval / data module)

**AI / ML**
- Ollama (local LLM hosting)
- Chat model: `qwen2.5:7b-instruct-q4_K_M`
- Embedding model: `nomic-embed-text`
- Optional: Google Gemini
- Retrieval-Augmented Generation (RAG)

**Database**
- PostgreSQL 16 with the **pgvector** extension
- Neon Serverless Postgres (cloud, recommended) or local Docker

**External APIs**
- WeatherAPI

**DevOps / Tooling**
- Docker & Docker Compose
- PowerShell automation scripts
- Spring Boot Actuator health checks

## ✅ Prerequisites

Make sure you have the following installed:

- **Java JDK 17+**
- **Python 3.9+**
- **Docker Desktop** (only if using the local database)
- **[Ollama](https://ollama.com/download)**
- A free **[WeatherAPI](https://www.weatherapi.com/)** key
- *(Optional)* A **[Neon](https://console.neon.tech)** account for a managed Postgres + pgvector database

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/Noobplayer77777/Multilingual-AI-Powered-Agricultural-Assistant.git
cd Multilingual-AI-Powered-Agricultural-Assistant
```

### 2. Configure environment variables

```bash
cp .env.example .env        # Windows PowerShell: Copy-Item .env.example .env
```

Open `.env` and fill in your values:

| Variable | Description | Default |
|---|---|---|
| `DATABASE_URL` | Neon connection string (recommended) | n/a |
| `DB_NAME` / `DB_USER` / `DB_PASSWORD` / `DB_PORT` | Local Docker Postgres settings (if not using Neon) | `agridb` / `agriuser` / `agripass` / `5433` |
| `WEATHER_API_KEY` | Your WeatherAPI key | n/a |
| `AI_PROVIDER` | `ollama` or `gemini` | `ollama` |
| `OLLAMA_URL` | Ollama server URL | `http://localhost:11434` |
| `OLLAMA_MODEL` | Chat model | `qwen2.5:7b-instruct-q4_K_M` |
| `EMBEDDING_MODEL` | Embedding model | `nomic-embed-text:latest` |
| `RETRIEVAL_SERVICE_URL` | Retrieval service URL | `http://localhost:8083` |
| `RETRIEVAL_PORT` | Retrieval service port | `8083` |
| `SIMILARITY_THRESHOLD` | Minimum similarity score for a chunk to be used | `0.55` |
| `TOP_K_CHUNKS` | Number of chunks retrieved per query | `4` |
| `KNOWLEDGE_FILES_DIR` | Folder with your knowledge documents | `data-module/knowledge_files` |

> ⚠️ Never commit your `.env` file. It is already listed in `.gitignore`.

### 3. Pull the Ollama models

```bash
ollama pull qwen2.5:7b-instruct-q4_K_M
ollama pull nomic-embed-text
```

### 4. Add your knowledge base

Place your agricultural documents (**PDF, TXT, MD, JSON**) in:

```
data-module/knowledge_files/
```

These are chunked, embedded and used by the retrieval service to ground the answers.

### 5. Install Python dependencies

```bash
cd data-module
pip install -r requirements.txt   # TODO: confirm the requirements file path
cd ..
```

### 6. Run the project

#### Option A: One command (Windows, PowerShell)

```powershell
.\start-all.ps1
```

This script will:
1. Use Neon if configured, otherwise start the local PostgreSQL + pgvector container with Docker
2. Check that Ollama is running
3. Launch the Retrieval Service (`8083`), Advisory Service (`8082`) and Query Service (`8081`) in separate terminals
4. Wait for the health check and open **http://localhost:8081** in your browser

To stop everything:

```powershell
.\stop-all.ps1
```

#### Option B: Run each service manually (any OS)

```bash
# 1. Database (skip if using Neon)
docker compose up -d

# 2. Make sure Ollama is running
ollama serve

# 3. Retrieval service (port 8083)
python data-module/src/retrieval_server.py

# 4. Advisory service (port 8082)
./mvnw spring-boot:run -f advisory-service/pom.xml

# 5. Query service + frontend (port 8081)
./mvnw spring-boot:run -f query-service/pom.xml
```

Then open **http://localhost:8081**.

## 💬 How to Use

1. Open **http://localhost:8081** in your browser.
2. Choose your preferred language. 
3. Type your farming question, for example:
   - *"Which crop is best to sow this month in my region?"*
   - *"My tomato leaves are turning yellow. What should I do?"*
   - *"Will it rain this week? Should I irrigate today?"*
4. Receive an answer grounded in the knowledge base and current weather.

<img width="1547" height="1600" alt="WhatsApp Image 2026-10-11 at 10 16 20" src="https://github.com/user-attachments/assets/b63fe461-1de7-4a3e-88ac-d5efb44fab0a" />
<img width="1600" height="792" alt="image" src="https://github.com/user-attachments/assets/e5d36e3e-b3a6-4313-81e4-0aea1f347bee" />


## 🖧 Running RAG on a Separate Device (Optional)

If you have a more powerful machine (e.g. with a GPU), run Ollama and the retrieval service there and point the other services to it. In `.env` on the main machine:

```env
RETRIEVAL_SERVICE_URL=http://<DEVICE_B_LAN_IP>:8083
OLLAMA_URL=http://<DEVICE_B_LAN_IP>:11434
```

## 📂 Project Structure

```
.
├── advisory-service/     # Spring Boot service that generates advice (8082)
├── query-service/        # Spring Boot service + frontend (8081)
├── data-module/          # Python RAG retrieval service + knowledge files (8083)
├── contracts/            # Shared API contracts
├── docs/                 # Documentation and diagrams
├── scripts/              # Helper scripts
├── docker-compose.yml    # Local PostgreSQL + pgvector
├── init.sql              # Database initialization
├── .env.example          # Environment variable template
├── start-all.ps1         # Start all services (Windows)
└── stop-all.ps1          # Stop all services (Windows)
```

## 🩺 Troubleshooting

| Problem | Fix |
|---|---|
| `Ollama is not responding at localhost:11434` | Start Ollama Desktop or run `ollama serve` |
| Database connection errors | Verify `DATABASE_URL` or make sure the Docker container is running (`docker compose ps`) |
| Empty or irrelevant answers | Add documents to `knowledge_files/`, or lower `SIMILARITY_THRESHOLD` |
| Port already in use | Stop the process using ports `8081`, `8082`, `8083` or `5433` |
| Services slow to start | The first Maven run downloads dependencies and compiles, so give it a minute |

## 🗺️ Roadmap

- [ ] Add more regional languages
- [ ] Voice input and output for low-literacy users
- [ ] Crop disease detection from photos
- [ ] Market price and government scheme integration
- [ ] Mobile app

## 🤝 Contributing

Contributions are welcome!

1. Fork the repo
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m "Add your feature"`
4. Push to the branch: `git push origin feature/your-feature`
5. Open a Pull Request

## 📄 License

<!-- TODO: add a LICENSE file (e.g. MIT) and update this line -->
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## 👤 Author

**Vijayendra S**
GitHub: [@Noobplayer77777](https://github.com/Noobplayer77777)

---

⭐ If you found this project useful, consider giving it a star!
