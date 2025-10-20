#!/bin/bash

# Script para testar conexão Oracle de forma independente
# Sistema de Micromedição Automatizada

echo "🔬 =================================================="
echo "   TESTE DE CONEXÃO ORACLE DATABASE"
echo "   Sistema de Micromedição Automatizada"
echo "=================================================="
echo

# Verifica se o driver Oracle existe
if [ ! -f "ojdbc8.jar" ]; then
    echo "📦 Baixando driver Oracle JDBC..."
    curl -L -o ojdbc8.jar "https://repo1.maven.org/maven2/com/oracle/database/jdbc/ojdbc8/21.1.0.0/ojdbc8-21.1.0.0.jar"
    if [ $? -eq 0 ]; then
        echo "✅ Driver Oracle baixado com sucesso!"
    else
        echo "❌ Erro ao baixar driver Oracle!"
        exit 1
    fi
fi

echo "🔨 Compilando classes de teste..."
cd src

javac -cp "../ojdbc8.jar:." -encoding UTF-8 \
    br/com/micromedicao/connection/*.java \
    br/com/micromedicao/model/*.java \
    br/com/micromedicao/dao/*.java

if [ $? -ne 0 ]; then
    echo "❌ Erro na compilação!"
    exit 1
fi

echo "✅ Compilação bem-sucedida!"
echo

echo "🔍 Testando conexão Oracle..."
echo "📋 Configurações:"
echo "   URL: jdbc:oracle:thin:@oracle.fiap.com.br:1521:orcl"
echo "   Usuário: RM555211"
echo

java -cp "../ojdbc8.jar:." br.com.micromedicao.connection.ConnectionFactory

echo
echo "🏁 Teste concluído!"
echo "💡 Se a conexão falhou, verifique:"
echo "   - Conectividade com oracle.fiap.com.br"
echo "   - Credenciais (usuário/senha)"
echo "   - Firewall/proxy"