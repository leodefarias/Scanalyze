#!/bin/bash

# Sistema de Micromedição - Launcher Java Rápido
# Executa apenas o backend Java

echo "☕ Iniciando Backend Java..."
echo "💡 Demonstração completa do sistema de dados"
echo

# Verificar se está compilado
if [ ! -f "backend-java/src/br/com/micromedicao/app/App.class" ]; then
    echo "🔨 Compilando Java..."
    cd backend-java/src
    javac -encoding UTF-8 br/com/micromedicao/model/*.java br/com/micromedicao/service/*.java br/com/micromedicao/integration/*.java br/com/micromedicao/app/*.java
    if [ $? -ne 0 ]; then
        echo "❌ Erro na compilação!"
        exit 1
    fi
    cd ../..
fi

cd backend-java/src
java br.com.micromedicao.app.App
cd ../..