# Load these values into the current PowerShell session before `mvn spring-boot:run`.
# The database credentials must match the local MySQL setup in README.md.
$env:DB_URL = 'jdbc:mysql://localhost:3306/freelance_platform?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
$env:DB_USERNAME = 'freelance'
$env:DB_PASSWORD = 'freelance_dev_only'
$env:SERVER_PORT = '8080'
$env:SPRING_PROFILES_ACTIVE = 'dev'
$env:FRONTEND_ORIGIN = 'http://localhost:5173'
$env:SMTP_HOST = 'localhost'
$env:SMTP_PORT = '1025'
$env:SMTP_AUTH = 'false'
$env:SMTP_STARTTLS = 'false'
$env:MAIL_FROM = 'noreply@localhost'
$env:PASSWORD_RESET_URL = 'http://localhost:5173/reset-password'
$env:APP_UPLOAD_DIR = Join-Path (Split-Path $PSScriptRoot -Parent) 'var\private-uploads'

$jwtBytes = New-Object byte[] 48
$paymentBytes = New-Object byte[] 48
$random = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$random.GetBytes($jwtBytes)
$random.GetBytes($paymentBytes)
$random.Dispose()
$env:JWT_SECRET = [Convert]::ToBase64String($jwtBytes)
$env:PAYMENT_SANDBOX_WEBHOOK_SECRET = [Convert]::ToBase64String($paymentBytes)

Write-Host 'Development settings loaded for this PowerShell window.'
Write-Host 'Database: localhost:3306/freelance_platform; API: http://localhost:8080'
