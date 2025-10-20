# 🧪 Teste Local com Docker - Antes do Deploy

Antes de fazer deploy na Azure, teste tudo localmente com Docker.

## Pré-requisitos

- Docker instalado
- Docker Compose instalado
- Arquivo `.env` configurado

## 🚀 Passo 1: Configurar .env

```bash
cp .env.example .env
nano .env
```

Preencha apenas as credenciais do Oracle:

```bash
ORACLE_HOST=oracle.fiap.com.br
ORACLE_PORT=1521
ORACLE_SID=orcl
ORACLE_USER=RM555211
ORACLE_PASSWORD=sua_senha_aqui
```

## 🐳 Passo 2: Build e Start

```bash
# Build das imagens
docker-compose build

# Iniciar serviços
docker-compose up -d

# Ver logs
docker-compose logs -f
```

## ✅ Passo 3: Testar

### 3.1 Testar API

```bash
# Health check
curl http://localhost:8081/api/health

# Listar operadores
curl http://localhost:8081/api/operators

# Listar medições
curl http://localhost:8081/api/measurements
```

### 3.2 Testar Frontend

Abra no navegador: http://localhost:3000

Você deve ver:
- ✅ Dashboard carregando
- ✅ Dados vindos da API
- ✅ Gráficos renderizando
- ✅ Tabelas com medições

### 3.3 Testar Integração Python

```bash
cd python-vision

# Atualizar URL da API para local
# Em api_integration.py, use: http://localhost:8081/api

python microscope_gui.py
```

## 🛑 Parar Serviços

```bash
# Parar containers
docker-compose down

# Parar e remover volumes
docker-compose down -v

# Parar e remover imagens
docker-compose down --rmi all
```

## 🔍 Troubleshooting

### Container backend não inicia

```bash
# Ver logs detalhados
docker-compose logs backend

# Verificar se Oracle está acessível
docker-compose exec backend ping oracle.fiap.com.br
```

### Erro de conexão Oracle

```bash
# Testar conexão manualmente
docker-compose exec backend sh

# Dentro do container
java -cp "ojdbc8.jar:classes" \
  br.com.micromedicao.api.ApiServer
```

### Frontend não conecta na API

1. Verificar se backend está rodando: `docker-compose ps`
2. Testar API manualmente: `curl http://localhost:8081/api/health`
3. Ver logs do frontend: `docker-compose logs frontend`

## ✨ Se tudo funcionar localmente...

Você está pronto para o deploy na Azure! 🚀

Siga o guia [DEPLOY.md](DEPLOY.md)
