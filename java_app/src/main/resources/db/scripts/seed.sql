-- Active: 1764800155343@@127.0.0.1@5432@erp_oficial

-- SEED.SQL — ERP MERCADO
-- Dados iniciais necessários para o sistema funcionar do zero.
-- Rode DEPOIS de erp-oficial.sql (schema já deve existir).
-- Todos os IDs são resolvidos por subquery.



-- 1. UNIDADES DE MEDIDA
INSERT INTO unidades_medida (sigla, descricao) VALUES
    ('UN',  'Unidade'),
    ('KG',  'Quilograma'),
    ('G',   'Grama'),
    ('LT',  'Litro'),
    ('ML',  'Mililitro'),
    ('CX',  'Caixa'),
    ('PCT', 'Pacote'),
    ('FD',  'Fardo'),
    ('DZ',  'Dúzia'),
    ('MT',  'Metro')
ON CONFLICT (sigla) DO NOTHING;


-- 2. PERFIS DE ACESSO
INSERT INTO perfis_acesso (nome, descricao, nivel) VALUES
    ('Administrador', 'Acesso total ao sistema',               1),
    ('Gerente',       'Acesso gerencial (sem config sistema)', 2),
    ('Fiscal',        'Acesso fiscal e tributário',            3),
    ('Operador',      'Operador de caixa',                     5)
ON CONFLICT DO NOTHING;

-- 3. MÓDULOS DO SISTEMA

INSERT INTO modulos_sistema (codigo, nome) VALUES
    ('PDV',        'Frente de Caixa'),
    ('CADASTROS',  'Cadastros Gerais'),
    ('ESTOQUE',    'Controle de Estoque'),
    ('FISCAL',     'Fiscal e Tributário'),
    ('FINANCEIRO', 'Financeiro'),
    ('RELATORIOS', 'Relatórios'),
    ('CONFIG',     'Configurações do Sistema'),
    ('USUARIOS',   'Gestão de Usuários')
ON CONFLICT (codigo) DO NOTHING;

-- 4. FORMAS DE PAGAMENTO

INSERT INTO formas_pagamento (descricao, tipo, cod_pagamento_nfe, permite_troco) VALUES
    ('Dinheiro',          'DINHEIRO', '01', TRUE),
    ('Cartão de Crédito', 'CREDITO',  '03', FALSE),
    ('Cartão de Débito',  'DEBITO',   '04', FALSE),
    ('PIX',               'PIX',      '17', FALSE)
ON CONFLICT DO NOTHING;


-- 5. CONFIGURAÇÕES DO SISTEMA

INSERT INTO configuracoes (modulo, chave, valor, tipo_dado, descricao) VALUES
    ('LICENCA',    'GRACE_PERIOD_DIAS',            '7',          'INTEGER', 'Dias de operação offline após falha no heartbeat'),
    ('LICENCA',    'HEARTBEAT_INTERVALO_HORAS',    '24',         'INTEGER', 'Intervalo entre heartbeats em horas'),
    ('PDV',        'PERMITIR_VENDA_SEM_ESTOQUE',   'false',      'BOOLEAN', 'Permite vender produto sem estoque'),
    ('PDV',        'DESCONTO_MAXIMO_PERCENTUAL',   '10',         'DECIMAL', 'Desconto máximo permitido sem supervisor'),
    ('IMPRESSAO',  'IMPRESSORA_PADRAO',            '',           'STRING',  'Nome da impressora térmica padrão'),
    ('GERAL',      'NOME_SISTEMA',                 'ERP Mercado','STRING',  'Nome exibido no sistema')
ON CONFLICT (modulo, chave) DO NOTHING;


-- 6. USUÁRIO ADMIN PADRÃO
-- ATENÇÃO: senha em texto simples, temporário até implementar bcrypt.

INSERT INTO usuarios (id_perfil, login, senha_hash, nome_exibicao)
SELECT
    (SELECT id_perfil FROM perfis_acesso WHERE nome = 'Administrador' LIMIT 1),
    'admin',
    '123456',
    'Administrador'
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios WHERE login = 'admin'
);


-- 7. TRIBUTAÇÃO — PERFIS FISCAIS
INSERT INTO tributacao_perfil (
    nome, descricao,
    ncm, cest,
    cst_icms, csosn,
    aliq_icms, aliq_icms_st, mva_st,
    cfop_venda, cfop_venda_interestadual,
    cst_pis, cst_cofins, aliq_pis, aliq_cofins,
    cst_ipi, aliq_ipi,
    ativo
) VALUES
(
    'Alimento Basico Isento',
    'Arroz, feijao, farinha, acucar - ICMS e PIS/COFINS zerados',
    null, null, null, '500',
    0.00, 0.00, null,
    '5102', '6102',
    '07', '07', 0.0000, 0.0000,
    '99', 0.00, true
),
(
    'Bebida Nao Alcoolica ST',
    'Refrigerantes, aguas, sucos - ST ja recolhida (LC 87/96)',
    null, '0300100', null, '500',
    18.00, 0.00, 78.55,
    '5405', '6404',
    '04', '04', 0.0000, 0.0000,
    '99', 0.00, true
),
(
    'Carnes Monofasico',
    'Bovinos, frango, suino - PIS/COFINS aliq zero no varejo (Lei 10.865/2004)',
    null, null, null, '500',
    7.00, 0.00, null,
    '5102', '6102',
    '04', '04', 0.0000, 0.0000,
    '99', 0.00, true
),
(
    'Higiene Limpeza Pleno',
    'Sabao, shampoo, detergente - tributacao normal',
    null, null, null, '500',
    18.00, 0.00, null,
    '5102', '6102',
    '01', '01', 0.0065, 0.0300,
    '99', 0.00, true
),
(
    'Padaria Confeitaria',
    'Paes, bolos, doces - ICMS isento, PIS/COFINS zerados',
    null, null, null, '500',
    0.00, 0.00, null,
    '5102', '6102',
    '07', '07', 0.0000, 0.0000,
    '99', 0.00, true
),
(
    'Cigarro Tabaco ST',
    'Cigarros e cigarrilhas - ST recolhida, PIS/COFINS monofasico',
    null, '3400100', null, '500',
    18.00, 0.00, 61.35,
    '5405', '6404',
    '04', '04', 0.0000, 0.0000,
    '99', 0.00, true
)
ON CONFLICT DO NOTHING;


-- 8. NCM → TRIBUTAÇÃO (fallback fiscal por NCM)

INSERT INTO ncm_tributacao (ncm, descricao_ncm, id_tributacao)
SELECT ncm, descricao_ncm, id_tributacao FROM (
    -- Alimentos básicos
    SELECT '10063021' AS ncm, 'Arroz branco polido, graos longos finos' AS descricao_ncm,
        (SELECT id_tributacao FROM tributacao_perfil WHERE nome = 'Alimento Basico Isento' LIMIT 1) AS id_tributacao
    UNION ALL SELECT '07133310', 'Feijao carioca e preto, sementes',
        (SELECT id_tributacao FROM tributacao_perfil WHERE nome = 'Alimento Basico Isento' LIMIT 1)
    UNION ALL SELECT '11010010', 'Farinha de trigo',
        (SELECT id_tributacao FROM tributacao_perfil WHERE nome = 'Alimento Basico Isento' LIMIT 1)
    UNION ALL SELECT '17019990', 'Acucar de cana cristal e refinado',
        (SELECT id_tributacao FROM tributacao_perfil WHERE nome = 'Alimento Basico Isento' LIMIT 1)
    -- Bebidas
    UNION ALL SELECT '22021000', 'Aguas minerais e bebidas gaseificadas com adicao de acucar',
        (SELECT id_tributacao FROM tributacao_perfil WHERE nome = 'Bebida Nao Alcoolica ST' LIMIT 1)
    UNION ALL SELECT '20091200', 'Suco de laranja congelado',
        (SELECT id_tributacao FROM tributacao_perfil WHERE nome = 'Bebida Nao Alcoolica ST' LIMIT 1)
    UNION ALL SELECT '22029000', 'Outras bebidas nao alcoolicas (nectares e refrescos)',
        (SELECT id_tributacao FROM tributacao_perfil WHERE nome = 'Bebida Nao Alcoolica ST' LIMIT 1)
    -- Carnes
    UNION ALL SELECT '02013000', 'Carne bovina desossada, fresca ou refrigerada',
        (SELECT id_tributacao FROM tributacao_perfil WHERE nome = 'Carnes Monofasico' LIMIT 1)
    UNION ALL SELECT '02071200', 'Frango nao cortado, congelado',
        (SELECT id_tributacao FROM tributacao_perfil WHERE nome = 'Carnes Monofasico' LIMIT 1)
    UNION ALL SELECT '02031900', 'Carne suina congelada',
        (SELECT id_tributacao FROM tributacao_perfil WHERE nome = 'Carnes Monofasico' LIMIT 1)
    -- Higiene
    UNION ALL SELECT '34011900', 'Sabonetes de uso ordinario',
        (SELECT id_tributacao FROM tributacao_perfil WHERE nome = 'Higiene Limpeza Pleno' LIMIT 1)
    UNION ALL SELECT '33051000', 'Xampus para cabelos',
        (SELECT id_tributacao FROM tributacao_perfil WHERE nome = 'Higiene Limpeza Pleno' LIMIT 1)
    UNION ALL SELECT '34022000', 'Preparacoes para lavagem e limpeza em embalagens de varejo',
        (SELECT id_tributacao FROM tributacao_perfil WHERE nome = 'Higiene Limpeza Pleno' LIMIT 1)
    -- Padaria
    UNION ALL SELECT '19059090', 'Outros paes, bolos e produtos de panificacao',
        (SELECT id_tributacao FROM tributacao_perfil WHERE nome = 'Padaria Confeitaria' LIMIT 1)
    UNION ALL SELECT '18063200', 'Chocolates e preparacoes com cacau, recheados',
        (SELECT id_tributacao FROM tributacao_perfil WHERE nome = 'Padaria Confeitaria' LIMIT 1)
    -- Tabaco
    UNION ALL SELECT '24022000', 'Cigarros contendo tabaco',
        (SELECT id_tributacao FROM tributacao_perfil WHERE nome = 'Cigarro Tabaco ST' LIMIT 1)
    UNION ALL SELECT '24021000', 'Charutos e cigarrilhas contendo tabaco',
        (SELECT id_tributacao FROM tributacao_perfil WHERE nome = 'Cigarro Tabaco ST' LIMIT 1)
) AS dados
ON CONFLICT (ncm) DO NOTHING;

-- 9. CATEGORIAS — raízes

INSERT INTO categorias (parent_id, nome, id_tributacao_padrao, ativo)
SELECT null, v.nome,
    (SELECT id_tributacao FROM tributacao_perfil WHERE nome = v.perfil LIMIT 1),
    true
FROM (VALUES
    ('Alimentos',             'Alimento Basico Isento'),
    ('Bebidas',               'Bebida Nao Alcoolica ST'),
    ('Carnes',                'Carnes Monofasico'),
    ('Higiene e Limpeza',     'Higiene Limpeza Pleno'),
    ('Padaria e Confeitaria', 'Padaria Confeitaria'),
    ('Tabacaria',             'Cigarro Tabaco ST')
) AS v(nome, perfil)
ON CONFLICT DO NOTHING;


-- 10. CATEGORIAS — subcategorias

INSERT INTO categorias (parent_id, nome, id_tributacao_padrao, ativo)
SELECT
    (SELECT id_categoria FROM categorias WHERE nome = v.pai LIMIT 1),
    v.nome,
    (SELECT id_tributacao FROM tributacao_perfil WHERE nome = v.perfil LIMIT 1),
    true
FROM (VALUES
    ('Alimentos',         'Graos e Cereais',                 'Alimento Basico Isento'),
    ('Alimentos',         'Acucar e Adocantes',              'Alimento Basico Isento'),
    ('Alimentos',         'Farinhas e Massas',               'Alimento Basico Isento'),
    ('Bebidas',           'Refrigerantes',                   'Bebida Nao Alcoolica ST'),
    ('Bebidas',           'Aguas e Sucos',                   'Bebida Nao Alcoolica ST'),
    ('Carnes',            'Bovinos',                         'Carnes Monofasico'),
    ('Carnes',            'Aves',                            'Carnes Monofasico'),
    ('Higiene e Limpeza', 'Sabonetes e Higiene Pessoal',     'Higiene Limpeza Pleno'),
    ('Higiene e Limpeza', 'Detergentes e Limpadores',        'Higiene Limpeza Pleno')
) AS v(pai, nome, perfil)
ON CONFLICT DO NOTHING;


-- 11. PRODUTOS DE EXEMPLO

INSERT INTO produto (
    descricao, codigo_barras,
    preco_custo, preco_venda, margem_lucro,
    estoque_atual, estoque_minimo, estoque_maximo,
    id_categoria, id_tributacao,
    unidade_medida,
    permite_fracionamento, controla_estoque, balanca, ativo
) VALUES
('Arroz Branco Tipo 1 5kg',          '7891234500001', 22.90, 28.90, 26.20, 150, 30,  500, (SELECT id_categoria FROM categorias WHERE nome = 'Graos e Cereais'             LIMIT 1), null, 'PCT', false, true, false, true),
('Feijao Carioca 1kg',               '7891234500002',  7.50,  9.90, 32.00, 120, 25,  400, (SELECT id_categoria FROM categorias WHERE nome = 'Graos e Cereais'             LIMIT 1), null, 'PCT', false, true, false, true),
('Farinha de Trigo 1kg',             '7891234500003',  4.20,  5.50, 30.95,  80, 20,  300, (SELECT id_categoria FROM categorias WHERE nome = 'Farinhas e Massas'           LIMIT 1), null, 'PCT', false, true, false, true),
('Acucar Cristal 1kg',               '7891234500004',  4.80,  6.20, 29.17,  60, 20,  300, (SELECT id_categoria FROM categorias WHERE nome = 'Acucar e Adocantes'          LIMIT 1), null, 'PCT', false, true, false, true),
('Refrigerante Coca-Cola 2L',        '7891234500005',  6.50,  9.90, 52.31, 200, 50,  600, (SELECT id_categoria FROM categorias WHERE nome = 'Refrigerantes'               LIMIT 1), null, 'UN',  false, true, false, true),
('Refrigerante Guarana Antarctica 2L','7891234500006',  5.90,  8.90, 50.85, 180, 50,  600, (SELECT id_categoria FROM categorias WHERE nome = 'Refrigerantes'               LIMIT 1), null, 'UN',  false, true, false, true),
('Agua Mineral Crystal 500ml',       '7891234500007',  1.00,  2.00,100.00, 300, 80,  800, (SELECT id_categoria FROM categorias WHERE nome = 'Aguas e Sucos'               LIMIT 1), null, 'UN',  false, true, false, true),
('Suco Del Valle Laranja 1L',        '7891234500008',  5.90,  8.50, 44.07,  90, 30,  300, (SELECT id_categoria FROM categorias WHERE nome = 'Aguas e Sucos'               LIMIT 1), null, 'UN',  false, true, false, true),
('Peito de Frango por kg',           '7891234500009', 15.90, 22.90, 44.03,  50, 10,  100, (SELECT id_categoria FROM categorias WHERE nome = 'Aves'                        LIMIT 1), null, 'KG',  true,  true, true,  true),
('Alcatra Bovina por kg',            '7891234500010', 32.90, 42.90, 30.40,  35,  5,   80, (SELECT id_categoria FROM categorias WHERE nome = 'Bovinos'                     LIMIT 1), null, 'KG',  true,  true, true,  true),
('Sabonete Dove Hidratante 90g',     '7891234500011',  2.80,  4.90, 75.00,  80, 20,  200, (SELECT id_categoria FROM categorias WHERE nome = 'Sabonetes e Higiene Pessoal' LIMIT 1), null, 'UN',  false, true, false, true),
('Detergente Ype Neutro 500ml',      '7891234500012',  2.50,  3.90, 56.00, 120, 30,  400, (SELECT id_categoria FROM categorias WHERE nome = 'Detergentes e Limpadores'    LIMIT 1), null, 'UN',  false, true, false, true),
('Pao Frances por kg',               '7891234500013', 10.00, 15.90, 59.00,  20,  5,   50, (SELECT id_categoria FROM categorias WHERE nome = 'Padaria e Confeitaria'       LIMIT 1), null, 'KG',  true,  true, true,  true),
('Cigarro Marlboro Vermelho',        '7891234500014', 12.90, 16.90, 30.93, 100, 30,  200, (SELECT id_categoria FROM categorias WHERE nome = 'Tabacaria'                   LIMIT 1), null, 'PCT', false, true, false, true),
('Cigarro Lucky Strike',             '7891234500015', 12.50, 15.90, 27.20,  50, 15,  150, (SELECT id_categoria FROM categorias WHERE nome = 'Tabacaria'                   LIMIT 1), null, 'PCT', false, true, false, true)
ON CONFLICT (codigo_barras) DO NOTHING;


-- 12. FLAG — marca que o seed foi aplicado
-- Usada pelo DatabaseInitializer para não rodar duas vezes.
INSERT INTO configuracoes (modulo, chave, valor, tipo_dado, descricao)
VALUES ('SISTEMA', 'SEED_APLICADO', 'true', 'BOOLEAN', 'Indica que o seed inicial foi executado')
ON CONFLICT (modulo, chave) DO NOTHING;