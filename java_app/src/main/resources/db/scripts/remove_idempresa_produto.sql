-- Script para remover coluna id_empresa da tabela produto
-- Execute após backup da tabela

ALTER TABLE produto DROP COLUMN IF EXISTS id_empresa;

-- Verificar se coluna foi removida
-- SELECT column_name FROM information_schema.columns
-- Script para remover coluna id_empresa da tabela produto
