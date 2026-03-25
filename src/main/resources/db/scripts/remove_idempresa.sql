-- Script para remover campo id_empresa da tabela fornecedor
-- Execute após backup da tabela

ALTER TABLE fornecedor DROP COLUMN IF EXISTS id_empresa;

-- Verificar se coluna foi removida
-- SELECT column_name FROM information_schema.columns
-- WHERE table_name='fornecedor' AND table_schema='public';
