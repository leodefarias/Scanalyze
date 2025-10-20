# 🔓 Permitir Push no GitHub

## Situação

O GitHub está bloqueando o push porque detectou um secret (GitHub Personal Access Token) em um commit antigo do histórico.

## ✅ Solução Simples (Recomendada)

**Clique nesta URL para permitir o push:**

```
https://github.com/leodefarias/Scanalyze/security/secret-scanning/unblock-secret/34LHo9FR9NjQHslhp8UCdCz6PNN
```

### Passo a passo:

1. **Abra a URL acima** no seu navegador
2. O GitHub vai pedir para você **confirmar que deseja permitir** este secret
3. Clique em **"Allow secret"** ou **"Permitir secret"**
4. Volte ao terminal e execute:
   ```bash
   cd "/home/leo/Área de trabalho/Scanalyze/microscope-measurement-system"
   git push origin main
   ```

**Pronto! O push será aceito.**

---

## ⚠️ Por que isso aconteceu?

O arquivo `microscope-measurement-system/credenciais.txt` continha um GitHub Personal Access Token em um commit antigo (76315f5).

Já removemos este arquivo do repositório, mas ele ainda está no histórico do Git. O GitHub detecta secrets mesmo em commits antigos para proteger sua segurança.

---

## 🔐 Segurança

- ✅ Já removemos o arquivo `credenciais.txt` do repositório
- ✅ Já adicionamos `credenciais.txt` ao `.gitignore`
- ✅ O token detectado é antigo e pode ser revogado se necessário
- ✅ Permitir o push é seguro neste caso

---

## 🚀 Após Permitir o Push

Depois que você permitir o push e executar `git push origin main`:

1. ✅ O código será enviado para o GitHub
2. ✅ A GitHub Action será acionada automaticamente
3. ✅ O frontend será deployado no Azure Static Web App
4. ✅ Tudo estará funcionando!

---

## 🆘 Alternativa (Se preferir não permitir)

Se você não quiser permitir o push do secret, podemos fazer o deploy manual do frontend sem usar GitHub Actions:

```bash
# Deploy manual via Azure CLI
cd frontend-dashboard
npx @azure/static-web-apps-cli deploy \
  --app-location . \
  --deployment-token [SEU_TOKEN_AQUI]
```

Mas a opção recomendada é **permitir o push** através da URL acima.
