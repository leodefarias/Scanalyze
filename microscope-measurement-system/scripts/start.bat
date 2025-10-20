@echo off
REM Sistema de Micromedição Automatizada - Launcher Windows
REM Execução simplificada do sistema completo

echo ============================================================
echo    SISTEMA DE MICROMEDICAO AUTOMATIZADA
echo ============================================================
echo.

REM Verificar se está instalado
if not exist "python-vision\requirements.txt" (
    echo ❌ Sistema nao encontrado! Execute primeiro:
    echo    install.bat
    pause
    exit /b 1
)

REM Função para executar Python com ambiente virtual
set "PYTHON_CMD=python"
if exist "python-vision\venv\Scripts\python.exe" (
    echo 🐍 Usando ambiente virtual Python...
    set "PYTHON_CMD=python-vision\venv\Scripts\python.exe"
) else (
    echo ⚠️ Usando Python do sistema...
)

REM Verificar se Java está compilado (incluindo API)
if not exist "backend-java\src\br\com\micromedicao\app\App.class" (
    echo ⚠️ Backend Java nao esta compilado. Compilando...
    cd backend-java\src
    javac -cp "..\ojdbc8.jar;." -encoding UTF-8 br\com\micromedicao\model\*.java br\com\micromedicao\dao\*.java br\com\micromedicao\connection\*.java br\com\micromedicao\service\*.java br\com\micromedicao\integration\*.java br\com\micromedicao\api\*.java br\com\micromedicao\app\*.java
    if %errorlevel% neq 0 (
        echo ❌ Erro na compilacao Java!
        pause
        exit /b 1
    )
    cd ..\..
    echo ✅ Compilacao concluida!
)

echo 🚀 Iniciando Sistema de Micromedicao...
echo.
echo Selecione o modo de execucao:
echo.
echo 1️⃣  Sistema Completo com API REST (recomendado) 🆕
echo 2️⃣  Sistema Completo (modo tradicional)
echo 3️⃣  Apenas API REST + Dashboard
echo 4️⃣  Apenas Visao Computacional Python
echo 5️⃣  Apenas Backend Java (demo)
echo 6️⃣  Apenas Dashboard Web
echo 7️⃣  Integracao Automatica Python + API
echo 8️⃣  Menu Avancado (run_system.py)
echo 0️⃣  Sair
echo.

set /p choice="Digite sua escolha (1-8, 0 para sair): "

if "%choice%"=="1" (
    echo 🚀 Iniciando sistema completo com API REST...
    echo.
    echo 📋 O que sera executado:
    echo    • API REST Server em http://localhost:8081
    echo    • Dashboard web com sincronizacao automatica
    echo    • Interface Python de captura (microscope_gui.py)
    echo    • Salvamento automatico no banco via API
    echo.
    echo 💡 Pressione Ctrl+C para encerrar
    echo.

    REM Criar diretório de logs se não existir
    if not exist logs mkdir logs

    REM Iniciar API REST em background
    echo 📡 Iniciando API REST Server...
    cd backend-java\src
    start /b java -cp "..\ojdbc8.jar;." br.com.micromedicao.api.ApiServer > ..\..\logs\api.log 2>&1
    cd ..\..
    echo ✅ API Server iniciada

    REM Aguardar API inicializar
    echo ⏳ Aguardando API inicializar...
    timeout /t 5 /nobreak >nul

    REM Abrir dashboard no navegador
    echo 🌐 Abrindo Dashboard Web...
    start frontend-dashboard\index.html

    timeout /t 2 /nobreak >nul
    echo 🐍 Iniciando interface de captura Python...
    %PYTHON_CMD% python-vision\microscope_gui.py

) else if "%choice%"=="2" (
    echo 🚀 Iniciando sistema completo (modo tradicional)...
    echo.
    echo 📋 O que sera executado:
    echo    • Interface Python para captura em tempo real
    echo    • Dashboard web sera aberto automaticamente
    echo    • Dados de integracao serao carregados
    echo.
    echo 💡 Pressione Ctrl+C para encerrar
    echo.

    REM Abrir dashboard no navegador
    echo Abrindo dashboard web...
    start frontend-dashboard\index.html

    timeout /t 2 /nobreak >nul
    echo Iniciando interface de captura...
    %PYTHON_CMD% python-vision\microscope_gui.py

) else if "%choice%"=="3" (
    echo 📡 Iniciando API REST + Dashboard...
    echo.
    echo 📋 Componentes:
    echo    • API REST em http://localhost:8081
    echo    • Dashboard web integrado
    echo.

    REM Iniciar API REST
    cd backend-java\src
    echo 📡 Iniciando API Server...
    start java -cp "..\ojdbc8.jar;." br.com.micromedicao.api.ApiServer
    cd ..\..

    timeout /t 3 /nobreak >nul

    REM Abrir dashboard
    echo 🌐 Abrindo Dashboard...
    start frontend-dashboard\index.html

    echo ✅ Sistema API + Dashboard iniciado!
    echo 💡 Para parar: Ctrl+C
    pause

) else if "%choice%"=="4" (
    echo 🐍 Iniciando Visao Computacional Python...
    %PYTHON_CMD% python-vision\microscope_gui.py

) else if "%choice%"=="5" (
    echo ☕ Executando Backend Java (demo)...
    cd backend-java\src
    java br.com.micromedicao.app.App
    cd ..\..

) else if "%choice%"=="6" (
    echo 🌐 Abrindo Dashboard Web...
    start frontend-dashboard\index.html

) else if "%choice%"=="7" (
    echo 🤖 Iniciando Integracao Automatica Python + API...
    echo.
    echo 📋 O que sera executado:
    echo    • Sistema Python simulando captura automatica
    echo    • Dados salvos automaticamente via API
    echo    • Dashboard atualizado em tempo real
    echo.

    REM Verificar se API está rodando
    curl -s http://localhost:8081/api/health >nul 2>&1
    if %errorlevel% neq 0 (
        echo ⚠️ API REST nao detectada. Iniciando...
        if not exist logs mkdir logs
        cd backend-java\src
        start /b java -cp "..\ojdbc8.jar;." br.com.micromedicao.api.ApiServer > ..\..\logs\api.log 2>&1
        cd ..\..
        timeout /t 5 /nobreak >nul
    ) else (
        echo ✅ API REST detectada em execucao
    )

    %PYTHON_CMD% python-vision\api_integration.py

) else if "%choice%"=="8" (
    echo 🔧 Abrindo menu avancado...
    %PYTHON_CMD% run_system.py

) else if "%choice%"=="0" (
    echo 👋 Saindo...
    exit /b 0

) else (
    echo ❌ Opcao invalida!
    pause
    exit /b 1
)