# ============================================================================
# Dockerfile para Backend Java - Sistema de Micromedição Scanalyze
# ============================================================================

# Estágio 1: Build
FROM eclipse-temurin:11-jdk-alpine AS builder

WORKDIR /app

# Copia código fonte e dependências
COPY src/ ./src/
COPY ojdbc8.jar ./

# Compila o código Java (incluindo todos os pacotes)
RUN mkdir -p classes && \
    javac -d classes \
    -cp "ojdbc8.jar" \
    src/br/com/micromedicao/connection/*.java \
    src/br/com/micromedicao/model/*.java \
    src/br/com/micromedicao/dao/*.java \
    src/br/com/micromedicao/service/*.java \
    src/br/com/micromedicao/integration/*.java \
    src/br/com/micromedicao/api/*.java \
    src/br/com/micromedicao/app/*.java

# Estágio 2: Runtime
FROM eclipse-temurin:11-jre-alpine

WORKDIR /app

# Instala curl para health checks
RUN apk add --no-cache curl

# Cria usuário não-root para segurança
RUN addgroup -g 1001 scanalyze && \
    adduser -D -u 1001 -G scanalyze scanalyze

# Copia apenas os arquivos necessários do estágio de build
COPY --from=builder /app/classes ./classes
COPY --from=builder /app/ojdbc8.jar ./

# Cria diretório para dados (se necessário)
RUN mkdir -p data-integration && \
    chown -R scanalyze:scanalyze /app

# Muda para usuário não-root
USER scanalyze

# Variáveis de ambiente (podem ser sobrescritas)
ENV PORT=8081 \
    JAVA_OPTS="-Xmx512m -Xms256m" \
    ORACLE_HOST=oracle.fiap.com.br \
    ORACLE_PORT=1521 \
    ORACLE_SID=orcl \
    ORACLE_USER=RM555211

# Expõe a porta da API
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8081/api/health || exit 1

# Comando para iniciar a API
CMD java ${JAVA_OPTS} \
    -cp "ojdbc8.jar:classes" \
    br.com.micromedicao.api.ApiServer
