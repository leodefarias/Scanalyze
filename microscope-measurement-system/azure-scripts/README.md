# 🚀 Scripts de Deploy Azure - Scanalyze

Scripts automatizados para deploy na Azure Cloud.

## 📁 Arquivos

| Script | Descrição |
|--------|-----------|
| `setup-azure.sh` | Setup completo do backend (ACR + Container Apps) |
| `setup-static-web-app.sh` | Setup do frontend (Static Web Apps) |

## ⚡ Quick Start

### 1. Configurar credenciais

```bash
# No diretório raiz do projeto
cp .env.example .env
nano .env  # Preencha suas credenciais
```

### 2. Login na Azure

```bash
az login
```

### 3. Deploy do Backend

```bash
cd azure-scripts
./setup-azure.sh
```

**Output esperado:**
- ✅ Resource Group criado
- ✅ Container Registry criado
- ✅ Imagem Docker enviada
- ✅ Container App rodando
- 📡 URL da API exibida

### 4. Deploy do Frontend

```bash
./setup-static-web-app.sh
```

**Output esperado:**
- ✅ Static Web App criado
- 🔑 Deployment token exibido

### 5. Configurar GitHub Actions

Adicione os secrets no GitHub (veja DEPLOY.md para detalhes):
- `AZURE_STATIC_WEB_APPS_API_TOKEN`
- `AZURE_CREDENTIALS`
- Credenciais do Oracle

### 6. Fazer deploy via Git

```bash
cd ..
git add .
git commit -m "feat: deploy para Azure"
git push origin main
```

## 🔍 Comandos Úteis

### Ver logs da API

```bash
az containerapp logs show \
  --name scanalyze-api \
  --resource-group rg-scanalyze \
  --follow
```

### Atualizar Container App

```bash
az containerapp update \
  --name scanalyze-api \
  --resource-group rg-scanalyze \
  --image scanalyze.azurecr.io/scanalyze-backend:latest
```

### Verificar custos

```bash
az consumption usage list \
  --resource-group rg-scanalyze
```

### Deletar tudo

```bash
az group delete --name rg-scanalyze --yes --no-wait
```

## 💰 Estimativa de Custos

Com os $100 de créditos Azure:

- **Static Web Apps**: Grátis
- **Container Apps**: ~$15-25/mês
- **Container Registry**: ~$5/mês
- **Total**: ~$20-30/mês

**Duração estimada dos créditos: 3-5 meses**

## 📚 Documentação Completa

Veja [DEPLOY.md](../DEPLOY.md) para guia completo e troubleshooting.
