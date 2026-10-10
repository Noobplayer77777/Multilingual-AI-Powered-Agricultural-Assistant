# Start Database container
$rootDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $rootDir

Write-Host "Ensuring PostgreSQL + pgvector is running..." -ForegroundColor Cyan
docker compose up -d db

Write-Host "Starting Advisory Service in separate window..." -ForegroundColor Cyan
Start-Process powershell.exe -ArgumentList "-NoExit", "-ExecutionPolicy", "Bypass", "-File", "`"$rootDir\start-advisory-service.ps1`""

Write-Host "Starting Query Service in separate window..." -ForegroundColor Cyan
Start-Process powershell.exe -ArgumentList "-NoExit", "-ExecutionPolicy", "Bypass", "-File", "`"$rootDir\start-query-service.ps1`""

Write-Host ""
Write-Host "Both services launched in separate windows!" -ForegroundColor Green
Write-Host "Farmer Chat UI:    http://localhost:8081" -ForegroundColor Yellow
Write-Host "Query History UI:  http://localhost:8081/history" -ForegroundColor Yellow
Write-Host "Advisory API:      http://localhost:8082/advise" -ForegroundColor Yellow
