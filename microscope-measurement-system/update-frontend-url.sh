#!/bin/bash
# Script para atualizar URL da API no frontend

echo "======================================"
echo "Atualizar URL da API no Frontend"
echo "======================================"
echo ""

# Pedir URL da API
read -p "Cole a URL da API (exemplo: https://scanalyze-api.XXX.eastus2.azurecontainerapps.io): " API_URL

# Remover / no final se houver
API_URL="${API_URL%/}"

# Adicionar /api
API_URL_WITH_PATH="${API_URL}/api"

echo ""
echo "URL completa que será configurada: $API_URL_WITH_PATH"
echo ""

# Fazer backup do config.js
cp frontend-dashboard/config.js frontend-dashboard/config.js.backup

# Atualizar o arquivo
sed -i "s|https://scanalyze-api\.agreeableplant-ba923b61\.eastus2\.azurecontainerapps\.io/api|${API_URL_WITH_PATH}|g" frontend-dashboard/config.js

echo "✅ Arquivo config.js atualizado!"
echo ""
echo "Próximos passos:"
echo "1. Verifique o arquivo: cat frontend-dashboard/config.js"
echo "2. Commit as mudanças:"
echo "   git add frontend-dashboard/config.js"
echo "   git commit -m 'Update API URL for Azure deployment'"
echo "   git push origin main"
echo ""
echo "Backup salvo em: frontend-dashboard/config.js.backup"
