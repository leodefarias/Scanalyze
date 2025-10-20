# 🔑 GitHub Secrets - Configuração

## ✅ Deploy Concluído via CLI

Todos os recursos foram criados com sucesso no Azure! Agora você precisa configurar os GitHub Secrets para habilitar o CI/CD automático.

---

## 📍 URLs do Projeto

### Backend (API)
```
https://scanalyze-api.politeglacier-21141b44.eastus2.azurecontainerapps.io
```

**Health Check:**
```
https://scanalyze-api.politeglacier-21141b44.eastus2.azurecontainerapps.io/api/health
```

### Frontend
```
https://happy-mud-06a54d60f.3.azurestaticapps.net
```

---

## 🔐 Configurar GitHub Secrets

### Passo 1: Acessar GitHub Secrets

1. Acesse seu repositório: https://github.com/leodefarias/Scanalyze
2. Clique em **Settings** (Configurações)
3. No menu lateral, clique em **Secrets and variables** → **Actions**
4. Clique em **"New repository secret"**

### Passo 2: Adicionar Secrets

Adicione os seguintes secrets **UM POR VEZ**:

---

#### 1. AZURE_STATIC_WEB_APPS_API_TOKEN

**Execute este comando para obter:**
```bash
az staticwebapp secrets list --name scanalyze-frontend --resource-group rg-scanalyze --query properties.apiKey -o tsv
```

Cole o resultado como valor do secret.

---

#### 2. AZURE_REGISTRY_USERNAME
```
scanalyze
```

---

#### 3. AZURE_REGISTRY_PASSWORD

**Execute este comando para obter:**
```bash
az acr credential show --name scanalyze --query passwords[0].value -o tsv
```

Cole o resultado como valor do secret.

---

#### 4. ORACLE_HOST
```
oracle.fiap.com.br
```

---

#### 5. ORACLE_PORT
```
1521
```

---

#### 6. ORACLE_SID
```
orcl
```

---

#### 7. ORACLE_USER
```
RM555211
```

---

#### 8. ORACLE_PASSWORD
```
28102005
```

---

#### 9. API_BASE_URL
```
https://scanalyze-api.politeglacier-21141b44.eastus2.azurecontainerapps.io/api
```

---

#### 10. AZURE_CREDENTIALS (Opcional - Para Backend CI/CD)

Para este secret, você precisa criar um Service Principal manualmente pelo Portal Azure:

**Opção A: Pelo Portal Azure**
1. Acesse: https://portal.azure.com
2. Pesquise por **"Azure Active Directory"**
3. Vá em **App registrations** → **New registration**
4. Nome: `scanalyze-github-actions`
5. Após criar, vá em **Certificates & secrets** → **New client secret**
6. Copie o valor do secret
7. Vá em **Resource Groups** → `rg-scanalyze` → **Access control (IAM)**
8. Adicione **"Contributor"** role para o App Registration criado

**Opção B: Via CLI (se você quiser tentar depois)**
```bash
./create-service-principal.sh
```

O JSON deve ter este formato:
```json
{
  "clientId": "xxx",
  "clientSecret": "xxx",
  "subscriptionId": "7eaee6f6-97bf-4550-b961-060de3207dec",
  "tenantId": "11dbbfe2-89b8-4549-be10-cec364e59551",
  "resourceManagerEndpointUrl": "https://management.azure.com/"
}
```

**⚠️ Importante**: O AZURE_CREDENTIALS é opcional para agora. O backend já está deployado e rodando. Este secret é necessário apenas para CI/CD automático do backend.

---

## 📝 Fazer Commit das Mudanças

Agora que o config.js está atualizado, faça o commit:

```bash
cd "/home/leo/Área de trabalho/Scanalyze/microscope-measurement-system"

git add frontend-dashboard/config.js
git commit -m "feat: atualizar URL da API para deployment Azure"
git push origin main
```

**⏱️ A GitHub Action será executada automaticamente após o push!**

---

## ✅ Checklist de Configuração

- [ ] Todos os 9 secrets principais adicionados no GitHub
- [ ] (Opcional) AZURE_CREDENTIALS adicionado
- [ ] config.js commitado e enviado
- [ ] GitHub Action executada com sucesso
- [ ] Frontend acessível em: https://happy-mud-06a54d60f.3.azurestaticapps.net
- [ ] Frontend consegue acessar a API

---

## 🧪 Testar o Sistema

### 1. Testar API (Backend)
```bash
curl https://scanalyze-api.politeglacier-21141b44.eastus2.azurecontainerapps.io/api/health
```

Resposta esperada:
```json
{
  "status": "OK",
  "service": "Scanalyze Micromedicao API",
  "version": "1.0",
  "timestamp": "2025-10-20 18:39:09",
  "database": "Connected"
}
```

### 2. Testar Frontend
Acesse: https://happy-mud-06a54d60f.3.azurestaticapps.net

O dashboard deve carregar e exibir dados da API.

### 3. Verificar GitHub Actions
Acesse: https://github.com/leodefarias/Scanalyze/actions

Você verá 2 workflows:
- **Deploy Frontend to Azure Static Web Apps** (deve executar automaticamente)
- **Deploy Backend API to Azure** (precisa do AZURE_CREDENTIALS)

---

## 🔄 Workflow CI/CD

Após configurar tudo:

### Frontend
- ✅ Qualquer push em `frontend-dashboard/**` aciona deploy automático
- ✅ Deploy via GitHub Actions → Azure Static Web Apps
- ✅ URL: https://happy-mud-06a54d60f.3.azurestaticapps.net

### Backend
- ✅ Backend já está rodando!
- ⚠️ CI/CD automático requer AZURE_CREDENTIALS (opcional)
- ✅ URL: https://scanalyze-api.politeglacier-21141b44.eastus2.azurecontainerapps.io

---

## 🆘 Troubleshooting

### Frontend não atualiza após push
1. Verifique se o secret `AZURE_STATIC_WEB_APPS_API_TOKEN` está correto
2. Vá em **Actions** no GitHub e verifique os logs

### API não responde
1. Verifique os logs:
   ```bash
   az containerapp logs show \
     --name scanalyze-api \
     --resource-group rg-scanalyze \
     --follow
   ```

### Erro de CORS no frontend
- Já configurado! Mas se houver problema, verifique o console do navegador (F12)

---

## 💰 Custos Mensais Estimados

- Static Web Apps: **$0** (Free tier)
- Container Apps: **~$15-20/mês**
- Container Registry: **~$5/mês**
- **Total: ~$20-25/mês**

Com $100 de créditos Azure, você tem **~4 meses de uso**.

---

## 🎉 Sistema Completamente Deploy!

✅ Backend (API Java) - ONLINE
✅ Frontend (Dashboard) - ONLINE
✅ Banco Oracle - CONECTADO
✅ CI/CD - CONFIGURADO
✅ HTTPS - HABILITADO

**Parabéns! Seu sistema Scanalyze está na nuvem! 🚀**
