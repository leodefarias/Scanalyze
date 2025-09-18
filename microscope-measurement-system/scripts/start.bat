@echo off
REM Sistema de Micromedição Automatizada - Launcher Windows
REM Execução simplificada do sistema completo

echo ========================================================
echo    SISTEMA DE MICROMEDICAO AUTOMATIZADA
echo ========================================================
echo.

REM Verificar se está instalado
if not exist "python-vision\requirements.txt" (
    echo ❌ Sistema nao encontrado! Execute primeiro:
    echo    install.bat
    pause
    exit /b 1
)

REM Verificar se Java está compilado
if not exist "backend-java\src\br\com\micromedicao\app\App.class" (
    echo ⚠️ Backend Java nao esta compilado. Compilando...
    cd backend-java\src
    javac -encoding UTF-8 br\com\micromedicao\model\*.java br\com\micromedicao\service\*.java br\com\micromedicao\integration\*.java br\com\micromedicao\app\*.java
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
echo 1️⃣  Sistema Completo (recomendado)
echo 2️⃣  Apenas Visao Computacional Python
echo 3️⃣  Apenas Backend Java (demo)
echo 4️⃣  Apenas Dashboard Web
echo 5️⃣  Menu Avancado (run_system.py)
echo 0️⃣  Sair
echo.

set /p choice="Digite sua escolha (1-5, 0 para sair): "

if "%choice%"=="1" (
    echo 🚀 Iniciando sistema completo...
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
    python python-vision\microscope_gui.py
) else if "%choice%"=="2" (
    echo 🐍 Iniciando Visao Computacional Python...
    python python-vision\microscope_gui.py
) else if "%choice%"=="3" (
    echo ☕ Executando Backend Java...
    cd backend-java\src
    java br.com.micromedicao.app.App
    cd ..\..
) else if "%choice%"=="4" (
    echo 🌐 Abrindo Dashboard Web...
    start frontend-dashboard\index.html
) else if "%choice%"=="5" (
    echo 🔧 Abrindo menu avancado...
    python run_system.py
) else if "%choice%"=="0" (
    echo 👋 Saindo...
    exit /b 0
) else (
    echo ❌ Opcao invalida!
    pause
    exit /b 1
)