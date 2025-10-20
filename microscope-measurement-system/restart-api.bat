@echo off
REM Script para reiniciar o servidor API do Sistema de Micromedicao
REM Versao: 1.0

echo Reiniciando Scanalyze API Server...

REM Para processos Java existentes
echo Parando processos Java existentes...
taskkill /F /FI "WINDOWTITLE eq ApiServer*" >nul 2>&1
taskkill /F /FI "IMAGENAME eq java.exe" /FI "COMMANDLINE eq *br.com.micromedicao*" >nul 2>&1

REM Aguarda um pouco para garantir que os processos foram finalizados
timeout /t 2 /nobreak >nul

REM Verifica se a porta 8081 esta livre
netstat -ano | findstr ":8081" >nul
if %errorlevel% equ 0 (
    echo Porta 8081 ainda ocupada, aguardando...
    timeout /t 3 /nobreak >nul
)

REM Navega para o diretorio do backend
cd /d "%~dp0backend-java"

REM Cria diretorio de classes se nao existir
if not exist "classes" mkdir classes

REM Recompila se necessario
echo Compilando classes Java...
javac -cp ".\ojdbc8.jar" -d classes src\br\com\micromedicao\model\*.java src\br\com\micromedicao\dao\*.java src\br\com\micromedicao\service\*.java src\br\com\micromedicao\connection\*.java src\br\com\micromedicao\integration\*.java src\br\com\micromedicao\api\*.java

if %errorlevel% neq 0 (
    echo Erro na compilacao!
    pause
    exit /b 1
)

REM Cria diretorio de logs se nao existir
if not exist "..\logs" mkdir "..\logs"

REM Inicia o servidor em background
echo Iniciando API Server na porta 8081...
start /B java -cp ".\ojdbc8.jar;.\classes" br.com.micromedicao.api.ApiServer > ..\logs\api-server.log 2>&1

REM Aguarda inicializacao
echo Aguardando inicializacao...
timeout /t 5 /nobreak >nul

REM Testa se o servidor esta funcionando
echo Testando conectividade...
curl -s -f http://localhost:8081/api/health >nul 2>&1
if %errorlevel% equ 0 (
    echo API Server funcionando!
    echo Acesse: http://localhost:8081/api/health
    echo Dashboard: Abra frontend-dashboard\index.html
) else (
    echo Erro ao iniciar API Server
    echo Verifique os logs: type logs\api-server.log
)

cd ..
pause