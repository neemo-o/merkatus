-- Active: 1764800155343@@127.0.0.1@5432@erp_licencas

#   RODE O SCRIPT DEPOIS DE CRIAR A DATABASE COM O ESTE COMANDO ISOLADO
#   CREATE DATABASE erp_licencas;


-- TABELA: clientes_licenciados

CREATE TABLE IF NOT EXISTS clientes_licenciados (
    id_cliente          SERIAL PRIMARY KEY,
    cnpj                VARCHAR(18) UNIQUE NOT NULL,
    razao_social        VARCHAR(255) NOT NULL,
    nome_fantasia       VARCHAR(255),
    inscricao_estadual  VARCHAR(50),
    telefone            VARCHAR(20),
    email               VARCHAR(255),
    responsavel         VARCHAR(100),
    logradouro          VARCHAR(255),
    numero              VARCHAR(10),
    complemento         VARCHAR(255),
    bairro              VARCHAR(100),
    cidade              VARCHAR(100),
    estado              VARCHAR(2),
    cep                 VARCHAR(10),
    observacoes         TEXT,
    ativo               BOOLEAN NOT NULL DEFAULT TRUE,
    data_cadastro       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 

-- TABELA: licencas

CREATE TABLE IF NOT EXISTS licencas (
    id_licenca                  SERIAL PRIMARY KEY,
    id_cliente                  INTEGER NOT NULL REFERENCES clientes_licenciados(id_cliente) ON DELETE RESTRICT,
    chave_ativacao              VARCHAR(100) UNIQUE NOT NULL,
    -- Capacidade
    qtd_pdv_incluso             SMALLINT NOT NULL DEFAULT 1,
    qtd_gerenciador_incluso     SMALLINT NOT NULL DEFAULT 1,
    qtd_pdv_adicional           SMALLINT NOT NULL DEFAULT 0,
    qtd_gerenciador_adicional   SMALLINT NOT NULL DEFAULT 0,
    qtd_pdv_total               SMALLINT NOT NULL DEFAULT 1,
    qtd_gerenciador_total       SMALLINT NOT NULL DEFAULT 1,
    -- Validade
    data_ativacao               DATE,
    data_validade               DATE NOT NULL,
    status                      VARCHAR(15) NOT NULL DEFAULT 'ATIVA'
                                    CHECK (status IN ('ATIVA', 'EXPIRADA', 'SUSPENSA', 'CANCELADA')),
    -- Segurança
    hash_validacao              VARCHAR(255),
    data_cadastro               TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 

-- TABELA: terminais_autorizados

CREATE TABLE IF NOT EXISTS terminais_autorizados (
    id_terminal             SERIAL PRIMARY KEY,
    id_licenca              INTEGER NOT NULL REFERENCES licencas(id_licenca) ON DELETE RESTRICT,
    tipo                    VARCHAR(15) NOT NULL CHECK (tipo IN ('PDV', 'GERENCIADOR')),
    nome                    VARCHAR(60) NOT NULL,
    identificador_maquina   VARCHAR(255) NOT NULL,
    status                  VARCHAR(15) NOT NULL DEFAULT 'ATIVO'
                                CHECK (status IN ('ATIVO', 'INATIVO', 'BLOQUEADO')),
    data_ativacao           TIMESTAMP,
    ultimo_heartbeat        TIMESTAMP,
    data_cadastro           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- Uma máquina só pode ser registrada uma vez por licença
    CONSTRAINT uq_licenca_maquina UNIQUE (id_licenca, identificador_maquina)
);
 

-- TABELA: validacoes_licenca

CREATE TABLE IF NOT EXISTS validacoes_licenca (
    id_validacao            BIGSERIAL PRIMARY KEY,
    id_licenca              INTEGER NOT NULL REFERENCES licencas(id_licenca) ON DELETE RESTRICT,
    id_terminal             INTEGER REFERENCES terminais_autorizados(id_terminal) ON DELETE SET NULL,
    tipo_validacao          VARCHAR(20) NOT NULL CHECK (tipo_validacao IN ('HEARTBEAT', 'ATIVACAO', 'VERIFICACAO')),
    identificador_maquina   VARCHAR(255),
    ip_origem               VARCHAR(45),
    resultado               VARCHAR(15) NOT NULL CHECK (resultado IN ('APROVADA', 'REJEITADA', 'EXPIRADA')),
    motivo_rejeicao         VARCHAR(200),
    dados_retornados        TEXT, -- JSON com pacote retornado ao local
    data_validacao          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- TABELA: usuarios_equipe

CREATE TABLE IF NOT EXISTS usuarios_equipe (
    id_usuario      SERIAL PRIMARY KEY,
    nome            VARCHAR(100) NOT NULL,
    email           VARCHAR(120) UNIQUE NOT NULL,
    senha_hash      VARCHAR(255) NOT NULL,
    perfil          VARCHAR(20) NOT NULL DEFAULT 'SUPORTE'
                        CHECK (perfil IN ('ADMIN', 'SUPORTE', 'COMERCIAL')),
    ativo           BOOLEAN NOT NULL DEFAULT TRUE,
    ultimo_login    TIMESTAMP,
    data_cadastro   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 

-- TABELA: log_acoes_equipe

CREATE TABLE IF NOT EXISTS log_acoes_equipe (
    id_log              BIGSERIAL PRIMARY KEY,
    id_usuario_equipe   INTEGER NOT NULL REFERENCES usuarios_equipe(id_usuario) ON DELETE RESTRICT,
    acao                VARCHAR(80) NOT NULL,
    tabela              VARCHAR(60),
    id_registro         INTEGER,
    dados_antes         TEXT, -- JSON
    dados_depois        TEXT, -- JSON
    ip_address          VARCHAR(45),
    data_acao           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 

-- ÍNDICES

CREATE INDEX IF NOT EXISTS idx_clientes_cnpj ON clientes_licenciados(cnpj);
CREATE INDEX IF NOT EXISTS idx_clientes_ativo ON clientes_licenciados(ativo);
CREATE INDEX IF NOT EXISTS idx_licencas_cliente ON licencas(id_cliente);
CREATE INDEX IF NOT EXISTS idx_licencas_chave ON licencas(chave_ativacao);
CREATE INDEX IF NOT EXISTS idx_licencas_status ON licencas(status);
CREATE INDEX IF NOT EXISTS idx_terminais_licenca ON terminais_autorizados(id_licenca, tipo, status);
CREATE INDEX IF NOT EXISTS idx_validacoes_licenca ON validacoes_licenca(id_licenca, data_validacao);
CREATE INDEX IF NOT EXISTS idx_validacoes_data ON validacoes_licenca(data_validacao);
CREATE INDEX IF NOT EXISTS idx_log_equipe_usuario ON log_acoes_equipe(id_usuario_equipe);
CREATE INDEX IF NOT EXISTS idx_log_equipe_data ON log_acoes_equipe(data_acao);
 

-- TRIGGERS

CREATE OR REPLACE FUNCTION fn_atualizar_data_modificacao()
RETURNS TRIGGER AS $$
BEGIN
    NEW.data_atualizacao = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
 
CREATE TRIGGER trg_clientes_licenciados_update
    BEFORE UPDATE ON clientes_licenciados
    FOR EACH ROW EXECUTE FUNCTION fn_atualizar_data_modificacao();
 
CREATE TRIGGER trg_licencas_update
    BEFORE UPDATE ON licencas
    FOR EACH ROW EXECUTE FUNCTION fn_atualizar_data_modificacao();
 
CREATE TRIGGER trg_terminais_update
    BEFORE UPDATE ON terminais_autorizados
    FOR EACH ROW EXECUTE FUNCTION fn_atualizar_data_modificacao();
 

-- DADOS INICIAIS

 
-- Usuário admin da equipe (senha: trocar após primeiro login)
INSERT INTO usuarios_equipe (nome, email, senha_hash, perfil)
VALUES ('Administrador', 'admin@erp.com', '123456', 'ADMIN')
ON CONFLICT DO NOTHING;
 
-- DADOS DE TESTE (3 mercados com licenças)
 
-- Mercado 1
INSERT INTO clientes_licenciados (cnpj, razao_social, nome_fantasia, inscricao_estadual, telefone, email, responsavel, logradouro, numero, bairro, cidade, estado, cep, ativo)
VALUES ('12.345.678/0001-90', 'Supermercado Bom Preço LTDA', 'Bom Preço', '123456789', '(71) 99876-5432', 'contato@bompreco.com.br', 'João Silva', 'Rua das Palmeiras', '150', 'Centro', 'Salvador', 'BA', '40020-000', TRUE)
ON CONFLICT (cnpj) DO NOTHING;
 
INSERT INTO licencas (id_cliente, chave_ativacao, qtd_pdv_incluso, qtd_gerenciador_incluso, qtd_pdv_adicional, qtd_gerenciador_adicional, qtd_pdv_total, qtd_gerenciador_total, data_ativacao, data_validade, status)
SELECT id_cliente, 'BOMPRECO-AAAA-1111-XXXX', 1, 1, 2, 0, 3, 1, CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 'ATIVA'
FROM clientes_licenciados WHERE cnpj = '12.345.678/0001-90'
ON CONFLICT (chave_ativacao) DO NOTHING;
 
-- Mercado 2
INSERT INTO clientes_licenciados (cnpj, razao_social, nome_fantasia, inscricao_estadual, telefone, email, responsavel, logradouro, numero, bairro, cidade, estado, cep, ativo)
VALUES ('98.765.432/0001-10', 'Mercadinho Economia EIRELI', 'Economia', '987654321', '(75) 98765-1234', 'adm@economia.com.br', 'Maria Souza', 'Av. Brasil', '500', 'Jardim Europa', 'Feira de Santana', 'BA', '44001-100', TRUE)
ON CONFLICT (cnpj) DO NOTHING;
 
INSERT INTO licencas (id_cliente, chave_ativacao, qtd_pdv_incluso, qtd_gerenciador_incluso, qtd_pdv_adicional, qtd_gerenciador_adicional, qtd_pdv_total, qtd_gerenciador_total, data_ativacao, data_validade, status)
SELECT id_cliente, 'ECONOMIA-BBBB-2222-YYYY', 1, 1, 0, 0, 1, 1, CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 'ATIVA'
FROM clientes_licenciados WHERE cnpj = '98.765.432/0001-10'
ON CONFLICT (chave_ativacao) DO NOTHING;
 
-- Mercado 3 (licença expirada — para testar bloqueio)
INSERT INTO clientes_licenciados (cnpj, razao_social, nome_fantasia, inscricao_estadual, telefone, email, responsavel, logradouro, numero, bairro, cidade, estado, cep, ativo)
VALUES ('11.222.333/0001-44', 'Atacadão Popular LTDA', 'Atacadão Popular', '112233445', '(71) 91234-5678', 'financeiro@atacadao.com.br', 'Carlos Oliveira', 'Rua do Comércio', '1200', 'Boca do Rio', 'Salvador', 'BA', '41710-000', TRUE)
ON CONFLICT (cnpj) DO NOTHING;
 
INSERT INTO licencas (id_cliente, chave_ativacao, qtd_pdv_incluso, qtd_gerenciador_incluso, qtd_pdv_adicional, qtd_gerenciador_adicional, qtd_pdv_total, qtd_gerenciador_total, data_ativacao, data_validade, status)
SELECT id_cliente, 'ATACADAO-CCCC-3333-ZZZZ', 1, 1, 1, 0, 2, 1, '2024-01-01', '2025-01-01', 'EXPIRADA'
FROM clientes_licenciados WHERE cnpj = '11.222.333/0001-44'
ON CONFLICT (chave_ativacao) DO NOTHING;
-- COMENTÁRIOS

COMMENT ON TABLE clientes_licenciados IS 'Mercados clientes que possuem o sistema instalado';
COMMENT ON TABLE licencas IS 'Licenças emitidas — uma por mercado, controla capacidade e validade';
COMMENT ON TABLE terminais_autorizados IS 'Máquinas aprovadas (PDV/Gerenciador) por licença';
COMMENT ON TABLE validacoes_licenca IS 'Log de heartbeats, ativações e verificações de licença';
COMMENT ON TABLE usuarios_equipe IS 'Usuários da equipe de gestão do sistema';
COMMENT ON TABLE log_acoes_equipe IS 'Auditoria de ações realizadas pela equipe';