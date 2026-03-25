-- Active: 1762899015081@@127.0.0.1@5432@erp_oficial

#   RODE O SCRIPT DEPOIS DE CRIAR A DATABASE COM O ESTE COMANDO ISOLADO
#   CREATE DATABASE erp_oficial;

-- ========================================
-- TABELA ENDEREÇOS
-- ========================================
CREATE TABLE IF NOT EXISTS enderecos (
    id_endereco SERIAL PRIMARY KEY,
    logradouro VARCHAR(255) NOT NULL,
    numero VARCHAR(10),
    complemento VARCHAR(255),
    bairro VARCHAR(255) NOT NULL,
    cidade VARCHAR(255) NOT NULL,
    estado VARCHAR(2) NOT NULL,
    cep VARCHAR(9) NOT NULL CHECK (cep ~ '^\d{5}-\d{3}$') -- Formato CEP brasileiro
);

-- ========================================
-- TABELA FORNECEDOR
-- ========================================
CREATE TABLE IF NOT EXISTS fornecedor (
    id_fornecedor SERIAL PRIMARY KEY,
    cnpj VARCHAR(18) UNIQUE NOT NULL,
    razao_social VARCHAR(255) NOT NULL,
    telefone VARCHAR(20),
    e_mail VARCHAR(255),
    id_endereco INTEGER REFERENCES enderecos(id_endereco),
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- TABELA PRODUTO
-- ========================================
CREATE TABLE IF NOT EXISTS produto (
    id_produto SERIAL PRIMARY KEY,
    descricao TEXT NOT NULL,
    codigo_barras VARCHAR(50) UNIQUE,
    unidade_medida VARCHAR(10),
    preco_custo DECIMAL(10, 2) CHECK (preco_custo >= 0),
    preco_venda DECIMAL(10, 2) CHECK (preco_venda >= 0),
    estoque_atual INTEGER DEFAULT 0 CHECK (estoque_atual >= 0),
    id_fornecedor INTEGER,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_fornecedor) REFERENCES fornecedor(id_fornecedor) ON DELETE SET NULL
);

-- ========================================
-- TABELA MOVIMENTACAO_ESTOQUE
-- ========================================
CREATE TABLE IF NOT EXISTS movimentacao_estoque (
    id_movimento SERIAL PRIMARY KEY,
    id_produto INTEGER NOT NULL,
    tipo VARCHAR(20) CHECK (tipo IN ('ENTRADA', 'SAIDA', 'AJUSTE')),
    quantidade INTEGER NOT NULL CHECK (quantidade > 0),
    data_movimento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    origem TEXT,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_produto) REFERENCES produto(id_produto) ON DELETE CASCADE
);

-- ========================================
-- TABELA ITEM_VENDA
-- ========================================
CREATE TABLE IF NOT EXISTS item_venda (
    id_item SERIAL PRIMARY KEY,
    id_venda INTEGER NOT NULL,
    id_produto INTEGER NOT NULL,
    quantidade INTEGER NOT NULL CHECK (quantidade > 0),
    preco_unitario DECIMAL(10, 2) NOT NULL CHECK (preco_unitario >= 0),
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_produto) REFERENCES produto(id_produto) ON DELETE RESTRICT
);

-- ========================================
-- TABELA VENDA
-- ========================================
CREATE TABLE IF NOT EXISTS venda (
    id_venda SERIAL PRIMARY KEY,
    data_venda TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    valor_total DECIMAL(10, 2) NOT NULL CHECK (valor_total >= 0),
    forma_pagamento VARCHAR(50) CHECK (forma_pagamento IN ('PIX', 'CARTAO', 'DINHEIRO')),
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- TABELA COMPRA
-- ========================================
CREATE TABLE IF NOT EXISTS compra (
    id_compra SERIAL PRIMARY KEY,
    id_fornecedor INTEGER NOT NULL,
    data_compra TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    valor_total DECIMAL(10, 2) NOT NULL CHECK (valor_total >= 0),
    forma_pagamento VARCHAR(50) CHECK (forma_pagamento IN ('PIX', 'CARTAO', 'DINHEIRO')),
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_fornecedor) REFERENCES fornecedor(id_fornecedor) ON DELETE RESTRICT
);

-- ========================================
-- TABELA ITEM_COMPRA
-- ========================================
CREATE TABLE IF NOT EXISTS item_compra (
    id_item SERIAL PRIMARY KEY,
    id_compra INTEGER NOT NULL,
    id_produto INTEGER NOT NULL,
    quantidade INTEGER NOT NULL CHECK (quantidade > 0),
    preco_unitario DECIMAL(10, 2) NOT NULL CHECK (preco_unitario >= 0),
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_compra) REFERENCES compra(id_compra) ON DELETE CASCADE,
    FOREIGN KEY (id_produto) REFERENCES produto(id_produto) ON DELETE RESTRICT
);

-- ========================================
-- TABELA CLIENTES
-- ========================================
CREATE TABLE IF NOT EXISTS clientes (
    cnpj VARCHAR(14) NOT NULL PRIMARY KEY CHECK (LENGTH(cnpj) = 11), -- CPF: 11 dígitos
    razao_social VARCHAR(255) NOT NULL, -- Nome completo do cliente (pessoa física)
    nome_fantasia VARCHAR(255),
    inscricao_estadual VARCHAR(255),
    email_cliente VARCHAR(255) NOT NULL,
    telefone_cliente VARCHAR(20),
    id_endereco_cliente INTEGER REFERENCES enderecos(id_endereco),
    status_cliente VARCHAR(10) NOT NULL CHECK (status_cliente IN ('PAGO', 'NAO_PAGO'))
);

-- ========================================
-- TABELA LICENÇAS
-- ========================================
CREATE TABLE IF NOT EXISTS licencas(
    id_usuario integer GENERATED ALWAYS AS IDENTITY NOT NULL,
    name_usuario varchar(255),
    senha_usuario varchar(255),
    tipo_usuario varchar(255),
    PRIMARY KEY(id_usuario),
    CONSTRAINT licencas_tipo_usuario_check CHECK (((tipo_usuario)::text = ANY ((ARRAY['admin'::character varying, 'user'::character varying])::text[])))
);

-- Exemplo de inserção de dados
INSERT INTO licencas (name_usuario, senha_usuario, tipo_usuario) VALUES ('User 1','123456','admin') ON CONFLICT DO NOTHING;


-- ========================================
-- ÍNDICES PARA MELHORAR PERFORMANCE
-- ========================================
CREATE INDEX IF NOT EXISTS idx_fornecedor_cnpj ON fornecedor(cnpj);
CREATE INDEX IF NOT EXISTS idx_fornecedor_endereco ON fornecedor(id_endereco);
CREATE INDEX IF NOT EXISTS idx_produto_codigo_barras ON produto(codigo_barras);
CREATE INDEX IF NOT EXISTS idx_produto_fornecedor ON produto(id_fornecedor);
CREATE INDEX IF NOT EXISTS idx_movimentacao_produto ON movimentacao_estoque(id_produto);
CREATE INDEX IF NOT EXISTS idx_movimentacao_data ON movimentacao_estoque(data_movimento);
CREATE INDEX IF NOT EXISTS idx_item_venda_venda ON item_venda(id_venda);
CREATE INDEX IF NOT EXISTS idx_item_venda_produto ON item_venda(id_produto);
CREATE INDEX IF NOT EXISTS idx_venda_data ON venda(data_venda);
CREATE INDEX IF NOT EXISTS idx_compra_fornecedor ON compra(id_fornecedor);
CREATE INDEX IF NOT EXISTS idx_compra_data ON compra(data_compra);
CREATE INDEX IF NOT EXISTS idx_item_compra_compra ON item_compra(id_compra);
CREATE INDEX IF NOT EXISTS idx_item_compra_produto ON item_compra(id_produto);
CREATE INDEX IF NOT EXISTS idx_enderecos_cidade ON enderecos(cidade);
CREATE INDEX IF NOT EXISTS idx_enderecos_cep ON enderecos(cep);
CREATE INDEX IF NOT EXISTS idx_clientes_endereco ON clientes(id_endereco_cliente);
CREATE INDEX IF NOT EXISTS idx_clientes_email ON clientes(email_cliente);

-- ========================================
-- TRIGGERS PARA ATUALIZAR DATA_ATUALIZACAO
-- ========================================
CREATE OR REPLACE FUNCTION atualizar_data_modificacao()
RETURNS TRIGGER AS $$
BEGIN
    NEW.data_atualizacao = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_atualizar_fornecedor
    BEFORE UPDATE ON fornecedor
    FOR EACH ROW
    EXECUTE FUNCTION atualizar_data_modificacao();

CREATE TRIGGER trigger_atualizar_produto
    BEFORE UPDATE ON produto
    FOR EACH ROW
    EXECUTE FUNCTION atualizar_data_modificacao();

-- ========================================
-- COMENTÁRIOS NAS TABELAS
-- ========================================
COMMENT ON TABLE fornecedor IS 'Cadastro de fornecedores (pessoas jurídicas)';
COMMENT ON TABLE produto IS 'Cadastro de produtos';
COMMENT ON TABLE movimentacao_estoque IS 'Registro de movimentações de estoque';
COMMENT ON TABLE item_venda IS 'Itens de cada venda';
COMMENT ON TABLE venda IS 'Registro de vendas (clientes pessoa física)';
COMMENT ON TABLE compra IS 'Registro de compras';
COMMENT ON TABLE item_compra IS 'Itens de cada compra';
COMMENT ON TABLE enderecos IS 'Cadastro de endereços compartilhados';
COMMENT ON TABLE clientes IS 'Cadastro de clientes pessoa física (CPF)';
COMMENT ON TABLE licencas IS 'Usuários do sistema';

-- ========================================
-- MELHORIAS IMPLEMENTADAS:
-- - Removidos campos id_empresa de fornecedor e produto (remanescências)
-- - Adicionados CHECK constraints para valores positivos e formatos
-- - Índices adicionais para FKs de endereços
-- - Validação CEP brasileira
-- - Comentários aprimorados

