@echo off
cd /d "%~dp0.."

for %%I in ("%CD%") do set "PROJECT_ROOT=%%~sI"
set "JAVA_HOME=%PROJECT_ROOT%\.tools\jdk-21"
set "PATH=%JAVA_HOME%\bin;%PATH%"

if not exist target\classes\BenchmarkLatencia.class call "%PROJECT_ROOT%\comandos\rpc.bat" compilar
if errorlevel 1 exit /b 1

call "%PROJECT_ROOT%\.tools\apache-maven-3.9.16\bin\mvn.cmd" -q -Dproject.short.root="%PROJECT_ROOT%" -Dmaven.repo.local="%PROJECT_ROOT%\.tools\m2" -Dexec.mainClass=BenchmarkLatencia exec:java
exit /b %errorlevel%
