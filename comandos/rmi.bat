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
if not exist build\rmi mkdir build\rmi
".\.tools\jdk-21\bin\javac.exe" -d .\build\rmi .\src\FilaAtendimento.java .\src\ClienteFila.java .\src\ConsultaRemota.java .\src\FilaAtendimentoRemota.java .\src\ServicoFilaRmi.java .\src\ServidorRmi.java .\src\ClienteRmi.java .\src\ClienteSocket.java .\src\TelaFila.java .\src\TelaFilaRmi.java
if errorlevel 1 exit /b 1
echo Compilacao RMI concluida.
exit /b 0

:SERVIDOR
if not exist build\rmi\ServidorRmi.class call "%~f0" compilar
if errorlevel 1 exit /b 1
echo Iniciando servidor RMI. Pressione Ctrl+C para encerrar.
".\.tools\jdk-21\bin\java.exe" -cp .\build\rmi ServidorRmi
exit /b %errorlevel%

:ENTRAR
set "RMI_ARGS=ENTRAR %~2"
goto EXEC_CLIENTE

:CONSULTAR
if "%~2"=="" (
    echo Uso: comandos\rmi.bat consultar senha
    exit /b 1
)
set "RMI_ARGS=CONSULTAR %~2"
goto EXEC_CLIENTE

:CHAMAR
set "RMI_ARGS=CHAMAR"
goto EXEC_CLIENTE

:FINALIZAR
if "%~2"=="" (
    echo Uso: comandos\rmi.bat finalizar senha
    exit /b 1
)
set "RMI_ARGS=FINALIZAR %~2"

:EXEC_CLIENTE
if not exist build\rmi\ClienteRmi.class call "%~f0" compilar
if errorlevel 1 exit /b 1
".\.tools\jdk-21\bin\java.exe" -cp .\build\rmi ClienteRmi %RMI_ARGS%
exit /b %errorlevel%

:TELA
if not exist build\rmi\TelaFilaRmi.class call "%~f0" compilar
if errorlevel 1 exit /b 1
echo Abrindo a tela RMI. O servidor RMI precisa estar rodando.
".\.tools\jdk-21\bin\java.exe" -cp .\build\rmi TelaFilaRmi
exit /b %errorlevel%

:AJUDA
echo Comandos RMI:
echo   comandos\rmi.bat compilar
echo   comandos\rmi.bat servidor
echo   comandos\rmi.bat entrar "Ana Maria"
echo   comandos\rmi.bat consultar 1
echo   comandos\rmi.bat chamar
echo   comandos\rmi.bat finalizar 1
echo   comandos\rmi.bat tela
exit /b 1
