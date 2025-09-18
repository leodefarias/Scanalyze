@echo off
REM Sistema de Micromedição Automatizada - Instalador Windows
REM Script de instalação automatizada para Windows

echo ========================================================
echo    SISTEMA DE MICROMEDICAO AUTOMATIZADA - INSTALADOR
echo ========================================================
echo.

REM Verificar Python
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Python nao encontrado! Instale Python 3.7+ primeiro.
    echo 💡 Download: https://www.python.org/downloads/
    pause
    exit /b 1
) else (
    echo ✅ Python encontrado
)

REM Verificar pip
pip --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ pip nao encontrado!
    pause
    exit /b 1
) else (
    echo ✅ pip encontrado
)

REM Verificar Java
java --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java nao encontrado! Instale Java 8+ primeiro.
    echo 💡 Download: https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
) else (
    echo ✅ Java encontrado
)

REM Verificar javac
javac --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ javac nao encontrado! Instale JDK primeiro.
    pause
    exit /b 1
) else (
    echo ✅ JDK encontrado
)

echo.
echo ✅ Todos os pre-requisitos encontrados!
echo.

REM Instalar dependências Python
echo 📦 Instalando dependencias Python...
cd python-vision
if exist requirements.txt (
    pip install -r requirements.txt --user
    echo ✅ Dependencias Python instaladas!
) else (
    echo ❌ Arquivo requirements.txt nao encontrado!
    pause
    exit /b 1
)
cd ..

REM Compilar backend Java
echo.
echo 🔨 Compilando backend Java...
cd backend-java\src
javac -encoding UTF-8 br\com\micromedicao\model\*.java br\com\micromedicao\service\*.java br\com\micromedicao\integration\*.java br\com\micromedicao\app\*.java

if %errorlevel% equ 0 (
    echo ✅ Compilacao Java bem-sucedida!
) else (
    echo ❌ Erro na compilacao Java!
    pause
    exit /b 1
)
cd ..\..

REM Gerar dados de exemplo
echo.
echo 📊 Gerando dados de exemplo...
python integration_example.py

echo.
echo 🎉 INSTALACAO CONCLUIDA COM SUCESSO!
echo.
echo 📋 Para executar o sistema:
echo    start.bat  - Executar sistema completo
echo    python run_system.py  - Menu interativo (alternativa)
echo.
echo 📋 Componentes individuais:
echo    quick-python.bat  - Apenas visao computacional
echo    quick-java.bat    - Apenas backend Java
echo    quick-web.bat     - Apenas dashboard web
echo.
pause