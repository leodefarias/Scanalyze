-- =============================================================================
-- SISTEMA DE MICROMEDIÇÃO AUTOMATIZADA - SCANALYZE
-- Script DQL - Data Query Language
-- =============================================================================
--
-- Descrição: Script com 3 consultas SQL-DQL conforme requisitos
-- Autor: Sistema de Micromedição Automatizada
-- Data: 2025
-- Versão: 1.0
--
-- Conteúdo:
--   - Consulta 1 (10 pontos): INNER JOIN com 2 tabelas
--   - Consulta 2 (10 pontos): GROUP BY com agregação
--   - Consulta 3 (10 pontos): INNER JOIN + WHERE + ORDER BY
--
-- Pontuação: 30 pontos (Item 5)
--
-- =============================================================================

-- =============================================================================
-- CONSULTA 1: INNER JOIN (10 PONTOS)
-- =============================================================================
--
-- Descrição:
--   Esta consulta realiza uma junção entre as tabelas TB_MEASUREMENTS e
--   TB_SAMPLES para mostrar informações detalhadas de cada medição realizada,
--   incluindo dados da amostra analisada.
--
-- Objetivo:
--   Listar todas as medições com informações das amostras correspondentes,
--   mostrando o tipo de amostra, nome, área calculada e data da medição.
--
-- Tabelas utilizadas:
--   - TB_MEASUREMENTS (medições realizadas)
--   - TB_SAMPLES (amostras biológicas)
--
-- Relacionamento:
--   TB_MEASUREMENTS.SAMPLE_ID_FK = TB_SAMPLES.ID (1:N)
--
-- =============================================================================

PROMPT
PROMPT =============================================================================
PROMPT CONSULTA 1: MEDIÇÕES COM INFORMAÇÕES DAS AMOSTRAS (INNER JOIN)
PROMPT =============================================================================
PROMPT

SELECT
    m.MEASUREMENT_ID AS "ID Medição",
    s.SAMPLE_ID AS "ID Amostra",
    s.TIPO AS "Tipo de Amostra",
    s.NOME AS "Nome da Amostra",
    m.AREA_PIXELS AS "Área (pixels)",
    m.AREA_MICROMETERS AS "Área (μm²)",
    m.DATA_MEDICAO AS "Data da Medição",
    m.VALIDADA AS "Validada"
FROM TB_MEASUREMENTS m
INNER JOIN TB_SAMPLES s ON m.SAMPLE_ID_FK = s.ID;

PROMPT

-- =============================================================================
-- CONSULTA 2: GROUP BY (10 PONTOS)
-- =============================================================================
--
-- Descrição:
--   Esta consulta agrupa as medições por tipo de amostra e calcula estatísticas
--   agregadas, incluindo contagem, média, valor mínimo e máximo da área em
--   micrômetros quadrados.
--
-- Objetivo:
--   Apresentar um resumo estatístico das medições realizadas, agrupadas por
--   tipo de amostra (sangue, tecido, neurônio, epitélio), permitindo análise
--   comparativa entre os diferentes tipos.
--
-- Tabelas utilizadas:
--   - TB_SAMPLES (amostras biológicas)
--   - TB_MEASUREMENTS (medições realizadas)
--
-- Funções de agregação:
--   - COUNT: contagem total de medições
--   - AVG: média da área em μm²
--   - MIN: menor área medida
--   - MAX: maior área medida
--
-- =============================================================================

PROMPT
PROMPT =============================================================================
PROMPT CONSULTA 2: ESTATÍSTICAS DE MEDIÇÕES POR TIPO DE AMOSTRA (GROUP BY)
PROMPT =============================================================================
PROMPT

SELECT
    s.TIPO AS "Tipo de Amostra",
    COUNT(m.ID) AS "Total de Medições",
    ROUND(AVG(m.AREA_MICROMETERS), 2) AS "Área Média (μm²)",
    ROUND(MIN(m.AREA_MICROMETERS), 2) AS "Área Mínima (μm²)",
    ROUND(MAX(m.AREA_MICROMETERS), 2) AS "Área Máxima (μm²)",
    ROUND(SUM(m.AREA_MICROMETERS), 2) AS "Área Total (μm²)"
FROM TB_SAMPLES s
INNER JOIN TB_MEASUREMENTS m ON s.ID = m.SAMPLE_ID_FK
GROUP BY s.TIPO
ORDER BY COUNT(m.ID) DESC;

PROMPT

-- =============================================================================
-- CONSULTA 3: INNER JOIN + WHERE + ORDER BY (10 PONTOS)
-- =============================================================================
--
-- Descrição:
--   Esta consulta combina junção de tabelas com filtro condicional e ordenação.
--   Realiza uma junção entre TB_MEASUREMENTS e TB_OPERATORS, filtra apenas
--   medições validadas e ordena os resultados por data de medição decrescente.
--
-- Objetivo:
--   Listar medições validadas com informações do operador responsável,
--   mostrando apenas as medições que passaram por validação técnica,
--   ordenadas da mais recente para a mais antiga.
--
-- Tabelas utilizadas:
--   - TB_MEASUREMENTS (medições realizadas)
--   - TB_OPERATORS (operadores do sistema)
--
-- Relacionamento:
--   TB_MEASUREMENTS.OPERATOR_ID_FK = TB_OPERATORS.ID (1:N)
--
-- Condição de filtro:
--   WHERE m.VALIDADA = 'S' (apenas medições validadas)
--
-- Ordenação:
--   ORDER BY m.DATA_MEDICAO DESC (da mais recente para mais antiga)
--
-- =============================================================================

PROMPT
PROMPT =============================================================================
PROMPT CONSULTA 3: MEDIÇÕES VALIDADAS POR OPERADOR (INNER JOIN + WHERE + ORDER BY)
PROMPT =============================================================================
PROMPT

SELECT
    m.MEASUREMENT_ID AS "ID Medição",
    o.OPERATOR_ID AS "ID Operador",
    o.NOME AS "Nome do Operador",
    o.NIVEL_ACESSO AS "Nível de Acesso",
    m.AREA_MICROMETERS AS "Área Medida (μm²)",
    m.DATA_MEDICAO AS "Data da Medição",
    m.DATA_VALIDACAO AS "Data da Validação",
    m.OPERADOR_VALIDACAO AS "Validado Por"
FROM TB_MEASUREMENTS m
INNER JOIN TB_OPERATORS o ON m.OPERATOR_ID_FK = o.ID
WHERE m.VALIDADA = 'S'
ORDER BY m.DATA_MEDICAO DESC;

PROMPT

-- =============================================================================
-- CONSULTAS ADICIONAIS (BONUS - DEMONSTRAÇÃO)
-- =============================================================================
--
-- As consultas abaixo são exemplos adicionais de consultas úteis para o sistema,
-- demonstrando a versatilidade do modelo de dados e diferentes técnicas SQL.
--
-- =============================================================================

PROMPT
PROMPT =============================================================================
PROMPT CONSULTA BONUS 1: MEDIÇÕES POR MICROSCÓPIO E OPERADOR
PROMPT =============================================================================
PROMPT

SELECT
    dm.MICROSCOPE_ID AS "ID Microscópio",
    dm.MODELO AS "Modelo",
    dm.FABRICANTE AS "Fabricante",
    o.NOME AS "Operador",
    COUNT(m.ID) AS "Total de Medições",
    ROUND(AVG(m.AREA_MICROMETERS), 2) AS "Área Média (μm²)"
FROM TB_MEASUREMENTS m
INNER JOIN TB_DIGITAL_MICROSCOPES dm ON m.MICROSCOPE_ID_FK = dm.ID
INNER JOIN TB_OPERATORS o ON m.OPERATOR_ID_FK = o.ID
GROUP BY dm.MICROSCOPE_ID, dm.MODELO, dm.FABRICANTE, o.NOME
ORDER BY COUNT(m.ID) DESC;

PROMPT

PROMPT
PROMPT =============================================================================
PROMPT CONSULTA BONUS 2: ANÁLISE COMPLETA DE MEDIÇÕES (4 TABELAS)
PROMPT =============================================================================
PROMPT

SELECT
    m.MEASUREMENT_ID AS "ID Medição",
    s.TIPO AS "Tipo Amostra",
    s.NOME AS "Amostra",
    o.NOME AS "Operador",
    dm.MODELO AS "Microscópio",
    i.NOME_ARQUIVO AS "Arquivo Imagem",
    m.AREA_MICROMETERS AS "Área (μm²)",
    m.DATA_MEDICAO AS "Data",
    CASE
        WHEN m.VALIDADA = 'S' THEN 'Validada'
        ELSE 'Pendente'
    END AS "Status"
FROM TB_MEASUREMENTS m
INNER JOIN TB_SAMPLES s ON m.SAMPLE_ID_FK = s.ID
INNER JOIN TB_OPERATORS o ON m.OPERATOR_ID_FK = o.ID
INNER JOIN TB_DIGITAL_MICROSCOPES dm ON m.MICROSCOPE_ID_FK = dm.ID
INNER JOIN TB_MICROSCOPY_IMAGES i ON m.IMAGE_ID_FK = i.ID
ORDER BY m.DATA_MEDICAO DESC;

PROMPT

PROMPT
PROMPT =============================================================================
PROMPT CONSULTA BONUS 3: PERFORMANCE DOS OPERADORES
PROMPT =============================================================================
PROMPT

SELECT
    o.NOME AS "Operador",
    o.NIVEL_ACESSO AS "Nível",
    COUNT(m.ID) AS "Total de Medições",
    SUM(CASE WHEN m.VALIDADA = 'S' THEN 1 ELSE 0 END) AS "Validadas",
    SUM(CASE WHEN m.VALIDADA = 'N' THEN 1 ELSE 0 END) AS "Pendentes",
    ROUND(
        (SUM(CASE WHEN m.VALIDADA = 'S' THEN 1 ELSE 0 END) * 100.0) / COUNT(m.ID),
        2
    ) AS "% Validação",
    ROUND(AVG(m.AREA_MICROMETERS), 2) AS "Área Média (μm²)"
FROM TB_OPERATORS o
LEFT JOIN TB_MEASUREMENTS m ON o.ID = m.OPERATOR_ID_FK
GROUP BY o.NOME, o.NIVEL_ACESSO
HAVING COUNT(m.ID) > 0
ORDER BY COUNT(m.ID) DESC;

PROMPT

PROMPT
PROMPT =============================================================================
PROMPT CONSULTA BONUS 4: UTILIZAÇÃO DOS MICROSCÓPIOS
PROMPT =============================================================================
PROMPT

SELECT
    dm.MICROSCOPE_ID AS "ID",
    dm.MODELO AS "Modelo",
    dm.FABRICANTE AS "Fabricante",
    dm.STATUS AS "Status",
    COUNT(m.ID) AS "Total de Usos",
    MIN(m.DATA_MEDICAO) AS "Primeiro Uso",
    MAX(m.DATA_MEDICAO) AS "Último Uso"
FROM TB_DIGITAL_MICROSCOPES dm
LEFT JOIN TB_MEASUREMENTS m ON dm.ID = m.MICROSCOPE_ID_FK
GROUP BY dm.MICROSCOPE_ID, dm.MODELO, dm.FABRICANTE, dm.STATUS
ORDER BY COUNT(m.ID) DESC;

PROMPT

PROMPT
PROMPT =============================================================================
PROMPT CONSULTA BONUS 5: AMOSTRAS COM MÚLTIPLAS MEDIÇÕES
PROMPT =============================================================================
PROMPT

SELECT
    s.SAMPLE_ID AS "ID Amostra",
    s.NOME AS "Nome",
    s.TIPO AS "Tipo",
    s.DATA_COLETA AS "Data Coleta",
    COUNT(m.ID) AS "Nº Medições",
    ROUND(MIN(m.AREA_MICROMETERS), 2) AS "Área Min (μm²)",
    ROUND(MAX(m.AREA_MICROMETERS), 2) AS "Área Max (μm²)",
    ROUND(AVG(m.AREA_MICROMETERS), 2) AS "Área Média (μm²)",
    ROUND(STDDEV(m.AREA_MICROMETERS), 2) AS "Desvio Padrão"
FROM TB_SAMPLES s
INNER JOIN TB_MEASUREMENTS m ON s.ID = m.SAMPLE_ID_FK
GROUP BY s.SAMPLE_ID, s.NOME, s.TIPO, s.DATA_COLETA
ORDER BY COUNT(m.ID) DESC, s.DATA_COLETA DESC;

PROMPT

-- =============================================================================
-- RESUMO FINAL E ESTATÍSTICAS GERAIS
-- =============================================================================

PROMPT
PROMPT =============================================================================
PROMPT RESUMO FINAL DO SISTEMA
PROMPT =============================================================================
PROMPT

SELECT
    'ESTATÍSTICAS GERAIS DO SISTEMA' AS "RESUMO",
    (SELECT COUNT(*) FROM TB_OPERATORS) AS "Total Operadores",
    (SELECT COUNT(*) FROM TB_DIGITAL_MICROSCOPES) AS "Total Microscópios",
    (SELECT COUNT(*) FROM TB_SAMPLES) AS "Total Amostras",
    (SELECT COUNT(*) FROM TB_MICROSCOPY_IMAGES) AS "Total Imagens",
    (SELECT COUNT(*) FROM TB_MEASUREMENTS) AS "Total Medições",
    (SELECT COUNT(*) FROM TB_MEASUREMENTS WHERE VALIDADA = 'S') AS "Medições Validadas",
    (SELECT COUNT(*) FROM TB_MEASUREMENTS WHERE VALIDADA = 'N') AS "Medições Pendentes"
FROM DUAL;

PROMPT

SELECT
    'ANÁLISE DE DADOS' AS "TIPO",
    ROUND(AVG(m.AREA_MICROMETERS), 2) AS "Área Média Global (μm²)",
    ROUND(MIN(m.AREA_MICROMETERS), 2) AS "Menor Área (μm²)",
    ROUND(MAX(m.AREA_MICROMETERS), 2) AS "Maior Área (μm²)",
    ROUND(SUM(m.AREA_MICROMETERS), 2) AS "Área Total (μm²)"
FROM TB_MEASUREMENTS m;

PROMPT
PROMPT =============================================================================
PROMPT FIM DO SCRIPT DQL
PROMPT =============================================================================
PROMPT
PROMPT Todas as consultas foram executadas com sucesso!
PROMPT
PROMPT IMPORTANTE:
PROMPT - Consulta 1: INNER JOIN com 2 tabelas (TB_MEASUREMENTS e TB_SAMPLES)
PROMPT - Consulta 2: GROUP BY com agregação (COUNT, AVG, MIN, MAX)
PROMPT - Consulta 3: INNER JOIN + WHERE + ORDER BY (medições validadas ordenadas)
PROMPT
PROMPT As consultas BONUS são exemplos adicionais para demonstrar o sistema.
PROMPT
PROMPT =============================================================================
