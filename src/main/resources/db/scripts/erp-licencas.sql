-- Active: 1762899015081@@127.0.0.1@5432@erp_licencas

#   RODE O SCRIPT DEPOIS DE CRIAR A DATABASE COM O ESTE COMANDO ISOLADO
#   CREATE DATABASE erp_licencas;

CREATE TABLE IF NOT EXISTS licencas (
    cnpj VARCHAR(18) PRIMARY KEY,
    razao_social VARCHAR(255) NOT NULL,
    nome_fantasia VARCHAR(255),
    inscricao_estadual VARCHAR(50),
    telefone VARCHAR(20),
    e_mail VARCHAR(255),
    status VARCHAR(20) CHECK (status IN ('PAGO', 'NAO_PAGO')),
    rua VARCHAR(255),
    numero VARCHAR(10),
    bairro VARCHAR(100),
    cidade VARCHAR(100),
    estado VARCHAR(2),
    cep VARCHAR(10),
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para melhorar performance
CREATE INDEX idx_razao_social ON licencas(razao_social);
CREATE INDEX idx_nome_fantasia ON licencas(nome_fantasia);
CREATE INDEX idx_status ON licencas(status);
CREATE INDEX idx_cidade ON licencas(cidade);
CREATE INDEX idx_estado ON licencas(estado);

-- Trigger para atualizar automaticamente data_atualizacao
CREATE OR REPLACE FUNCTION atualizar_data_modificacao()
RETURNS TRIGGER AS $$
BEGIN
    NEW.data_atualizacao = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_atualizar_data
    BEFORE UPDATE ON licencas
    FOR EACH ROW
    EXECUTE FUNCTION atualizar_data_modificacao();

-- Exemplo de inserção de dados
INSERT INTO licencas (
    cnpj, razao_social, nome_fantasia, inscricao_estadual,
    telefone, e_mail, status, rua, numero, bairro,
    cidade, estado, cep
) VALUES (
    '12.345.678/0001-90',
    'Empresa Exemplo LTDA',
    'Exemplo Corp',
    '123456789',
    '(11) 98765-4321',
    'contato@exemplo.com.br',
    'PAGO',
    'Rua das Flores',
    '123',
    'Centro',
    'São Paulo',
    'SP',
    '01234-567'
);
