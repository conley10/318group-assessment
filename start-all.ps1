Write-Host "Starting AI Travel Booking System..."

# Automatically find the project root.
# $PSScriptRoot is the folder containing this start-all.ps1 file.
$ProjectRoot = $PSScriptRoot

Write-Host "Project root: $ProjectRoot"

# Files created by an elevated (admin) run are owned by Administrators and can
# break later runs from a normal terminal, so warn before starting anything.
$IsAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if ($IsAdmin) {
    Write-Warning "This script is running as Administrator. Please run it from a normal (non-admin) PowerShell window instead."
    $Answer = Read-Host "Continue anyway? (y/N)"
    if ($Answer -notmatch '^[Yy]') { exit 1 }
}

Write-Host "Starting Kafka..."
docker info *> $null
if ($LASTEXITCODE -ne 0) {
    Write-Warning "Docker is not running. Start Docker Desktop, wait until it is ready, then run this script again."
    exit 1
}
docker start travel-kafka

Start-Sleep -Seconds 2

Write-Host "Starting Catalogue Service..."
Start-Process powershell -ArgumentList '-NoExit', '-Command', "cd '$ProjectRoot\catalogue-service'; .\mvnw.cmd spring-boot:run"

Start-Sleep -Seconds 2

Write-Host "Starting Booking Service..."
Start-Process powershell -ArgumentList '-NoExit', '-Command', "cd '$ProjectRoot\booking-service'; .\mvnw.cmd spring-boot:run"

Start-Sleep -Seconds 2

Write-Host "Starting Payment Service..."
Start-Process powershell -ArgumentList '-NoExit', '-Command', "cd '$ProjectRoot\payment-service'; .\mvnw.cmd spring-boot:run"

Start-Sleep -Seconds 2

Write-Host "Starting Notification Service..."
Start-Process powershell -ArgumentList '-NoExit', '-Command', "cd '$ProjectRoot\notification-service'; .\mvnw.cmd spring-boot:run"

Start-Sleep -Seconds 2

Write-Host "Starting Travel Assistant Service..."
Start-Process powershell -ArgumentList '-NoExit', '-Command', "cd '$ProjectRoot\travel-assistant-service'; .\mvnw.cmd spring-boot:run"

Start-Sleep -Seconds 2

Write-Host "Starting Stream Analytics Service..."
Start-Process powershell -ArgumentList '-NoExit', '-Command', "cd '$ProjectRoot\stream-analytics-service'; .\mvnw.cmd spring-boot:run"

Write-Host ""
Write-Host "All services are starting."
Write-Host "Catalogue:       http://localhost:8081"
Write-Host "Booking:         http://localhost:8082"
Write-Host "Payment:         http://localhost:8083"
Write-Host "Notification:    http://localhost:8084"
Write-Host "Travel Assistant:http://localhost:8085"
Write-Host "Stream Analytics:http://localhost:8086"