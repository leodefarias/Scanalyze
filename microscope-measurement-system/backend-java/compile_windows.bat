@echo off
echo ======================================================
echo    Sistema de Micromedicao - Compilacao Windows
echo ======================================================
echo.

cd src

echo Compilando classes Java individualmente...

echo - Compilando modelo...
javac -encoding UTF-8 br\com\micromedicao\model\Sample.java
javac -encoding UTF-8 br\com\micromedicao\model\Operator.java
javac -encoding UTF-8 br\com\micromedicao\model\DigitalMicroscope.java
javac -encoding UTF-8 br\com\micromedicao\model\MicroscopyImage.java
javac -encoding UTF-8 br\com\micromedicao\model\Measurement.java

echo - Compilando servico...
javac -encoding UTF-8 -cp . br\com\micromedicao\service\MicromedicaoService.java

echo - Compilando integracao...
javac -encoding UTF-8 -cp . br\com\micromedicao\integration\DataIntegration.java

echo - Compilando aplicacao...
javac -encoding UTF-8 -cp . br\com\micromedicao\app\App.java

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ Compilacao bem-sucedida!
    echo.
    echo Executando aplicacao...
    echo.
    java br.com.micromedicao.app.App
) else (
    echo ❌ Erro na compilacao!
    pause
)

echo.
echo Pressione qualquer tecla para sair...
pause > nul