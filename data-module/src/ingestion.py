import os
import psycopg2
from psycopg2.extras import execute_values
from datetime import datetime
from typing import List, Dict, Any

# Note: The embedding model is not yet decided. 
# This is a placeholder for the multilingual embedding function.
def generate_embedding(text: str) -> List[float]:
    """
    Placeholder for the actual embedding generation logic.
    Returns a dummy vector.
    """
    # Assuming a placeholder dimension of 3 for testing, but in reality
    # this depends on the chosen model (e.g., 768 or 1536).
    return [0.0, 0.0, 0.0]

def chunk_text(text: str, chunk_size: int = 500) -> List[str]:
    """
    Splits text into sensible chunks.
    A simple word-based chunker for demonstration.
    """
    words = text.split()
    chunks = []
    current_chunk = []
    
    for word in words:
        current_chunk.append(word)
        if len(current_chunk) >= chunk_size:
            chunks.append(" ".join(current_chunk))
            current_chunk = []
            
    if current_chunk:
        chunks.append(" ".join(current_chunk))
        
    return chunks

class KnowledgeIngestor:
    def __init__(self, db_url: str = None):
        self.db_url = db_url or os.getenv("DATABASE_URL", "postgresql://agriuser:agripass@localhost:5432/agridb")

    def get_connection(self):
        return psycopg2.connect(self.db_url)

    def ingest_document(self, title: str, source_org: str, content: str, url: str = None):
        """
        Ingests a single document into the database, chunking and embedding it.
        Makes ingestion repeatable by avoiding duplicate document titles/urls.
        """
        try:
            with self.get_connection() as conn:
                with conn.cursor() as cur:
                    # Check for duplicates based on title and URL
                    cur.execute("SELECT id FROM documents WHERE title = %s AND url = %s", (title, url))
                    existing = cur.fetchone()
                    if existing:
                        print(f"Document '{title}' already exists. Skipping.")
                        return
                    
                    # Insert document
                    cur.execute(
                        """
                        INSERT INTO documents (title, source_organization, url, retrieved_at)
                        VALUES (%s, %s, %s, %s) RETURNING id
                        """,
                        (title, source_org, url, datetime.now())
                    )
                    doc_id = cur.fetchone()[0]
                    
                    # Chunk text
                    chunks = chunk_text(content)
                    
                    # Embed and insert chunks
                    chunk_records = []
                    for i, chunk in enumerate(chunks):
                        embedding = generate_embedding(chunk)
                        chunk_records.append((doc_id, chunk, i, embedding))
                    
                    if chunk_records:
                        execute_values(
                            cur,
                            """
                            INSERT INTO document_chunks (document_id, content, chunk_index, embedding)
                            VALUES %s
                            """,
                            chunk_records
                        )
                        
                    conn.commit()
                    print(f"Successfully ingested '{title}' with {len(chunks)} chunks.")
                    
        except psycopg2.Error as e:
            print(f"Database error during ingestion of '{title}': {e}")
        except Exception as e:
            print(f"Unexpected error processing '{title}': {e}")

# Example Initial Collection (10-20 trustworthy docs context)
INITIAL_DOCS = [
    {
        "title": "Tomato Cultivation Guidelines",
        "source_org": "ICAR",
        "url": "https://icar.org.in/mock/tomato-cultivation",
        "content": "Tomatoes require well-drained soil and regular watering. Early blight can be managed by crop rotation and applying appropriate fungicides."
    },
    {
        "title": "Pest Management for Rice",
        "source_org": "TNAU",
        "url": "https://agritech.tnau.ac.in/mock/rice-pests",
        "content": "Stem borer is a major pest in rice. Integrated pest management (IPM) practices including light traps and biological control agents are recommended."
    }
    # Add up to 20 documents here
]

def run_initial_ingestion():
    ingestor = KnowledgeIngestor()
    for doc in INITIAL_DOCS:
        ingestor.ingest_document(
            title=doc["title"],
            source_org=doc["source_org"],
            content=doc["content"],
            url=doc["url"]
        )

if __name__ == "__main__":
    run_initial_ingestion()
