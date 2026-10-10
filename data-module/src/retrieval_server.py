import json
import os
import sys
from http.server import HTTPServer, BaseHTTPRequestHandler
from typing import List, Dict, Any

# Ensure UTF-8 console output
if sys.stdout.encoding != 'utf-8':
    try:
        sys.stdout.reconfigure(encoding='utf-8', errors='replace')
    except Exception:
        pass

# Add module path
current_dir = os.path.dirname(os.path.abspath(__file__))
sys.path.append(os.path.dirname(current_dir))

from src.ingestion import generate_embedding, KnowledgeIngestor
from src.weather_client import WeatherClient, extract_location_from_text

PORT = int(os.getenv("RETRIEVAL_PORT", 8083))
SIMILARITY_THRESHOLD = float(os.getenv("SIMILARITY_THRESHOLD", 0.38))

ingestor = KnowledgeIngestor()
weather_client = WeatherClient()

class RetrievalRequestHandler(BaseHTTPRequestHandler):

    def log_message(self, format, *args):
        # Override to keep logs clean
        sys.stderr.write(f"[{self.log_date_time_string()}] {format % args}\n")

    def do_GET(self):
        if self.path in ("/health", "/actuator/health"):
            try:
                with ingestor.get_connection() as conn:
                    with conn.cursor() as cur:
                        cur.execute("SELECT 1")
                self.send_response(200)
                self.send_header("Content-Type", "application/json")
                self.end_headers()
                self.wfile.write(json.dumps({"status": "UP", "database": "connected"}).encode("utf-8"))
            except Exception as e:
                self.send_response(503)
                self.send_header("Content-Type", "application/json")
                self.end_headers()
                self.wfile.write(json.dumps({"status": "DOWN", "error": str(e)}).encode("utf-8"))
        else:
            self.send_response(404)
            self.end_headers()

    def do_POST(self):
        if self.path == "/retrieve":
            try:
                content_len = int(self.headers.get("Content-Length", 0))
                post_body = self.rfile.read(content_len).decode("utf-8")
                request_data = json.loads(post_body) if post_body else {}

                question = request_data.get("question", "")
                location = request_data.get("location")
                crop = request_data.get("crop")

                context_items: List[Dict[str, Any]] = []

                # 1. Document Vector Search if question provided
                if question and question.strip():
                    query_text = f"{crop} {question}".strip() if crop else question
                    emb = generate_embedding(query_text)
                    vector_str = "[" + ",".join(str(f) for f in emb) + "]"

                    try:
                        with ingestor.get_connection() as conn:
                            with conn.cursor() as cur:
                                cur.execute(
                                    """
                                    SELECT c.content, d.title, d.source_organization, d.url,
                                           (c.embedding <=> %s::vector) AS dist
                                    FROM document_chunks c
                                    JOIN documents d ON c.document_id = d.id
                                    ORDER BY dist ASC
                                    LIMIT 2;
                                    """,
                                    (vector_str,)
                                )
                                rows = cur.fetchall()
                                for row in rows:
                                    content, title, org, url, dist = row
                                    # Only include if distance is within relevance threshold
                                    if dist <= SIMILARITY_THRESHOLD:
                                        context_items.append({
                                            "text": content,
                                            "sourceName": f"{title} ({org})",
                                            "sourceUrl": url,
                                            "type": "DOCUMENT"
                                        })
                    except Exception as db_err:
                        sys.stderr.write(f"DB search error: {db_err}\n")

                # 2. Live Weather Context if location provided
                if location and location.strip():
                    try:
                        weather_item = weather_client.get_weather_context(location.strip())
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
                self.wfile.write(json.dumps(context_items).encode("utf-8"))

            except Exception as e:
                self.send_response(500)
                self.send_header("Content-Type", "application/json")
                self.end_headers()
                self.wfile.write(json.dumps({"error": str(e)}).encode("utf-8"))
        else:
            self.send_response(404)
            self.end_headers()

def run_server():
    server_address = ('', PORT)
    httpd = HTTPServer(server_address, RetrievalRequestHandler)
    print(f"Member C Retrieval Service running on port {PORT}...")
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print("\nShutting down retrieval server.")
        httpd.server_close()

if __name__ == "__main__":
    run_server()
