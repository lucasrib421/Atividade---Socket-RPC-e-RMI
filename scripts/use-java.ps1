$projectRoot = Split-Path -Parent $PSScriptRoot
$jdkDirectory = Join-Path $projectRoot '.tools\jdk-21'
$javaExecutable = Join-Path $jdkDirectory 'bin\java.exe'
$javacExecutable = Join-Path $jdkDirectory 'bin\javac.exe'

if (-not (Test-Path $javaExecutable) -or -not (Test-Path $javacExecutable)) {
    throw 'JDK do projeto nao encontrado. Execute .\scripts\setup-java.ps1 primeiro.'
}

$env:JAVA_HOME = $jdkDirectory
$env:Path = (Join-Path $jdkDirectory 'bin') + [IO.Path]::PathSeparator + $env:Path
Write-Host "Java deste terminal: $env:JAVA_HOME"
