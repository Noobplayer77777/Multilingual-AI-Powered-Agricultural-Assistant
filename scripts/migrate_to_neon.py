#!/usr/bin/env python3
"""
Neon PostgreSQL Migration & Seeding Script
===========================================
Migrates the Multilingual Agricultural Assistant database to Neon Serverless PostgreSQL.
- Verifies connection with SSL (sslmode=require)
- Enables the pgvector extension
- Applies init.sql schema idempotently
- Ingests official ICAR/TNAU multilingual knowledge base with 768-dim embeddings
- Validates vector similarity retrieval
- Updates .env configuration
"""

import sys
import os
import argparse
import re
from datetime import datetime

# UTF-8 stdout
if sys.stdout.encoding != 'utf-8':
    try:
        sys.stdout.reconfigure(encoding='utf-8', errors='replace')
    except Exception:
        pass

# Add data-module to sys.path
root_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
data_module_dir = os.path.join(root_dir, "data-module")
if data_module_dir not in sys.path:
    sys.path.insert(0, data_module_dir)

# Import psycopg
try:
    import psycopg
    PSYCOPG_V3 = True
except ImportError:
    try:
        import psycopg2
        PSYCOPG_V3 = False
    except ImportError:
        print("[ERROR] Neither 'psycopg' nor 'psycopg2' is installed.")
        print("Please install psycopg: pip install 'psycopg[binary]'")
        sys.exit(1)

def load_env(env_path: str):
    """Parses .env file into os.environ."""
    if os.path.exists(env_path):
        try:
            with open(env_path, "r", encoding="utf-8") as f:
                for line in f:
                    line = line.strip()
                    if line and not line.startswith("#") and "=" in line:
                        k, v = line.split("=", 1)
                        k, v = k.strip(), v.strip().strip("'").strip('"')
                        if k not in os.environ:
                            os.environ[k] = v
        except Exception:
            pass

def ensure_ssl_mode(url: str) -> str:
    """Ensures sslmode=require is appended for Neon PostgreSQL connections."""
    if "neon.tech" in url and "sslmode=" not in url:
        sep = "&" if "?" in url else "?"
        url = f"{url}{sep}sslmode=require"
    return url

def update_env_file(env_path: str, db_url: str):
    """Updates or adds DATABASE_URL in .env file."""
    lines = []
    found = False
    if os.path.exists(env_path):
        with open(env_path, "r", encoding="utf-8") as f:
            for line in f:
                if line.strip().startswith("DATABASE_URL="):
                    lines.append(f"DATABASE_URL={db_url}\n")
                    found = True
                else:
                    lines.append(line)
    if not found:
        lines.append(f"\n# Neon Serverless PostgreSQL Database URL\nDATABASE_URL={db_url}\n")

    with open(env_path, "w", encoding="utf-8") as f:
        f.writelines(lines)
    print(f"[OK] Updated DATABASE_URL in {env_path}")

def main():
    parser = argparse.ArgumentParser(description="Migrate agricultural database to Neon PostgreSQL")
    parser.add_argument("--url", help="Neon PostgreSQL connection string (postgresql://user:pass@host/db?sslmode=require)")
    parser.add_argument("--skip-seed", action="store_true", help="Skip re-seeding ICAR/TNAU knowledge base documents")
    args = parser.parse_args()

    env_path = os.path.join(root_dir, ".env")
    load_env(env_path)

    db_url = args.url or os.getenv("DATABASE_URL")

    print("=" * 65)
    print("  Kisan AI — Neon PostgreSQL Cloud Migration & Setup")
    print("=" * 65)

    if not db_url or "localhost" in db_url:
        print("\nNo remote Neon DATABASE_URL provided.")
        print("\nTo get a free Neon database:")
        print("  1. Sign in to https://neon.tech (free tier, no credit card required)")
        print("  2. Create a new project (e.g. 'kisan-agri-db')")
        print("  3. Copy your connection string from the Dashboard:")
        print("     postgresql://[user]:[password]@[endpoint].neon.tech/neondb?sslmode=require\n")
        try:
            db_url = input("Enter your Neon PostgreSQL connection string: ").strip()
        except (EOFError, KeyboardInterrupt):
            print("\nMigration aborted.")
            sys.exit(1)

    if not db_url:
        print("[ERROR] No connection string provided. Aborting.")
        sys.exit(1)

    db_url = ensure_ssl_mode(db_url)

    # 1. Test Connection
    print("\n[Step 1/4] Connecting to Neon PostgreSQL...")
    try:
        connect_fn = psycopg.connect if PSYCOPG_V3 else psycopg2.connect
        with connect_fn(db_url) as conn:
            with conn.cursor() as cur:
                cur.execute("SELECT version();")
                pg_ver = cur.fetchone()[0]
                cur.execute("SELECT current_database(), current_user;")
                db_name, db_user = cur.fetchone()
                print(f"  Connected successfully!")
                print(f"  Database : {db_name}")
                print(f"  User     : {db_user}")
                print(f"  Version  : {pg_ver.split(',')[0]}")
    except Exception as e:
        print(f"[ERROR] Failed to connect to Neon PostgreSQL: {e}")
        sys.exit(1)

    # 2. Apply init.sql Schema
    init_sql_path = os.path.join(root_dir, "init.sql")
    print(f"\n[Step 2/4] Initializing Schema from {os.path.basename(init_sql_path)}...")
    if not os.path.exists(init_sql_path):
        print(f"[ERROR] init.sql not found at {init_sql_path}")
        sys.exit(1)

    with open(init_sql_path, "r", encoding="utf-8") as f:
        sql_content = f.read()

    try:
        with connect_fn(db_url) as conn:
            with conn.cursor() as cur:
                # Enable vector extension
                print("  Enabling pgvector extension...")
                cur.execute("CREATE EXTENSION IF NOT EXISTS vector;")
                
                # Execute table creations
                print("  Creating application and RAG tables...")
                cur.execute(sql_content)
            conn.commit()
            print("  [OK] Schema successfully applied (pgvector, users, queries, responses, sources, documents, document_chunks).")
    except Exception as e:
        print(f"[ERROR] Failed applying schema: {e}")
        sys.exit(1)

    # 3. Seed Knowledge Base Documents & Vectors
    if not args.skip_seed:
        print("\n[Step 3/4] Ingesting Agricultural Knowledge Base (ICAR & TNAU)...")
        from src.ingestion import KnowledgeIngestor, INITIAL_DOCS
        ingestor = KnowledgeIngestor(db_url=db_url)

        for doc in INITIAL_DOCS:
            ingestor.ingest_document(
                title=doc["title"],
                source_org=doc["source_org"],
                content=doc["content"],
                url=doc.get("url")
            )
        print("  [OK] Knowledge base documents and vector embeddings synced.")
    else:
        print("\n[Step 3/4] Skipping document seeding (--skip-seed specified).")

    # 4. Verify Vector Similarity Search & Row Counts
    print("\n[Step 4/4] Verifying Vector Similarity Search on Neon...")
    try:
        with connect_fn(db_url) as conn:
            with conn.cursor() as cur:
                # Count rows
                cur.execute("SELECT COUNT(*) FROM documents;")
                doc_count = cur.fetchone()[0]
                cur.execute("SELECT COUNT(*) FROM document_chunks;")
                chunk_count = cur.fetchone()[0]
                cur.execute("SELECT COUNT(*) FROM queries;")
                query_count = cur.fetchone()[0]

                print(f"  Verified database contents:")
                print(f"    - Documents:       {doc_count}")
                print(f"    - Document Chunks: {chunk_count}")
                print(f"    - Farmer Queries:  {query_count}")

                # Test search
                from src.ingestion import generate_embedding
                test_query = "tomato blight fungal management"
                test_emb = generate_embedding(test_query)
                emb_str = "[" + ",".join(str(f) for f in test_emb) + "]"

                cur.execute(
                    """
                    SELECT d.title, dc.content, 1 - (dc.embedding <=> %s::vector) AS similarity
                    FROM document_chunks dc
                    JOIN documents d ON dc.document_id = d.id
                    ORDER BY dc.embedding <=> %s::vector
                    LIMIT 1
                    """,
                    (emb_str, emb_str)
                )
                row = cur.fetchone()
                if row:
                    print(f"\n  [PASS] pgvector Similarity Search Test:")
                    print(f"    Matched Doc: {row[0]}")
                    print(f"    Similarity : {float(row[2]):.4f}")
                    print(f"    Sample Snippet: {row[1][:90]}...")
                else:
                    print("  [WARN] Similarity search returned 0 rows.")
    except Exception as e:
        print(f"[ERROR] Verification failed: {e}")
        sys.exit(1)

    # 5. Update .env
    update_env_file(env_path, db_url)

    print("\n" + "=" * 65)
    print("  MIGRATION TO NEON POSTGRESQL SUCCESSFUL!")
    print("=" * 65)
    print("Both the Spring Boot Query Service and the Python RAG engine")
    print("will now communicate with your Neon cloud database.")
    print("=" * 65 + "\n")

if __name__ == "__main__":
    main()
