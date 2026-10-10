# ==============================================================================
# Stop All Services: Multilingual AI-Powered Agricultural Assistant
# ==============================================================================

Write-Host "Stopping Kisan AI services..." -ForegroundColor Yellow

# Ports to free: 8081 (Query), 8082 (Advisory), 8083 (Retrieval)
$ports = @(8081, 8082, 8083)

foreach ($port in $ports) {
    $connections = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    if ($connections) {
        $pids = $connections | Select-Object -ExpandProperty OwningProcess -Unique
        foreach ($p in $pids) {
            Write-Host "Terminating process on port $port (PID: $p)..." -ForegroundColor Cyan
            Stop-Process -Id $p -Force -ErrorAction SilentlyContinue
        }
    } else {
        Write-Host "Port $port is already free." -ForegroundColor Gray
    }
}

Write-Host "`nAll application services have been stopped." -ForegroundColor Green
Write-Host "Note: PostgreSQL container is still running. Run 'docker compose down' if you wish to stop the database as well." -ForegroundColor Gray
