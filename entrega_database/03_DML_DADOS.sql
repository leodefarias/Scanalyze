-- =============================================================================
-- SISTEMA DE MICROMEDIÇÃO AUTOMATIZADA - SCANALYZE
-- Script DML - Data Manipulation Language
-- =============================================================================
--
-- Descrição: Script completo de carga de dados de exemplo
-- Autor: Sistema de Micromedição Automatizada
-- Data: 2025
-- Versão: 1.0
--
-- Conteúdo:
--   - INSERT em TB_OPERATORS (5 registros)
--   - INSERT em TB_DIGITAL_MICROSCOPES (5 registros)
--   - INSERT em TB_SAMPLES (5 registros)
--   - INSERT em TB_MICROSCOPY_IMAGES (5 registros)
--   - INSERT em TB_MEASUREMENTS (5 registros)
--   - COMMIT e verificações finais
--
-- Pontuação: 20 pontos (Item 4)
--
-- Observação: Mínimo de 5 registros por tabela conforme requisito
--
-- =============================================================================

-- =============================================================================
-- SEÇÃO 1: INSERÇÃO DE DADOS - TB_OPERATORS
-- =============================================================================

-- Operador 1: Administrador do sistema
INSERT INTO TB_OPERATORS (
    ID, OPERATOR_ID, NOME, EMAIL, NIVEL_ACESSO, DATA_CRIACAO, ATIVO
) VALUES (
    SQ_OPERATORS.NEXTVAL,
    'OP001',
    'Dr. João Silva',
    'joao.silva@hospital.com.br',
    'ADMIN',
    SYSDATE,
    'S'
);

-- Operador 2: Técnico de laboratório
INSERT INTO TB_OPERATORS (
    ID, OPERATOR_ID, NOME, EMAIL, NIVEL_ACESSO, DATA_CRIACAO, ATIVO
) VALUES (
    SQ_OPERATORS.NEXTVAL,
    'OP002',
    'Maria Santos',
    'maria.santos@hospital.com.br',
    'TECNICO',
    SYSDATE,
    'S'
);

-- Operador 3: Operador básico
INSERT INTO TB_OPERATORS (
    ID, OPERATOR_ID, NOME, EMAIL, NIVEL_ACESSO, DATA_CRIACAO, ATIVO
) VALUES (
    SQ_OPERATORS.NEXTVAL,
    'OP003',
    'Carlos Oliveira',
    'carlos.oliveira@hospital.com.br',
    'OPERADOR',
    SYSDATE,
    'S'
);

-- Operador 4: Técnico de laboratório
INSERT INTO TB_OPERATORS (
    ID, OPERATOR_ID, NOME, EMAIL, NIVEL_ACESSO, DATA_CRIACAO, ATIVO
) VALUES (
    SQ_OPERATORS.NEXTVAL,
    'OP004',
    'Ana Costa',
    'ana.costa@hospital.com.br',
    'TECNICO',
    SYSDATE,
    'S'
);

-- Operador 5: Operador básico
INSERT INTO TB_OPERATORS (
    ID, OPERATOR_ID, NOME, EMAIL, NIVEL_ACESSO, DATA_CRIACAO, ATIVO
) VALUES (
    SQ_OPERATORS.NEXTVAL,
    'OP005',
    'Pedro Almeida',
    'pedro.almeida@hospital.com.br',
    'OPERADOR',
    SYSDATE,
    'S'
);

-- =============================================================================
-- SEÇÃO 2: INSERÇÃO DE DADOS - TB_DIGITAL_MICROSCOPES
-- =============================================================================

-- Microscópio 1: Leica DM6000B - Principal
INSERT INTO TB_DIGITAL_MICROSCOPES (
    ID, MICROSCOPE_ID, MODELO, FABRICANTE, NUMERO_SERIE,
    MAGNIFICACAO_MAXIMA, ESCALA_PIXELS_UM, RESOLUCAO_MAXIMA,
    DATA_AQUISICAO, STATUS, OBSERVACOES, DATA_CRIACAO
) VALUES (
    SQ_DIGITAL_MICROSCOPES.NEXTVAL,
    'MICRO001',
    'DM6000B',
    'Leica',
    'LM001234567',
    1000.0,
    10.0,
    '1920x1080',
    TO_DATE('2023-01-15', 'YYYY-MM-DD'),
    'ATIVO',
    'Microscópio principal do laboratório de patologia',
    SYSDATE
);

-- Microscópio 2: Olympus BX53 - Secundário
INSERT INTO TB_DIGITAL_MICROSCOPES (
    ID, MICROSCOPE_ID, MODELO, FABRICANTE, NUMERO_SERIE,
    MAGNIFICACAO_MAXIMA, ESCALA_PIXELS_UM, RESOLUCAO_MAXIMA,
    DATA_AQUISICAO, STATUS, OBSERVACOES, DATA_CRIACAO
) VALUES (
    SQ_DIGITAL_MICROSCOPES.NEXTVAL,
    'MICRO002',
    'BX53',
    'Olympus',
    'OL987654321',
    400.0,
    8.5,
    '1280x960',
    TO_DATE('2023-06-20', 'YYYY-MM-DD'),
    'ATIVO',
    'Microscópio secundário para análises rápidas',
    SYSDATE
);

-- Microscópio 3: Zeiss Axio Imager - Em manutenção
INSERT INTO TB_DIGITAL_MICROSCOPES (
    ID, MICROSCOPE_ID, MODELO, FABRICANTE, NUMERO_SERIE,
    MAGNIFICACAO_MAXIMA, ESCALA_PIXELS_UM, RESOLUCAO_MAXIMA,
    DATA_AQUISICAO, STATUS, OBSERVACOES, DATA_CRIACAO
) VALUES (
    SQ_DIGITAL_MICROSCOPES.NEXTVAL,
    'MICRO003',
    'Axio Imager',
    'Zeiss',
    'ZE555888999',
    1500.0,
    12.0,
    '2048x1536',
    TO_DATE('2022-11-10', 'YYYY-MM-DD'),
    'MANUTENCAO',
    'Em manutenção preventiva - previsão de retorno em 1 semana',
    SYSDATE
);

-- Microscópio 4: Nikon Eclipse - Alta resolução
INSERT INTO TB_DIGITAL_MICROSCOPES (
    ID, MICROSCOPE_ID, MODELO, FABRICANTE, NUMERO_SERIE,
    MAGNIFICACAO_MAXIMA, ESCALA_PIXELS_UM, RESOLUCAO_MAXIMA,
    DATA_AQUISICAO, STATUS, OBSERVACOES, DATA_CRIACAO
) VALUES (
    SQ_DIGITAL_MICROSCOPES.NEXTVAL,
    'MICRO004',
    'Eclipse Ni-E',
    'Nikon',
    'NK777444111',
    1200.0,
    11.0,
    '2560x1920',
    TO_DATE('2023-09-05', 'YYYY-MM-DD'),
    'ATIVO',
    'Microscópio de alta resolução para pesquisas avançadas',
    SYSDATE
);

-- Microscópio 5: Olympus CX23 - Inativo
INSERT INTO TB_DIGITAL_MICROSCOPES (
    ID, MICROSCOPE_ID, MODELO, FABRICANTE, NUMERO_SERIE,
    MAGNIFICACAO_MAXIMA, ESCALA_PIXELS_UM, RESOLUCAO_MAXIMA,
    DATA_AQUISICAO, STATUS, OBSERVACOES, DATA_CRIACAO
) VALUES (
    SQ_DIGITAL_MICROSCOPES.NEXTVAL,
    'MICRO005',
    'CX23',
    'Olympus',
    'OL111222333',
    200.0,
    7.5,
    '640x480',
    TO_DATE('2020-03-12', 'YYYY-MM-DD'),
    'INATIVO',
    'Equipamento antigo, substituído por modelo mais moderno',
    SYSDATE
);

-- =============================================================================
-- SEÇÃO 3: INSERÇÃO DE DADOS - TB_SAMPLES
-- =============================================================================

-- Amostra 1: Sangue (eritrócitos)
INSERT INTO TB_SAMPLES (
    ID, SAMPLE_ID, NOME, TIPO, DATA_COLETA, OPERADOR_RESPONSAVEL,
    DESCRICAO, PACIENTE_ID, LOCALIZACAO_ANATOMICA, METODO_PREPARACAO,
    DATA_CRIACAO, STATUS
) VALUES (
    SQ_SAMPLES.NEXTVAL,
    'SAMPLE_001',
    'Sangue Paciente A - Eritrócitos',
    'Sangue',
    TO_DATE('2024-01-15 08:00:00', 'YYYY-MM-DD HH24:MI:SS'),
    'Dr. João Silva',
    'Amostra de sangue coletada para análise de eritrócitos e contagem celular',
    'PAC001',
    'Veia antecubital',
    'Esfregaço em lâmina com coloração Giemsa',
    SYSDATE,
    'ATIVA'
);

-- Amostra 2: Tecido muscular
INSERT INTO TB_SAMPLES (
    ID, SAMPLE_ID, NOME, TIPO, DATA_COLETA, OPERADOR_RESPONSAVEL,
    DESCRICAO, PACIENTE_ID, LOCALIZACAO_ANATOMICA, METODO_PREPARACAO,
    DATA_CRIACAO, STATUS
) VALUES (
    SQ_SAMPLES.NEXTVAL,
    'SAMPLE_002',
    'Biópsia Muscular - Paciente B',
    'Tecido',
    TO_DATE('2024-01-15 09:30:00', 'YYYY-MM-DD HH24:MI:SS'),
    'Maria Santos',
    'Biópsia de tecido muscular para análise histopatológica de fibras',
    'PAC002',
    'Músculo deltóide',
    'Fixação em formalina e corte histológico',
    SYSDATE,
    'ATIVA'
);

-- Amostra 3: Neurônio (cultura celular)
INSERT INTO TB_SAMPLES (
    ID, SAMPLE_ID, NOME, TIPO, DATA_COLETA, OPERADOR_RESPONSAVEL,
    DESCRICAO, PACIENTE_ID, LOCALIZACAO_ANATOMICA, METODO_PREPARACAO,
    DATA_CRIACAO, STATUS
) VALUES (
    SQ_SAMPLES.NEXTVAL,
    'SAMPLE_003',
    'Cultura Neuronal - Paciente C',
    'Neurônio',
    TO_DATE('2024-01-15 15:00:00', 'YYYY-MM-DD HH24:MI:SS'),
    'Carlos Oliveira',
    'Cultura de neurônios para estudo de morfologia celular e dendritos',
    'PAC003',
    'Córtex cerebral',
    'Cultura celular em meio DMEM com soro fetal bovino',
    SYSDATE,
    'ATIVA'
);

-- Amostra 4: Tecido hepático
INSERT INTO TB_SAMPLES (
    ID, SAMPLE_ID, NOME, TIPO, DATA_COLETA, OPERADOR_RESPONSAVEL,
    DESCRICAO, PACIENTE_ID, LOCALIZACAO_ANATOMICA, METODO_PREPARACAO,
    DATA_CRIACAO, STATUS
) VALUES (
    SQ_SAMPLES.NEXTVAL,
    'SAMPLE_004',
    'Biópsia Hepática - Paciente D',
    'Tecido',
    TO_DATE('2024-01-16 10:15:00', 'YYYY-MM-DD HH24:MI:SS'),
    'Ana Costa',
    'Biópsia hepática para análise de hepatócitos e estrutura lobular',
    'PAC004',
    'Lóbulo hepático direito',
    'Fixação e processamento histológico padrão',
    SYSDATE,
    'PROCESSADA'
);

-- Amostra 5: Células epiteliais
INSERT INTO TB_SAMPLES (
    ID, SAMPLE_ID, NOME, TIPO, DATA_COLETA, OPERADOR_RESPONSAVEL,
    DESCRICAO, PACIENTE_ID, LOCALIZACAO_ANATOMICA, METODO_PREPARACAO,
    DATA_CRIACAO, STATUS
) VALUES (
    SQ_SAMPLES.NEXTVAL,
    'SAMPLE_005',
    'Células Epiteliais Bucais - Paciente E',
    'Epitélio',
    TO_DATE('2024-01-16 14:20:00', 'YYYY-MM-DD HH24:MI:SS'),
    'Pedro Almeida',
    'Raspado de mucosa bucal para análise citológica de células epiteliais descamativas',
    'PAC005',
    'Mucosa bucal',
    'Raspado e fixação em álcool 70%',
    SYSDATE,
    'ATIVA'
);

-- =============================================================================
-- SEÇÃO 4: INSERÇÃO DE DADOS - TB_MICROSCOPY_IMAGES
-- =============================================================================

-- Imagem 1: Eritrócitos
INSERT INTO TB_MICROSCOPY_IMAGES (
    ID, IMAGE_ID, NOME_ARQUIVO, IMAGEM_BLOB, RESOLUCAO,
    TAMANHO_ARQUIVO, FORMATO, DATA_CRIACAO, OBSERVACOES
) VALUES (
    SQ_MICROSCOPY_IMAGES.NEXTVAL,
    'IMG_1749780022',
    'eritrocitos_pac001_20240115.jpg',
    NULL,
    '1920x1080',
    2048576,
    'JPEG',
    SYSDATE,
    'Imagem de eritrócitos com boa definição e contraste'
);

-- Imagem 2: Tecido muscular
INSERT INTO TB_MICROSCOPY_IMAGES (
    ID, IMAGE_ID, NOME_ARQUIVO, IMAGEM_BLOB, RESOLUCAO,
    TAMANHO_ARQUIVO, FORMATO, DATA_CRIACAO, OBSERVACOES
) VALUES (
    SQ_MICROSCOPY_IMAGES.NEXTVAL,
    'IMG_1749950847',
    'tecido_muscular_pac002_20240115.jpg',
    NULL,
    '1920x1080',
    3145728,
    'JPEG',
    SYSDATE,
    'Tecido muscular com fibras bem preservadas e núcleos visíveis'
);

-- Imagem 3: Neurônios
INSERT INTO TB_MICROSCOPY_IMAGES (
    ID, IMAGE_ID, NOME_ARQUIVO, IMAGEM_BLOB, RESOLUCAO,
    TAMANHO_ARQUIVO, FORMATO, DATA_CRIACAO, OBSERVACOES
) VALUES (
    SQ_MICROSCOPY_IMAGES.NEXTVAL,
    'IMG_1749951001',
    'neuronios_pac003_20240115.jpg',
    NULL,
    '1280x960',
    1572864,
    'JPEG',
    SYSDATE,
    'Neurônios com dendritos e soma neuronal visíveis'
);

-- Imagem 4: Hepatócitos
INSERT INTO TB_MICROSCOPY_IMAGES (
    ID, IMAGE_ID, NOME_ARQUIVO, IMAGEM_BLOB, RESOLUCAO,
    TAMANHO_ARQUIVO, FORMATO, DATA_CRIACAO, OBSERVACOES
) VALUES (
    SQ_MICROSCOPY_IMAGES.NEXTVAL,
    'IMG_1749951695',
    'hepatocitos_pac004_20240116.jpg',
    NULL,
    '1920x1080',
    2621440,
    'JPEG',
    SYSDATE,
    'Hepatócitos com núcleo central bem definido e citoplasma preservado'
);

-- Imagem 5: Células epiteliais
INSERT INTO TB_MICROSCOPY_IMAGES (
    ID, IMAGE_ID, NOME_ARQUIVO, IMAGEM_BLOB, RESOLUCAO,
    TAMANHO_ARQUIVO, FORMATO, DATA_CRIACAO, OBSERVACOES
) VALUES (
    SQ_MICROSCOPY_IMAGES.NEXTVAL,
    'IMG_1749951957',
    'celulas_epiteliais_pac005_20240116.jpg',
    NULL,
    '1280x960',
    1310720,
    'JPEG',
    SYSDATE,
    'Células epiteliais descamativas com núcleo bem visível'
);

-- =============================================================================
-- SEÇÃO 5: INSERÇÃO DE DADOS - TB_MEASUREMENTS
-- =============================================================================

-- Medição 1: Eritrócitos (VALIDADA)
INSERT INTO TB_MEASUREMENTS (
    ID, MEASUREMENT_ID, SAMPLE_ID_FK, IMAGE_ID_FK, MICROSCOPE_ID_FK, OPERATOR_ID_FK,
    AREA_PIXELS, AREA_MICROMETERS, SCALE_PIXELS_PER_UM, DATA_MEDICAO,
    METODO_PROCESSAMENTO, PARAMETROS_PROCESSAMENTO, OBSERVACOES,
    VALIDADA, DATA_VALIDACAO, OPERADOR_VALIDACAO, DATA_CRIACAO
) VALUES (
    SQ_MEASUREMENTS.NEXTVAL,
    'MEAS_1749780022',
    1, -- SAMPLE_001 (Sangue)
    1, -- IMG_1749780022 (Eritrócitos)
    1, -- MICRO001 (Leica DM6000B)
    1, -- OP001 (Dr. João Silva)
    23929,
    239.29,
    10.0,
    TO_DATE('2024-01-15 10:30:22', 'YYYY-MM-DD HH24:MI:SS'),
    'OpenCV Threshold + Contour Detection',
    '{"threshold": 120, "blur_kernel": 5, "min_area": 500, "method": "OTSU"}',
    'Medição automática de eritrócitos - contagem celular',
    'S',
    TO_DATE('2024-01-15 11:00:00', 'YYYY-MM-DD HH24:MI:SS'),
    'Dr. João Silva',
    SYSDATE
);

-- Medição 2: Tecido muscular (VALIDADA)
INSERT INTO TB_MEASUREMENTS (
    ID, MEASUREMENT_ID, SAMPLE_ID_FK, IMAGE_ID_FK, MICROSCOPE_ID_FK, OPERATOR_ID_FK,
    AREA_PIXELS, AREA_MICROMETERS, SCALE_PIXELS_PER_UM, DATA_MEDICAO,
    METODO_PROCESSAMENTO, PARAMETROS_PROCESSAMENTO, OBSERVACOES,
    VALIDADA, DATA_VALIDACAO, OPERADOR_VALIDACAO, DATA_CRIACAO
) VALUES (
    SQ_MEASUREMENTS.NEXTVAL,
    'MEAS_1749950847',
    2, -- SAMPLE_002 (Tecido muscular)
    2, -- IMG_1749950847 (Tecido muscular)
    1, -- MICRO001 (Leica DM6000B)
    2, -- OP002 (Maria Santos)
    301661,
    3016.61,
    10.0,
    TO_DATE('2024-01-15 11:45:00', 'YYYY-MM-DD HH24:MI:SS'),
    'OpenCV Threshold + Contour Detection',
    '{"threshold": 115, "blur_kernel": 5, "min_area": 800, "method": "ADAPTIVE"}',
    'Área total de fibras musculares - análise histológica',
    'S',
    TO_DATE('2024-01-15 12:15:00', 'YYYY-MM-DD HH24:MI:SS'),
    'Maria Santos',
    SYSDATE
);

-- Medição 3: Neurônios (NÃO VALIDADA)
INSERT INTO TB_MEASUREMENTS (
    ID, MEASUREMENT_ID, SAMPLE_ID_FK, IMAGE_ID_FK, MICROSCOPE_ID_FK, OPERATOR_ID_FK,
    AREA_PIXELS, AREA_MICROMETERS, SCALE_PIXELS_PER_UM, DATA_MEDICAO,
    METODO_PROCESSAMENTO, PARAMETROS_PROCESSAMENTO, OBSERVACOES,
    VALIDADA, DATA_VALIDACAO, OPERADOR_VALIDACAO, DATA_CRIACAO
) VALUES (
    SQ_MEASUREMENTS.NEXTVAL,
    'MEAS_1749951001',
    3, -- SAMPLE_003 (Neurônio)
    3, -- IMG_1749951001 (Neurônios)
    2, -- MICRO002 (Olympus BX53)
    3, -- OP003 (Carlos Oliveira)
    302050,
    3020.50,
    8.5,
    TO_DATE('2024-01-15 16:10:00', 'YYYY-MM-DD HH24:MI:SS'),
    'OpenCV Threshold + Contour Detection',
    '{"threshold": 125, "blur_kernel": 3, "min_area": 300, "method": "BINARY"}',
    'Soma neuronal principal - aguardando validação técnica',
    'N',
    NULL,
    NULL,
    SYSDATE
);

-- Medição 4: Hepatócitos (VALIDADA)
INSERT INTO TB_MEASUREMENTS (
    ID, MEASUREMENT_ID, SAMPLE_ID_FK, IMAGE_ID_FK, MICROSCOPE_ID_FK, OPERATOR_ID_FK,
    AREA_PIXELS, AREA_MICROMETERS, SCALE_PIXELS_PER_UM, DATA_MEDICAO,
    METODO_PROCESSAMENTO, PARAMETROS_PROCESSAMENTO, OBSERVACOES,
    VALIDADA, DATA_VALIDACAO, OPERADOR_VALIDACAO, DATA_CRIACAO
) VALUES (
    SQ_MEASUREMENTS.NEXTVAL,
    'MEAS_1749951695',
    4, -- SAMPLE_004 (Tecido hepático)
    4, -- IMG_1749951695 (Hepatócitos)
    1, -- MICRO001 (Leica DM6000B)
    4, -- OP004 (Ana Costa)
    312138,
    3121.38,
    10.0,
    TO_DATE('2024-01-16 11:20:00', 'YYYY-MM-DD HH24:MI:SS'),
    'OpenCV Threshold + Contour Detection',
    '{"threshold": 110, "blur_kernel": 5, "min_area": 600, "method": "OTSU"}',
    'Área de hepatócitos funcionais - biópsia hepática',
    'S',
    TO_DATE('2024-01-16 12:00:00', 'YYYY-MM-DD HH24:MI:SS'),
    'Ana Costa',
    SYSDATE
);

-- Medição 5: Células epiteliais (NÃO VALIDADA)
INSERT INTO TB_MEASUREMENTS (
    ID, MEASUREMENT_ID, SAMPLE_ID_FK, IMAGE_ID_FK, MICROSCOPE_ID_FK, OPERATOR_ID_FK,
    AREA_PIXELS, AREA_MICROMETERS, SCALE_PIXELS_PER_UM, DATA_MEDICAO,
    METODO_PROCESSAMENTO, PARAMETROS_PROCESSAMENTO, OBSERVACOES,
    VALIDADA, DATA_VALIDACAO, OPERADOR_VALIDACAO, DATA_CRIACAO
) VALUES (
    SQ_MEASUREMENTS.NEXTVAL,
    'MEAS_1749951957',
    5, -- SAMPLE_005 (Epitélio)
    5, -- IMG_1749951957 (Células epiteliais)
    2, -- MICRO002 (Olympus BX53)
    5, -- OP005 (Pedro Almeida)
    289764,
    2897.64,
    8.5,
    TO_DATE('2024-01-16 15:30:00', 'YYYY-MM-DD HH24:MI:SS'),
    'OpenCV Threshold + Contour Detection',
    '{"threshold": 130, "blur_kernel": 5, "min_area": 400, "method": "ADAPTIVE"}',
    'Células epiteliais descamativas - análise citológica pendente',
    'N',
    NULL,
    NULL,
    SYSDATE
);

-- =============================================================================
-- SEÇÃO 6: COMMIT E VERIFICAÇÕES FINAIS
-- =============================================================================

-- Confirmar todas as inserções
COMMIT;

-- Mensagem de sucesso
SELECT 'Dados inseridos com sucesso!' AS STATUS FROM DUAL;

-- =============================================================================
-- SEÇÃO 7: RELATÓRIOS DE VERIFICAÇÃO
-- =============================================================================

-- Verificação de contagem de registros por tabela
PROMPT
PROMPT =============================================================================
PROMPT VERIFICAÇÃO DE DADOS INSERIDOS
PROMPT =============================================================================
PROMPT

SELECT 'TB_OPERATORS: ' || COUNT(*) || ' registros' AS RESUMO FROM TB_OPERATORS;
SELECT 'TB_DIGITAL_MICROSCOPES: ' || COUNT(*) || ' registros' AS RESUMO FROM TB_DIGITAL_MICROSCOPES;
SELECT 'TB_SAMPLES: ' || COUNT(*) || ' registros' AS RESUMO FROM TB_SAMPLES;
SELECT 'TB_MICROSCOPY_IMAGES: ' || COUNT(*) || ' registros' AS RESUMO FROM TB_MICROSCOPY_IMAGES;
SELECT 'TB_MEASUREMENTS: ' || COUNT(*) || ' registros' AS RESUMO FROM TB_MEASUREMENTS;

-- Relatório consolidado
SELECT
    'TOTAL DE REGISTROS INSERIDOS' AS TIPO,
    (SELECT COUNT(*) FROM TB_OPERATORS) AS OPERADORES,
    (SELECT COUNT(*) FROM TB_DIGITAL_MICROSCOPES) AS MICROSCOPIOS,
    (SELECT COUNT(*) FROM TB_SAMPLES) AS AMOSTRAS,
    (SELECT COUNT(*) FROM TB_MICROSCOPY_IMAGES) AS IMAGENS,
    (SELECT COUNT(*) FROM TB_MEASUREMENTS) AS MEDICOES
FROM DUAL;

-- Verificação de integridade referencial
SELECT
    'VERIFICAÇÃO DE INTEGRIDADE REFERENCIAL' AS TESTE,
    CASE
        WHEN EXISTS (
            SELECT 1 FROM TB_MEASUREMENTS m
            WHERE m.SAMPLE_ID_FK NOT IN (SELECT ID FROM TB_SAMPLES)
        ) THEN 'FALHA'
        ELSE 'OK'
    END AS SAMPLE_FK,
    CASE
        WHEN EXISTS (
            SELECT 1 FROM TB_MEASUREMENTS m
            WHERE m.IMAGE_ID_FK NOT IN (SELECT ID FROM TB_MICROSCOPY_IMAGES)
        ) THEN 'FALHA'
        ELSE 'OK'
    END AS IMAGE_FK,
    CASE
        WHEN EXISTS (
            SELECT 1 FROM TB_MEASUREMENTS m
            WHERE m.MICROSCOPE_ID_FK NOT IN (SELECT ID FROM TB_DIGITAL_MICROSCOPES)
        ) THEN 'FALHA'
        ELSE 'OK'
    END AS MICROSCOPE_FK,
    CASE
        WHEN EXISTS (
            SELECT 1 FROM TB_MEASUREMENTS m
            WHERE m.OPERATOR_ID_FK NOT IN (SELECT ID FROM TB_OPERATORS)
        ) THEN 'FALHA'
        ELSE 'OK'
    END AS OPERATOR_FK
FROM DUAL;

-- Distribuição de níveis de acesso
SELECT
    NIVEL_ACESSO,
    COUNT(*) AS TOTAL_OPERADORES
FROM TB_OPERATORS
GROUP BY NIVEL_ACESSO
ORDER BY NIVEL_ACESSO;

-- Status dos microscópios
SELECT
    STATUS,
    COUNT(*) AS TOTAL_MICROSCOPIOS
FROM TB_DIGITAL_MICROSCOPES
GROUP BY STATUS
ORDER BY STATUS;

-- Tipos de amostras
SELECT
    TIPO,
    COUNT(*) AS TOTAL_AMOSTRAS
FROM TB_SAMPLES
GROUP BY TIPO
ORDER BY TIPO;

-- Status de validação das medições
SELECT
    VALIDADA,
    COUNT(*) AS TOTAL_MEDICOES,
    ROUND(AVG(AREA_MICROMETERS), 2) AS AREA_MEDIA_UM
FROM TB_MEASUREMENTS
GROUP BY VALIDADA
ORDER BY VALIDADA DESC;

-- =============================================================================
-- FIM DO SCRIPT DML
-- =============================================================================
