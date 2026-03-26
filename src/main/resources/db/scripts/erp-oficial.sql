-- Active: 1762899015081@@127.0.0.1@5432@erp_oficial

#   RODE O SCRIPT DEPOIS DE CRIAR A DATABASE COM O ESTE COMANDO ISOLADO
#   CREATE DATABASE erp_oficial;

-- ========================================
-- 1. EMPRESA (singleton — sempre 1 registro)
-- ========================================
CREATE TABLE IF NOT EXISTS empresa (
    id_empresa          SERIAL PRIMARY KEY,
    razao_social        VARCHAR(255) NOT NULL,
    nome_fantasia       VARCHAR(255),
    cnpj                VARCHAR(18) UNIQUE NOT NULL,
    inscricao_estadual  VARCHAR(20),
    inscricao_municipal VARCHAR(20),
 
    -- Tributação
    regime_tributario    SMALLINT NOT NULL DEFAULT 1, -- 1=Simples, 2=Presumido, 3=Real
    crt                  SMALLINT NOT NULL DEFAULT 1, -- Código Regime Tributário
 
    -- Endereço
    logradouro          VARCHAR(200),
    numero              VARCHAR(10),
    complemento         VARCHAR(60),
    bairro              VARCHAR(80),
    cidade              VARCHAR(80),
    uf                  VARCHAR(2),
    cep                 VARCHAR(10),
    cod_municipio_ibge  VARCHAR(7),
 
    -- Contato
    telefone            VARCHAR(20),
    email               VARCHAR(120),
 
    -- Certificado digital (NF-e)
    logo                BYTEA,
    certificado_digital BYTEA,
    senha_certificado   VARCHAR(100), -- criptografada
 
    -- NF-e / NFC-e
    ambiente_nfe        SMALLINT NOT NULL DEFAULT 2, -- 1=Produção, 2=Homologação
    serie_nfce          SMALLINT NOT NULL DEFAULT 1,
    serie_nfe           SMALLINT NOT NULL DEFAULT 1,
    proximo_nfce        INTEGER NOT NULL DEFAULT 1,
    proximo_nfe         INTEGER NOT NULL DEFAULT 1,
    token_csc           VARCHAR(100),
    id_csc              VARCHAR(10),
 
    data_cadastro       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 
COMMENT ON TABLE empresa IS 'Singleton — dados da empresa dona deste banco local';
 
-- ========================================
-- 2. LICENÇA LOCAL (singleton — cache do banco principal)
-- ========================================
CREATE TABLE IF NOT EXISTS licenca_local (
    id_licenca              SERIAL PRIMARY KEY,
    chave_ativacao          VARCHAR(100) NOT NULL,
    cnpj                    VARCHAR(18) NOT NULL,
    qtd_pdv_total           SMALLINT NOT NULL DEFAULT 1,
    qtd_gerenciador_total   SMALLINT NOT NULL DEFAULT 1,
    data_validade           DATE NOT NULL,
    status                  VARCHAR(15) NOT NULL DEFAULT 'ATIVA'
                                CHECK (status IN ('ATIVA', 'EXPIRADA', 'SUSPENSA')),
    hash_validacao          VARCHAR(255),
    ultimo_heartbeat        TIMESTAMP,
    proximo_heartbeat       TIMESTAMP,
    grace_period_ate        TIMESTAMP, -- último heartbeat + N dias
    dados_licenca_json      TEXT,      -- JSON completo recebido do principal
    data_atualizacao        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 
COMMENT ON TABLE licenca_local IS 'Cache local da licença obtida do banco principal. Singleton.';
 
-- ========================================
-- 3. ENDEREÇOS (compartilhado por clientes e fornecedores)
-- ========================================
CREATE TABLE IF NOT EXISTS enderecos (
    id_endereco     SERIAL PRIMARY KEY,
    logradouro      VARCHAR(255) NOT NULL,
    numero          VARCHAR(10),
    complemento     VARCHAR(255),
    bairro          VARCHAR(255) NOT NULL,
    cidade          VARCHAR(255) NOT NULL,
    estado          VARCHAR(2) NOT NULL,
    cep             VARCHAR(9) NOT NULL,
    data_cadastro   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 
COMMENT ON TABLE enderecos IS 'Endereços compartilhados por clientes e fornecedores';
 
-- ========================================
-- 4. FORNECEDOR
-- ========================================
CREATE TABLE IF NOT EXISTS fornecedor (
    id_fornecedor       SERIAL PRIMARY KEY,
    cnpj                VARCHAR(18) UNIQUE NOT NULL,
    razao_social        VARCHAR(255) NOT NULL,
    nome_fantasia       VARCHAR(255),
    telefone            VARCHAR(20),
    e_mail              VARCHAR(255),
    id_endereco         INTEGER REFERENCES enderecos(id_endereco) ON DELETE SET NULL,
    ativo               BOOLEAN NOT NULL DEFAULT TRUE,
    data_cadastro       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 
COMMENT ON TABLE fornecedor IS 'Cadastro de fornecedores';
 
-- ========================================
-- 5. CATEGORIAS (hierárquica)
-- ========================================
CREATE TABLE IF NOT EXISTS categorias (
    id_categoria    SERIAL PRIMARY KEY,
    parent_id       INTEGER REFERENCES categorias(id_categoria) ON DELETE SET NULL,
    nome            VARCHAR(100) NOT NULL,
    ativo           BOOLEAN NOT NULL DEFAULT TRUE,
    data_cadastro   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 
COMMENT ON TABLE categorias IS 'Categorias de produtos (hierárquica via parent_id)';
 
-- ========================================
-- 6. UNIDADES DE MEDIDA
-- ========================================
CREATE TABLE IF NOT EXISTS unidades_medida (
    id_unidade  SERIAL PRIMARY KEY,
    sigla       VARCHAR(6) UNIQUE NOT NULL,
    descricao   VARCHAR(50) NOT NULL
);
 
COMMENT ON TABLE unidades_medida IS 'Unidades de medida (UN, KG, LT, CX, etc.)';
 
-- Dados iniciais
INSERT INTO unidades_medida (sigla, descricao) VALUES
    ('UN', 'Unidade'),
    ('KG', 'Quilograma'),
    ('G', 'Grama'),
    ('LT', 'Litro'),
    ('ML', 'Mililitro'),
    ('CX', 'Caixa'),
    ('PCT', 'Pacote'),
    ('FD', 'Fardo'),
    ('DZ', 'Dúzia'),
    ('MT', 'Metro')
ON CONFLICT DO NOTHING;
 
-- ========================================
-- 7. PRODUTO
-- Mantém compatibilidade com ProdutoDAO existente.
-- Campos fiscais adicionados para o futuro.
-- ========================================
CREATE TABLE IF NOT EXISTS produto (
    id_produto          SERIAL PRIMARY KEY,
    descricao           TEXT NOT NULL,
    codigo_barras       VARCHAR(50) UNIQUE,
    unidade_medida      VARCHAR(10),           -- campo legado (compatibilidade com DAO atual)
    id_unidade_medida   INTEGER REFERENCES unidades_medida(id_unidade) ON DELETE SET NULL,
    id_categoria        INTEGER REFERENCES categorias(id_categoria) ON DELETE SET NULL,
    preco_custo         DECIMAL(12, 4) CHECK (preco_custo >= 0),
    preco_venda         DECIMAL(12, 4) CHECK (preco_venda >= 0),
    margem_lucro        DECIMAL(5, 2),
    estoque_atual       INTEGER DEFAULT 0 CHECK (estoque_atual >= 0),
    estoque_minimo      DECIMAL(12, 3),
    estoque_maximo      DECIMAL(12, 3),
    id_fornecedor       INTEGER REFERENCES fornecedor(id_fornecedor) ON DELETE SET NULL,
 
    -- Campos fiscais (para uso futuro — nullable por enquanto)
    ncm                 VARCHAR(8),
    cest                VARCHAR(7),
    cfop_venda          VARCHAR(4),
    cst_icms            VARCHAR(3),
    csosn               VARCHAR(4),
    cst_pis             VARCHAR(2),
    cst_cofins          VARCHAR(2),
    cst_ipi             VARCHAR(2),
    aliq_icms           DECIMAL(5, 2),
    aliq_pis            DECIMAL(5, 4),
    aliq_cofins         DECIMAL(5, 4),
    aliq_ipi            DECIMAL(5, 2),
 
    -- Flags
    peso_liquido            DECIMAL(10, 3),
    peso_bruto              DECIMAL(10, 3),
    permite_fracionamento   BOOLEAN NOT NULL DEFAULT FALSE,
    controla_estoque        BOOLEAN NOT NULL DEFAULT TRUE,
    balanca                 BOOLEAN NOT NULL DEFAULT FALSE,
    ativo                   BOOLEAN NOT NULL DEFAULT TRUE,
 
    data_cadastro       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 
COMMENT ON TABLE produto IS 'Cadastro de produtos — campos fiscais preparados para NF-e futura';
 
-- ========================================
-- 8. TABELAS DE PREÇO
-- ========================================
CREATE TABLE IF NOT EXISTS tabelas_preco (
    id_tabela       SERIAL PRIMARY KEY,
    nome            VARCHAR(60) NOT NULL,
    data_inicio     DATE,
    data_fim        DATE,
    ativo           BOOLEAN NOT NULL DEFAULT TRUE,
    data_cadastro   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 
CREATE TABLE IF NOT EXISTS tabelas_preco_itens (
    id_item         SERIAL PRIMARY KEY,
    id_tabela       INTEGER NOT NULL REFERENCES tabelas_preco(id_tabela) ON DELETE CASCADE,
    id_produto      INTEGER NOT NULL REFERENCES produto(id_produto) ON DELETE RESTRICT,
    preco           DECIMAL(12, 4) NOT NULL CHECK (preco >= 0),
    CONSTRAINT uq_tabela_produto UNIQUE (id_tabela, id_produto)
);
 
-- ========================================
-- 9. CLIENTES
-- Mantém compatibilidade com ClienteDAO existente.
-- PK continua sendo cnpj (compatibilidade), mas id_cliente adicionado.
-- ========================================
CREATE TABLE IF NOT EXISTS clientes (
    id_cliente              SERIAL,
    cnpj                    VARCHAR(14) NOT NULL PRIMARY KEY, -- PK legada (CPF 11 dígitos ou CNPJ)
    razao_social            VARCHAR(255) NOT NULL,
    nome_fantasia           VARCHAR(255),
    inscricao_estadual      VARCHAR(255),
    email_cliente           VARCHAR(255),
    telefone_cliente        VARCHAR(20),
    id_endereco_cliente     INTEGER REFERENCES enderecos(id_endereco) ON DELETE SET NULL,
    status_cliente          VARCHAR(10) NOT NULL DEFAULT 'PAGO'
                                CHECK (status_cliente IN ('PAGO', 'NAO_PAGO')),
    data_nascimento         DATE,
    limite_credito          DECIMAL(12, 2),
    ativo                   BOOLEAN NOT NULL DEFAULT TRUE,
    data_cadastro           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 
COMMENT ON TABLE clientes IS 'Cadastro de clientes — PK é cnpj por compatibilidade com DAO existente';
 
-- ========================================
-- 10. FUNCIONÁRIOS
-- ========================================
CREATE TABLE IF NOT EXISTS funcionarios (
    id_funcionario  SERIAL PRIMARY KEY,
    nome            VARCHAR(200) NOT NULL,
    cpf             VARCHAR(14) UNIQUE NOT NULL,
    cargo           VARCHAR(60),
    data_admissao   DATE,
    data_demissao   DATE,
    telefone        VARCHAR(20),
    email           VARCHAR(120),
    ativo           BOOLEAN NOT NULL DEFAULT TRUE,
    data_cadastro   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 
-- ========================================
-- 11. ESTOQUE (saldo por produto)
-- ========================================
CREATE TABLE IF NOT EXISTS estoque (
    id_estoque      SERIAL PRIMARY KEY,
    id_produto      INTEGER UNIQUE NOT NULL REFERENCES produto(id_produto) ON DELETE RESTRICT,
    quantidade      DECIMAL(12, 3) NOT NULL DEFAULT 0,
    custo_medio     DECIMAL(12, 4),
    data_atualizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 
COMMENT ON TABLE estoque IS 'Saldo consolidado de estoque por produto';
 
-- ========================================
-- 12. MOVIMENTAÇÃO DE ESTOQUE
-- Mantém compatibilidade com tabela existente.
-- ========================================
CREATE TABLE IF NOT EXISTS movimentacao_estoque (
    id_movimento    SERIAL PRIMARY KEY,
    id_produto      INTEGER NOT NULL REFERENCES produto(id_produto) ON DELETE CASCADE,
    tipo            VARCHAR(20) NOT NULL CHECK (tipo IN ('ENTRADA', 'SAIDA', 'AJUSTE')),
    quantidade      INTEGER NOT NULL CHECK (quantidade > 0),
    custo_unitario  DECIMAL(12, 4),
    origem          TEXT,
    documento_id    INTEGER,
    observacao      TEXT,
    id_usuario      INTEGER, -- FK para usuarios (adicionada depois)
    data_movimento  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_cadastro   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 
-- ========================================
-- 13. CONFIGURAÇÃO FISCAL (singleton)
-- ========================================
CREATE TABLE IF NOT EXISTS config_fiscal (
    id_config               SERIAL PRIMARY KEY,
    aliq_icms_padrao        DECIMAL(5, 2),
    aliq_pis_padrao         DECIMAL(5, 4),
    aliq_cofins_padrao      DECIMAL(5, 4),
    cfop_venda_padrao       VARCHAR(4),
    cfop_devolucao_padrao   VARCHAR(4),
    cst_icms_padrao         VARCHAR(3),
    csosn_padrao            VARCHAR(4),
    cst_pis_padrao          VARCHAR(2),
    cst_cofins_padrao       VARCHAR(2),
    data_atualizacao        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 
COMMENT ON TABLE config_fiscal IS 'Configurações fiscais padrão da empresa — singleton';
 
-- ========================================
-- 14. CFOP (tabela de referência)
-- ========================================
CREATE TABLE IF NOT EXISTS cfop_cadastro (
    id_cfop         SERIAL PRIMARY KEY,
    codigo          VARCHAR(4) UNIQUE NOT NULL,
    descricao       VARCHAR(300) NOT NULL,
    tipo_operacao   CHAR(1) NOT NULL CHECK (tipo_operacao IN ('E', 'S')) -- E=Entrada, S=Saída
);
 
COMMENT ON TABLE cfop_cadastro IS 'Tabela de referência de CFOPs';
 
-- ========================================
-- 15. PERFIS DE ACESSO
-- ========================================
CREATE TABLE IF NOT EXISTS perfis_acesso (
    id_perfil       SERIAL PRIMARY KEY,
    nome            VARCHAR(60) NOT NULL,
    descricao       VARCHAR(200),
    nivel           SMALLINT NOT NULL DEFAULT 10, -- 1=mais alto
    ativo           BOOLEAN NOT NULL DEFAULT TRUE,
    data_cadastro   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 
-- Dados iniciais
INSERT INTO perfis_acesso (nome, descricao, nivel) VALUES
    ('Administrador', 'Acesso total ao sistema', 1),
    ('Gerente', 'Acesso gerencial (sem config sistema)', 2),
    ('Operador', 'Operador de caixa', 5),
    ('Fiscal', 'Acesso fiscal e tributário', 3)
ON CONFLICT DO NOTHING;
 
-- ========================================
-- 16. MÓDULOS DO SISTEMA
-- ========================================
CREATE TABLE IF NOT EXISTS modulos_sistema (
    id_modulo   SERIAL PRIMARY KEY,
    codigo      VARCHAR(30) UNIQUE NOT NULL,
    nome        VARCHAR(60) NOT NULL,
    descricao   VARCHAR(200)
);
 
INSERT INTO modulos_sistema (codigo, nome) VALUES
    ('PDV', 'Frente de Caixa'),
    ('CADASTROS', 'Cadastros Gerais'),
    ('ESTOQUE', 'Controle de Estoque'),
    ('FISCAL', 'Fiscal e Tributário'),
    ('FINANCEIRO', 'Financeiro'),
    ('RELATORIOS', 'Relatórios'),
    ('CONFIG', 'Configurações do Sistema'),
    ('USUARIOS', 'Gestão de Usuários')
ON CONFLICT DO NOTHING;
 
-- ========================================
-- 17. OPERAÇÕES DO SISTEMA
-- ========================================
CREATE TABLE IF NOT EXISTS operacoes_sistema (
    id_operacao SERIAL PRIMARY KEY,
    id_modulo   INTEGER NOT NULL REFERENCES modulos_sistema(id_modulo) ON DELETE CASCADE,
    codigo      VARCHAR(50) UNIQUE NOT NULL,
    nome        VARCHAR(80) NOT NULL,
    descricao   VARCHAR(200),
    critica     BOOLEAN NOT NULL DEFAULT FALSE
);
 
-- ========================================
-- 18. PERMISSÕES (perfil × operação)
-- ========================================
CREATE TABLE IF NOT EXISTS permissoes (
    id_permissao    SERIAL PRIMARY KEY,
    id_perfil       INTEGER NOT NULL REFERENCES perfis_acesso(id_perfil) ON DELETE CASCADE,
    id_operacao     INTEGER NOT NULL REFERENCES operacoes_sistema(id_operacao) ON DELETE CASCADE,
    permitido       BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_perfil_operacao UNIQUE (id_perfil, id_operacao)
);
 
-- ========================================
-- 19. USUÁRIOS DO SISTEMA LOCAL
-- Substitui a tabela "licencas" antiga que era usada como usuários.
-- ========================================
CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario          SERIAL PRIMARY KEY,
    id_funcionario      INTEGER REFERENCES funcionarios(id_funcionario) ON DELETE SET NULL,
    id_perfil           INTEGER NOT NULL REFERENCES perfis_acesso(id_perfil) ON DELETE RESTRICT,
    login               VARCHAR(50) UNIQUE NOT NULL,
    senha_hash          VARCHAR(255) NOT NULL,
    nome_exibicao       VARCHAR(100) NOT NULL,
    email               VARCHAR(120),
    tentativas_login    SMALLINT NOT NULL DEFAULT 0,
    bloqueado           BOOLEAN NOT NULL DEFAULT FALSE,
    ultimo_login        TIMESTAMP,
    ativo               BOOLEAN NOT NULL DEFAULT TRUE,
    data_cadastro       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 
COMMENT ON TABLE usuarios IS 'Usuários do sistema local — substitui a antiga tabela licencas (usuários)';
 
-- Usuário admin padrão (senha: trocar após primeiro login)
INSERT INTO usuarios (id_perfil, login, senha_hash, nome_exibicao)
VALUES (1, 'admin', '123456', 'Administrador')
ON CONFLICT DO NOTHING;
-- ATENÇÃO: senha em texto simples por compatibilidade temporária.
-- Quando implementar Hibernate + bcrypt, migrar para hash real.
 
-- ========================================
-- 20. TERMINAIS
-- ========================================
CREATE TABLE IF NOT EXISTS terminais (
    id_terminal             SERIAL PRIMARY KEY,
    tipo                    VARCHAR(15) NOT NULL CHECK (tipo IN ('PDV', 'GERENCIADOR')),
    nome                    VARCHAR(60) NOT NULL,
    identificador_maquina   VARCHAR(255) UNIQUE NOT NULL,
    ip_local                VARCHAR(45),
    status                  VARCHAR(15) NOT NULL DEFAULT 'ATIVO'
                                CHECK (status IN ('ATIVO', 'INATIVO', 'BLOQUEADO')),
    autorizado_principal    BOOLEAN NOT NULL DEFAULT FALSE,
    ultimo_acesso           TIMESTAMP,
    data_cadastro           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 
COMMENT ON TABLE terminais IS 'Terminais locais — validados contra licenca_local';
 
-- ========================================
-- 21. SESSÕES
-- ========================================
CREATE TABLE IF NOT EXISTS sessoes (
    id_sessao       SERIAL PRIMARY KEY,
    id_usuario      INTEGER NOT NULL REFERENCES usuarios(id_usuario) ON DELETE RESTRICT,
    id_terminal     INTEGER NOT NULL REFERENCES terminais(id_terminal) ON DELETE RESTRICT,
    token           VARCHAR(255) NOT NULL,
    ip_address      VARCHAR(45),
    data_inicio     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_fim        TIMESTAMP,
    ativa           BOOLEAN NOT NULL DEFAULT TRUE
);
 
-- ========================================
-- 22. FORMAS DE PAGAMENTO
-- Expande o antigo CHECK de forma_pagamento.
-- ========================================
CREATE TABLE IF NOT EXISTS formas_pagamento (
    id_forma_pagamento  SERIAL PRIMARY KEY,
    descricao           VARCHAR(60) NOT NULL,
    tipo                VARCHAR(20) NOT NULL
                            CHECK (tipo IN ('DINHEIRO', 'CREDITO', 'DEBITO', 'PIX', 'VOUCHER', 'CREDIARIO')),
    cod_pagamento_nfe   VARCHAR(2), -- Código meio pgto NF-e
    tef                 BOOLEAN NOT NULL DEFAULT FALSE,
    permite_troco       BOOLEAN NOT NULL DEFAULT FALSE,
    ativo               BOOLEAN NOT NULL DEFAULT TRUE,
    data_cadastro       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 
-- Dados iniciais (compatibilidade com valores antigos)
INSERT INTO formas_pagamento (descricao, tipo, cod_pagamento_nfe, permite_troco) VALUES
    ('Dinheiro', 'DINHEIRO', '01', TRUE),
    ('Cartão de Crédito', 'CREDITO', '03', FALSE),
    ('Cartão de Débito', 'DEBITO', '04', FALSE),
    ('PIX', 'PIX', '17', FALSE)
ON CONFLICT DO NOTHING;
 
-- ========================================
-- 23. CAIXAS (abertura/fechamento de turno)
-- ========================================
CREATE TABLE IF NOT EXISTS caixas (
    id_caixa                    SERIAL PRIMARY KEY,
    id_terminal                 INTEGER NOT NULL REFERENCES terminais(id_terminal) ON DELETE RESTRICT,
    id_operador                 INTEGER NOT NULL REFERENCES usuarios(id_usuario) ON DELETE RESTRICT,
    data_abertura               TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_fechamento             TIMESTAMP,
    valor_abertura              DECIMAL(12, 2) NOT NULL DEFAULT 0,
    valor_fechamento            DECIMAL(12, 2),
    valor_sistema               DECIMAL(12, 2),
    diferenca                   DECIMAL(12, 2),
    status                      VARCHAR(10) NOT NULL DEFAULT 'ABERTO'
                                    CHECK (status IN ('ABERTO', 'FECHADO')),
    observacao                  TEXT,
    id_supervisor_fechamento    INTEGER REFERENCES usuarios(id_usuario) ON DELETE SET NULL,
    data_cadastro               TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 
-- ========================================
-- 24. SANGRIAS E SUPRIMENTOS
-- ========================================
CREATE TABLE IF NOT EXISTS sangrias_suprimentos (
    id_sangria_suprimento   SERIAL PRIMARY KEY,
    id_caixa                INTEGER NOT NULL REFERENCES caixas(id_caixa) ON DELETE RESTRICT,
    tipo                    CHAR(1) NOT NULL CHECK (tipo IN ('S', 'U')), -- S=Sangria, U=Suprimento
    valor                   DECIMAL(12, 2) NOT NULL CHECK (valor > 0),
    motivo                  VARCHAR(200),
    id_operador             INTEGER NOT NULL REFERENCES usuarios(id_usuario) ON DELETE RESTRICT,
    id_supervisor           INTEGER REFERENCES usuarios(id_usuario) ON DELETE SET NULL,
    data_cadastro           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 
-- ========================================
-- 25. VENDA
-- Mantém compatibilidade com VendasDAO existente.
-- Campos expandidos para o modelo completo.
-- ========================================
CREATE TABLE IF NOT EXISTS venda (
    id_venda            SERIAL PRIMARY KEY,
    id_caixa            INTEGER REFERENCES caixas(id_caixa) ON DELETE RESTRICT,
    id_terminal         INTEGER REFERENCES terminais(id_terminal) ON DELETE RESTRICT,
    id_operador         INTEGER REFERENCES usuarios(id_usuario) ON DELETE RESTRICT,
    id_cliente          INTEGER, -- FK para clientes.id_cliente (não cnpj, para futuro)
    numero_venda        INTEGER,
    data_venda          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    subtotal            DECIMAL(12, 2),
    desconto            DECIMAL(12, 2) DEFAULT 0,
    acrescimo           DECIMAL(12, 2) DEFAULT 0,
    valor_total         DECIMAL(12, 2) NOT NULL CHECK (valor_total >= 0),
    troco               DECIMAL(12, 2) DEFAULT 0,
    cpf_nota            VARCHAR(14),
    forma_pagamento     VARCHAR(50), -- campo legado (compatibilidade com DAO atual)
    status              VARCHAR(15) NOT NULL DEFAULT 'FINALIZADA'
                            CHECK (status IN ('FINALIZADA', 'CANCELADA', 'PENDENTE')),
    observacao          TEXT,
    data_cadastro       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 
COMMENT ON TABLE venda IS 'Vendas — campo forma_pagamento mantido por compatibilidade, usar vendas_pagamentos para múltiplas formas';
 
-- ========================================
-- 26. ITEM_VENDA
-- Mantém compatibilidade com ItemVendaDAO existente.
-- Campos fiscais adicionados para NF-e futura.
-- ========================================
CREATE TABLE IF NOT EXISTS item_venda (
    id_item             SERIAL PRIMARY KEY,
    id_venda            INTEGER NOT NULL REFERENCES venda(id_venda) ON DELETE RESTRICT,
    id_produto          INTEGER NOT NULL REFERENCES produto(id_produto) ON DELETE RESTRICT,
    sequencia           SMALLINT,
    quantidade          INTEGER NOT NULL CHECK (quantidade > 0), -- legado (inteiro)
    preco_unitario      DECIMAL(12, 4) NOT NULL CHECK (preco_unitario >= 0),
    desconto            DECIMAL(12, 2) DEFAULT 0,
    acrescimo           DECIMAL(12, 2) DEFAULT 0,
    total_item          DECIMAL(12, 2),
 
    -- Campos fiscais (nullable — para uso futuro)
    cfop                VARCHAR(4),
    ncm                 VARCHAR(8),
    cest                VARCHAR(7),
    cst_icms            VARCHAR(3),
    csosn               VARCHAR(4),
    aliq_icms           DECIMAL(5, 2),
    valor_icms          DECIMAL(12, 2),
    base_icms           DECIMAL(12, 2),
    cst_pis             VARCHAR(2),
    aliq_pis            DECIMAL(5, 4),
    valor_pis           DECIMAL(12, 2),
    cst_cofins          VARCHAR(2),
    aliq_cofins         DECIMAL(5, 4),
    valor_cofins        DECIMAL(12, 2),
 
    cancelado           BOOLEAN NOT NULL DEFAULT FALSE,
    motivo_cancelamento VARCHAR(200),
    data_cadastro       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 
-- ========================================
-- 27. VENDAS_PAGAMENTOS (múltiplas formas por venda)
-- ========================================
CREATE TABLE IF NOT EXISTS vendas_pagamentos (
    id_pagamento        SERIAL PRIMARY KEY,
    id_venda            INTEGER NOT NULL REFERENCES venda(id_venda) ON DELETE RESTRICT,
    id_forma_pagamento  INTEGER NOT NULL REFERENCES formas_pagamento(id_forma_pagamento) ON DELETE RESTRICT,
    valor               DECIMAL(12, 2) NOT NULL CHECK (valor > 0),
    nsu                 VARCHAR(30),  -- NSU transação TEF
    autorizacao         VARCHAR(30),  -- Cód. autorização TEF
    bandeira            VARCHAR(30),  -- Bandeira do cartão
    data_cadastro       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 
-- ========================================
-- 28. CANCELAMENTOS
-- ========================================
CREATE TABLE IF NOT EXISTS cancelamentos (
    id_cancelamento     SERIAL PRIMARY KEY,
    id_venda            INTEGER NOT NULL REFERENCES venda(id_venda) ON DELETE RESTRICT,
    id_operador         INTEGER NOT NULL REFERENCES usuarios(id_usuario) ON DELETE RESTRICT,
    id_supervisor       INTEGER REFERENCES usuarios(id_usuario) ON DELETE SET NULL,
    motivo              VARCHAR(300) NOT NULL,
    tipo                VARCHAR(10) NOT NULL CHECK (tipo IN ('TOTAL', 'PARCIAL')),
    data_cancelamento   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_cadastro       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 
-- ========================================
-- 29. COMPRA (mantém compatibilidade)
-- ========================================
CREATE TABLE IF NOT EXISTS compra (
    id_compra           SERIAL PRIMARY KEY,
    id_fornecedor       INTEGER NOT NULL REFERENCES fornecedor(id_fornecedor) ON DELETE RESTRICT,
    data_compra         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valor_total         DECIMAL(12, 2) NOT NULL CHECK (valor_total >= 0),
    forma_pagamento     VARCHAR(50) CHECK (forma_pagamento IN ('PIX', 'CARTAO', 'DINHEIRO')),
    data_cadastro       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 
CREATE TABLE IF NOT EXISTS item_compra (
    id_item         SERIAL PRIMARY KEY,
    id_compra       INTEGER NOT NULL REFERENCES compra(id_compra) ON DELETE CASCADE,
    id_produto      INTEGER NOT NULL REFERENCES produto(id_produto) ON DELETE RESTRICT,
    quantidade      INTEGER NOT NULL CHECK (quantidade > 0),
    preco_unitario  DECIMAL(12, 4) NOT NULL CHECK (preco_unitario >= 0),
    data_cadastro   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 
-- ========================================
-- 30. NOTAS FISCAIS
-- ========================================
CREATE TABLE IF NOT EXISTS notas_fiscais (
    id_nota                 SERIAL PRIMARY KEY,
    id_venda                INTEGER REFERENCES venda(id_venda) ON DELETE SET NULL,
    tipo                    VARCHAR(5) NOT NULL CHECK (tipo IN ('NFCE', 'NFE')),
    numero                  INTEGER NOT NULL,
    serie                   SMALLINT NOT NULL,
    chave_acesso            VARCHAR(44) UNIQUE,
    protocolo               VARCHAR(20),
    data_emissao            TIMESTAMP NOT NULL,
    data_autorizacao        TIMESTAMP,
    valor_total             DECIMAL(12, 2) NOT NULL,
    status                  VARCHAR(15) NOT NULL
                                CHECK (status IN ('AUTORIZADA', 'CANCELADA', 'DENEGADA', 'REJEITADA', 'CONTINGENCIA')),
    xml_envio               TEXT,
    xml_retorno             TEXT,
    xml_cancelamento        TEXT,
    motivo_cancelamento     VARCHAR(300),
    data_cancelamento       TIMESTAMP,
    contingencia            BOOLEAN NOT NULL DEFAULT FALSE,
    data_cadastro           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 
-- ========================================
-- 31. INUTILIZAÇÕES NF-e
-- ========================================
CREATE TABLE IF NOT EXISTS inutilizacoes_nfe (
    id_inutilizacao SERIAL PRIMARY KEY,
    tipo            VARCHAR(5) NOT NULL CHECK (tipo IN ('NFCE', 'NFE')),
    serie           SMALLINT NOT NULL,
    numero_inicio   INTEGER NOT NULL,
    numero_fim      INTEGER NOT NULL,
    justificativa   VARCHAR(300) NOT NULL,
    protocolo       VARCHAR(20),
    xml_retorno     TEXT,
    data_inutilizacao TIMESTAMP NOT NULL,
    id_usuario      INTEGER REFERENCES usuarios(id_usuario) ON DELETE RESTRICT,
    data_cadastro   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 
-- ========================================
-- 32. CONFIGURAÇÕES (chave-valor)
-- ========================================
CREATE TABLE IF NOT EXISTS configuracoes (
    id_config       SERIAL PRIMARY KEY,
    modulo          VARCHAR(30) NOT NULL,
    chave           VARCHAR(80) NOT NULL,
    valor           TEXT,
    tipo_dado       VARCHAR(20) NOT NULL DEFAULT 'STRING'
                        CHECK (tipo_dado IN ('STRING', 'INTEGER', 'BOOLEAN', 'JSON', 'DECIMAL')),
    descricao       VARCHAR(200),
    data_atualizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_modulo_chave UNIQUE (modulo, chave)
);
 
COMMENT ON TABLE configuracoes IS 'Parâmetros do sistema em chave-valor por módulo';
 
-- Configurações iniciais
INSERT INTO configuracoes (modulo, chave, valor, tipo_dado, descricao) VALUES
    ('LICENCA', 'GRACE_PERIOD_DIAS', '7', 'INTEGER', 'Dias de operação offline após falha no heartbeat'),
    ('LICENCA', 'HEARTBEAT_INTERVALO_HORAS', '24', 'INTEGER', 'Intervalo entre heartbeats em horas'),
    ('PDV', 'PERMITIR_VENDA_SEM_ESTOQUE', 'false', 'BOOLEAN', 'Permite vender produto sem estoque'),
    ('PDV', 'DESCONTO_MAXIMO_PERCENTUAL', '10', 'DECIMAL', 'Desconto máximo permitido sem supervisor'),
    ('IMPRESSAO', 'IMPRESSORA_PADRAO', '', 'STRING', 'Nome da impressora térmica padrão'),
    ('GERAL', 'NOME_SISTEMA', 'ERP Mercado', 'STRING', 'Nome exibido no sistema')
ON CONFLICT DO NOTHING;
 
-- ========================================
-- 33. LOG DE LOGIN
-- ========================================
CREATE TABLE IF NOT EXISTS log_login (
    id_log          BIGSERIAL PRIMARY KEY,
    id_usuario      INTEGER REFERENCES usuarios(id_usuario) ON DELETE SET NULL,
    id_terminal     INTEGER REFERENCES terminais(id_terminal) ON DELETE SET NULL,
    login_tentado   VARCHAR(50) NOT NULL,
    sucesso         BOOLEAN NOT NULL,
    ip_address      VARCHAR(45),
    motivo_falha    VARCHAR(100),
    data_log        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 
-- ========================================
-- 34. LOG DE ALTERAÇÕES
-- ========================================
CREATE TABLE IF NOT EXISTS log_alteracoes (
    id_log          BIGSERIAL PRIMARY KEY,
    id_usuario      INTEGER NOT NULL REFERENCES usuarios(id_usuario) ON DELETE RESTRICT,
    id_terminal     INTEGER REFERENCES terminais(id_terminal) ON DELETE SET NULL,
    tabela          VARCHAR(60) NOT NULL,
    id_registro     INTEGER NOT NULL,
    campo           VARCHAR(60) NOT NULL,
    valor_anterior  TEXT,
    valor_novo      TEXT,
    data_log        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 
-- ========================================
-- 35. LOG DE EXCLUSÕES
-- ========================================
CREATE TABLE IF NOT EXISTS log_exclusoes (
    id_log          BIGSERIAL PRIMARY KEY,
    id_usuario      INTEGER NOT NULL REFERENCES usuarios(id_usuario) ON DELETE RESTRICT,
    id_terminal     INTEGER REFERENCES terminais(id_terminal) ON DELETE SET NULL,
    tabela          VARCHAR(60) NOT NULL,
    id_registro     INTEGER NOT NULL,
    dados_registro  TEXT NOT NULL, -- JSON snapshot
    motivo          VARCHAR(300),
    data_log        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 
-- ========================================
-- 36. LOG DE OPERAÇÕES CRÍTICAS
-- ========================================
CREATE TABLE IF NOT EXISTS log_operacoes_criticas (
    id_log              BIGSERIAL PRIMARY KEY,
    id_usuario          INTEGER NOT NULL REFERENCES usuarios(id_usuario) ON DELETE RESTRICT,
    id_terminal         INTEGER REFERENCES terminais(id_terminal) ON DELETE SET NULL,
    operacao            VARCHAR(80) NOT NULL,
    descricao           TEXT,
    tabela_referencia   VARCHAR(60),
    id_registro         INTEGER,
    dados_adicionais    TEXT, -- JSON
    data_log            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 
-- ========================================
-- ÍNDICES
-- ========================================
 
-- Produto
CREATE INDEX IF NOT EXISTS idx_produto_codigo_barras ON produto(codigo_barras);
CREATE INDEX IF NOT EXISTS idx_produto_fornecedor ON produto(id_fornecedor);
CREATE INDEX IF NOT EXISTS idx_produto_categoria ON produto(id_categoria);
CREATE INDEX IF NOT EXISTS idx_produto_ativo ON produto(ativo);
 
-- Fornecedor
CREATE INDEX IF NOT EXISTS idx_fornecedor_cnpj ON fornecedor(cnpj);
CREATE INDEX IF NOT EXISTS idx_fornecedor_endereco ON fornecedor(id_endereco);
 
-- Clientes
CREATE INDEX IF NOT EXISTS idx_clientes_endereco ON clientes(id_endereco_cliente);
CREATE INDEX IF NOT EXISTS idx_clientes_email ON clientes(email_cliente);
 
-- Estoque
CREATE INDEX IF NOT EXISTS idx_estoque_produto ON estoque(id_produto);
 
-- Movimentação
CREATE INDEX IF NOT EXISTS idx_movimentacao_produto ON movimentacao_estoque(id_produto);
CREATE INDEX IF NOT EXISTS idx_movimentacao_data ON movimentacao_estoque(data_movimento);
 
-- Venda
CREATE INDEX IF NOT EXISTS idx_venda_data ON venda(data_venda);
CREATE INDEX IF NOT EXISTS idx_venda_caixa ON venda(id_caixa);
CREATE INDEX IF NOT EXISTS idx_venda_status ON venda(status);
 
-- Item venda
CREATE INDEX IF NOT EXISTS idx_item_venda_venda ON item_venda(id_venda);
CREATE INDEX IF NOT EXISTS idx_item_venda_produto ON item_venda(id_produto);
 
-- Compra
CREATE INDEX IF NOT EXISTS idx_compra_fornecedor ON compra(id_fornecedor);
CREATE INDEX IF NOT EXISTS idx_compra_data ON compra(data_compra);
CREATE INDEX IF NOT EXISTS idx_item_compra_compra ON item_compra(id_compra);
 
-- Notas fiscais
CREATE INDEX IF NOT EXISTS idx_notas_chave ON notas_fiscais(chave_acesso);
CREATE INDEX IF NOT EXISTS idx_notas_tipo_numero ON notas_fiscais(tipo, numero, serie);
CREATE INDEX IF NOT EXISTS idx_notas_status ON notas_fiscais(status);
 
-- Usuarios
CREATE INDEX IF NOT EXISTS idx_usuarios_login ON usuarios(login);
CREATE INDEX IF NOT EXISTS idx_usuarios_perfil ON usuarios(id_perfil);
 
-- Terminais
CREATE INDEX IF NOT EXISTS idx_terminais_maquina ON terminais(identificador_maquina);
 
-- Sessões
CREATE INDEX IF NOT EXISTS idx_sessoes_usuario ON sessoes(id_usuario, ativa);
 
-- Logs
CREATE INDEX IF NOT EXISTS idx_log_login_data ON log_login(data_log);
CREATE INDEX IF NOT EXISTS idx_log_login_usuario ON log_login(id_usuario);
CREATE INDEX IF NOT EXISTS idx_log_alteracoes_data ON log_alteracoes(data_log);
CREATE INDEX IF NOT EXISTS idx_log_exclusoes_data ON log_exclusoes(data_log);
CREATE INDEX IF NOT EXISTS idx_log_criticas_data ON log_operacoes_criticas(data_log);
 
-- Endereços
CREATE INDEX IF NOT EXISTS idx_enderecos_cidade ON enderecos(cidade);
CREATE INDEX IF NOT EXISTS idx_enderecos_cep ON enderecos(cep);
 
-- ========================================
-- TRIGGERS
-- ========================================
CREATE OR REPLACE FUNCTION fn_atualizar_data_modificacao()
RETURNS TRIGGER AS $$
BEGIN
    NEW.data_atualizacao = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
 
CREATE TRIGGER trg_fornecedor_update
    BEFORE UPDATE ON fornecedor
    FOR EACH ROW EXECUTE FUNCTION fn_atualizar_data_modificacao();
 
CREATE TRIGGER trg_produto_update
    BEFORE UPDATE ON produto
    FOR EACH ROW EXECUTE FUNCTION fn_atualizar_data_modificacao();
 
CREATE TRIGGER trg_clientes_update
    BEFORE UPDATE ON clientes
    FOR EACH ROW EXECUTE FUNCTION fn_atualizar_data_modificacao();
 
CREATE TRIGGER trg_funcionarios_update
    BEFORE UPDATE ON funcionarios
    FOR EACH ROW EXECUTE FUNCTION fn_atualizar_data_modificacao();
 
CREATE TRIGGER trg_usuarios_update
    BEFORE UPDATE ON usuarios
    FOR EACH ROW EXECUTE FUNCTION fn_atualizar_data_modificacao();
 
CREATE TRIGGER trg_terminais_update
    BEFORE UPDATE ON terminais
    FOR EACH ROW EXECUTE FUNCTION fn_atualizar_data_modificacao();
 
CREATE TRIGGER trg_empresa_update
    BEFORE UPDATE ON empresa
    FOR EACH ROW EXECUTE FUNCTION fn_atualizar_data_modificacao();
 
-- ========================================
-- COMENTÁRIOS
-- ========================================
COMMENT ON TABLE fornecedor IS 'Cadastro de fornecedores';
COMMENT ON TABLE produto IS 'Cadastro de produtos com campos fiscais preparados';
COMMENT ON TABLE movimentacao_estoque IS 'Registro de movimentações de estoque';
COMMENT ON TABLE item_venda IS 'Itens de cada venda com espelhamento fiscal';
COMMENT ON TABLE venda IS 'Vendas realizadas no PDV';
COMMENT ON TABLE compra IS 'Registro de compras';
COMMENT ON TABLE item_compra IS 'Itens de cada compra';
COMMENT ON TABLE enderecos IS 'Endereços compartilhados';
COMMENT ON TABLE clientes IS 'Cadastro de clientes';
COMMENT ON TABLE usuarios IS 'Usuários do sistema local';
COMMENT ON TABLE terminais IS 'Terminais PDV/Gerenciador validados contra licença';
COMMENT ON TABLE notas_fiscais IS 'NFC-e e NF-e emitidas — XMLs armazenados por 5 anos';
