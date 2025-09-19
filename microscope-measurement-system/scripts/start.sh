#!/bin/bash

# Sistema de Micromedição Automatizada - Launcher Rápido
# Execução simplificada do sistema completo

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔬============================================================${NC}"
echo -e "${BLUE}   SISTEMA DE MICROMEDIÇÃO AUTOMATIZADA${NC}"
echo -e "${BLUE}============================================================${NC}"
echo

# Verificar se está instalado
if [ ! -f "python-vision/requirements.txt" ]; then
    echo -e "${RED}❌ Sistema não encontrado! Execute primeiro:${NC}"
    echo -e "   ${YELLOW}./install${NC}"
    exit 1
fi

# Função para ativar ambiente virtual se existir
activate_venv() {
    if [ -f "python-vision/venv/bin/activate" ]; then
        echo -e "${BLUE}🐍 Ativando ambiente virtual Python...${NC}"
        source python-vision/venv/bin/activate
        export VENV_ACTIVATED=1
    fi
}

# Função para executar Python com ambiente virtual
run_python() {
    # Para api_integration.py, sempre use python3 do sistema (tem requests)
    if [[ "$1" == *"api_integration.py"* ]]; then
        python3 "$@"
    elif [ -f "python-vision/venv/bin/python" ]; then
        python-vision/venv/bin/python "$@"
    else
        python3 "$@"
    fi
}

# Verificar se Java está compilado (incluindo API)
if [ ! -f "backend-java/src/br/com/micromedicao/app/App.class" ] || [ ! -f "backend-java/src/br/com/micromedicao/api/ApiServer.class" ]; then
    echo -e "${YELLOW}⚠️  Backend Java não está compilado. Compilando...${NC}"
    cd backend-java/src
    javac -encoding UTF-8 br/com/micromedicao/model/*.java br/com/micromedicao/dao/*.java br/com/micromedicao/connection/*.java br/com/micromedicao/service/*.java br/com/micromedicao/integration/*.java br/com/micromedicao/api/*.java br/com/micromedicao/app/*.java
    if [ $? -ne 0 ]; then
        echo -e "${RED}❌ Erro na compilação Java!${NC}"
        exit 1
    fi
    cd ../..
    echo -e "${GREEN}✅ Compilação concluída!${NC}"
fi

echo -e "${GREEN}🚀 Iniciando Sistema de Micromedição...${NC}"
echo
echo -e "${YELLOW}Selecione o modo de execução:${NC}"
echo
echo "1️⃣  Sistema Completo com API REST (recomendado) 🆕"
echo "2️⃣  Sistema Completo (modo tradicional)"
echo "3️⃣  Apenas API REST + Dashboard"
echo "4️⃣  Apenas Visão Computacional Python"
echo "5️⃣  Apenas Backend Java (demo)"
echo "6️⃣  Apenas Dashboard Web"
echo "7️⃣  Integração Automática Python + API"
echo "8️⃣  Menu Avançado (run_system.py)"
echo "0️⃣  Sair"
echo

read -p "Digite sua escolha (1-8, 0 para sair): " choice

case $choice in
    1)
        echo -e "${GREEN}🚀 Iniciando sistema completo com API REST...${NC}"
        echo
        echo -e "${BLUE}📋 O que será executado:${NC}"
        echo "   • API REST Server em http://localhost:8080"
        echo "   • Dashboard web com sincronização automática"
        echo "   • Interface Python de captura (microscope_gui.py)"
        echo "   • Salvamento automático no banco via API"
        echo
        echo -e "${YELLOW}💡 Pressione Ctrl+C para encerrar${NC}"
        echo

        # Iniciar API REST em background
        echo -e "${BLUE}📡 Iniciando API REST Server...${NC}"
        cd backend-java/src
        nohup java br.com.micromedicao.api.ApiServer > ../../logs/api.log 2>&1 &
        API_PID=$!
        cd ../..
        mkdir -p logs
        echo -e "${GREEN}✅ API Server iniciada (PID: $API_PID)${NC}"

        # Aguardar API inicializar
        echo "⏳ Aguardando API inicializar..."
        sleep 5

        # Abrir dashboard no navegador
        echo -e "${BLUE}🌐 Abrindo Dashboard Web...${NC}"
        if command -v xdg-open &> /dev/null; then
            xdg-open "frontend-dashboard/index.html" &
        elif command -v open &> /dev/null; then
            open "frontend-dashboard/index.html" &
        fi

        sleep 2
        echo -e "${BLUE}🐍 Iniciando interface de captura Python...${NC}"
        run_python python-vision/microscope_gui.py

        # Cleanup ao sair
        echo -e "${YELLOW}🛑 Parando API Server...${NC}"
        kill $API_PID 2>/dev/null
        ;;
    2)
        echo -e "${GREEN}🚀 Iniciando sistema completo (modo tradicional)...${NC}"
        echo
        echo -e "${BLUE}📋 O que será executado:${NC}"
        echo "   • Interface Python para captura em tempo real"
        echo "   • Dashboard web será aberto automaticamente"
        echo "   • Dados de integração serão carregados"
        echo
        echo -e "${YELLOW}💡 Pressione Ctrl+C para encerrar${NC}"
        echo

        # Abrir dashboard no navegador (em background)
        if command -v xdg-open &> /dev/null; then
            echo "Abrindo dashboard web..."
            xdg-open "frontend-dashboard/index.html" &
        elif command -v open &> /dev/null; then
            echo "Abrindo dashboard web..."
            open "frontend-dashboard/index.html" &
        fi

        sleep 2
        echo "Iniciando interface de captura..."
        run_python python-vision/microscope_gui.py
        ;;
    3)
        echo -e "${GREEN}📡 Iniciando API REST + Dashboard...${NC}"
        echo
        echo -e "${BLUE}📋 Componentes:${NC}"
        echo "   • API REST em http://localhost:8080"
        echo "   • Dashboard web integrado"
        echo

        # Iniciar API REST
        cd backend-java/src
        echo -e "${BLUE}📡 Iniciando API Server...${NC}"
        java br.com.micromedicao.api.ApiServer &
        API_PID=$!
        cd ../..

        sleep 3

        # Abrir dashboard
        echo -e "${BLUE}🌐 Abrindo Dashboard...${NC}"
        if command -v xdg-open &> /dev/null; then
            xdg-open "frontend-dashboard/index.html"
        elif command -v open &> /dev/null; then
            open "frontend-dashboard/index.html"
        else
            echo -e "${YELLOW}💡 Abra manualmente: frontend-dashboard/index.html${NC}"
        fi

        echo -e "${GREEN}✅ Sistema API + Dashboard iniciado!${NC}"
        echo -e "${YELLOW}💡 Para parar: Ctrl+C${NC}"
        wait $API_PID
        ;;
    4)
        echo -e "${GREEN}🐍 Iniciando Visão Computacional Python...${NC}"
        run_python python-vision/microscope_gui.py
        ;;
    5)
        echo -e "${GREEN}☕ Executando Backend Java (demo)...${NC}"
        cd backend-java/src
        java br.com.micromedicao.app.App
        cd ../..
        ;;
    6)
        echo -e "${GREEN}🌐 Abrindo Dashboard Web...${NC}"
        if command -v xdg-open &> /dev/null; then
            xdg-open "frontend-dashboard/index.html"
        elif command -v open &> /dev/null; then
            open "frontend-dashboard/index.html"
        else
            echo -e "${YELLOW}💡 Abra manualmente: frontend-dashboard/index.html${NC}"
        fi
        ;;
    7)
        echo -e "${GREEN}🤖 Iniciando Integração Automática Python + API...${NC}"
        echo
        echo -e "${BLUE}📋 O que será executado:${NC}"
        echo "   • Sistema Python simulando captura automática"
        echo "   • Dados salvos automaticamente via API"
        echo "   • Dashboard atualizado em tempo real"
        echo

        # Verificar se API está rodando
        if curl -s http://localhost:8080/api/health > /dev/null 2>&1; then
            echo -e "${GREEN}✅ API REST detectada em execução${NC}"
        else
            echo -e "${YELLOW}⚠️  API REST não detectada. Iniciando...${NC}"
            cd backend-java/src
            nohup java br.com.micromedicao.api.ApiServer > ../../logs/api.log 2>&1 &
            API_PID=$!
            cd ../..
            mkdir -p logs
            sleep 5
        fi

        run_python python-vision/api_integration.py

        # Cleanup se iniciamos a API
        if [ ! -z "$API_PID" ]; then
            echo -e "${YELLOW}🛑 Parando API Server...${NC}"
            kill $API_PID 2>/dev/null
        fi
        ;;
    8)
        echo -e "${GREEN}🔧 Abrindo menu avançado...${NC}"
        run_python run_system.py
        ;;
    0)
        echo -e "${YELLOW}👋 Saindo...${NC}"
        exit 0
        ;;
    *)
        echo -e "${RED}❌ Opção inválida!${NC}"
        exit 1
        ;;
esac