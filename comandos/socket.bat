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
if not exist build mkdir build
".\.tools\jdk-21\bin\javac.exe" -d .\build .\src\FilaAtendimento.java .\src\ClienteFila.java .\src\ServidorSocket.java .\src\ClienteSocket.java .\src\TelaFila.java
if errorlevel 1 exit /b 1
echo Compilacao Socket concluida.
exit /b 0

:SERVIDOR
call "%~f0" compilar
if errorlevel 1 exit /b 1
echo Iniciando servidor Socket. Pressione Ctrl+C para encerrar.
".\.tools\jdk-21\bin\java.exe" -cp .\build ServidorSocket %~2 %~3
exit /b %errorlevel%

:ENTRAR
if "%~2"=="" (
    echo Uso: comandos\socket.bat entrar "Nome do paciente"
    exit /b 1
)
if not exist .\build\ClienteSocket.class (
    echo Classes nao compiladas. Execute comandos\socket.bat compilar primeiro.
    exit /b 1
)
set "SOCKET_PORT=%~3"
if not defined SOCKET_PORT set "SOCKET_PORT=5000"
".\.tools\jdk-21\bin\java.exe" -cp .\build ClienteSocket ENTRAR "%~2" %SOCKET_PORT%
exit /b %errorlevel%

:CONSULTAR
if "%~2"=="" (
    echo Uso: comandos\socket.bat consultar senha
    exit /b 1
)
if not exist .\build\ClienteSocket.class (
    echo Classes nao compiladas. Execute comandos\socket.bat compilar primeiro.
    exit /b 1
)
set "SOCKET_PORT=%~3"
if not defined SOCKET_PORT set "SOCKET_PORT=5000"
".\.tools\jdk-21\bin\java.exe" -cp .\build ClienteSocket CONSULTAR "%~2" %SOCKET_PORT%
exit /b %errorlevel%

:CHAMAR
if not exist .\build\ClienteSocket.class (
    echo Classes nao compiladas. Execute comandos\socket.bat compilar primeiro.
    exit /b 1
)
set "SOCKET_PORT=%~2"
if not defined SOCKET_PORT set "SOCKET_PORT=5000"
".\.tools\jdk-21\bin\java.exe" -cp .\build ClienteSocket CHAMAR %SOCKET_PORT%
exit /b %errorlevel%

:FINALIZAR
if "%~2"=="" (
    echo Uso: comandos\socket.bat finalizar senha
    exit /b 1
)
if not exist .\build\ClienteSocket.class (
    echo Classes nao compiladas. Execute comandos\socket.bat compilar primeiro.
    exit /b 1
)
set "SOCKET_PORT=%~3"
if not defined SOCKET_PORT set "SOCKET_PORT=5000"
".\.tools\jdk-21\bin\java.exe" -cp .\build ClienteSocket FINALIZAR "%~2" %SOCKET_PORT%
exit /b %errorlevel%

:TELA
call "%~f0" compilar
if errorlevel 1 exit /b 1
echo Abrindo a tela cliente. O servidor Socket precisa estar rodando.
".\.tools\jdk-21\bin\java.exe" -cp .\build TelaFila
exit /b %errorlevel%

:AJUDA
echo Comandos do Socket:
echo   comandos\socket.bat compilar
echo   comandos\socket.bat servidor [porta] [--quiet]
echo   comandos\socket.bat entrar "Ana Maria" [porta]
echo   comandos\socket.bat consultar 1 [porta]
echo   comandos\socket.bat chamar [porta]
echo   comandos\socket.bat finalizar 1 [porta]
echo   comandos\socket.bat tela
exit /b 1
