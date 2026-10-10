# ==============================================================================
# Start All Services: Multilingual AI-Powered Agricultural Assistant
# ==============================================================================

Write-Host "=====================================================" -ForegroundColor Green
Write-Host " Starting Kisan AI Agricultural Assistant Pipeline..." -ForegroundColor Green
Write-Host "=====================================================" -ForegroundColor Green

# 1. Start PostgreSQL with pgvector container
Write-Host "`n[1/5] Checking Docker & starting PostgreSQL/pgvector (Port 5433)..." -ForegroundColor Cyan
docker compose up -d

# 2. Check Ollama
Write-Host "`n[2/5] Checking Ollama Engine (Port 11434)..." -ForegroundColor Cyan
try {
    $ollamaCheck = Invoke-RestMethod -Uri "http://localhost:11434/api/tags" -Method Get -TimeoutSec 3
    Write-Host "  Ollama is running with $($ollamaCheck.models.Count) models loaded." -ForegroundColor Green
} catch {
    Write-Host "  Warning: Ollama is not responding at http://localhost:11434." -ForegroundColor Yellow
    Write-Host "  Please start Ollama Desktop or run 'ollama serve' in another terminal." -ForegroundColor Yellow
}

# 3. Start Member C Retrieval Service (Port 8083)
Write-Host "`n[3/5] Starting Member C Retrieval Service (Port 8083)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Write-Host 'Starting Member C Retrieval Server on port 8083...' -ForegroundColor Cyan; python data-module/src/retrieval_server.py"

# 4. Start Member B Advisory Service (Port 8082)
Write-Host "`n[4/5] Starting Member B Advisory Service (Port 8082)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Write-Host 'Starting Member B Advisory Service on port 8082...' -ForegroundColor Yellow; .\mvnw.cmd spring-boot:run -f advisory-service/pom.xml"

# 5. Start Member A Query Service (Port 8081)
Write-Host "`n[5/5] Starting Member A Query Service & Frontend (Port 8081)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Write-Host 'Starting Member A Query Service on port 8081...' -ForegroundColor Green; .\mvnw.cmd spring-boot:run -f query-service/pom.xml"

Write-Host "`nAll 3 services are launching in separate terminal windows." -ForegroundColor Green
Write-Host "Waiting for Query Service frontend (http://localhost:8081) to become ready..." -ForegroundColor Yellow

# Wait for 8081 to be UP
$maxAttempts = 40
$attempt = 0
$isReady = $false

while ($attempt -lt $maxAttempts) {
    Start-Sleep -Seconds 2
    $attempt++
    try {
        $health = Invoke-RestMethod -Uri "http://localhost:8081/actuator/health" -Method Get -TimeoutSec 2
        if ($health.status -eq "UP") {
            $isReady = $true
            break
        }
    } catch {
        Write-Host -NoNewline "."
    }
}

Write-Host ""
if ($isReady) {
    Write-Host "`n=====================================================" -ForegroundColor Green
    Write-Host " Kisan AI is LIVE and READY!" -ForegroundColor Green
    Write-Host " Opening browser at http://localhost:8081" -ForegroundColor Green
    Write-Host "=====================================================" -ForegroundColor Green
    Start-Process "http://localhost:8081"
} else {
    Write-Host "`nServices are still compiling/starting up." -ForegroundColor Yellow
    Write-Host "Once ready, open: http://localhost:8081 in your browser." -ForegroundColor Yellow
}
