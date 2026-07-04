package main.database.DAOs;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.stereotype.Component;

import main.database.GenericDAO;
import main.models.Venda;

@Component
public class VendaDAO extends GenericDAO<Venda, Integer> {

    @Override
    protected String getTabela() {
        return "venda";
    }

    @Override
    protected String getColunaId() {
        return "id_venda";
    }

    @Override
    protected void setGeneratedId(Venda v, Number id) {
        v.setIdVenda(id.intValue());
    }

    @Override
    protected String getSqlInsert() {
        return """
                INSERT INTO venda
                (id_caixa, id_terminal, id_operador, id_cliente, numero_venda, data_venda,
                 subtotal, desconto, acrescimo, valor_total, troco, cpf_nota,
                 forma_pagamento, status, observacao, data_cadastro)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
    }

    @Override
    protected void setParametrosInsert(PreparedStatement stmt, Venda v) throws SQLException {
        stmt.setObject(1, v.getIdCaixa());
        stmt.setObject(2, v.getIdTerminal());
        stmt.setObject(3, v.getIdOperador());
        stmt.setObject(4, v.getIdCliente());
        stmt.setObject(5, v.getNumeroVenda());
        stmt.setObject(6, v.getDataVenda());
        stmt.setObject(7, v.getSubtotal());
        stmt.setObject(8, v.getDesconto());
        stmt.setObject(9, v.getAcrescimo());
        stmt.setObject(10, v.getValorTotal());
        stmt.setObject(11, v.getTroco());
        stmt.setString(12, v.getCpfNota());
        stmt.setString(13, v.getFormaPagamento());
        stmt.setString(14, v.getStatus());
        stmt.setString(15, v.getObservacao());
        stmt.setObject(16, v.getDataCadastro());
    }

    @Override
    protected String getSqlUpdate() {
        return """
                UPDATE venda SET
                id_cliente = ?, cpf_nota = ?, status = ?, observacao = ?
                WHERE id_venda = ?
                """;
    }

    @Override
    protected void setParametrosUpdate(PreparedStatement stmt, Venda v) throws SQLException {
        stmt.setObject(1, v.getIdCliente());
        stmt.setString(2, v.getCpfNota());
        stmt.setString(3, v.getStatus());
        stmt.setString(4, v.getObservacao());
        stmt.setInt(5, v.getIdVenda());
    }

    @Override
    protected Venda mapear(ResultSet rs) throws SQLException {
        Venda v = new Venda();
        v.setIdVenda(rs.getInt("id_venda"));
        v.setIdCaixa(rs.getObject("id_caixa", Integer.class));
        v.setIdTerminal(rs.getObject("id_terminal", Integer.class));
        v.setIdOperador(rs.getObject("id_operador", Integer.class));
        v.setIdCliente(rs.getObject("id_cliente", Integer.class));
        v.setNumeroVenda(rs.getObject("numero_venda", Integer.class));
        java.sql.Timestamp tsVenda = rs.getTimestamp("data_venda");
        v.setDataVenda(tsVenda != null ? tsVenda.toLocalDateTime() : null);
        v.setSubtotal(rs.getBigDecimal("subtotal"));
        v.setDesconto(rs.getBigDecimal("desconto"));
        v.setAcrescimo(rs.getBigDecimal("acrescimo"));
        v.setValorTotal(rs.getBigDecimal("valor_total"));
        v.setTroco(rs.getBigDecimal("troco"));
        v.setCpfNota(rs.getString("cpf_nota"));
        v.setFormaPagamento(rs.getString("forma_pagamento"));
        v.setStatus(rs.getString("status"));
        v.setObservacao(rs.getString("observacao"));
        java.sql.Timestamp tsCadastro = rs.getTimestamp("data_cadastro");
        v.setDataCadastro(tsCadastro != null ? tsCadastro.toLocalDateTime() : null);
        return v;
    }

    /**
     * Lista as vendas da mais recente para a mais antiga (para a tela de vendas).
     */
    public List<Venda> findRecentes() {
        String sql = "SELECT * FROM venda ORDER BY data_venda DESC, id_venda DESC";
        return getJdbc().query(sql, (rs, rowNum) -> mapear(rs));
    }

    /**
     * Registra o pagamento da venda na tabela vendas_pagamentos.
     */
    public void registrarPagamento(Integer idVenda, Integer idFormaPagamento, BigDecimal valor) {
        String sql = """
                INSERT INTO vendas_pagamentos (id_venda, id_forma_pagamento, valor)
                VALUES (?, ?, ?)
                """;
        getJdbc().update(sql, idVenda, idFormaPagamento, valor);
    }

    /**
     * Registra o cancelamento na tabela de auditoria cancelamentos.
     */
    public void registrarCancelamento(Integer idVenda, Integer idOperador, String motivo) {
        String sql = """
                INSERT INTO cancelamentos (id_venda, id_operador, motivo, tipo)
                VALUES (?, ?, ?, 'TOTAL')
                """;
        getJdbc().update(sql, idVenda, idOperador, motivo);
    }
}
