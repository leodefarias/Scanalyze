#!/bin/bash

echo "======================================================"
echo "   Sistema de Micromedicao - Backend Java"
echo "   Compilacao e Execucao Automatica"
echo "======================================================"
echo

# Verifica se o driver Oracle existe
if [ ! -f "ojdbc8.jar" ]; then
    echo "❌ Driver Oracle (ojdbc8.jar) não encontrado!"
    echo "Baixando driver Oracle..."
    curl -L -o ojdbc8.jar "https://repo1.maven.org/maven2/com/oracle/database/jdbc/ojdbc8/21.1.0.0/ojdbc8-21.1.0.0.jar"
    if [ $? -eq 0 ]; then
        echo "✅ Driver Oracle baixado com sucesso!"
    else
        echo "❌ Erro ao baixar driver Oracle!"
        exit 1
    fi
fi

cd src

echo "Compilando classes Java com driver Oracle..."
javac -cp "../ojdbc8.jar:." -encoding UTF-8 br/com/micromedicao/model/*.java br/com/micromedicao/dao/*.java br/com/micromedicao/connection/*.java br/com/micromedicao/service/*.java br/com/micromedicao/integration/*.java br/com/micromedicao/app/*.java br/com/micromedicao/test/*.java

if [ $? -eq 0 ]; then
    echo "✅ Compilacao bem-sucedida!"
    echo
    echo "Executando aplicacao..."
    echo
    java -cp "../ojdbc8.jar:." br.com.micromedicao.app.App
else
    echo "❌ Erro na compilacao!"
    exit 1
fi

echo
echo "Execucao concluida."