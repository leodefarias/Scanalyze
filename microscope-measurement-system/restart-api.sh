#!/bin/bash

# Script para reiniciar o servidor API do Sistema de Micromedição
# Versão: 1.0

echo "🔄 Reiniciando Scanalyze API Server..."

# Para processos Java existentes
echo "⏹️  Parando processos Java existentes..."
pkill -f "br.com.micromedicao" 2>/dev/null || true

# Aguarda um pouco para garantir que os processos foram finalizados
sleep 2

# Verifica se a porta 8081 está livre
if netstat -tulpn 2>/dev/null | grep -q ":8081"; then
    echo "⚠️  Porta 8081 ainda ocupada, aguardando..."
    sleep 3
fi

# Navega para o diretório do backend
cd "$(dirname "$0")/backend-java"

# Recompila se necessário
echo "🔨 Compilando classes Java..."
javac -cp "./ojdbc8.jar" -d classes src/br/com/micromedicao/model/*.java src/br/com/micromedicao/dao/*.java src/br/com/micromedicao/service/*.java src/br/com/micromedicao/connection/*.java src/br/com/micromedicao/integration/*.java src/br/com/micromedicao/api/*.java

# Inicia o servidor em background
echo "🚀 Iniciando API Server na porta 8081..."
nohup java -cp "./ojdbc8.jar:./classes" br.com.micromedicao.api.ApiServer > ../logs/api-server.log 2>&1 &

# Aguarda inicialização
echo "⏳ Aguardando inicialização..."
sleep 5

# Testa se o servidor está funcionando
echo "🔍 Testando conectividade..."
if curl -s -f http://localhost:8081/api/health > /dev/null; then
    echo "✅ API Server funcionando!"
    echo "🌐 Acesse: http://localhost:8081/api/health"
    echo "📊 Dashboard: Abra frontend-dashboard/index.html"
else
    echo "❌ Erro ao iniciar API Server"
    echo "📝 Verifique os logs: tail -f logs/api-server.log"
fi