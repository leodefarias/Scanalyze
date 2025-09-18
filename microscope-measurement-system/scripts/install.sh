#!/bin/bash

# Sistema de Micromedição Automatizada - Instalador
# Script de instalação automatizada para Linux/Mac

set -e  # Para em caso de erro

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔬============================================================${NC}"
echo -e "${BLUE}   SISTEMA DE MICROMEDIÇÃO AUTOMATIZADA - INSTALADOR${NC}"
echo -e "${BLUE}============================================================${NC}"
echo

# Função para verificar comando
check_command() {
    if command -v "$1" &> /dev/null; then
        echo -e "${GREEN}✅ $1 encontrado${NC}"
        return 0
    else
        echo -e "${RED}❌ $1 não encontrado${NC}"
        return 1
    fi
}

# Verificar pré-requisitos
echo -e "${YELLOW}📋 Verificando pré-requisitos...${NC}"

MISSING_DEPS=0

# Verificar Python
if check_command python3; then
    PYTHON_VERSION=$(python3 --version | cut -d' ' -f2)
    echo -e "   Versão: $PYTHON_VERSION"
else
    echo -e "${RED}   Por favor, instale Python 3.7+${NC}"
    MISSING_DEPS=1
fi

# Verificar pip
if check_command pip3; then
    PIP_VERSION=$(pip3 --version | cut -d' ' -f2)
    echo -e "   Versão: $PIP_VERSION"
else
    echo -e "${RED}   Por favor, instale pip3${NC}"
    MISSING_DEPS=1
fi

# Verificar Java
if check_command java; then
    JAVA_VERSION=$(java --version | head -n1 | cut -d' ' -f2)
    echo -e "   Versão: $JAVA_VERSION"
else
    echo -e "${RED}   Por favor, instale Java 8+${NC}"
    MISSING_DEPS=1
fi

# Verificar javac
if check_command javac; then
    JAVAC_VERSION=$(javac --version | cut -d' ' -f2)
    echo -e "   Versão: $JAVAC_VERSION"
else
    echo -e "${RED}   Por favor, instale JDK (Java Development Kit)${NC}"
    MISSING_DEPS=1
fi

if [ $MISSING_DEPS -eq 1 ]; then
    echo
    echo -e "${RED}❌ Dependências em falta. Instale os requisitos e execute novamente.${NC}"
    echo
    echo -e "${YELLOW}💡 Comandos para Ubuntu/Debian:${NC}"
    echo "   sudo apt update"
    echo "   sudo apt install python3 python3-pip openjdk-11-jdk"
    echo
    exit 1
fi

echo
echo -e "${GREEN}✅ Todos os pré-requisitos encontrados!${NC}"
echo

# Instalar dependências Python
echo -e "${YELLOW}📦 Instalando dependências Python...${NC}"
cd python-vision

if [ -f "requirements.txt" ]; then
    echo "Verificando ambiente Python..."

    # Tenta instalação com --user primeiro
    if pip3 install -r requirements.txt --user 2>/dev/null; then
        echo -e "${GREEN}✅ Dependências Python instaladas com --user!${NC}"
    else
        echo -e "${YELLOW}⚠️ Ambiente gerenciado externamente detectado. Criando ambiente virtual...${NC}"

        # Verifica se python3-venv está instalado
        if ! python3 -m venv --help >/dev/null 2>&1; then
            echo -e "${YELLOW}📦 Instalando python3-venv...${NC}"
            sudo apt update && sudo apt install -y python3-venv python3-full
        fi

        # Cria ambiente virtual se não existir
        if [ ! -d "venv" ]; then
            echo "Criando ambiente virtual..."
            python3 -m venv venv
        fi

        # Ativa ambiente virtual e instala dependências
        echo "Ativando ambiente virtual e instalando dependências..."
        source venv/bin/activate
        pip install --upgrade pip
        pip install -r requirements.txt
        deactivate

        echo -e "${GREEN}✅ Dependências Python instaladas no ambiente virtual!${NC}"
        echo -e "${BLUE}💡 Para usar o sistema, o ambiente virtual será ativado automaticamente.${NC}"
    fi
else
    echo -e "${RED}❌ Arquivo requirements.txt não encontrado!${NC}"
    exit 1
fi

cd ..

# Compilar backend Java
echo
echo -e "${YELLOW}🔨 Compilando backend Java...${NC}"
cd backend-java

# Tornar script executável
chmod +x compile_and_run.sh

# Compilar apenas (sem executar)
cd src
echo "Compilando classes Java..."
javac -encoding UTF-8 br/com/micromedicao/model/*.java br/com/micromedicao/service/*.java br/com/micromedicao/integration/*.java br/com/micromedicao/app/*.java

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Compilação Java bem-sucedida!${NC}"
else
    echo -e "${RED}❌ Erro na compilação Java!${NC}"
    exit 1
fi

cd ../..

# Criar dados de exemplo
echo
echo -e "${YELLOW}📊 Gerando dados de exemplo...${NC}"
python3 scripts/integration_example.py

echo
echo -e "${GREEN}🎉 INSTALAÇÃO CONCLUÍDA COM SUCESSO!${NC}"
echo
echo -e "${BLUE}📋 Para executar o sistema:${NC}"
echo -e "   ${YELLOW}./start.sh${NC}  - Executar sistema completo"
echo -e "   ${YELLOW}python3 run_system.py${NC}  - Menu interativo (alternativa)"
echo
echo -e "${BLUE}📋 Componentes individuais:${NC}"
echo -e "   ${YELLOW}./quick-python.sh${NC}  - Apenas visão computacional"
echo -e "   ${YELLOW}./quick-java.sh${NC}    - Apenas backend Java"
echo -e "   ${YELLOW}./quick-web.sh${NC}     - Apenas dashboard web"
echo