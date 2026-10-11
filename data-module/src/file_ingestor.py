import os
import sys
import json
import re
import hashlib
import argparse
from datetime import datetime
from typing import List, Dict, Any, Optional

# Ensure UTF-8 console output
if sys.stdout.encoding != 'utf-8':
    try:
        sys.stdout.reconfigure(encoding='utf-8', errors='replace')
    except Exception:
        pass

# Module directory setup
current_dir = os.path.dirname(os.path.abspath(__file__))
module_dir = os.path.dirname(current_dir)
if module_dir not in sys.path:
    sys.path.append(module_dir)

from src.ingestion import generate_embedding, KnowledgeIngestor, load_env_vars

load_env_vars()

DEFAULT_KNOWLEDGE_DIR = os.getenv(
    "KNOWLEDGE_FILES_DIR",
    os.path.join(module_dir, "knowledge_files")
)

try:
    from pypdf import PdfReader
    PYPDF_AVAILABLE = True
except ImportError:
    try:
        from PyPDF2 import PdfReader
        PYPDF_AVAILABLE = True
    except ImportError:
        PYPDF_AVAILABLE = False


def clean_text(text: str) -> str:
    """Cleans up text by normalizing whitespace while preserving paragraphs."""
    if not text:
        return ""
    text = re.sub(r'\r\n', '\n', text)
    text = re.sub(r'[ \t]+', ' ', text)
    text = re.sub(r'\n{3,}', '\n\n', text)
    return text.strip()


def extract_title_and_org(filename: str, content: str) -> (str, str):
    """Extracts human-readable title and source organization from content or filename."""
    title = None
    org = "Agricultural Knowledge Base"

    # 1. Look for markdown heading 1
    m_h1 = re.search(r'^#\s+(.+)$', content, re.MULTILINE)
    if m_h1:
        title = m_h1.group(1).strip()

    # 2. Look for Source Organization pattern in top 500 chars
    m_org = re.search(r'(?:Source Organization|மூல நிறுவனம்|संस्थान)[:\s]+([^\n\r]+)', content[:1000], re.IGNORECASE)
    if m_org:
        org = m_org.group(1).strip().strip('*').strip()

    # 3. Detect known agricultural institutions
    if "TNAU" in content[:1500] or "Tamil Nadu Agricultural" in content[:1500] or "தமிழ்நாடு வேளாண்மை" in content[:1500]:
        if "TNAU" not in org:
            org = "TNAU (Tamil Nadu Agricultural University)"
    elif "ICAR" in content[:1500] or "Indian Council of Agricultural" in content[:1500] or "भारतीय कृषि" in content[:1500]:
        if "ICAR" not in org:
            org = "ICAR (Indian Council of Agricultural Research)"
    elif "CIBRC" in content[:1500] or "Insecticides Board" in content[:1500]:
        org = "CIBRC (Central Insecticides Board & Registration Committee)"
    elif "NCONF" in content[:1500] or "National Centre for Organic" in content[:1500]:
        org = "National Centre for Organic and Natural Farming (GoI)"

    # Fallback title from filename
    if not title:
        base = os.path.splitext(os.path.basename(filename))[0]
        title = base.replace('_', ' ').replace('-', ' ').title()

    return title, org


def sliding_window_chunk(text: str, chunk_size_words: int = 350, overlap_words: int = 50, title_prefix: str = "") -> List[str]:
    """
    Splits text into overlapping chunks of words with context header for better retrieval.
    """
    words = text.split()
    if not words:
        return []

    chunks = []
    step = max(1, chunk_size_words - overlap_words)

    for i in range(0, len(words), step):
        chunk_words = words[i:i + chunk_size_words]
        if not chunk_words:
            continue
        chunk_body = " ".join(chunk_words)
        
        # Attach title context header if available so individual chunks retain topical context
        if title_prefix:
            chunk_content = f"[{title_prefix}]\n{chunk_body}"
        else:
            chunk_content = chunk_body

        chunks.append(chunk_content)
        if i + chunk_size_words >= len(words):
            break

    return chunks


class FileKnowledgeIngestor(KnowledgeIngestor):
    """
    Extends KnowledgeIngestor to support multi-format local file ingestion:
    PDF, Markdown (.md), Plain text (.txt), JSON (.json), and CSV (.csv).
    """

    def read_file_content(self, filepath: str) -> (str, str, str):
        """Reads file and returns (content, title, source_org)."""
        ext = os.path.splitext(filepath)[1].lower()

        if ext in ('.txt', '.md'):
            with open(filepath, 'r', encoding='utf-8', errors='replace') as f:
                content = clean_text(f.read())
            title, org = extract_title_and_org(filepath, content)
            return content, title, org

        elif ext == '.pdf':
            if not PYPDF_AVAILABLE:
                raise ImportError("pypdf is required to index PDF files. Install via 'pip install pypdf'.")
            reader = PdfReader(filepath)
            pages_text = []
            for idx, page in enumerate(reader.pages):
                p_text = page.extract_text() or ""
                if p_text.strip():
                    pages_text.append(f"--- Page {idx + 1} ---\n" + p_text.strip())
            content = clean_text("\n\n".join(pages_text))
            title, org = extract_title_and_org(filepath, content)
            return content, title, org

        elif ext == '.json':
            with open(filepath, 'r', encoding='utf-8', errors='replace') as f:
                data = json.load(f)
            
            title = data.get("title") if isinstance(data, dict) else None
            org = data.get("source_organization", "Agricultural Knowledge Base") if isinstance(data, dict) else "Agricultural Knowledge Base"

            if isinstance(data, dict) and "guidelines" in data and isinstance(data["guidelines"], list):
                # Flatten structured guidelines into readable text
                paragraphs = []
                if "title" in data:
                    paragraphs.append(f"# {data['title']}\nSource: {org}")
                for item in data["guidelines"]:
                    lines = [f"Crop: {item.get('crop', 'General')}", f"Pest / Target: {item.get('pest_target', 'N/A')}"]
                    if "chemical_name" in item:
                        lines.append(f"Chemical: {item['chemical_name']}")
                    if "approved_dosage" in item:
                        lines.append(f"Approved Dosage: {item['approved_dosage']}")
                    if "phi_days" in item:
                        lines.append(f"Pre-Harvest Interval (PHI): {item['phi_days']} days")
                    if "rei_hours" in item:
                        lines.append(f"Re-Entry Interval (REI): {item['rei_hours']} hours")
                    if "safety_instructions" in item:
                        lines.append(f"Safety Instructions: {item['safety_instructions']}")
                    paragraphs.append("\n".join(lines))
                
                if "banned_substances_warning" in data:
                    paragraphs.append("Banned Substances Warnings:\n" + "\n".join(f"- {w}" for w in data["banned_substances_warning"]))

                content = "\n\n".join(paragraphs)
            elif isinstance(data, dict) and "content" in data:
                content = clean_text(data["content"])
            else:
                content = json.dumps(data, indent=2, ensure_ascii=False)

            if not title:
                title, _ = extract_title_and_org(filepath, content)
            return content, title, org

        elif ext == '.csv':
            import csv
            rows_text = []
            with open(filepath, 'r', encoding='utf-8', errors='replace') as f:
                reader = csv.reader(f)
                header = next(reader, None)
                for row in reader:
                    if header:
                        row_str = ", ".join(f"{h}: {val}" for h, val in zip(header, row) if val)
                    else:
                        row_str = ", ".join(row)
                    if row_str.strip():
                        rows_text.append(row_str)
            content = clean_text("\n".join(rows_text))
            title, org = extract_title_and_org(filepath, content)
            return content, title, org

        else:
            raise ValueError(f"Unsupported file format '{ext}'. Supported formats: .txt, .md, .pdf, .json, .csv")

    def ingest_file(self, filepath: str, reindex: bool = False) -> Dict[str, Any]:
        """Ingests a file, splits into chunks, embeds and saves to database."""
        filename = os.path.basename(filepath)
        content, title, org = self.read_file_content(filepath)
        
        if not content.strip():
            print(f"[SKIP] File '{filename}' is empty.")
            return {"file": filename, "status": "skipped_empty", "chunks": 0}

        rel_url = f"file://knowledge_files/{filename}"

        try:
            with self.get_connection() as conn:
                with conn.cursor() as cur:
                    # Check if document already exists
                    cur.execute("SELECT id FROM documents WHERE url = %s OR title = %s", (rel_url, title))
                    row = cur.fetchone()

                    if row:
                        doc_id = row[0]
                        if not reindex:
                            print(f"[EXISTS] Document '{title}' (ID {doc_id}) is already indexed. Use --reindex to update.")
                            return {"file": filename, "title": title, "status": "exists", "id": doc_id}
                        
                        # Reindex: delete existing chunks first
                        print(f"[REINDEX] Updating existing document '{title}' (ID {doc_id})...")
                        cur.execute("DELETE FROM document_chunks WHERE document_id = %s", (doc_id,))
                        cur.execute(
                            "UPDATE documents SET title = %s, source_organization = %s, retrieved_at = %s WHERE id = %s",
                            (title, org, datetime.now(), doc_id)
                        )
                    else:
                        cur.execute(
                            """
                            INSERT INTO documents (title, source_organization, url, retrieved_at)
                            VALUES (%s, %s, %s, %s) RETURNING id
                            """,
                            (title, org, rel_url, datetime.now())
                        )
                        doc_id = cur.fetchone()[0]

                    # Chunk content with sliding window
                    chunks = sliding_window_chunk(content, chunk_size_words=350, overlap_words=50, title_prefix=title)

                    # Embed and insert chunks
                    for idx, chunk in enumerate(chunks):
                        embedding = generate_embedding(chunk)
                        vector_str = "[" + ",".join(str(f) for f in embedding) + "]"
                        cur.execute(
                            """
                            INSERT INTO document_chunks (document_id, content, chunk_index, embedding)
                            VALUES (%s, %s, %s, %s::vector)
                            """,
                            (doc_id, chunk, idx, vector_str)
                        )

                    conn.commit()
                    print(f"[INDEXED] '{title}' ({filename}) -> {len(chunks)} chunks embedded.")
                    return {"file": filename, "title": title, "status": "indexed", "id": doc_id, "chunks": len(chunks)}

        except Exception as e:
            print(f"[ERROR] Failed to ingest '{filename}': {e}")
            return {"file": filename, "status": "error", "error": str(e)}

    def ingest_directory(self, directory_path: str = DEFAULT_KNOWLEDGE_DIR, reindex: bool = False) -> List[Dict[str, Any]]:
        """Scans directory and ingests all supported files."""
        if not os.path.exists(directory_path):
            os.makedirs(directory_path, exist_ok=True)
            print(f"Created knowledge directory at: {directory_path}")
            return []

        supported_extensions = ('.txt', '.md', '.pdf', '.json', '.csv')
        files = [
            os.path.join(directory_path, f)
            for f in os.listdir(directory_path)
            if os.path.splitext(f)[1].lower() in supported_extensions and not f.startswith('.')
        ]

        if not files:
            print(f"No documents found in '{directory_path}'. Place .txt, .md, .pdf, .json files there.")
            return []

        print(f"============================================================")
        print(f"Scanning '{directory_path}' ({len(files)} file(s) found)...")
        print(f"============================================================")

        results = []
        for filepath in files:
            res = self.ingest_file(filepath, reindex=reindex)
            results.append(res)

        print(f"============================================================")
        print(f"Ingestion completed: {len(results)} file(s) processed.")
        print(f"============================================================")
        return results

    def list_indexed_documents(self) -> List[Dict[str, Any]]:
        """Lists all documents and chunk counts currently in the database."""
        docs = []
        try:
            with self.get_connection() as conn:
                with conn.cursor() as cur:
                    cur.execute(
                        """
                        SELECT d.id, d.title, d.source_organization, d.url, count(c.id) AS chunk_count
                        FROM documents d
                        LEFT JOIN document_chunks c ON d.id = c.document_id
                        GROUP BY d.id, d.title, d.source_organization, d.url
                        ORDER BY d.id ASC;
                        """
                    )
                    rows = cur.fetchall()
                    for r in rows:
                        docs.append({
                            "id": r[0],
                            "title": r[1],
                            "source_org": r[2],
                            "url": r[3],
                            "chunk_count": r[4]
                        })
        except Exception as e:
            print(f"Database query error: {e}")
        return docs


def main():
    parser = argparse.ArgumentParser(description="Ingest local agricultural files for RAG into pgvector database.")
    parser.add_argument("--dir", default=DEFAULT_KNOWLEDGE_DIR, help="Path to knowledge directory containing files")
    parser.add_argument("--file", default=None, help="Path to single file to ingest")
    parser.add_argument("--reindex", action="store_true", help="Force reindexing of already existing files")
    parser.add_argument("--status", action="store_true", help="Display all currently indexed documents in pgvector")
    args = parser.parse_args()

    ingestor = FileKnowledgeIngestor()

    if args.status:
        docs = ingestor.list_indexed_documents()
        print(f"\nCurrently Indexed Documents in pgvector ({len(docs)} total):")
        print(f"{'ID':<4} | {'Chunks':<6} | {'Source Org':<30} | {'Title'}")
        print("-" * 80)
        for d in docs:
            print(f"{d['id']:<4} | {d['chunk_count']:<6} | {(d['source_org'] or '')[:28]:<30} | {d['title']}")
        print("-" * 80)
        return

    if args.file:
        ingestor.ingest_file(args.file, reindex=args.reindex)
    else:
        ingestor.ingest_directory(args.dir, reindex=args.reindex)


if __name__ == "__main__":
    main()
