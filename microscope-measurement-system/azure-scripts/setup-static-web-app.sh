#!/bin/bash
# ============================================================================
# Script de Setup Azure Static Web Apps - Frontend Scanalyze
# ============================================================================

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}🌐 Setup Azure Static Web Apps - Frontend${NC}"
echo "=============================================="

# Carregar variáveis do .env
if [ -f ../.env ]; then
    export $(cat ../.env | grep -v '^#' | xargs)
fi

echo -e "${YELLOW}📋 Criando Static Web App...${NC}"

# Criar Static Web App
az staticwebapp create \
    --name scanalyze-frontend \
    --resource-group "$AZURE_RESOURCE_GROUP" \
    --location "$AZURE_LOCATION" \
    --sku Free

echo -e "${GREEN}✓ Static Web App criado${NC}"

# Obter deployment token
DEPLOYMENT_TOKEN=$(az staticwebapp secrets list \
    --name scanalyze-frontend \
    --resource-group "$AZURE_RESOURCE_GROUP" \
    --query properties.apiKey -o tsv)

echo -e "\n${GREEN}✅ Setup concluído!${NC}"
echo "=============================================="
echo -e "${YELLOW}⚠️  Adicione este secret no GitHub:${NC}"
echo "Nome: AZURE_STATIC_WEB_APPS_API_TOKEN"
echo "Valor: $DEPLOYMENT_TOKEN"
echo "=============================================="
