# 🚀 Instalação e Execução Rápida

## ⚡ **EXECUÇÃO EM 2 COMANDOS**

### Linux/Mac:
```bash
./install.sh    # Instala tudo automaticamente
./start.sh      # Executa o sistema
```

### Windows:
```batch
install.bat     # Instala tudo automaticamente
start.bat       # Executa o sistema
```

## 🎯 **Novos Launchers Rápidos**

### Executar Componentes Individuais:

**Linux/Mac:**
- `./quick-python.sh` - Apenas visão computacional
- `./quick-java.sh` - Apenas backend Java
- `./quick-web.sh` - Apenas dashboard web

**Windows:**
- `quick-python.bat` - Apenas visão computacional
- `quick-java.bat` - Apenas backend Java
- `quick-web.bat` - Apenas dashboard web

## 📋 **O que o install.sh/install.bat faz:**

✅ **Verifica automaticamente:**
- Python 3.7+
- Java 8+ e JDK
- pip/pip3

✅ **Instala automaticamente:**
- Dependências Python (OpenCV, NumPy, Pillow)
- Compila backend Java
- Gera dados de exemplo

✅ **Configura:**
- Scripts executáveis
- Permissões corretas
- Estrutura de dados

## 🎮 **O que o start.sh/start.bat oferece:**

1️⃣ **Sistema Completo** - Inicia tudo junto (recomendado)
2️⃣ **Visão Python** - Apenas captura e processamento
3️⃣ **Backend Java** - Apenas lógica de negócio
4️⃣ **Dashboard Web** - Apenas visualização
5️⃣ **Menu Avançado** - Acesso ao run_system.py original

## 🔧 **Requisitos Mínimos**

- **Python 3.7+** (com pip)
- **Java 8+** (com JDK para compilação)
- **Webcam** (para captura)
- **Navegador moderno** (para dashboard)

## ⚠️ **Solução de Problemas**

### Erro: "Permission denied"
```bash
chmod +x *.sh
```

### Erro: "Python/Java not found"
Instale os requisitos primeiro:

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install python3 python3-pip openjdk-11-jdk
```

**Windows:**
- Python: https://www.python.org/downloads/
- Java: https://www.oracle.com/java/technologies/downloads/

### Erro de compilação Java
Verifique se o JDK está instalado:
```bash
javac --version
```

## 💡 **Dicas de Uso**

- **Primeira execução**: Use sempre o instalador primeiro
- **Uso diário**: Use `start.sh/start.bat` para acesso rápido
- **Desenvolvimento**: Use componentes individuais conforme necessário
- **Webcam**: Certifique-se de que está conectada antes de iniciar

## 🎉 **Execução Recomendada**

1. **Clone/baixe o projeto**
2. **Execute: `./install.sh` (Linux/Mac) ou `install.bat` (Windows)**
3. **Execute: `./start.sh` (Linux/Mac) ou `start.bat` (Windows)**
4. **Escolha opção 1 (Sistema Completo)**
5. **Aguarde a interface Python e dashboard web abrirem**

**Pronto! O sistema está funcionando em menos de 2 minutos!** 🎯