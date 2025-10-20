# 🚀 Guia de Deploy via Portal Azure - Scanalyze

## ✅ Recursos Já Criados (via CLI)

- ✅ Resource Group: `rg-scanalyze` (East US 2)
- ✅ Container Registry: `scanalyze.azurecr.io`
- ✅ Imagem Docker: `scanalyze-backend:latest` (já no registry)

## 📋 Credenciais Importantes

```
Container Registry:
- URL: scanalyze.azurecr.io
- Username: scanalyze
- Password: [Obtenha via: az acr credential show --name scanalyze]

Oracle Database:
- Host: oracle.fiap.com.br
- Port: 1521
- SID: orcl
- User: RM555211
- Password: 28102005
```

---

# 🎯 Passo a Passo: Deploy do Backend (API)

## 1. Acessar o Portal Azure

1. Acesse: https://portal.azure.com
2. Faça login com `RM555211@fiap.com.br`
3. Na barra de pesquisa superior, digite: **"Container Apps"**
4. Clique em **"Container Apps"**

## 2. Criar Container App

### 2.1 Informações Básicas

1. Clique em **"+ Create"** (Criar)
2. Preencha:
   - **Subscription**: Azure for Students
   - **Resource Group**: `rg-scanalyze` (selecione o existente)
   - **Container app name**: `scanalyze-api`
   - **Region**: `East US 2`

### 2.2 Container Apps Environment

1. Em **"Container Apps Environment"**, clique em **"Create new"**
2. Preencha:
   - **Environment name**: `scanalyze-env`
   - **Zone redundancy**: Disabled
   - Clique em **"Create"**

### 2.3 Configurar Container

1. Na seção **"Container"**, clique em **"Configure"** ou **"Next: Container"**
2. Desmarque **"Use quickstart image"**
3. Preencha:
   - **Name**: `scanalyze-backend`
   - **Image source**: **Azure Container Registry**
   - **Registry**: `scanalyze.azurecr.io` (selecione)
   - **Image**: `scanalyze-backend`
   - **Image tag**: `latest`

4. Em **"Registry credentials"**:
   - **Authentication**: Admin credentials
   - Se pedir, obtenha as credenciais via:
     ```bash
     az acr credential show --name scanalyze
     ```

### 2.4 Recursos de CPU e Memória

1. Role para baixo em **"Container resource allocation"**:
   - **CPU cores**: `0.5`
   - **Memory (Gi)**: `1`

### 2.5 Variáveis de Ambiente

1. Clique em **"Environment variables"** (ou aba "Variables")
2. Adicione as seguintes variáveis (clique em **"+ Add"** para cada):

| Name | Value | Type |
|------|-------|------|
| `ORACLE_HOST` | `oracle.fiap.com.br` | Normal |
| `ORACLE_PORT` | `1521` | Normal |
| `ORACLE_SID` | `orcl` | Normal |
| `ORACLE_USER` | `RM555211` | Normal |
| `ORACLE_PASSWORD` | `28102005` | **Secret** ⚠️ |
| `PORT` | `8081` | Normal |

**IMPORTANTE**:
- Para `ORACLE_PASSWORD`, selecione **"Secret"** como tipo
- Isso protege a senha no Azure

### 2.6 Ingress (Tráfego Externo)

1. Clique em **"Ingress"** (ou aba "Ingress")
2. Marque **"Enabled"** ✅
3. Configure:
   - **Ingress traffic**: **Accepting traffic from anywhere** (público)
   - **Ingress type**: **HTTP**
   - **Target port**: `8081`
   - **Transport**: HTTP

### 2.7 Escala (Scale)

1. Clique em **"Scale"** (ou aba "Scale")
2. Configure:
   - **Min replicas**: `1`
   - **Max replicas**: `3`

### 2.8 Revisar e Criar

1. Clique em **"Review + create"**
2. Verifique todas as configurações
3. Clique em **"Create"**

**⏱️ Tempo de criação: 3-5 minutos**

## 3. Obter URL da API

Após a criação:

1. Vá para o recurso `scanalyze-api`
2. Na página **Overview**, procure por **"Application URL"**
3. A URL será algo como:
   ```
   https://scanalyze-api.agreeableplant-XXXXX.eastus2.azurecontainerapps.io
   ```
4. **COPIE ESTA URL** - você precisará dela!

## 4. Testar a API

Abra o navegador e acesse:
```
https://SUA-URL-AQUI/api/health
```

Resposta esperada:
```json
{
  "status": "OK",
  "timestamp": "...",
  "database": "connected"
}
```

---

# 🌐 Passo a Passo: Deploy do Frontend

## 1. Criar Static Web App

1. Na barra de pesquisa do Portal Azure, digite: **"Static Web Apps"**
2. Clique em **"Static Web Apps"**
3. Clique em **"+ Create"**

### 1.1 Informações Básicas

1. Preencha:
   - **Subscription**: Azure for Students
   - **Resource Group**: `rg-scanalyze`
   - **Name**: `scanalyze-frontend`
   - **Plan type**: **Free**
   - **Region for Azure Functions**: `East US 2`

### 1.2 GitHub Integration

1. Em **"Deployment details"**:
   - **Source**: **GitHub**
   - Clique em **"Sign in with GitHub"**
   - Autorize a Azure

2. Após autorização:
   - **Organization**: Seu usuário GitHub
   - **Repository**: `Scanalyze` (ou nome do seu repo)
   - **Branch**: `main`

### 1.3 Build Details

1. Em **"Build Details"**:
   - **Build Presets**: **Custom**
   - **App location**: `/microscope-measurement-system/frontend-dashboard`
   - **Api location**: (deixe vazio)
   - **Output location**: (deixe vazio)

### 1.4 Criar

1. Clique em **"Review + create"**
2. Clique em **"Create"**

**⏱️ Tempo de criação: 1-2 minutos**

## 2. Obter Deployment Token

1. Vá para o recurso `scanalyze-frontend`
2. No menu lateral, clique em **"Manage deployment token"**
3. Clique em **"Manage deployment token"** novamente
4. **COPIE O TOKEN** - você precisará dele!

## 3. Obter URL do Frontend

1. Na página **Overview** do Static Web App
2. Procure por **"URL"**
3. Será algo como:
   ```
   https://scanalyze.azurestaticapps.net
   ```

---

# ⚙️ Configuração do GitHub Secrets

Agora vamos configurar os secrets do GitHub para CI/CD automático.

## 1. Acessar GitHub Secrets

1. Acesse: https://github.com/SEU-USUARIO/Scanalyze
2. Vá em **Settings** → **Secrets and variables** → **Actions**
3. Clique em **"New repository secret"**

## 2. Adicionar Secrets (um por vez)

Adicione os seguintes secrets:

### Secret 1: AZURE_STATIC_WEB_APPS_API_TOKEN
```
Value: [Cole o token do Static Web App que você copiou]
```

### Secret 2: AZURE_REGISTRY_USERNAME
```
Value: scanalyze
```

### Secret 3: AZURE_REGISTRY_PASSWORD
```
Value: [Obtenha via: az acr credential show --name scanalyze --query passwords[0].value -o tsv]
```

### Secret 4: ORACLE_HOST
```
Value: oracle.fiap.com.br
```

### Secret 5: ORACLE_PORT
```
Value: 1521
```

### Secret 6: ORACLE_SID
```
Value: orcl
```

### Secret 7: ORACLE_USER
```
Value: RM555211
```

### Secret 8: ORACLE_PASSWORD
```
Value: 28102005
```

### Secret 9: API_BASE_URL
```
Value: https://[SUA-URL-DA-API]/api
```
**⚠️ Importante**: Substitua `[SUA-URL-DA-API]` pela URL que você copiou do Container App, e adicione `/api` no final!

Exemplo:
```
https://scanalyze-api.agreeableplant-XXXXX.eastus2.azurecontainerapps.io/api
```

### Secret 10: AZURE_CREDENTIALS

Para este secret, você precisa criar um Service Principal. Execute no terminal:

```bash
SUBSCRIPTION_ID=$(az account show --query id -o tsv)

az ad sp create-for-rbac \
  --name "scanalyze-github-actions" \
  --role contributor \
  --scopes /subscriptions/$SUBSCRIPTION_ID/resourceGroups/rg-scanalyze \
  --sdk-auth
```

Copie **TODO O JSON** retornado e cole como valor do secret `AZURE_CREDENTIALS`.

---

# 🔄 Atualizar Frontend com URL da API

Agora você precisa atualizar o arquivo de configuração do frontend:

## Opção A: Editar pelo GitHub (Recomendado)

1. Acesse: https://github.com/SEU-USUARIO/Scanalyze
2. Navegue até: `microscope-measurement-system/frontend-dashboard/config.js`
3. Clique no ícone de lápis (Edit)
4. Altere a linha 14:
   ```javascript
   // DE:
   : 'https://scanalyze-api.agreeableplant-ba923b61.eastus2.azurecontainerapps.io/api',

   // PARA:
   : 'https://[SUA-URL-DA-API]/api',
   ```
5. Commit a mudança
6. A GitHub Action será executada automaticamente!

## Opção B: Editar Localmente

Execute estes comandos no terminal (já estou preparando para você):

---

# ✅ Checklist Final

Marque conforme for completando:

## Backend (API)
- [ ] Container App `scanalyze-api` criado
- [ ] Environment `scanalyze-env` criado automaticamente
- [ ] Imagem `scanalyze.azurecr.io/scanalyze-backend:latest` configurada
- [ ] Variáveis de ambiente configuradas (incluindo secrets)
- [ ] Ingress habilitado na porta 8081
- [ ] URL da API copiada
- [ ] Endpoint `/api/health` testado e respondendo

## Frontend
- [ ] Static Web App `scanalyze-frontend` criado
- [ ] Integração com GitHub configurada
- [ ] Deployment token copiado
- [ ] URL do frontend copiada

## GitHub Secrets
- [ ] AZURE_STATIC_WEB_APPS_API_TOKEN
- [ ] AZURE_REGISTRY_USERNAME
- [ ] AZURE_REGISTRY_PASSWORD
- [ ] ORACLE_HOST, ORACLE_PORT, ORACLE_SID
- [ ] ORACLE_USER, ORACLE_PASSWORD
- [ ] API_BASE_URL (com URL da API real)
- [ ] AZURE_CREDENTIALS (Service Principal JSON)

## Configuração
- [ ] config.js atualizado com URL da API
- [ ] Código commitado no GitHub
- [ ] GitHub Actions executadas com sucesso
- [ ] Frontend acessível
- [ ] Frontend consegue se comunicar com a API

---

# 🆘 Troubleshooting

## API não inicia

1. Vá para `scanalyze-api` → **Log stream**
2. Verifique os logs para erros
3. Comum: verificar se todas as variáveis de ambiente estão corretas

## Frontend não atualiza

1. Vá para GitHub → **Actions**
2. Verifique se o workflow executou
3. Se houver erro, verifique os logs

## Erro de conexão com Oracle

1. Verifique se o IP da Azure está liberado no firewall Oracle
2. Teste a conexão manualmente na seção de logs

---

# 🎉 Pronto!

Após completar todos os passos:

- ✅ Backend (API) rodando em: `https://scanalyze-api.XXX.eastus2.azurecontainerapps.io`
- ✅ Frontend rodando em: `https://scanalyze.azurestaticapps.net`
- ✅ CI/CD automático configurado
- ✅ Sistema completamente na nuvem!

**Custo estimado mensal: ~$20-25 (seus $100 durarão ~4 meses)**
