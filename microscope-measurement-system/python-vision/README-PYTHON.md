# 🔬 Interface Python - Scanalyze

Sistema de captura e medição automática de microscopia integrado com a API cloud.

## 📋 Pré-requisitos

- Python 3.8 ou superior
- Conexão com internet (para usar API cloud)
- Webcam ou câmera USB (para captura de imagens)

## 🚀 Instalação

### 1. Instalar Dependências

```bash
cd python-vision
pip install -r requirements.txt
```

Ou instale manualmente:
```bash
pip install opencv-python numpy Pillow requests
```

**Nota:** O módulo `oracledb` é opcional (apenas se quiser conectar diretamente ao banco).

## 💻 Como Usar

### Opção 1: Interface Gráfica (Recomendado)

Execute o programa principal:

```bash
python microscope_gui.py
```

**Funcionalidades:**
- 🎥 **Captura de Imagens:** Conecta automaticamente à webcam
- 📏 **Medição Automática:** Detecta contornos e calcula áreas
- 💾 **Salvamento Automático:** Envia dados para API cloud
- 📊 **Visualização em Tempo Real:** Mostra imagem processada
- ✅ **Indicador de Status:** Mostra se API está conectada

**Passos:**
1. Abra a interface
2. Verifique se mostra "API ✅" (conexão com cloud)
3. Ajuste o ID da amostra e operador
4. Clique em "Iniciar Captura"
5. Posicione a amostra no microscópio
6. Clique em "Capturar Medição"
7. Dados são salvos automaticamente na nuvem! ☁️

### Opção 2: Script de Teste da API

Para testar a integração com a API:

```bash
python api_integration.py
```

Este script irá:
- ✅ Verificar conexão com API
- ✅ Criar amostras de teste
- ✅ Registrar medições de exemplo
- ✅ Exibir resultados no terminal

## 🔧 Configuração

### URL da API

A API está configurada para usar a cloud Azure:
```
https://scanalyze-api.agreeableplant-ba923b61.eastus2.azurecontainerapps.io/api
```

**Para usar localhost (desenvolvimento):**
Edite os arquivos e mude para:
```python
# microscope_gui.py linha 69
self.api_base_url = "http://localhost:8081/api"

# api_integration.py linha 18
API_BASE_URL = "http://localhost:8081/api"
```

## 📊 Fluxo de Dados

```
┌─────────────────┐
│  Microscópio    │
│   (Webcam)      │
└────────┬────────┘
         │ Captura
         ▼
┌─────────────────┐
│ Python GUI      │
│ - Processa      │
│ - Mede área     │
└────────┬────────┘
         │ HTTP POST
         ▼
┌─────────────────┐
│  API Azure      │
│ (Container App) │
└────────┬────────┘
         │ JDBC
         ▼
┌─────────────────┐
│ Oracle Database │
│ (oracle.fiap)   │
└─────────────────┘
         │
         ▼
┌─────────────────┐
│ Dashboard Web   │
│ (Static App)    │
└─────────────────┘
```

## 🐛 Troubleshooting

### Problema: "API ❌" na interface

**Causa:** Sem conexão com internet ou API offline

**Solução:**
1. Verifique sua conexão com internet
2. Teste a API manualmente:
   ```bash
   curl https://scanalyze-api.agreeableplant-ba923b61.eastus2.azurecontainerapps.io/api/health
   ```
3. Se retornar `{"status":"OK"}`, a API está funcionando

**Nota:** Mesmo com API offline, o sistema salva dados em arquivos JSON locais!

### Problema: "No module named 'requests'"

**Solução:**
```bash
pip install requests
```

### Problema: Webcam não detectada

**Solução:**
1. Verifique se a webcam está conectada
2. Tente outro índice de câmera (linha 429 do código):
   ```python
   self.cap = cv2.VideoCapture(1)  # Tente 0, 1, 2...
   ```

### Problema: Erro ao instalar opencv-python

**Solução (Linux):**
```bash
sudo apt-get install python3-opencv
```

**Solução (Windows):**
- Instale via Anaconda: `conda install opencv`
- Ou use wheel: baixe de https://www.lfd.uci.edu/~gohlke/pythonlibs/

## 📁 Arquivos Gerados

Quando a API está offline, os dados são salvos localmente em:

```
python-vision/
├── captured_images/       # Imagens capturadas
│   └── IMG_timestamp.jpg
├── samples.json          # Dados de amostras
└── measurements.json     # Dados de medições
```

Você pode importar esses arquivos manualmente no dashboard web depois.

## 🔗 Links Úteis

- **Dashboard Web:** https://victorious-bay-01c8fcf0f.1.azurestaticapps.net
- **API Docs:** `/api/health` - Health check
- **Repositório:** https://github.com/leodefarias/Scanalyze

## 📞 Suporte

Para problemas ou dúvidas:
1. Verifique os logs no terminal
2. Consulte a documentação principal: `../DEPLOY.md`
3. Abra uma issue no GitHub

---

**🎉 Pronto para usar! Execute `python microscope_gui.py` e comece a capturar medições!**
