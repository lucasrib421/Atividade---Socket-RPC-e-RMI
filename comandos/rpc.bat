@echo off
cd /d "%~dp0.."

if /i "%~1"=="compilar" goto COMPILAR
if /i "%~1"=="servidor" goto SERVIDOR
if /i "%~1"=="entrar" goto ENTRAR
if /i "%~1"=="consultar" goto CONSULTAR
if /i "%~1"=="chamar" goto CHAMAR
if /i "%~1"=="finalizar" goto FINALIZAR
if /i "%~1"=="tela" goto TELA
goto AJUDA

:COMPILAR
for %%I in ("%CD%") do set "PROJECT_ROOT=%%~sI"
set "JAVA_HOME=%PROJECT_ROOT%\.tools\jdk-21"
set "PATH=%JAVA_HOME%\bin;%PATH%"
call "%PROJECT_ROOT%\.tools\apache-maven-3.9.16\bin\mvn.cmd" -q -Dproject.short.root="%PROJECT_ROOT%" -Dmaven.repo.local="%PROJECT_ROOT%\.tools\m2" -DskipTests compile
if errorlevel 1 exit /b 1
echo Compilacao RPC concluida.
exit /b %errorlevel%

:SERVIDOR
for %%I in ("%CD%") do set "PROJECT_ROOT=%%~sI"
if not exist target\classes\ServidorRpc.class call "%~f0" compilar
if errorlevel 1 exit /b 1
if not exist target\classes\fila\rpc\FilaAtendimentoServiceGrpc.class call "%~f0" compilar
if errorlevel 1 exit /b 1
for %%I in ("%CD%") do set "PROJECT_ROOT=%%~sI"
set "JAVA_HOME=%PROJECT_ROOT%\.tools\jdk-21"
set "PATH=%JAVA_HOME%\bin;%PATH%"
call "%PROJECT_ROOT%\.tools\apache-maven-3.9.16\bin\mvn.cmd" -q -Dproject.short.root="%PROJECT_ROOT%" -Dmaven.repo.local="%PROJECT_ROOT%\.tools\m2" -Dexec.mainClass=ServidorRpc exec:java
exit /b %errorlevel%

:TELA
for %%I in ("%CD%") do set "PROJECT_ROOT=%%~sI"
if not exist target\classes\TelaFilaRpc.class call "%~f0" compilar
if errorlevel 1 exit /b 1
if not exist target\classes\fila\rpc\FilaAtendimentoServiceGrpc.class call "%~f0" compilar
if errorlevel 1 exit /b 1
for %%I in ("%CD%") do set "PROJECT_ROOT=%%~sI"
set "JAVA_HOME=%PROJECT_ROOT%\.tools\jdk-21"
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo Abrindo a tela RPC. O servidor RPC precisa estar rodando.
call "%PROJECT_ROOT%\.tools\apache-maven-3.9.16\bin\mvn.cmd" -q -Dproject.short.root="%PROJECT_ROOT%" -Dmaven.repo.local="%PROJECT_ROOT%\.tools\m2" -Dexec.mainClass=TelaFilaRpc exec:java
exit /b %errorlevel%

:ENTRAR
set "RPC_ARGS=ENTRAR %~2"
goto EXEC_CLIENTE

:CONSULTAR
if "%~2"=="" (
    echo Uso: comandos\rpc.bat consultar senha
    exit /b 1
)
set "RPC_ARGS=CONSULTAR %~2"
goto EXEC_CLIENTE

:CHAMAR
set "RPC_ARGS=CHAMAR"
goto EXEC_CLIENTE

:FINALIZAR
if "%~2"=="" (
    echo Uso: comandos\rpc.bat finalizar senha
    exit /b 1
)
set "RPC_ARGS=FINALIZAR %~2"

:EXEC_CLIENTE
if not exist target\classes\ClienteRpc.class call "%~f0" compilar
if errorlevel 1 exit /b 1
if not exist target\classes\fila\rpc\FilaAtendimentoServiceGrpc.class call "%~f0" compilar
if errorlevel 1 exit /b 1
for %%I in ("%CD%") do set "PROJECT_ROOT=%%~sI"
set "JAVA_HOME=%PROJECT_ROOT%\.tools\jdk-21"
set "PATH=%JAVA_HOME%\bin;%PATH%"
call "%PROJECT_ROOT%\.tools\apache-maven-3.9.16\bin\mvn.cmd" -q -Dproject.short.root="%PROJECT_ROOT%" -Dmaven.repo.local="%PROJECT_ROOT%\.tools\m2" -Dexec.mainClass=ClienteRpc "-Dexec.args=%RPC_ARGS%" exec:java
exit /b %errorlevel%

:AJUDA
echo Comandos do RPC:
echo   comandos\rpc.bat compilar
echo   comandos\rpc.bat servidor
echo   comandos\rpc.bat entrar "Ana Maria"
echo   comandos\rpc.bat consultar 1
echo   comandos\rpc.bat chamar
echo   comandos\rpc.bat finalizar 1
echo   comandos\rpc.bat tela
exit /b 1
