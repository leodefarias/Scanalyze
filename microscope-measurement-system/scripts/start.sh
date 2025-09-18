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
    if [ -f "python-vision/venv/bin/python" ]; then
        python-vision/venv/bin/python "$@"
    else
        python3 "$@"
    fi
}

# Verificar se Java está compilado
if [ ! -f "backend-java/src/br/com/micromedicao/app/App.class" ]; then
    echo -e "${YELLOW}⚠️  Backend Java não está compilado. Compilando...${NC}"
    cd backend-java/src
    javac -encoding UTF-8 br/com/micromedicao/model/*.java br/com/micromedicao/service/*.java br/com/micromedicao/integration/*.java br/com/micromedicao/app/*.java
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
echo "1️⃣  Sistema Completo (recomendado)"
echo "2️⃣  Apenas Visão Computacional Python"
echo "3️⃣  Apenas Backend Java (demo)"
echo "4️⃣  Apenas Dashboard Web"
echo "5️⃣  Menu Avançado (run_system.py)"
echo "0️⃣  Sair"
echo

read -p "Digite sua escolha (1-5, 0 para sair): " choice

case $choice in
    1)
        echo -e "${GREEN}🚀 Iniciando sistema completo...${NC}"
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
    2)
        echo -e "${GREEN}🐍 Iniciando Visão Computacional Python...${NC}"
        run_python python-vision/microscope_gui.py
        ;;
    3)
        echo -e "${GREEN}☕ Executando Backend Java...${NC}"
        cd backend-java/src
        java br.com.micromedicao.app.App
        cd ../..
        ;;
    4)
        echo -e "${GREEN}🌐 Abrindo Dashboard Web...${NC}"
        if command -v xdg-open &> /dev/null; then
            xdg-open "frontend-dashboard/index.html"
        elif command -v open &> /dev/null; then
            open "frontend-dashboard/index.html"
        else
            echo -e "${YELLOW}💡 Abra manualmente: frontend-dashboard/index.html${NC}"
        fi
        ;;
    5)
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