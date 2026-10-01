$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'

$projectRoot = Split-Path -Parent $PSScriptRoot
$toolsDirectory = Join-Path $projectRoot '.tools'
$jdkDirectory = Join-Path $toolsDirectory 'jdk-21'
$archivePath = Join-Path $toolsDirectory 'temurin-21.zip'
$downloadUrl = 'https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.12.1%2B1/OpenJDK21U-jdk_x64_windows_hotspot_21.0.12.1_1.zip'
$expectedSha256 = 'f9d6e191ab098c0d416e7d588a24420a8621cd2f4720dab2459b8b7b2d2d8b4e'

if (Test-Path (Join-Path $jdkDirectory 'bin\javac.exe')) {
    Write-Host "JDK ja preparado em $jdkDirectory"
    exit 0
}

New-Item -ItemType Directory -Path $toolsDirectory -Force | Out-Null

if (-not (Test-Path $archivePath)) {
    Write-Host 'Baixando Eclipse Temurin JDK 21...'
    Invoke-WebRequest -Uri $downloadUrl -OutFile $archivePath
}

$actualSha256 = (Get-FileHash -LiteralPath $archivePath -Algorithm SHA256).Hash.ToLowerInvariant()
if ($actualSha256 -ne $expectedSha256) {
    Remove-Item -LiteralPath $archivePath -Force
    throw 'O checksum do arquivo baixado nao corresponde ao publicado pelo fornecedor. Execute novamente.'
}

Write-Host 'Extraindo JDK...'
Expand-Archive -LiteralPath $archivePath -DestinationPath $toolsDirectory -Force
$extractedDirectory = Get-ChildItem -LiteralPath $toolsDirectory -Directory |
    Where-Object { $_.Name -like 'jdk-21.0.12*' } |
    Select-Object -First 1
if (-not $extractedDirectory -or -not (Test-Path (Join-Path $extractedDirectory.FullName 'bin\javac.exe'))) {
    throw 'O arquivo foi extraido, mas javac.exe nao foi encontrado.'
}

Move-Item -LiteralPath $extractedDirectory.FullName -Destination $jdkDirectory
Remove-Item -LiteralPath $archivePath -Force
Write-Host "JDK preparado em $jdkDirectory"
