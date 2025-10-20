-- =============================================================================
-- SCRIPT DE SETUP COMPLETO - BANCO ORACLE
-- Sistema de Micromedição Automatizada
--
-- Este script cria todas as tabelas e insere dados iniciais necessários
-- para o funcionamento completo do sistema.
--
-- Autor: Sistema de Micromedição
-- Versão: 2.0
-- Data: 2025
-- =============================================================================

WHENEVER SQLERROR EXIT SQL.SQLCODE

-- =============================================================================
-- 1. EXECUTAR SCHEMA PRINCIPAL
-- =============================================================================

PROMPT
PROMPT ============================================================
PROMPT  INICIANDO SETUP DO BANCO ORACLE - SISTEMA DE MICROMEDIÇÃO
PROMPT ============================================================
PROMPT

-- Executar o schema principal
@@../backend-java/database/oracle_schema.sql

-- =============================================================================
-- 2. INSERIR DADOS INICIAIS OBRIGATÓRIOS
-- =============================================================================

PROMPT
PROMPT ============================================================
PROMPT  INSERINDO DADOS INICIAIS
PROMPT ============================================================
PROMPT

-- Operadores do sistema
PROMPT Inserindo operadores...

INSERT INTO TB_OPERATORS (ID, OPERATOR_ID, NOME, EMAIL, NIVEL_ACESSO, ATIVO)
VALUES (1, 'OP_SYSTEM', 'Sistema Automático', 'sistema@scanalyze.com', 'ADMIN', 'S');

INSERT INTO TB_OPERATORS (ID, OPERATOR_ID, NOME, EMAIL, NIVEL_ACESSO, ATIVO)
VALUES (2, 'OP_ADMIN', 'Administrador', 'admin@scanalyze.com', 'ADMIN', 'S');

INSERT INTO TB_OPERATORS (ID, OPERATOR_ID, NOME, EMAIL, NIVEL_ACESSO, ATIVO)
VALUES (3, 'OP_TECH01', 'Técnico Laboratório', 'tecnico@scanalyze.com', 'TECNICO', 'S');

-- Microscópios digitais
PROMPT Inserindo microscópios...

INSERT INTO TB_DIGITAL_MICROSCOPES (ID, MICROSCOPE_ID, MODELO, FABRICANTE, NUMERO_SERIE, MAGNIFICACAO_MAXIMA, ESCALA_PIXELS_UM, RESOLUCAO_MAXIMA, STATUS)
VALUES (1, 'MICRO_001', 'DMi8', 'Leica Microsystems', 'LMS001', 1000.00, 10.0, '1920x1080', 'ATIVO');

INSERT INTO TB_DIGITAL_MICROSCOPES (ID, MICROSCOPE_ID, MODELO, FABRICANTE, NUMERO_SERIE, MAGNIFICACAO_MAXIMA, ESCALA_PIXELS_UM, RESOLUCAO_MAXIMA, STATUS)
VALUES (2, 'MICRO_002', 'Eclipse Ti2', 'Nikon', 'NIK002', 1500.00, 8.5, '2048x1536', 'ATIVO');

-- Amostras de teste (opcional)
PROMPT Inserindo amostras de exemplo...

INSERT INTO TB_SAMPLES (ID, SAMPLE_ID, NOME, TIPO, DATA_COLETA, OPERADOR_RESPONSAVEL, STATUS)
VALUES (1, 'SAMPLE_TEST_001', 'Amostra Teste Sistema', 'Teste', SYSDATE, 'Sistema Automático', 'ATIVA');

-- Imagem de exemplo
PROMPT Inserindo imagem de exemplo...

INSERT INTO TB_MICROSCOPY_IMAGES (ID, IMAGE_ID, NOME_ARQUIVO, RESOLUCAO, TAMANHO_ARQUIVO, FORMATO, OBSERVACOES)
VALUES (1, 'IMG_TEST_001', 'teste_sistema.jpg', '1920x1080', 1024000, 'JPEG', 'Imagem de teste do sistema');

-- Medição de exemplo
PROMPT Inserindo medição de exemplo...

INSERT INTO TB_MEASUREMENTS (
    ID, MEASUREMENT_ID, SAMPLE_ID_FK, IMAGE_ID_FK, MICROSCOPE_ID_FK, OPERATOR_ID_FK,
    AREA_PIXELS, AREA_MICROMETERS, SCALE_PIXELS_PER_UM, DATA_MEDICAO,
    METODO_PROCESSAMENTO, VALIDADA
) VALUES (
    1, 'MEAS_TEST_001', 1, 1, 1, 1,
    10000, 100.00, 10.0, SYSDATE,
    'OpenCV Automatic Detection', 'S'
);

-- Commit das alterações
COMMIT;

-- =============================================================================
-- 3. VERIFICAÇÃO DO SETUP
-- =============================================================================

PROMPT
PROMPT ============================================================
PROMPT  VERIFICANDO SETUP
PROMPT ============================================================
PROMPT

-- Verificar tabelas criadas
PROMPT Verificando tabelas criadas...
SELECT 'Tabelas criadas: ' || COUNT(*) as RESULTADO
FROM USER_TABLES
WHERE TABLE_NAME IN (
    'TB_OPERATORS',
    'TB_DIGITAL_MICROSCOPES',
    'TB_SAMPLES',
    'TB_MICROSCOPY_IMAGES',
    'TB_MEASUREMENTS'
);

-- Verificar sequences criadas
PROMPT Verificando sequences criadas...
SELECT 'Sequences criadas: ' || COUNT(*) as RESULTADO
FROM USER_SEQUENCES
WHERE SEQUENCE_NAME IN (
    'SQ_OPERATORS',
    'SQ_DIGITAL_MICROSCOPES',
    'SQ_SAMPLES',
    'SQ_MICROSCOPY_IMAGES',
    'SQ_MEASUREMENTS'
);

-- Verificar dados inseridos
PROMPT Verificando dados inseridos...

SELECT 'Operadores: ' || COUNT(*) as RESULTADO FROM TB_OPERATORS;
SELECT 'Microscópios: ' || COUNT(*) as RESULTADO FROM TB_DIGITAL_MICROSCOPES;
SELECT 'Amostras: ' || COUNT(*) as RESULTADO FROM TB_SAMPLES;
SELECT 'Imagens: ' || COUNT(*) as RESULTADO FROM TB_MICROSCOPY_IMAGES;
SELECT 'Medições: ' || COUNT(*) as RESULTADO FROM TB_MEASUREMENTS;

-- =============================================================================
-- 4. TESTE DE CONECTIVIDADE
-- =============================================================================

PROMPT
PROMPT ============================================================
PROMPT  TESTE DE CONECTIVIDADE
PROMPT ============================================================
PROMPT

-- Teste básico de conectividade
SELECT 'Teste de conectividade Oracle: OK' as STATUS FROM DUAL;

-- Verificar versão do Oracle
SELECT 'Versão Oracle: ' || BANNER as VERSAO
FROM V$VERSION
WHERE BANNER LIKE 'Oracle%'
AND ROWNUM = 1;

-- Verificar usuário atual
SELECT 'Usuário conectado: ' || USER as USUARIO FROM DUAL;

-- Verificar data/hora do servidor
SELECT 'Data/Hora servidor: ' || TO_CHAR(SYSDATE, 'DD/MM/YYYY HH24:MI:SS') as DATA_HORA FROM DUAL;

-- =============================================================================
-- 5. CONFIGURAÇÕES ADICIONAIS
-- =============================================================================

PROMPT
PROMPT ============================================================
PROMPT  CONFIGURAÇÕES ADICIONAIS
PROMPT ============================================================
PROMPT

-- Ajustar sequences para próximos valores
ALTER SEQUENCE SQ_OPERATORS RESTART START WITH 10;
ALTER SEQUENCE SQ_DIGITAL_MICROSCOPES RESTART START WITH 10;
ALTER SEQUENCE SQ_SAMPLES RESTART START WITH 100;
ALTER SEQUENCE SQ_MICROSCOPY_IMAGES RESTART START WITH 100;
ALTER SEQUENCE SQ_MEASUREMENTS RESTART START WITH 1000;

COMMIT;

-- =============================================================================
-- 6. RESULTADO FINAL
-- =============================================================================

PROMPT
PROMPT ============================================================
PROMPT  SETUP CONCLUÍDO COM SUCESSO!
PROMPT ============================================================
PROMPT
PROMPT ✅ Schema Oracle criado
PROMPT ✅ Tabelas e sequences configuradas
PROMPT ✅ Dados iniciais inseridos
PROMPT ✅ Sistema pronto para uso
PROMPT
PROMPT 📋 Resumo:
PROMPT    - 5 tabelas principais
PROMPT    - 5 sequences configuradas
PROMPT    - 3 operadores cadastrados
PROMPT    - 2 microscópios disponíveis
PROMPT    - Dados de teste inseridos
PROMPT
PROMPT 🚀 O sistema Oracle está operacional!
PROMPT ============================================================
PROMPT

-- Verificação final
SELECT
    '🎉 SETUP ORACLE COMPLETO - ' ||
    TO_CHAR(SYSDATE, 'DD/MM/YYYY HH24:MI:SS') as STATUS_FINAL
FROM DUAL;

EXIT;