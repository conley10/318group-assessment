Write-Host "Starting Kafka..."
docker start travel-kafka

Write-Host "Starting Catalogue Service..."
Start-Process powershell -ArgumentList '-NoExit', '-Command', 'cd "C:\Users\conle\Desktop\ai-travel-booking\catalogue-service"; .\mvnw.cmd spring-boot:run'

Start-Sleep -Seconds 2

Write-Host "Starting Booking Service..."
Start-Process powershell -ArgumentList '-NoExit', '-Command', 'cd "C:\Users\conle\Desktop\ai-travel-booking\booking-service"; .\mvnw.cmd spring-boot:run'

Start-Sleep -Seconds 2

Write-Host "Starting Payment Service..."
Start-Process powershell -ArgumentList '-NoExit', '-Command', 'cd "C:\Users\conle\Desktop\ai-travel-booking\payment-service"; .\mvnw.cmd spring-boot:run'

Start-Sleep -Seconds 2

Write-Host "Starting Notification Service..."
Start-Process powershell -ArgumentList '-NoExit', '-Command', 'cd "C:\Users\conle\Desktop\ai-travel-booking\notification-service"; .\mvnw.cmd spring-boot:run'

Start-Sleep -Seconds 2

Write-Host "Starting Travel Assistant Service..."
Start-Process powershell -ArgumentList '-NoExit', '-Command', 'cd "C:\Users\conle\Desktop\ai-travel-booking\travel-assistant-service"; .\mvnw.cmd spring-boot:run'

Start-Sleep -Seconds 2

Write-Host "Starting Stream Analytics Service..."
Start-Process powershell -ArgumentList '-NoExit', '-Command', 'cd "C:\Users\conle\Desktop\ai-travel-booking\stream-analytics-service"; .\mvnw.cmd spring-boot:run'

Write-Host "All services are starting."