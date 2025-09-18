@echo off
REM Sistema de Micromedição - Launcher Java Windows
REM Executa apenas o backend Java

echo ☕ Iniciando Backend Java...
echo 💡 Demonstracao completa do sistema de dados
echo.

REM Verificar se está compilado
if not exist "backend-java\src\br\com\micromedicao\app\App.class" (
    echo 🔨 Compilando Java...
    cd backend-java\src
    javac -encoding UTF-8 br\com\micromedicao\model\*.java br\com\micromedicao\service\*.java br\com\micromedicao\integration\*.java br\com\micromedicao\app\*.java
    if %errorlevel% neq 0 (
        echo ❌ Erro na compilacao!
        pause
        exit /b 1
    )
    cd ..\..
)

cd backend-java\src
java br.com.micromedicao.app.App
cd ..\..