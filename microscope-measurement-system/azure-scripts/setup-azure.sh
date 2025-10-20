#!/bin/bash
# ============================================================================
# Script de Setup Azure - Sistema de Micromedição Scanalyze
# ============================================================================
# Este script cria toda a infraestrutura necessária na Azure
#
# Pré-requisitos:
# - Azure CLI instalado (az)
# - Login feito (az login)
# - Arquivo .env configurado com credenciais
# ============================================================================

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}🚀 Setup Azure - Scanalyze${NC}"
echo "=============================================="

# Carregar variáveis do .env
if [ -f ../.env ]; then
    echo -e "${GREEN}✓ Carregando variáveis do .env${NC}"
    export $(cat ../.env | grep -v '^#' | xargs)
else
    echo -e "${RED}✗ Arquivo .env não encontrado!${NC}"
    echo "Copie .env.example para .env e configure suas credenciais"
    exit 1
fi

# Validar variáveis necessárias
if [ -z "$AZURE_RESOURCE_GROUP" ] || [ -z "$AZURE_LOCATION" ]; then
    echo -e "${RED}✗ Variáveis AZURE_RESOURCE_GROUP e AZURE_LOCATION são obrigatórias!${NC}"
    exit 1
fi

# 1. Criar Resource Group
echo -e "\n${YELLOW}📦 Criando Resource Group...${NC}"
az group create \
    --name "$AZURE_RESOURCE_GROUP" \
    --location "$AZURE_LOCATION"
echo -e "${GREEN}✓ Resource Group criado${NC}"

# 2. Criar Container Registry
echo -e "\n${YELLOW}🐳 Criando Container Registry...${NC}"
az acr create \
    --resource-group "$AZURE_RESOURCE_GROUP" \
    --name "$AZURE_REGISTRY_NAME" \
    --sku Basic \
    --admin-enabled true
echo -e "${GREEN}✓ Container Registry criado${NC}"

# Obter credenciais do ACR
ACR_USERNAME=$(az acr credential show --name "$AZURE_REGISTRY_NAME" --query username -o tsv)
ACR_PASSWORD=$(az acr credential show --name "$AZURE_REGISTRY_NAME" --query passwords[0].value -o tsv)

echo -e "${GREEN}✓ Credenciais do ACR:${NC}"
echo "   Username: $ACR_USERNAME"
echo "   Password: $ACR_PASSWORD"

# 3. Criar Container Apps Environment
echo -e "\n${YELLOW}🌍 Criando Container Apps Environment...${NC}"
az containerapp env create \
    --name "$AZURE_CONTAINER_ENVIRONMENT" \
    --resource-group "$AZURE_RESOURCE_GROUP" \
    --location "$AZURE_LOCATION"
echo -e "${GREEN}✓ Environment criado${NC}"

# 4. Build e Push da imagem Docker
echo -e "\n${YELLOW}🏗️  Building e fazendo push da imagem Docker...${NC}"
cd ../backend-java

# Login no ACR
az acr login --name "$AZURE_REGISTRY_NAME"

# Build e push
docker build -t "$AZURE_REGISTRY_NAME.azurecr.io/scanalyze-backend:latest" .
docker push "$AZURE_REGISTRY_NAME.azurecr.io/scanalyze-backend:latest"

echo -e "${GREEN}✓ Imagem Docker enviada${NC}"

# 5. Criar Container App com secrets
echo -e "\n${YELLOW}📱 Criando Container App...${NC}"
az containerapp create \
    --name "$AZURE_CONTAINER_APP_NAME" \
    --resource-group "$AZURE_RESOURCE_GROUP" \
    --environment "$AZURE_CONTAINER_ENVIRONMENT" \
    --image "$AZURE_REGISTRY_NAME.azurecr.io/scanalyze-backend:latest" \
    --target-port 8081 \
    --ingress external \
    --registry-server "$AZURE_REGISTRY_NAME.azurecr.io" \
    --registry-username "$ACR_USERNAME" \
    --registry-password "$ACR_PASSWORD" \
    --cpu 0.5 \
    --memory 1.0Gi \
    --min-replicas 1 \
    --max-replicas 3 \
    --secrets \
        oracle-password="$ORACLE_PASSWORD" \
    --env-vars \
        ORACLE_HOST="$ORACLE_HOST" \
        ORACLE_PORT="$ORACLE_PORT" \
        ORACLE_SID="$ORACLE_SID" \
        ORACLE_USER="$ORACLE_USER" \
        ORACLE_PASSWORD=secretref:oracle-password \
        PORT=8081

echo -e "${GREEN}✓ Container App criado${NC}"

# Obter URL da API
API_URL=$(az containerapp show \
    --name "$AZURE_CONTAINER_APP_NAME" \
    --resource-group "$AZURE_RESOURCE_GROUP" \
    --query properties.configuration.ingress.fqdn -o tsv)

echo -e "\n${GREEN}✅ Setup Azure concluído com sucesso!${NC}"
echo "=============================================="
echo -e "${GREEN}📡 URL da API: https://$API_URL${NC}"
echo -e "${YELLOW}⚠️  Atualize o arquivo config.js do frontend com esta URL!${NC}"
echo ""
echo "Próximos passos:"
echo "1. Teste a API: curl https://$API_URL/api/health"
echo "2. Configure GitHub Secrets (veja DEPLOY.md)"
echo "3. Deploy do frontend via GitHub Actions"
echo "=============================================="
