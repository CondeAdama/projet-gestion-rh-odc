# Import du dump local vers Aiven MySQL (minerva-mysql)
# Usage : $env:AIVEN_MYSQL_PASSWORD = "votre_mot_de_passe"; .\import-aiven.ps1

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$Dump = Join-Path $Root "database\gestion_rh_db.sql"
$Ca = Join-Path $Root "database\aiven-minerva-ca.pem"
$HostName = "minerva-mysql-minerva-group.h.aivencloud.com"
$Port = 23766
$User = "avnadmin"

if (-not $env:AIVEN_MYSQL_PASSWORD) {
    Write-Error "Definissez AIVEN_MYSQL_PASSWORD avant d'executer ce script."
}

$mysql = Get-Command mysql -ErrorAction SilentlyContinue
if (-not $mysql) {
    Write-Error "Client mysql introuvable dans le PATH."
}

Write-Host "Import vers $HostName`:$Port (collation MySQL compatible)..."
$env:MYSQL_PWD = $env:AIVEN_MYSQL_PASSWORD
$sql = (Get-Content $Dump -Raw) -replace 'utf8mb4_uca1400_ai_ci', 'utf8mb4_unicode_ci'
$sql | & $mysql.Source --ssl-mode=VERIFY_CA --ssl-ca=$Ca -h $HostName -P $Port -u $User

Write-Host "Verification..."
& $mysql.Source --ssl-mode=VERIFY_CA --ssl-ca=$Ca -h $HostName -P $Port -u $User gestion_rh_db -e "SHOW TABLES; SELECT COUNT(*) AS utilisateurs FROM utilisateurs;"
Write-Host "Import termine."
