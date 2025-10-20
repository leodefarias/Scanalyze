-- =============================================================================
-- SISTEMA DE MICROMEDIÇÃO AUTOMATIZADA - SCANALYZE
-- Script DDL - Data Definition Language
-- =============================================================================
--
-- Descrição: Script completo de criação do schema do banco de dados Oracle
-- Autor: Sistema de Micromedição Automatizada
-- Data: 2025
-- Versão: 1.0
--
-- Conteúdo:
--   1. DROP de tabelas e sequences existentes
--   2. CREATE SEQUENCE para auto-incremento
--   3. CREATE TABLE para 5 tabelas
--   4. ALTER TABLE para constraints (PK, UK, FK, CHECK)
--   5. CREATE INDEX para performance
--   6. COMMENT ON para documentação
--
-- Pontuação: 20 pontos (Item 3)
--
-- =============================================================================

-- =============================================================================
-- SEÇÃO 1: LIMPEZA DO SCHEMA (DROP)
-- =============================================================================

-- Dropar tabelas na ordem correta (respeitando foreign keys)
BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE TB_MEASUREMENTS CASCADE CONSTRAINTS';
EXCEPTION
    WHEN OTHERS THEN NULL;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE TB_MICROSCOPY_IMAGES CASCADE CONSTRAINTS';
EXCEPTION
    WHEN OTHERS THEN NULL;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE TB_SAMPLES CASCADE CONSTRAINTS';
EXCEPTION
    WHEN OTHERS THEN NULL;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE TB_DIGITAL_MICROSCOPES CASCADE CONSTRAINTS';
EXCEPTION
    WHEN OTHERS THEN NULL;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE TB_OPERATORS CASCADE CONSTRAINTS';
EXCEPTION
    WHEN OTHERS THEN NULL;
END;
/

-- Dropar sequences
BEGIN
    EXECUTE IMMEDIATE 'DROP SEQUENCE SQ_OPERATORS';
EXCEPTION
    WHEN OTHERS THEN NULL;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP SEQUENCE SQ_DIGITAL_MICROSCOPES';
EXCEPTION
    WHEN OTHERS THEN NULL;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP SEQUENCE SQ_SAMPLES';
EXCEPTION
    WHEN OTHERS THEN NULL;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP SEQUENCE SQ_MICROSCOPY_IMAGES';
EXCEPTION
    WHEN OTHERS THEN NULL;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP SEQUENCE SQ_MEASUREMENTS';
EXCEPTION
    WHEN OTHERS THEN NULL;
END;
/

-- =============================================================================
-- SEÇÃO 2: CRIAÇÃO DAS SEQUENCES
-- =============================================================================

-- Sequence para TB_OPERATORS
CREATE SEQUENCE SQ_OPERATORS
    START WITH 1
    INCREMENT BY 1
    NOMAXVALUE
    NOCYCLE
    CACHE 10;

-- Sequence para TB_DIGITAL_MICROSCOPES
CREATE SEQUENCE SQ_DIGITAL_MICROSCOPES
    START WITH 1
    INCREMENT BY 1
    NOMAXVALUE
    NOCYCLE
    CACHE 10;

-- Sequence para TB_SAMPLES
CREATE SEQUENCE SQ_SAMPLES
    START WITH 1
    INCREMENT BY 1
    NOMAXVALUE
    NOCYCLE
    CACHE 10;

-- Sequence para TB_MICROSCOPY_IMAGES
CREATE SEQUENCE SQ_MICROSCOPY_IMAGES
    START WITH 1
    INCREMENT BY 1
    NOMAXVALUE
    NOCYCLE
    CACHE 10;

-- Sequence para TB_MEASUREMENTS
CREATE SEQUENCE SQ_MEASUREMENTS
    START WITH 1
    INCREMENT BY 1
    NOMAXVALUE
    NOCYCLE
    CACHE 10;

-- =============================================================================
-- SEÇÃO 3: CRIAÇÃO DAS TABELAS
-- =============================================================================

-- -----------------------------------------------------------------------------
-- TABELA: TB_OPERATORS
-- Descrição: Operadores do sistema de micromedição
-- -----------------------------------------------------------------------------
CREATE TABLE TB_OPERATORS (
    ID NUMBER NOT NULL,
    OPERATOR_ID VARCHAR2(50) NOT NULL,
    NOME VARCHAR2(100) NOT NULL,
    EMAIL VARCHAR2(150) NOT NULL,
    NIVEL_ACESSO VARCHAR2(20) NOT NULL,
    DATA_CRIACAO DATE DEFAULT SYSDATE,
    ATIVO CHAR(1) DEFAULT 'S'
);

-- -----------------------------------------------------------------------------
-- TABELA: TB_DIGITAL_MICROSCOPES
-- Descrição: Microscópios digitais disponíveis no laboratório
-- -----------------------------------------------------------------------------
CREATE TABLE TB_DIGITAL_MICROSCOPES (
    ID NUMBER NOT NULL,
    MICROSCOPE_ID VARCHAR2(50) NOT NULL,
    MODELO VARCHAR2(100) NOT NULL,
    FABRICANTE VARCHAR2(100) NOT NULL,
    NUMERO_SERIE VARCHAR2(100),
    MAGNIFICACAO_MAXIMA NUMBER(10,2),
    ESCALA_PIXELS_UM NUMBER(10,4) DEFAULT 10.0,
    RESOLUCAO_MAXIMA VARCHAR2(20),
    DATA_AQUISICAO DATE,
    STATUS VARCHAR2(20) DEFAULT 'ATIVO',
    OBSERVACOES VARCHAR2(500),
    DATA_CRIACAO DATE DEFAULT SYSDATE
);

-- -----------------------------------------------------------------------------
-- TABELA: TB_SAMPLES
-- Descrição: Amostras biológicas coletadas para análise microscópica
-- -----------------------------------------------------------------------------
CREATE TABLE TB_SAMPLES (
    ID NUMBER NOT NULL,
    SAMPLE_ID VARCHAR2(50) NOT NULL,
    NOME VARCHAR2(200) NOT NULL,
    TIPO VARCHAR2(100) NOT NULL,
    DATA_COLETA DATE NOT NULL,
    OPERADOR_RESPONSAVEL VARCHAR2(100) NOT NULL,
    DESCRICAO VARCHAR2(1000),
    PACIENTE_ID VARCHAR2(50),
    LOCALIZACAO_ANATOMICA VARCHAR2(200),
    METODO_PREPARACAO VARCHAR2(200),
    DATA_CRIACAO DATE DEFAULT SYSDATE,
    STATUS VARCHAR2(20) DEFAULT 'ATIVA'
);

-- -----------------------------------------------------------------------------
-- TABELA: TB_MICROSCOPY_IMAGES
-- Descrição: Imagens microscópicas capturadas
-- -----------------------------------------------------------------------------
CREATE TABLE TB_MICROSCOPY_IMAGES (
    ID NUMBER NOT NULL,
    IMAGE_ID VARCHAR2(50) NOT NULL,
    NOME_ARQUIVO VARCHAR2(500) NOT NULL,
    IMAGEM_BLOB BLOB,
    RESOLUCAO VARCHAR2(20),
    TAMANHO_ARQUIVO NUMBER,
    FORMATO VARCHAR2(10) DEFAULT 'JPEG',
    DATA_CRIACAO DATE DEFAULT SYSDATE,
    OBSERVACOES VARCHAR2(1000)
);

-- -----------------------------------------------------------------------------
-- TABELA: TB_MEASUREMENTS
-- Descrição: Medições realizadas nas imagens microscópicas (tabela central)
-- -----------------------------------------------------------------------------
CREATE TABLE TB_MEASUREMENTS (
    ID NUMBER NOT NULL,
    MEASUREMENT_ID VARCHAR2(50) NOT NULL,
    SAMPLE_ID_FK NUMBER,
    IMAGE_ID_FK NUMBER,
    MICROSCOPE_ID_FK NUMBER,
    OPERATOR_ID_FK NUMBER,
    AREA_PIXELS NUMBER NOT NULL,
    AREA_MICROMETERS NUMBER(15,6) NOT NULL,
    SCALE_PIXELS_PER_UM NUMBER(10,4) NOT NULL,
    DATA_MEDICAO DATE NOT NULL,
    METODO_PROCESSAMENTO VARCHAR2(100),
    PARAMETROS_PROCESSAMENTO VARCHAR2(1000),
    OBSERVACOES VARCHAR2(1000),
    VALIDADA CHAR(1) DEFAULT 'N',
    DATA_VALIDACAO DATE,
    OPERADOR_VALIDACAO VARCHAR2(100),
    DATA_CRIACAO DATE DEFAULT SYSDATE
);

-- =============================================================================
-- SEÇÃO 4: CONSTRAINTS (ALTER TABLE)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Constraints para TB_OPERATORS
-- -----------------------------------------------------------------------------

-- Primary Key
ALTER TABLE TB_OPERATORS
    ADD CONSTRAINT PK_OPERATORS PRIMARY KEY (ID);

-- Unique Keys
ALTER TABLE TB_OPERATORS
    ADD CONSTRAINT UK_OPERATORS_OPERATOR_ID UNIQUE (OPERATOR_ID);

ALTER TABLE TB_OPERATORS
    ADD CONSTRAINT UK_OPERATORS_EMAIL UNIQUE (EMAIL);

-- Check Constraints
ALTER TABLE TB_OPERATORS
    ADD CONSTRAINT CK_OPERATORS_NIVEL_ACESSO
    CHECK (NIVEL_ACESSO IN ('ADMIN', 'TECNICO', 'OPERADOR'));

ALTER TABLE TB_OPERATORS
    ADD CONSTRAINT CK_OPERATORS_ATIVO
    CHECK (ATIVO IN ('S', 'N'));

-- -----------------------------------------------------------------------------
-- Constraints para TB_DIGITAL_MICROSCOPES
-- -----------------------------------------------------------------------------

-- Primary Key
ALTER TABLE TB_DIGITAL_MICROSCOPES
    ADD CONSTRAINT PK_DIGITAL_MICROSCOPES PRIMARY KEY (ID);

-- Unique Keys
ALTER TABLE TB_DIGITAL_MICROSCOPES
    ADD CONSTRAINT UK_DIGITAL_MICROSCOPES_ID UNIQUE (MICROSCOPE_ID);

ALTER TABLE TB_DIGITAL_MICROSCOPES
    ADD CONSTRAINT UK_DIGITAL_MICROSCOPES_SERIE UNIQUE (NUMERO_SERIE);

-- Check Constraints
ALTER TABLE TB_DIGITAL_MICROSCOPES
    ADD CONSTRAINT CK_DIGITAL_MICROSCOPES_STATUS
    CHECK (STATUS IN ('ATIVO', 'INATIVO', 'MANUTENCAO'));

-- -----------------------------------------------------------------------------
-- Constraints para TB_SAMPLES
-- -----------------------------------------------------------------------------

-- Primary Key
ALTER TABLE TB_SAMPLES
    ADD CONSTRAINT PK_SAMPLES PRIMARY KEY (ID);

-- Unique Keys
ALTER TABLE TB_SAMPLES
    ADD CONSTRAINT UK_SAMPLES_SAMPLE_ID UNIQUE (SAMPLE_ID);

-- Check Constraints
ALTER TABLE TB_SAMPLES
    ADD CONSTRAINT CK_SAMPLES_STATUS
    CHECK (STATUS IN ('ATIVA', 'PROCESSADA', 'ARQUIVADA'));

-- -----------------------------------------------------------------------------
-- Constraints para TB_MICROSCOPY_IMAGES
-- -----------------------------------------------------------------------------

-- Primary Key
ALTER TABLE TB_MICROSCOPY_IMAGES
    ADD CONSTRAINT PK_MICROSCOPY_IMAGES PRIMARY KEY (ID);

-- Unique Keys
ALTER TABLE TB_MICROSCOPY_IMAGES
    ADD CONSTRAINT UK_MICROSCOPY_IMAGES_ID UNIQUE (IMAGE_ID);

-- Check Constraints
ALTER TABLE TB_MICROSCOPY_IMAGES
    ADD CONSTRAINT CK_MICROSCOPY_IMAGES_FORMATO
    CHECK (FORMATO IN ('JPEG', 'JPG', 'PNG', 'TIFF', 'BMP'));

-- -----------------------------------------------------------------------------
-- Constraints para TB_MEASUREMENTS
-- -----------------------------------------------------------------------------

-- Primary Key
ALTER TABLE TB_MEASUREMENTS
    ADD CONSTRAINT PK_MEASUREMENTS PRIMARY KEY (ID);

-- Unique Keys
ALTER TABLE TB_MEASUREMENTS
    ADD CONSTRAINT UK_MEASUREMENTS_ID UNIQUE (MEASUREMENT_ID);

-- Foreign Keys
ALTER TABLE TB_MEASUREMENTS
    ADD CONSTRAINT FK_MEASUREMENTS_SAMPLE
    FOREIGN KEY (SAMPLE_ID_FK) REFERENCES TB_SAMPLES(ID);

ALTER TABLE TB_MEASUREMENTS
    ADD CONSTRAINT FK_MEASUREMENTS_IMAGE
    FOREIGN KEY (IMAGE_ID_FK) REFERENCES TB_MICROSCOPY_IMAGES(ID);

ALTER TABLE TB_MEASUREMENTS
    ADD CONSTRAINT FK_MEASUREMENTS_MICROSCOPE
    FOREIGN KEY (MICROSCOPE_ID_FK) REFERENCES TB_DIGITAL_MICROSCOPES(ID);

ALTER TABLE TB_MEASUREMENTS
    ADD CONSTRAINT FK_MEASUREMENTS_OPERATOR
    FOREIGN KEY (OPERATOR_ID_FK) REFERENCES TB_OPERATORS(ID);

-- Check Constraints
ALTER TABLE TB_MEASUREMENTS
    ADD CONSTRAINT CK_MEASUREMENTS_AREA_PIXELS
    CHECK (AREA_PIXELS > 0);

ALTER TABLE TB_MEASUREMENTS
    ADD CONSTRAINT CK_MEASUREMENTS_AREA_UM
    CHECK (AREA_MICROMETERS > 0);

ALTER TABLE TB_MEASUREMENTS
    ADD CONSTRAINT CK_MEASUREMENTS_SCALE
    CHECK (SCALE_PIXELS_PER_UM > 0);

ALTER TABLE TB_MEASUREMENTS
    ADD CONSTRAINT CK_MEASUREMENTS_VALIDADA
    CHECK (VALIDADA IN ('S', 'N'));

-- =============================================================================
-- SEÇÃO 5: ÍNDICES PARA PERFORMANCE
-- =============================================================================

-- Índices para TB_OPERATORS
CREATE INDEX IDX_OPERATORS_EMAIL ON TB_OPERATORS(EMAIL);
CREATE INDEX IDX_OPERATORS_NIVEL_ACESSO ON TB_OPERATORS(NIVEL_ACESSO);
CREATE INDEX IDX_OPERATORS_ATIVO ON TB_OPERATORS(ATIVO);

-- Índices para TB_DIGITAL_MICROSCOPES
CREATE INDEX IDX_MICROSCOPES_STATUS ON TB_DIGITAL_MICROSCOPES(STATUS);
CREATE INDEX IDX_MICROSCOPES_FABRICANTE ON TB_DIGITAL_MICROSCOPES(FABRICANTE);

-- Índices para TB_SAMPLES
CREATE INDEX IDX_SAMPLES_TIPO ON TB_SAMPLES(TIPO);
CREATE INDEX IDX_SAMPLES_DATA_COLETA ON TB_SAMPLES(DATA_COLETA);
CREATE INDEX IDX_SAMPLES_STATUS ON TB_SAMPLES(STATUS);
CREATE INDEX IDX_SAMPLES_OPERADOR_RESP ON TB_SAMPLES(OPERADOR_RESPONSAVEL);

-- Índices para TB_MICROSCOPY_IMAGES
CREATE INDEX IDX_IMAGES_FORMATO ON TB_MICROSCOPY_IMAGES(FORMATO);

-- Índices para TB_MEASUREMENTS
CREATE INDEX IDX_MEASUREMENTS_DATA_MEDICAO ON TB_MEASUREMENTS(DATA_MEDICAO);
CREATE INDEX IDX_MEASUREMENTS_VALIDADA ON TB_MEASUREMENTS(VALIDADA);
CREATE INDEX IDX_MEASUREMENTS_AREA_UM ON TB_MEASUREMENTS(AREA_MICROMETERS);

-- =============================================================================
-- SEÇÃO 6: COMENTÁRIOS (DOCUMENTAÇÃO)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Comentários para TB_OPERATORS
-- -----------------------------------------------------------------------------
COMMENT ON TABLE TB_OPERATORS IS
    'Tabela de operadores do sistema de micromedição automatizada';

COMMENT ON COLUMN TB_OPERATORS.ID IS
    'Chave primária numérica gerada pela sequence SQ_OPERATORS';

COMMENT ON COLUMN TB_OPERATORS.OPERATOR_ID IS
    'Identificador único do operador (ex: OP001)';

COMMENT ON COLUMN TB_OPERATORS.NOME IS
    'Nome completo do operador';

COMMENT ON COLUMN TB_OPERATORS.EMAIL IS
    'Email do operador (deve ser único)';

COMMENT ON COLUMN TB_OPERATORS.NIVEL_ACESSO IS
    'Nível de acesso: ADMIN (administrador), TECNICO (técnico) ou OPERADOR (operador básico)';

COMMENT ON COLUMN TB_OPERATORS.DATA_CRIACAO IS
    'Data de criação do registro (default: data atual)';

COMMENT ON COLUMN TB_OPERATORS.ATIVO IS
    'Indica se o operador está ativo: S (sim) ou N (não)';

-- -----------------------------------------------------------------------------
-- Comentários para TB_DIGITAL_MICROSCOPES
-- -----------------------------------------------------------------------------
COMMENT ON TABLE TB_DIGITAL_MICROSCOPES IS
    'Tabela de microscópios digitais disponíveis no laboratório';

COMMENT ON COLUMN TB_DIGITAL_MICROSCOPES.ID IS
    'Chave primária numérica gerada pela sequence SQ_DIGITAL_MICROSCOPES';

COMMENT ON COLUMN TB_DIGITAL_MICROSCOPES.MICROSCOPE_ID IS
    'Identificador único do microscópio (ex: MICRO001)';

COMMENT ON COLUMN TB_DIGITAL_MICROSCOPES.MODELO IS
    'Modelo do microscópio (ex: DM6000B)';

COMMENT ON COLUMN TB_DIGITAL_MICROSCOPES.FABRICANTE IS
    'Fabricante do microscópio (ex: Leica, Olympus, Zeiss)';

COMMENT ON COLUMN TB_DIGITAL_MICROSCOPES.NUMERO_SERIE IS
    'Número de série do equipamento (único)';

COMMENT ON COLUMN TB_DIGITAL_MICROSCOPES.MAGNIFICACAO_MAXIMA IS
    'Magnificação máxima do microscópio (ex: 1000.0 para 1000x)';

COMMENT ON COLUMN TB_DIGITAL_MICROSCOPES.ESCALA_PIXELS_UM IS
    'Escala de conversão pixels por micrômetro (default: 10.0)';

COMMENT ON COLUMN TB_DIGITAL_MICROSCOPES.RESOLUCAO_MAXIMA IS
    'Resolução máxima suportada (ex: 1920x1080)';

COMMENT ON COLUMN TB_DIGITAL_MICROSCOPES.DATA_AQUISICAO IS
    'Data de aquisição do equipamento';

COMMENT ON COLUMN TB_DIGITAL_MICROSCOPES.STATUS IS
    'Status do equipamento: ATIVO, INATIVO ou MANUTENCAO';

COMMENT ON COLUMN TB_DIGITAL_MICROSCOPES.OBSERVACOES IS
    'Observações gerais sobre o microscópio';

COMMENT ON COLUMN TB_DIGITAL_MICROSCOPES.DATA_CRIACAO IS
    'Data de criação do registro (default: data atual)';

-- -----------------------------------------------------------------------------
-- Comentários para TB_SAMPLES
-- -----------------------------------------------------------------------------
COMMENT ON TABLE TB_SAMPLES IS
    'Tabela de amostras biológicas coletadas para análise microscópica';

COMMENT ON COLUMN TB_SAMPLES.ID IS
    'Chave primária numérica gerada pela sequence SQ_SAMPLES';

COMMENT ON COLUMN TB_SAMPLES.SAMPLE_ID IS
    'Identificador único da amostra (ex: SAMPLE_001)';

COMMENT ON COLUMN TB_SAMPLES.NOME IS
    'Nome descritivo da amostra';

COMMENT ON COLUMN TB_SAMPLES.TIPO IS
    'Tipo da amostra (ex: sangue, tecido, neurônio, epitélio)';

COMMENT ON COLUMN TB_SAMPLES.DATA_COLETA IS
    'Data e hora da coleta da amostra';

COMMENT ON COLUMN TB_SAMPLES.OPERADOR_RESPONSAVEL IS
    'Nome do operador responsável pela coleta';

COMMENT ON COLUMN TB_SAMPLES.DESCRICAO IS
    'Descrição detalhada da amostra';

COMMENT ON COLUMN TB_SAMPLES.PACIENTE_ID IS
    'Identificador do paciente (se aplicável)';

COMMENT ON COLUMN TB_SAMPLES.LOCALIZACAO_ANATOMICA IS
    'Localização anatômica da coleta';

COMMENT ON COLUMN TB_SAMPLES.METODO_PREPARACAO IS
    'Método de preparação utilizado na amostra';

COMMENT ON COLUMN TB_SAMPLES.DATA_CRIACAO IS
    'Data de criação do registro (default: data atual)';

COMMENT ON COLUMN TB_SAMPLES.STATUS IS
    'Status da amostra: ATIVA, PROCESSADA ou ARQUIVADA';

-- -----------------------------------------------------------------------------
-- Comentários para TB_MICROSCOPY_IMAGES
-- -----------------------------------------------------------------------------
COMMENT ON TABLE TB_MICROSCOPY_IMAGES IS
    'Tabela de imagens microscópicas capturadas';

COMMENT ON COLUMN TB_MICROSCOPY_IMAGES.ID IS
    'Chave primária numérica gerada pela sequence SQ_MICROSCOPY_IMAGES';

COMMENT ON COLUMN TB_MICROSCOPY_IMAGES.IMAGE_ID IS
    'Identificador único da imagem (ex: IMG_1749780022)';

COMMENT ON COLUMN TB_MICROSCOPY_IMAGES.NOME_ARQUIVO IS
    'Nome do arquivo de imagem';

COMMENT ON COLUMN TB_MICROSCOPY_IMAGES.IMAGEM_BLOB IS
    'Conteúdo binário da imagem armazenado como BLOB';

COMMENT ON COLUMN TB_MICROSCOPY_IMAGES.RESOLUCAO IS
    'Resolução da imagem (ex: 1920x1080)';

COMMENT ON COLUMN TB_MICROSCOPY_IMAGES.TAMANHO_ARQUIVO IS
    'Tamanho do arquivo em bytes';

COMMENT ON COLUMN TB_MICROSCOPY_IMAGES.FORMATO IS
    'Formato da imagem: JPEG, JPG, PNG, TIFF ou BMP';

COMMENT ON COLUMN TB_MICROSCOPY_IMAGES.DATA_CRIACAO IS
    'Data de criação do registro (default: data atual)';

COMMENT ON COLUMN TB_MICROSCOPY_IMAGES.OBSERVACOES IS
    'Observações sobre a imagem';

-- -----------------------------------------------------------------------------
-- Comentários para TB_MEASUREMENTS
-- -----------------------------------------------------------------------------
COMMENT ON TABLE TB_MEASUREMENTS IS
    'Tabela central de medições realizadas nas imagens microscópicas';

COMMENT ON COLUMN TB_MEASUREMENTS.ID IS
    'Chave primária numérica gerada pela sequence SQ_MEASUREMENTS';

COMMENT ON COLUMN TB_MEASUREMENTS.MEASUREMENT_ID IS
    'Identificador único da medição (ex: MEAS_1749780022)';

COMMENT ON COLUMN TB_MEASUREMENTS.SAMPLE_ID_FK IS
    'Chave estrangeira para TB_SAMPLES - amostra associada';

COMMENT ON COLUMN TB_MEASUREMENTS.IMAGE_ID_FK IS
    'Chave estrangeira para TB_MICROSCOPY_IMAGES - imagem analisada';

COMMENT ON COLUMN TB_MEASUREMENTS.MICROSCOPE_ID_FK IS
    'Chave estrangeira para TB_DIGITAL_MICROSCOPES - microscópio utilizado';

COMMENT ON COLUMN TB_MEASUREMENTS.OPERATOR_ID_FK IS
    'Chave estrangeira para TB_OPERATORS - operador que realizou a medição';

COMMENT ON COLUMN TB_MEASUREMENTS.AREA_PIXELS IS
    'Área calculada em pixels (deve ser > 0)';

COMMENT ON COLUMN TB_MEASUREMENTS.AREA_MICROMETERS IS
    'Área calculada em micrômetros quadrados (μm²) - deve ser > 0';

COMMENT ON COLUMN TB_MEASUREMENTS.SCALE_PIXELS_PER_UM IS
    'Escala de conversão utilizada (pixels por micrômetro) - deve ser > 0';

COMMENT ON COLUMN TB_MEASUREMENTS.DATA_MEDICAO IS
    'Data e hora da realização da medição';

COMMENT ON COLUMN TB_MEASUREMENTS.METODO_PROCESSAMENTO IS
    'Método de processamento utilizado (ex: OpenCV Threshold + Contour Detection)';

COMMENT ON COLUMN TB_MEASUREMENTS.PARAMETROS_PROCESSAMENTO IS
    'Parâmetros de processamento em formato JSON';

COMMENT ON COLUMN TB_MEASUREMENTS.OBSERVACOES IS
    'Observações sobre a medição';

COMMENT ON COLUMN TB_MEASUREMENTS.VALIDADA IS
    'Indica se a medição foi validada: S (sim) ou N (não)';

COMMENT ON COLUMN TB_MEASUREMENTS.DATA_VALIDACAO IS
    'Data e hora da validação (se aplicável)';

COMMENT ON COLUMN TB_MEASUREMENTS.OPERADOR_VALIDACAO IS
    'Nome do operador que validou a medição';

COMMENT ON COLUMN TB_MEASUREMENTS.DATA_CRIACAO IS
    'Data de criação do registro (default: data atual)';

-- =============================================================================
-- MENSAGEM DE CONCLUSÃO
-- =============================================================================

-- Exibir mensagem de sucesso
SELECT 'Schema do Sistema de Micromedição Automatizada criado com sucesso!' AS STATUS FROM DUAL;

-- Verificar tabelas criadas
SELECT 'Total de tabelas criadas: ' || COUNT(*) AS INFO
FROM USER_TABLES
WHERE TABLE_NAME IN (
    'TB_OPERATORS',
    'TB_DIGITAL_MICROSCOPES',
    'TB_SAMPLES',
    'TB_MICROSCOPY_IMAGES',
    'TB_MEASUREMENTS'
);

-- Verificar sequences criadas
SELECT 'Total de sequences criadas: ' || COUNT(*) AS INFO
FROM USER_SEQUENCES
WHERE SEQUENCE_NAME IN (
    'SQ_OPERATORS',
    'SQ_DIGITAL_MICROSCOPES',
    'SQ_SAMPLES',
    'SQ_MICROSCOPY_IMAGES',
    'SQ_MEASUREMENTS'
);

-- =============================================================================
-- FIM DO SCRIPT DDL
-- =============================================================================
