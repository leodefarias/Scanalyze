#!/bin/bash
# Script para criar Service Principal para GitHub Actions

echo "Criando Service Principal para GitHub Actions..."

SUBSCRIPTION_ID=$(az account show --query id -o tsv)

echo "Subscription ID: $SUBSCRIPTION_ID"
echo ""
echo "Criando Service Principal..."
echo ""

az ad sp create-for-rbac \
  --name "scanalyze-github-actions" \
  --role contributor \
  --scopes /subscriptions/$SUBSCRIPTION_ID/resourceGroups/rg-scanalyze \
  --sdk-auth

echo ""
echo "=========================================="
echo "IMPORTANTE:"
echo "Copie TODO o JSON acima e adicione como secret"
echo "AZURE_CREDENTIALS no GitHub!"
echo "=========================================="
