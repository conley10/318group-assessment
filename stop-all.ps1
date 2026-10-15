
Write-Host "Stopping Voyage services..." -ForegroundColor Yellow

# Ports used by the six backend services and React frontend
$ports = @(8081, 8082, 8083, 8084, 8085, 8086, 5173)

foreach ($port in $ports) {
    $processIds = @(
        Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue |
        Select-Object -ExpandProperty OwningProcess -Unique
    )

    foreach ($processId in $processIds) {
        Write-Host "Stopping process on port $port (PID $processId)"
        Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
    }
}

Write-Host "Voyage services stopped!" -ForegroundColor Green
