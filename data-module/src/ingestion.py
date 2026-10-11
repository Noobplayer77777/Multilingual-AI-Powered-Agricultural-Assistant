import os
import sys
import requests
from datetime import datetime
from typing import List, Dict, Any

# Ensure UTF-8 stdout on Windows
if sys.stdout.encoding != 'utf-8':
    try:
        sys.stdout.reconfigure(encoding='utf-8', errors='replace')
    except Exception:
        pass

# Support both psycopg 3 and psycopg2
try:
    import psycopg
    PSYCOPG_V3 = True
except ImportError:
    import psycopg2
    from psycopg2.extras import execute_values
    PSYCOPG_V3 = False

def load_env_vars():
    """Loads .env file into os.environ if not already present."""
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
                            k, v = k.strip(), v.strip().strip("'").strip('"')
                            if k not in os.environ:
                                os.environ[k] = v
            except Exception:
                pass

load_env_vars()

OLLAMA_URL = os.getenv("OLLAMA_URL", "http://localhost:11434")
EMBEDDING_MODEL = os.getenv("EMBEDDING_MODEL", "nomic-embed-text:latest")

def generate_embedding(text: str) -> List[float]:
    """
    Generates text embedding using Ollama nomic-embed-text model.
    Falls back to a deterministic 768-dim vector if Ollama is unreachable.
    """
    try:
        response = requests.post(
            f"{OLLAMA_URL}/api/embeddings",
            json={"model": EMBEDDING_MODEL, "prompt": text},
            timeout=10
        )
        if response.status_code == 200:
            data = response.json()
            if "embedding" in data:
                return data["embedding"]
    except Exception as e:
        print(f"Ollama embedding call failed: {e}. Using deterministic fallback.")
    
    # Fallback to 768-dim deterministic vector based on text hash
    import hashlib
    seed = int(hashlib.md5(text.encode('utf-8')).hexdigest(), 16)
    vec = [(float((seed >> (i % 64)) & 1) * 2.0 - 1.0) / 10.0 for i in range(768)]
    return vec

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
        # Default to port 5433 where docker pgvector is exposed on the host
        self.db_url = db_url or os.getenv("DATABASE_URL", "postgresql://agriuser:agripass@localhost:5433/agridb")

    def get_connection(self):
        if PSYCOPG_V3:
            return psycopg.connect(self.db_url)
        else:
            return psycopg2.connect(self.db_url)

    def ingest_document(self, title: str, source_org: str, content: str, url: str = None):
        """
        Ingests a single document into the database, chunking and embedding it.
        Avoids duplicates based on title and URL.
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
                    for i, chunk in enumerate(chunks):
                        embedding = generate_embedding(chunk)
                        vector_str = "[" + ",".join(str(f) for f in embedding) + "]"
                        cur.execute(
                            """
                            INSERT INTO document_chunks (document_id, content, chunk_index, embedding)
                            VALUES (%s, %s, %s, %s::vector)
                            """,
                            (doc_id, chunk, i, vector_str)
                        )
                        
                    conn.commit()
                    print(f"Successfully ingested '{title}' with {len(chunks)} chunk(s).")
                    
        except Exception as e:
            print(f"Database error during ingestion of '{title}': {e}")

INITIAL_DOCS = [
    {
        "title": "Tomato Cultivation and Blight Management Guidelines",
        "source_org": "ICAR (Indian Council of Agricultural Research)",
        "url": "https://icar.org.in/guidelines/tomato-cultivation-blight",
        "content": "Tomatoes require well-drained loamy soil with a pH between 6.0 and 7.0 and regular watering at the base. Early blight (Alternaria solani) and late blight are major fungal threats in warm and humid seasons. Management includes crop rotation with non-solanaceous crops, wider spacing for air circulation, drip irrigation to prevent wet foliage, and prophylactic spraying of Mancozeb (2 g/L) or Copper Oxychloride (2.5 g/L). For summer cultivation, use heat-tolerant hybrid varieties and provide mulching to conserve moisture."
    },
    {
        "title": "Integrated Pest Management for Rice and Stem Borer Control",
        "source_org": "TNAU (Tamil Nadu Agricultural University)",
        "url": "https://agritech.tnau.ac.in/expert-system/rice-stem-borer-ipm",
        "content": "Yellow stem borer (Scirpophaga incertulas) causes dead hearts during vegetative stage and white ears during flowering in paddy crops. Integrated Pest Management (IPM) practices recommend: 1. Setting up light traps (1 per hectare) to monitor adult moths. 2. Installing pheromone traps at 5 per acre for mass trapping. 3. Releasing egg parasitoids Trichogramma japonicum at 1 lakh/ha weekly for 3 weeks. 4. Chemical intervention when dead heart reaches 10% threshold: apply Cartap Hydrochloride 50% SP at 1 kg/ha or Chlorantraniliprole 18.5% SC at 150 ml/ha."
    },
    {
        "title": "Cotton Cultivation and Pink Bollworm Management",
        "source_org": "ICAR - Central Institute for Cotton Research",
        "url": "https://cicr.icar.gov.in/cotton-bollworm-management",
        "content": "Pink bollworm (Pectinophora gossypiella) attacks squares, flowers, and bolls of cotton, reducing fiber quality. Recommended control strategies: cultivate short-duration varieties, set up pheromone traps with gossyplure lures, destroy crop residues after harvest, and avoid ratoon cropping. When rosette flowers reach 5-10%, spray Profenofos 50 EC at 2 ml/L or Emamectin Benzoate 5 SG at 0.4 g/L."
    },
    {
        "title": "Maize Best Cultivation Practices in Kharif and Rabi",
        "source_org": "ICAR - Indian Institute of Maize Research",
        "url": "https://iimr.icar.gov.in/maize-cultivation-guide",
        "content": "Maize grows best in fertile, well-drained sandy loam to clay loam soils. For Kharif sowing, sow with the onset of monsoon with seed rate of 20 kg/ha. Apply balanced NPK fertilizers (120:60:40 kg/ha). To control Fall Armyworm (Spodoptera frugiperda), apply Metarhizium anisopliae bio-pesticide or spray Spinetoram 11.7 SC at 0.5 ml/L during early whorl stage."
    },
    {
        "title": "Organic Manure, Compost, and Soil Health Guidelines",
        "source_org": "TNAU Agritech Portal",
        "url": "https://agritech.tnau.ac.in/organic-farming-soil-health",
        "content": "Enriched farmyard manure (FYM) and vermicompost improve soil microbial activity, moisture retention, and cation exchange capacity. Apply 5 to 10 tonnes of FYM per hectare during basal land preparation. Inoculate with Azospirillum and Phosphobacteria (2 kg/ha each) to enhance nitrogen and phosphorus availability. Neem cake application (250 kg/ha) helps suppress soil-borne nematodes and pathogens."
    },
    {
        "title": "Drip Irrigation and Fertigation Schedules for Horticultural Crops",
        "source_org": "TNAU Water Management Institute",
        "url": "https://agritech.tnau.ac.in/drip-irrigation-horticulture",
        "content": "Drip irrigation delivers water directly to the plant root zone, saving 40-60% water compared to furrow irrigation. Water-soluble fertilizers (such as 19-19-19 and 0-0-50) should be applied through fertigation tanks at weekly intervals corresponding to vegetative, flowering, and fruit-development stages. Operating pressure should be maintained between 1.0 and 1.5 kg/cm2 with disk filtration."
    },
    {
        "title": "தக்காளி சாகுபடி மற்றும் பூச்சி நோய் மேலாண்மை வழிகாட்டி",
        "source_org": "TNAU (தமிழ்நாடு வேளாண்மைப் பல்கலைக்கழகம்)",
        "url": "https://agritech.tnau.ac.in/ta/crop-protection/tomato",
        "content": "தக்காளி பயிரில் இலை கருகல் நோய் மற்றும் சாறு உறிஞ்சும் பூச்சிகள் அதிக இழப்பை ஏற்படுத்துகின்றன. கோடை சாகுபடிக்கு சொட்டு நீர் பாசனம் மற்றும் மல்ச்சிங் முறை மிகச் சிறந்தது. இலை சுருட்டல் நோயைக் கட்டுப்படுத்த வெள்ளை ஈக்களை கட்டுப்படுத்துவது அவசியம். இதற்கு வேப்பெண்ணெய் கரைசல் 3% அல்லது டைமெத்தோயேட் 2 மிலி/லிட்டர் தெளிக்கலாம். நிலத்தை உழுது போது ஏக்கருக்கு 10 டன் மக்கிய தொழு உரம் இட வேண்டும்."
    },
    {
        "title": "நெல் சாகுபடியில் தண்டு துளைப்பான் மற்றும் குலைநோய் கட்டுப்பாடு",
        "source_org": "TNAU (தமிழ்நாடு வேளாண்மைப் பல்கலைக்கழகம்)",
        "url": "https://agritech.tnau.ac.in/ta/rice-stem-borer",
        "content": "நெற்பயிரில் மஞ்சள் தண்டு துளைப்பான் குருத்து அழுகல் மற்றும் வெண் கதிர் அறிகுறிகளை ஏற்படுத்தும். ஒருங்கிணைந்த பூச்சி மேலாண்மை முறையில் ஏக்கருக்கு 5 இனக்கவர்ச்சி பொறிகள் வைக்கவும். வேப்பங்கொட்டை சாறு 5% அல்லது கார்டாப் ஹைட்ரோகுளோரைடு தெளிக்க பரிந்துரைக்கப்படுகிறது. தழைச்சத்து உரங்களை பிரித்து இட வேண்டும்."
    },
    {
        "title": "टमाटर की वैज्ञानिक खेती और कीट-रोग प्रबंधन",
        "source_org": "ICAR (भारतीय कृषि अनुसंधान परिषद)",
        "url": "https://icar.org.in/hi/tomato-farming-guide",
        "content": "टमाटर की अच्छी पैदावार के लिए जल निकासी वाली दोमट मिट्टी उपयुक्त होती है। अगेती व पिछेती झुलसा रोग की रोकथाम के लिए मैंकोजेब 2.5 ग्राम प्रति लीटर पानी में मिलाकर छिड़काव करें। गर्मियों में ड्रिप सिंचाई और मल्चिंग का उपयोग करने से नमी बनी रहती है और खरपतवार नियंत्रित रहते हैं।"
    },
    {
        "title": "धान में तना छेदक और पत्ती लपेटक कीट का समन्वित नियंत्रण",
        "source_org": "ICAR (भारतीय कृषि अनुसंधान परिषद)",
        "url": "https://icar.org.in/hi/rice-pest-management",
        "content": "धान की फसल में तना छेदक कीट की रोकथाम के लिए प्रकाश प्रपंच (लाइट ट्रैप) और फेरोमोन ट्रैप का प्रयोग करें। ट्राइकोग्रामा परजीवी 1 लाख प्रति हेक्टेयर की दर से छोड़ें। रासायनिक नियंत्रण के लिए क्लोरेंट्रानिलीप्रोल 150 मिली प्रति हेक्टेयर की दर से छिड़काव करें।"
    }
]

def run_initial_ingestion():
    print(f"Connecting to database to ingest knowledge base...")
    ingestor = KnowledgeIngestor()
    for doc in INITIAL_DOCS:
        ingestor.ingest_document(
            title=doc["title"],
            source_org=doc["source_org"],
            content=doc["content"],
            url=doc["url"]
        )
    print("Ingestion run completed.")

if __name__ == "__main__":
    run_initial_ingestion()
