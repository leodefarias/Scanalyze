-- =============================================================================
-- MIGRAÇÃO: Adicionar campo BLOB para armazenar imagens
-- Script de Alteração da Tabela TB_MICROSCOPY_IMAGES
--
-- Autor: Sistema de Micromedição
-- Versão: 1.1
-- Data: 2025-09-30
-- =============================================================================

-- Adiciona coluna IMAGEM_BLOB do tipo BLOB para armazenar o conteúdo da imagem
ALTER TABLE TB_MICROSCOPY_IMAGES ADD (
    IMAGEM_BLOB BLOB
);

-- Adiciona comentário explicativo
COMMENT ON COLUMN TB_MICROSCOPY_IMAGES.IMAGEM_BLOB IS 'Conteúdo binário da imagem armazenado como BLOB';

-- Mensagem de confirmação
SELECT 'Campo IMAGEM_BLOB adicionado com sucesso à tabela TB_MICROSCOPY_IMAGES!' as STATUS FROM DUAL;

-- Verifica a estrutura atualizada da tabela
SELECT COLUMN_NAME, DATA_TYPE, DATA_LENGTH, NULLABLE
FROM USER_TAB_COLUMNS
WHERE TABLE_NAME = 'TB_MICROSCOPY_IMAGES'
ORDER BY COLUMN_ID;