# 🚀 Guia de Deploy na Azure - Scanalyze

Este guia mostra como hospedar o sistema Scanalyze na Azure usando seus $100 de créditos.

## 📋 Pré-requisitos

1. **Conta Azure** com créditos ativos
2. **Azure CLI** instalado: https://docs.microsoft.com/cli/azure/install-azure-cli
3. **Docker** instalado: https://docs.docker.com/get-docker/
4. **Git** e conta no GitHub
5. **Node.js 18+** para build do frontend

## 🎯 Arquitetura Final

```
┌─────────────────────────────────────────────────┐
│  Azure Static Web Apps (Frontend)              │
│  https://scanalyze.azurestaticapps.net         │
└───────────────┬─────────────────────────────────┘
                │ HTTPS
                ▼
┌─────────────────────────────────────────────────┐
│  Azure Container Apps (Backend API)            │
│  https://scanalyze-api.azurecontainerapps.io   │
└───────────────┬─────────────────────────────────┘
                │ JDBC
                ▼
┌─────────────────────────────────────────────────┐
│  Oracle Database (oracle.fiap.com.br)          │
│  Mantido no servidor atual                      │
└─────────────────────────────────────────────────┘
```

## 📝 Passo 1: Configurar Credenciais

### 1.1 Login na Azure

```bash
# Login interativo
az login

# Verificar assinatura ativa
az account show

# Se tiver múltiplas assinaturas, selecione a correta
az account set --subscription "Nome ou ID da Assinatura"
```

### 1.2 Configurar variáveis de ambiente

```bash
cd microscope-measurement-system

# Copiar template
cp .env.example .env

# Editar com suas credenciais
nano .env
```

Preencha no `.env`:
```bash
# Oracle Database (suas credenciais existentes)
ORACLE_PASSWORD=sua_senha_oracle

# Azure (escolha nomes únicos)
AZURE_REGISTRY_NAME=scanalyze           # Apenas letras e números, único globalmente
AZURE_RESOURCE_GROUP=rg-scanalyze
AZURE_LOCATION=eastus                   # ou brazilsouth para Brasil
AZURE_CONTAINER_APP_NAME=scanalyze-api
```

## 🏗️ Passo 2: Deploy do Backend (API)

### 2.1 Executar script de setup

```bash
cd azure-scripts
./setup-azure.sh
```

Este script irá:
- ✅ Criar Resource Group
- ✅ Criar Container Registry (ACR)
- ✅ Criar Container Apps Environment
- ✅ Build da imagem Docker
- ✅ Push para ACR
- ✅ Deploy do Container App
- ✅ Configurar secrets e variáveis

**Tempo estimado: 5-10 minutos**

### 2.2 Testar API

Após o deploy, teste a API:

```bash
# URL será exibida no final do script
curl https://scanalyze-api-XXXXX.azurecontainerapps.io/api/health
```

Resposta esperada:
```json
{"status":"OK","timestamp":"...","database":"connected"}
```

## 🌐 Passo 3: Deploy do Frontend

### 3.1 Criar Static Web App

```bash
./setup-static-web-app.sh
```

### 3.2 Configurar GitHub Secrets

Vá em: `Settings` → `Secrets and variables` → `Actions` → `New repository secret`

Adicione os seguintes secrets:

| Nome | Valor | Onde encontrar |
|------|-------|----------------|
| `AZURE_STATIC_WEB_APPS_API_TOKEN` | Token do Static Web App | Output do script `setup-static-web-app.sh` |
| `AZURE_REGISTRY_USERNAME` | Username do ACR | `az acr credential show --name scanalyze` |
| `AZURE_REGISTRY_PASSWORD` | Password do ACR | `az acr credential show --name scanalyze` |
| `AZURE_CREDENTIALS` | JSON com Service Principal | Ver seção 3.3 abaixo |
| `ORACLE_HOST` | `oracle.fiap.com.br` | Seu arquivo .env |
| `ORACLE_PORT` | `1521` | Seu arquivo .env |
| `ORACLE_SID` | `orcl` | Seu arquivo .env |
| `ORACLE_USER` | `RM555211` | Seu arquivo .env |
| `ORACLE_PASSWORD` | Sua senha Oracle | Seu arquivo .env |
| `API_BASE_URL` | URL da API | Output do `setup-azure.sh` |

### 3.3 Criar Service Principal para GitHub Actions

```bash
# Obter ID da assinatura
SUBSCRIPTION_ID=$(az account show --query id -o tsv)

# Criar service principal
az ad sp create-for-rbac \
  --name "scanalyze-github-actions" \
  --role contributor \
  --scopes /subscriptions/$SUBSCRIPTION_ID/resourceGroups/rg-scanalyze \
  --sdk-auth
```

Copie o JSON completo e adicione como secret `AZURE_CREDENTIALS`.

### 3.4 Atualizar URL da API no Frontend

Edite `frontend-dashboard/config.js`:

```javascript
const CONFIG = {
    API_BASE_URL: window.location.hostname === 'localhost'
        ? 'http://localhost:8081/api'
        : 'https://SUA-URL-AQUI.azurecontainerapps.io/api',  // ← Atualize aqui
    // ...
};
```

### 3.5 Fazer deploy via GitHub

```bash
cd ..
git add .
git commit -m "feat: configuração para deploy na Azure"
git push origin main
```

As GitHub Actions serão acionadas automaticamente e farão o deploy!

## 🔄 Passo 4: Testar Sistema Completo

1. **Acesse o frontend**: https://scanalyze.azurestaticapps.net
2. **Verifique conexão com API**: Dashboard deve carregar dados
3. **Teste Python local**: Atualize URL em `python-vision/api_integration.py`

```python
# Em python-vision/api_integration.py
API_BASE_URL = "https://scanalyze-api-XXXXX.azurecontainerapps.io/api"
```

## 📊 Monitoramento e Logs

### Ver logs do Container App

```bash
# Logs em tempo real
az containerapp logs show \
  --name scanalyze-api \
  --resource-group rg-scanalyze \
  --follow

# Últimas 100 linhas
az containerapp logs show \
  --name scanalyze-api \
  --resource-group rg-scanalyze \
  --tail 100
```

### Verificar métricas

```bash
# Status do Container App
az containerapp show \
  --name scanalyze-api \
  --resource-group rg-scanalyze \
  --query properties.runningStatus
```

## 💰 Gerenciar Custos

### Verificar custos atuais

```bash
# Custos do Resource Group
az consumption usage list \
  --resource-group rg-scanalyze \
  --start-date 2025-10-01 \
  --end-date 2025-10-31
```

### Estimativa mensal (com tier básico)

- **Static Web Apps**: $0 (tier Free)
- **Container Apps**: ~$10-20/mês (0.5 vCPU, 1GB RAM)
- **Container Registry**: ~$5/mês (Basic tier)
- **Total**: ~$15-25/mês

### Economizar créditos

```bash
# Pausar Container App quando não estiver usando
az containerapp update \
  --name scanalyze-api \
  --resource-group rg-scanalyze \
  --min-replicas 0 \
  --max-replicas 1

# Reativar quando precisar
az containerapp update \
  --name scanalyze-api \
  --resource-group rg-scanalyze \
  --min-replicas 1
```

## 🛠️ Atualizações e Manutenção

### Atualizar Backend

Qualquer push para `main` que modifique `backend-java/**` aciona deploy automático via GitHub Actions.

Ou manualmente:

```bash
cd backend-java
docker build -t scanalyze.azurecr.io/scanalyze-backend:latest .
az acr login --name scanalyze
docker push scanalyze.azurecr.io/scanalyze-backend:latest

# Reiniciar Container App
az containerapp update \
  --name scanalyze-api \
  --resource-group rg-scanalyze \
  --image scanalyze.azurecr.io/scanalyze-backend:latest
```

### Atualizar Frontend

Push para `main` em `frontend-dashboard/**` aciona deploy automático.

## 🗑️ Limpar Recursos (quando não precisar mais)

```bash
# CUIDADO: Isso apaga TUDO!
az group delete \
  --name rg-scanalyze \
  --yes \
  --no-wait
```

## ❓ Troubleshooting

### Erro: "Container App não inicia"

```bash
# Ver logs detalhados
az containerapp logs show \
  --name scanalyze-api \
  --resource-group rg-scanalyze \
  --tail 200

# Verificar variáveis de ambiente
az containerapp show \
  --name scanalyze-api \
  --resource-group rg-scanalyze \
  --query properties.template.containers[0].env
```

### Erro: "Oracle Database não conecta"

1. Verificar se IP da Azure está liberado no firewall do Oracle
2. Testar conexão manualmente:

```bash
# Exec no container
az containerapp exec \
  --name scanalyze-api \
  --resource-group rg-scanalyze \
  --command /bin/sh
```

### Frontend não carrega dados

1. Verificar CORS no backend (já configurado)
2. Testar API diretamente no navegador
3. Verificar console do navegador (F12)

## 📞 Suporte

- **Documentação Azure**: https://docs.microsoft.com/azure
- **Issues GitHub**: https://github.com/SEU-REPO/issues
- **Azure Support**: Portal Azure → Ajuda + Suporte

---

**🎉 Parabéns! Seu sistema Scanalyze está na nuvem!**
