package main.database.DAOs;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import main.database.GenericDAO;
import main.models.ItemVenda;

@Component
public class ItemVendaDAO extends GenericDAO<ItemVenda, Integer> {

    @Override
    protected String getTabela() {
        return "item_venda";
    }

    @Override
    protected String getColunaId() {
        return "id_item";
    }

    @Override
    protected void setGeneratedId(ItemVenda i, Number id) {
        i.setIdItem(id.intValue());
    }

    @Override
    protected String getSqlInsert() {
        return """
                INSERT INTO item_venda
                (id_venda, id_produto, sequencia, quantidade, preco_unitario, desconto, acrescimo, total_item,
                 cfop, ncm, cest, cst_icms, csosn, aliq_icms, valor_icms, base_icms,
                 cst_pis, aliq_pis, valor_pis, cst_cofins, aliq_cofins, valor_cofins)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
    }

    @Override
    protected void setParametrosInsert(PreparedStatement stmt, ItemVenda i) throws SQLException {
        stmt.setInt(1, i.getIdVenda());
        stmt.setInt(2, i.getIdProduto());
        stmt.setObject(3, i.getSequencia());
        stmt.setInt(4, i.getQuantidade());
        stmt.setBigDecimal(5, i.getPrecoUnitario());
        stmt.setObject(6, i.getDesconto());
        stmt.setObject(7, i.getAcrescimo());
        stmt.setObject(8, i.getTotalItem());
        stmt.setString(9, i.getCfop());
        stmt.setString(10, i.getNcm());
        stmt.setString(11, i.getCest());
        stmt.setString(12, i.getCstIcms());
        stmt.setString(13, i.getCsosn());
        stmt.setObject(14, i.getAliqIcms());
        stmt.setObject(15, i.getValorIcms());
        stmt.setObject(16, i.getBaseIcms());
        stmt.setString(17, i.getCstPis());
        stmt.setObject(18, i.getAliqPis());
        stmt.setObject(19, i.getValorPis());
        stmt.setString(20, i.getCstCofins());
        stmt.setObject(21, i.getAliqCofins());
        stmt.setObject(22, i.getValorCofins());
    }

    @Override
    protected String getSqlUpdate() {
        return """
                UPDATE item_venda SET
                cancelado = ?, motivo_cancelamento = ?
                WHERE id_item = ?
                """;
    }

    @Override
    protected void setParametrosUpdate(PreparedStatement stmt, ItemVenda i) throws SQLException {
        stmt.setBoolean(1, Boolean.TRUE.equals(i.getCancelado()));
        stmt.setString(2, i.getMotivoCancelamento());
        stmt.setInt(3, i.getIdItem());
    }

    @Override
    protected ItemVenda mapear(ResultSet rs) throws SQLException {
        ItemVenda i = new ItemVenda();
        i.setIdItem(rs.getInt("id_item"));
        i.setIdVenda(rs.getInt("id_venda"));
        i.setIdProduto(rs.getInt("id_produto"));
        i.setSequencia(rs.getObject("sequencia", Integer.class));
        i.setQuantidade(rs.getInt("quantidade"));
        i.setPrecoUnitario(rs.getBigDecimal("preco_unitario"));
        i.setDesconto(rs.getBigDecimal("desconto"));
        i.setAcrescimo(rs.getBigDecimal("acrescimo"));
        i.setTotalItem(rs.getBigDecimal("total_item"));
        i.setCfop(rs.getString("cfop"));
        i.setNcm(rs.getString("ncm"));
        i.setCest(rs.getString("cest"));
        i.setCstIcms(rs.getString("cst_icms"));
        i.setCsosn(rs.getString("csosn"));
        i.setAliqIcms(rs.getBigDecimal("aliq_icms"));
        i.setValorIcms(rs.getBigDecimal("valor_icms"));
        i.setBaseIcms(rs.getBigDecimal("base_icms"));
        i.setCstPis(rs.getString("cst_pis"));
        i.setAliqPis(rs.getBigDecimal("aliq_pis"));
        i.setValorPis(rs.getBigDecimal("valor_pis"));
        i.setCstCofins(rs.getString("cst_cofins"));
        i.setAliqCofins(rs.getBigDecimal("aliq_cofins"));
        i.setValorCofins(rs.getBigDecimal("valor_cofins"));
        i.setCancelado(rs.getBoolean("cancelado"));
        i.setMotivoCancelamento(rs.getString("motivo_cancelamento"));
        java.sql.Timestamp tsCadastro = rs.getTimestamp("data_cadastro");
        i.setDataCadastro(tsCadastro != null ? tsCadastro.toLocalDateTime() : null);
        return i;
    }

    /**
     * Itens de uma venda com a descrição do produto para exibição.
     */
    public List<ItemVenda> findByVenda(Integer idVenda) {
        String sql = """
                SELECT iv.*, p.descricao AS descricao_produto
                FROM item_venda iv
                JOIN produto p ON p.id_produto = iv.id_produto
                WHERE iv.id_venda = ?
                ORDER BY iv.sequencia, iv.id_item
                """;
        return getJdbc().query(sql, (rs, rowNum) -> {
            ItemVenda i = mapear(rs);
            i.setDescricaoProduto(rs.getString("descricao_produto"));
            return i;
        }, idVenda);
    }

    /**
     * Quantidade de itens por venda (uma única consulta para a listagem de vendas).
     */
    public Map<Integer, Integer> contarPorVenda() {
        String sql = "SELECT id_venda, COUNT(*) AS total FROM item_venda GROUP BY id_venda";
        Map<Integer, Integer> contagem = new HashMap<>();
        getJdbc().query(sql, rs -> {
            contagem.put(rs.getInt("id_venda"), rs.getInt("total"));
        });
        return contagem;
    }
}
