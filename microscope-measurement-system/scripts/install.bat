@echo off
REM Sistema de Micromedição Automatizada - Instalador Windows
REM Script de instalação automatizada para Windows

echo ============================================================
echo    SISTEMA DE MICROMEDICAO AUTOMATIZADA - INSTALADOR
echo ============================================================
echo.

REM Função para verificar comandos
setlocal enabledelayedexpansion
set MISSING_DEPS=0

echo 📋 Verificando pre-requisitos...
echo.

REM Verificar Python
echo Verificando Python...
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Python nao encontrado
    echo    Por favor, instale Python 3.7+
    echo    💡 Download: https://www.python.org/downloads/
    set MISSING_DEPS=1
) else (
    echo ✅ Python encontrado
    for /f "tokens=2" %%i in ('python --version 2^>^&1') do echo    Versao: %%i
)

REM Verificar pip
echo Verificando pip...
pip --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ pip nao encontrado
    echo    Por favor, instale pip
    set MISSING_DEPS=1
) else (
    echo ✅ pip encontrado
    for /f "tokens=2" %%i in ('pip --version 2^>^&1') do echo    Versao: %%i
)

REM Verificar Java
echo Verificando Java...
java --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java nao encontrado
    echo    Por favor, instale Java 8+
    echo    💡 Download: https://www.oracle.com/java/technologies/downloads/
    set MISSING_DEPS=1
) else (
    echo ✅ Java encontrado
    for /f "tokens=2" %%i in ('java --version 2^>^&1') do echo    Versao: %%i
)

REM Verificar javac
echo Verificando javac...
javac --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ javac nao encontrado
    echo    Por favor, instale JDK (Java Development Kit)
    set MISSING_DEPS=1
) else (
    echo ✅ JDK encontrado
    for /f "tokens=2" %%i in ('javac --version 2^>^&1') do echo    Versao: %%i
)

if !MISSING_DEPS! equ 1 (
    echo.
    echo ❌ Dependencias em falta. Instale os requisitos e execute novamente.
    echo.
    echo 💡 Links de download:
    echo    Python: https://www.python.org/downloads/
    echo    Java JDK: https://www.oracle.com/java/technologies/downloads/
    echo.
    pause
    exit /b 1
)

echo.
echo ✅ Todos os pre-requisitos encontrados!
echo.

REM Instalar dependências Python
echo 📦 Instalando dependencias Python...
cd python-vision

if exist requirements.txt (
    echo Verificando ambiente Python...

    REM Tenta instalação com --user primeiro
    pip install -r requirements.txt --user >nul 2>&1
    if %errorlevel% equ 0 (
        echo ✅ Dependencias Python instaladas com --user!
    ) else (
        echo ⚠️ Ambiente gerenciado externamente detectado. Criando ambiente virtual...

        REM Cria ambiente virtual se não existir
        if not exist venv (
            echo Criando ambiente virtual...
            python -m venv venv
        )

        REM Ativa ambiente virtual e instala dependências
        echo Ativando ambiente virtual e instalando dependencias...
        call venv\Scripts\activate.bat
        python -m pip install --upgrade pip
        pip install -r requirements.txt
        call venv\Scripts\deactivate.bat

        echo ✅ Dependencias Python instaladas no ambiente virtual!
        echo 💡 Para usar o sistema, o ambiente virtual sera ativado automaticamente.
    )
) else (
    echo ❌ Arquivo requirements.txt nao encontrado!
    pause
    exit /b 1
)

cd ..

REM Compilar backend Java
echo.
echo 🔨 Compilando backend Java...
cd backend-java

REM Compilar apenas (sem executar)
cd src
echo Compilando classes Java...
javac -encoding UTF-8 br\com\micromedicao\model\*.java br\com\micromedicao\service\*.java br\com\micromedicao\integration\*.java br\com\micromedicao\app\*.java

if %errorlevel% equ 0 (
    echo ✅ Compilacao Java bem-sucedida!
) else (
    echo ❌ Erro na compilacao Java!
    pause
    exit /b 1
)

cd ..\..

REM Criar dados de exemplo
echo.
echo 📊 Gerando dados de exemplo...
python scripts\integration_example.py

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