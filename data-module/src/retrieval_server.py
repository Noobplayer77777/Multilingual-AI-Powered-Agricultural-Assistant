import json
import os
import sys
import re
from http.server import HTTPServer, BaseHTTPRequestHandler
from typing import List, Dict, Any, Set

# Ensure UTF-8 console output
if sys.stdout.encoding != 'utf-8':
    try:
        sys.stdout.reconfigure(encoding='utf-8', errors='replace')
    except Exception:
        pass

# Add module path
current_dir = os.path.dirname(os.path.abspath(__file__))
module_dir = os.path.dirname(current_dir)
if module_dir not in sys.path:
    sys.path.append(module_dir)

from src.ingestion import generate_embedding, KnowledgeIngestor, load_env_vars
from src.file_ingestor import FileKnowledgeIngestor
from src.weather_client import WeatherClient, extract_location_from_text

load_env_vars()

HOST = os.getenv("RETRIEVAL_HOST", "0.0.0.0")
PORT = int(os.getenv("RETRIEVAL_PORT", 8083))
SIMILARITY_THRESHOLD = float(os.getenv("SIMILARITY_THRESHOLD", 0.55))
TOP_K_CHUNKS = int(os.getenv("TOP_K_CHUNKS", 4))

file_ingestor = FileKnowledgeIngestor()
weather_client = WeatherClient()

STOPWORDS = {
    "what", "when", "where", "which", "who", "whom", "this", "that", "these", "those",
    "am", "is", "are", "was", "were", "be", "been", "being", "have", "has", "had",
    "having", "do", "does", "did", "doing", "would", "should", "could", "ought",
    "the", "and", "but", "if", "or", "because", "as", "until", "while", "of", "at",
    "by", "for", "with", "about", "against", "between", "into", "through", "during",
    "before", "after", "above", "below", "to", "from", "up", "down", "in", "out",
    "on", "off", "over", "under", "again", "further", "then", "once", "here", "there",
    "all", "any", "both", "each", "few", "more", "most", "other", "some", "such",
    "no", "nor", "not", "only", "own", "same", "so", "than", "too", "very", "can",
    "will", "just", "tell", "give", "please", "solve", "solution", "problem", "kisan"
}


def extract_search_terms(text: str) -> List[str]:
    """Extracts significant keywords for hybrid retrieval."""
    if not text:
        return []
    words = re.findall(r'[\w\u0900-\u097F\u0B80-\u0BFF]+', text.lower())
    terms = [w for w in words if len(w) >= 3 and w not in STOPWORDS]
    return terms[:8]


class RetrievalRequestHandler(BaseHTTPRequestHandler):

    def log_message(self, format, *args):
        # Clean logging
        sys.stderr.write(f"[{self.log_date_time_string()}] {format % args}\n")

    def do_GET(self):
        if self.path in ("/health", "/actuator/health"):
            try:
                docs_count = 0
                chunks_count = 0
                with file_ingestor.get_connection() as conn:
                    with conn.cursor() as cur:
                        cur.execute("SELECT count(*) FROM documents")
                        docs_count = cur.fetchone()[0]
                        cur.execute("SELECT count(*) FROM document_chunks")
                        chunks_count = cur.fetchone()[0]

                self.send_response(200)
                self.send_header("Content-Type", "application/json")
                self.end_headers()
                self.wfile.write(json.dumps({
                    "status": "UP",
                    "database": "connected",
                    "documentsCount": docs_count,
                    "chunksCount": chunks_count,
                    "topK": TOP_K_CHUNKS,
                    "threshold": SIMILARITY_THRESHOLD
                }).encode("utf-8"))
            except Exception as e:
                self.send_response(503)
                self.send_header("Content-Type", "application/json")
                self.end_headers()
                self.wfile.write(json.dumps({"status": "DOWN", "error": str(e)}).encode("utf-8"))

        elif self.path == "/documents":
            try:
                docs = file_ingestor.list_indexed_documents()
                self.send_response(200)
                self.send_header("Content-Type", "application/json")
                self.end_headers()
                self.wfile.write(json.dumps(docs, ensure_ascii=False).encode("utf-8"))
            except Exception as e:
                self.send_response(500)
                self.send_header("Content-Type", "application/json")
                self.end_headers()
                self.wfile.write(json.dumps({"error": str(e)}).encode("utf-8"))
        else:
            self.send_response(404)
            self.end_headers()

    def do_POST(self):
        if self.path == "/ingest":
            try:
                content_len = int(self.headers.get("Content-Length", 0))
                post_body = self.rfile.read(content_len).decode("utf-8") if content_len > 0 else "{}"
                body = json.loads(post_body) if post_body.strip() else {}
                reindex = body.get("reindex", False)

                results = file_ingestor.ingest_directory(reindex=reindex)
                self.send_response(200)
                self.send_header("Content-Type", "application/json")
                self.end_headers()
                self.wfile.write(json.dumps({
                    "status": "success",
                    "filesProcessed": len(results),
                    "details": results
                }, ensure_ascii=False).encode("utf-8"))
            except Exception as e:
                self.send_response(500)
                self.send_header("Content-Type", "application/json")
                self.end_headers()
                self.wfile.write(json.dumps({"error": str(e)}).encode("utf-8"))

        elif self.path == "/retrieve":
            try:
                content_len = int(self.headers.get("Content-Length", 0))
                post_body = self.rfile.read(content_len).decode("utf-8")
                request_data = json.loads(post_body) if post_body else {}

                question = request_data.get("question", "")
                location = request_data.get("location")
                crop = request_data.get("crop")

                context_items: List[Dict[str, Any]] = []
                seen_texts: Set[str] = set()

                # 1. Document Vector & Hybrid Search if question provided
                if question and question.strip():
                    query_text = f"{crop} {question}".strip() if crop else question
                    emb = generate_embedding(query_text)
                    vector_str = "[" + ",".join(str(f) for f in emb) + "]"
                    search_terms = extract_search_terms(query_text)

                    try:
                        with file_ingestor.get_connection() as conn:
                            with conn.cursor() as cur:
                                # A. Vector Similarity Retrieval
                                cur.execute(
                                    """
                                    SELECT c.content, d.title, d.source_organization, d.url,
                                           (c.embedding <=> %s::vector) AS dist
                                    FROM document_chunks c
                                    JOIN documents d ON c.document_id = d.id
                                    ORDER BY dist ASC
                                    LIMIT %s;
                                    """,
                                    (vector_str, TOP_K_CHUNKS * 2)
                                )
                                vector_rows = cur.fetchall()
                                for row in vector_rows:
                                    content, title, org, url, dist = row
                                    if dist <= SIMILARITY_THRESHOLD and content not in seen_texts:
                                        seen_texts.add(content)
                                        context_items.append({
                                            "text": content,
                                            "sourceName": f"{title} ({org})",
                                            "sourceUrl": url,
                                            "type": "DOCUMENT",
                                            "distance": round(float(dist), 4)
                                        })

                                # B. Keyword Fallback / Augment for exact terms (pesticides, pests, crop names)
                                if len(context_items) < TOP_K_CHUNKS and search_terms:
                                    # Build keyword conditions
                                    ilike_clauses = " OR ".join(["c.content ILIKE %s OR d.title ILIKE %s" for _ in search_terms])
                                    params = []
                                    for t in search_terms:
                                        params.extend([f"%{t}%", f"%{t}%"])
                                    
                                    cur.execute(
                                        f"""
                                        SELECT c.content, d.title, d.source_organization, d.url,
                                               (c.embedding <=> %s::vector) AS dist
                                        FROM document_chunks c
                                        JOIN documents d ON c.document_id = d.id
                                        WHERE {ilike_clauses}
                                        ORDER BY dist ASC
                                        LIMIT %s;
                                        """,
                                        [vector_str] + params + [TOP_K_CHUNKS]
                                    )
                                    kw_rows = cur.fetchall()
                                    for row in kw_rows:
                                        content, title, org, url, dist = row
                                        if content not in seen_texts and len(context_items) < TOP_K_CHUNKS:
                                            seen_texts.add(content)
                                            context_items.append({
                                                "text": content,
                                                "sourceName": f"{title} ({org})",
                                                "sourceUrl": url,
                                                "type": "DOCUMENT",
                                                "distance": round(float(dist), 4)
                                            })

                    except Exception as db_err:
                        sys.stderr.write(f"DB search error: {db_err}\n")

                # Limit to TOP_K_CHUNKS
                context_items = context_items[:TOP_K_CHUNKS]

                # 2. Live Weather Context (from question text or explicit location)
                detected_location = extract_location_from_text(question)
                target_location = detected_location or (location.strip() if location and location.strip() else None)

                if target_location:
                    try:
                        weather_item = weather_client.get_weather_context(target_location)
                        if weather_item:
                            context_items.append({
                                "text": weather_item.text,
                                "sourceName": weather_item.sourceName,
                                "sourceUrl": weather_item.sourceUrl,
                                "type": weather_item.type.value if hasattr(weather_item.type, 'value') else str(weather_item.type)
                            })
                    except Exception as weather_err:
                        sys.stderr.write(f"Weather error: {weather_err}\n")

                self.send_response(200)
                self.send_header("Content-Type", "application/json")
                self.end_headers()
                self.wfile.write(json.dumps(context_items, ensure_ascii=False).encode("utf-8"))

            except Exception as e:
                self.send_response(500)
                self.send_header("Content-Type", "application/json")
                self.end_headers()
                self.wfile.write(json.dumps({"error": str(e)}).encode("utf-8"))
        else:
            self.send_response(404)
            self.end_headers()


def run_server():
    server_address = (HOST, PORT)
    httpd = HTTPServer(server_address, RetrievalRequestHandler)
    print(f"RAG Retrieval Service listening on {HOST}:{PORT} (pgvector Top-K={TOP_K_CHUNKS}, Threshold={SIMILARITY_THRESHOLD})...")
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print("\nShutting down retrieval server.")
        httpd.server_close()


if __name__ == "__main__":
    run_server()
