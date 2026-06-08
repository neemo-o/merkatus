package main.database.DAOs;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import main.database.GenericDAO;
import main.models.Produto;

@Component
public class ProdutoDAO extends GenericDAO<Produto, Integer> {

    @Override
    protected String getTabela() {
        return "produto";
    }

    @Override
    protected String getColunaId() {
        return "id_produto";
    }

    // ATIVA O SOFT DELETE PARA PRODUTO
    @Override
    protected String getColunaAtivo() {
        return "ativo";
    }

    @Override
    protected void setGeneratedId(Produto p, Number id) {
        p.setIdProduto(id.intValue());
    }

    @Override
    protected String getSqlInsert() {
        return """
                INSERT INTO produto
                (descricao, codigo_barras, unidade_medida, id_unidade_medida,
                 id_categoria, preco_custo, preco_venda, margem_lucro,
                 estoque_atual, estoque_minimo, estoque_maximo, id_fornecedor,
                 ncm, cest, cfop_venda, cst_icms, csosn, cst_pis, cst_cofins, cst_ipi,
                 aliq_icms, aliq_pis, aliq_cofins, aliq_ipi,
                 id_tributacao, peso_liquido, peso_bruto,
                 permite_fracionamento, controla_estoque, balanca, ativo, data_cadastro)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
    }

    @Override
    protected void setParametrosInsert(PreparedStatement stmt, Produto p) throws SQLException {
        stmt.setString(1, p.getDescricao());
        stmt.setString(2, p.getCodigoBarras());
        stmt.setString(3, p.getUnidadeMedida());
        stmt.setObject(4, p.getIdUnidadeMedida());
        stmt.setObject(5, p.getIdCategoria());
        stmt.setObject(6, p.getPrecoCusto());
        stmt.setObject(7, p.getPrecoVenda());
        stmt.setObject(8, p.getMargemLucro());
        stmt.setInt(9, p.getEstoqueAtual());
        stmt.setObject(10, p.getEstoqueMinimo());
        stmt.setObject(11, p.getEstoqueMaximo());
        stmt.setObject(12, p.getIdFornecedor());
        stmt.setString(13, p.getNcm());
        stmt.setString(14, p.getCest());
        stmt.setString(15, p.getCfopVenda());
        stmt.setString(16, p.getCstIcms());
        stmt.setString(17, p.getCsosn());
        stmt.setString(18, p.getCstPis());
        stmt.setString(19, p.getCstCofins());
        stmt.setString(20, p.getCstIpi());
        stmt.setObject(21, p.getAliqIcms());
        stmt.setObject(22, p.getAliqPis());
        stmt.setObject(23, p.getAliqCofins());
        stmt.setObject(24, p.getAliqIpi());
        stmt.setObject(25, p.getIdTributacao());
        stmt.setObject(26, p.getPesoLiquido());
        stmt.setObject(27, p.getPesoBruto());
        stmt.setBoolean(28, p.isPermiteFracionamento());
        stmt.setBoolean(29, p.isControlaEstoque());
        stmt.setBoolean(30, p.isBalanca());
        stmt.setBoolean(31, p.isAtivo());
        stmt.setObject(32, p.getDataCadastro());
    }

    @Override
    protected void setParametrosUpdate(PreparedStatement stmt, Produto p) throws SQLException {
        stmt.setString(1, p.getDescricao());
        stmt.setString(2, p.getCodigoBarras());
        stmt.setString(3, p.getUnidadeMedida());
        stmt.setObject(4, p.getIdUnidadeMedida());
        stmt.setObject(5, p.getIdCategoria());
        stmt.setObject(6, p.getPrecoCusto());
        stmt.setObject(7, p.getPrecoVenda());
        stmt.setObject(8, p.getMargemLucro());
        stmt.setInt(9, p.getEstoqueAtual());
        stmt.setObject(10, p.getEstoqueMinimo());
        stmt.setObject(11, p.getEstoqueMaximo());
        stmt.setObject(12, p.getIdFornecedor());
        stmt.setString(13, p.getNcm());
        stmt.setString(14, p.getCest());
        stmt.setString(15, p.getCfopVenda());
        stmt.setString(16, p.getCstIcms());
        stmt.setString(17, p.getCsosn());
        stmt.setString(18, p.getCstPis());
        stmt.setString(19, p.getCstCofins());
        stmt.setString(20, p.getCstIpi());
        stmt.setObject(21, p.getAliqIcms());
        stmt.setObject(22, p.getAliqPis());
        stmt.setObject(23, p.getAliqCofins());
        stmt.setObject(24, p.getAliqIpi());
        stmt.setObject(25, p.getIdTributacao());
        stmt.setObject(26, p.getPesoLiquido());
        stmt.setObject(27, p.getPesoBruto());
        stmt.setBoolean(28, p.isPermiteFracionamento());
        stmt.setBoolean(29, p.isControlaEstoque());
        stmt.setBoolean(30, p.isBalanca());
        stmt.setBoolean(31, p.isAtivo());
        stmt.setObject(32, p.getDataAtualizacao());
        stmt.setInt(33, p.getIdProduto());
    }

    @Override
    protected String getSqlUpdate() {
        return """
                UPDATE produto SET
                    descricao = ?,
                    codigo_barras = ?,
                    unidade_medida = ?,
                    id_unidade_medida = ?,
                    id_categoria = ?,
                    preco_custo = ?,
                    preco_venda = ?,
                    margem_lucro = ?,
                    estoque_atual = ?,
                    estoque_minimo = ?,
                    estoque_maximo = ?,
                    id_fornecedor = ?,
                    ncm = ?,
                    cest = ?,
                    cfop_venda = ?,
                    cst_icms = ?,
                    csosn = ?,
                    cst_pis = ?,
                    cst_cofins = ?,
                    cst_ipi = ?,
                    aliq_icms = ?,
                    aliq_pis = ?,
                    aliq_cofins = ?,
                    aliq_ipi = ?,
                    id_tributacao = ?,
                    peso_liquido = ?,
                    peso_bruto = ?,
                    permite_fracionamento = ?,
                    controla_estoque = ?,
                    balanca = ?,
                    ativo = ?,
                    data_atualizacao = ?
                WHERE id_produto = ?
                    """;
    }

    @Override
    protected Produto mapear(ResultSet rs) throws SQLException {
        Produto p = new Produto();
        p.setIdProduto(rs.getInt("id_produto"));
        p.setDescricao(rs.getString("descricao"));
        p.setCodigoBarras(rs.getString("codigo_barras"));
        p.setUnidadeMedida(rs.getString("unidade_medida"));
        p.setIdUnidadeMedida(rs.getObject("id_unidade_medida", Integer.class));
        p.setIdCategoria(rs.getObject("id_categoria", Integer.class));
        p.setPrecoCusto(rs.getBigDecimal("preco_custo"));
        p.setPrecoVenda(rs.getBigDecimal("preco_venda"));
        p.setMargemLucro(rs.getBigDecimal("margem_lucro"));
        p.setEstoqueAtual(rs.getInt("estoque_atual"));
        p.setEstoqueMinimo(rs.getBigDecimal("estoque_minimo"));
        p.setEstoqueMaximo(rs.getBigDecimal("estoque_maximo"));
        p.setIdFornecedor(rs.getObject("id_fornecedor", Integer.class));
        p.setPermiteFracionamento(rs.getBoolean("permite_fracionamento"));
        p.setControlaEstoque(rs.getBoolean("controla_estoque"));
        p.setBalanca(rs.getBoolean("balanca"));
        p.setAtivo(rs.getBoolean("ativo"));
        p.setNcm(rs.getString("ncm"));
        p.setCest(rs.getString("cest"));
        p.setCfopVenda(rs.getString("cfop_venda"));
        p.setCstIcms(rs.getString("cst_icms"));
        p.setCsosn(rs.getString("csosn"));
        p.setCstPis(rs.getString("cst_pis"));
        p.setCstCofins(rs.getString("cst_cofins"));
        p.setCstIpi(rs.getString("cst_ipi"));
        p.setAliqIcms(rs.getBigDecimal("aliq_icms"));
        p.setAliqPis(rs.getBigDecimal("aliq_pis"));
        p.setAliqCofins(rs.getBigDecimal("aliq_cofins"));
        p.setAliqIpi(rs.getBigDecimal("aliq_ipi"));
        p.setIdTributacao(rs.getObject("id_tributacao", Integer.class));
        p.setPesoLiquido(rs.getBigDecimal("peso_liquido"));
        p.setPesoBruto(rs.getBigDecimal("peso_bruto"));
        
        java.sql.Timestamp tsCadastro = rs.getTimestamp("data_cadastro");
        if (tsCadastro != null) p.setDataCadastro(tsCadastro.toLocalDateTime());
        
        java.sql.Timestamp tsAtualizacao = rs.getTimestamp("data_atualizacao");
        if (tsAtualizacao != null) p.setDataAtualizacao(tsAtualizacao.toLocalDateTime());
        
        return p;
    }

    // ==============================
    // MÉTODOS ESPECÍFICOS PARA LIXEIRA (não genéricos)
    // ==============================

    /**
     * Exclusão FÍSICA real (para usar na lixeira).
     * Use com extrema cautela!
     */
    public boolean deletePermanente(Integer id) {
        String sql = "DELETE FROM " + getTabela() + " WHERE " + getColunaId() + " = ?";
        return getJdbc().update(sql, id) > 0;
    }

    /**
     * Busca com filtros dinâmicos (busca textual + categoria + fornecedor).
     * Este método é específico para Produto, então mantemos aqui.
     */
    public List<Produto> findByFiltros(String busca, Integer idCategoria, Integer idFornecedor) {
        StringBuilder sql = new StringBuilder(
            "SELECT * FROM " + getTabela() + " WHERE ativo = true"
        );
        List<Object> params = new ArrayList<>();

        if (busca != null && !busca.isBlank()) {
            sql.append(" AND (descricao LIKE ? OR codigo_barras LIKE ?)");
            params.add("%" + busca + "%");
            params.add("%" + busca + "%");
        }
        if (idCategoria != null) {
            sql.append(" AND id_categoria = ?");
            params.add(idCategoria);
        }
        if (idFornecedor != null) {
            sql.append(" AND id_fornecedor = ?");
            params.add(idFornecedor);
        }
        sql.append(" ORDER BY descricao");

        return getJdbc().query(sql.toString(), (rs, rowNum) -> mapear(rs), params.toArray());
    }
}