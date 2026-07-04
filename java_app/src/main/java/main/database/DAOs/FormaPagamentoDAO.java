package main.database.DAOs;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.stereotype.Component;

import main.database.GenericDAO;
import main.models.FormaPagamento;

@Component
public class FormaPagamentoDAO extends GenericDAO<FormaPagamento, Integer> {

    @Override
    protected String getTabela() {
        return "formas_pagamento";
    }

    @Override
    protected String getColunaId() {
        return "id_forma_pagamento";
    }

    // findAll() retorna apenas formas ativas
    @Override
    protected String getColunaAtivo() {
        return "ativo";
    }

    @Override
    protected void setGeneratedId(FormaPagamento f, Number id) {
        f.setIdFormaPagamento(id.intValue());
    }

    @Override
    protected String getSqlInsert() {
        return """
                INSERT INTO formas_pagamento
                (descricao, tipo, cod_pagamento_nfe, tef, permite_troco, ativo)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
    }

    @Override
    protected void setParametrosInsert(PreparedStatement stmt, FormaPagamento f) throws SQLException {
        stmt.setString(1, f.getDescricao());
        stmt.setString(2, f.getTipo());
        stmt.setString(3, f.getCodPagamentoNfe());
        stmt.setBoolean(4, Boolean.TRUE.equals(f.getTef()));
        stmt.setBoolean(5, Boolean.TRUE.equals(f.getPermiteTroco()));
        stmt.setBoolean(6, !Boolean.FALSE.equals(f.getAtivo()));
    }

    @Override
    protected String getSqlUpdate() {
        return """
                UPDATE formas_pagamento SET
                descricao = ?, tipo = ?, cod_pagamento_nfe = ?, tef = ?, permite_troco = ?, ativo = ?
                WHERE id_forma_pagamento = ?
                """;
    }

    @Override
    protected void setParametrosUpdate(PreparedStatement stmt, FormaPagamento f) throws SQLException {
        stmt.setString(1, f.getDescricao());
        stmt.setString(2, f.getTipo());
        stmt.setString(3, f.getCodPagamentoNfe());
        stmt.setBoolean(4, Boolean.TRUE.equals(f.getTef()));
        stmt.setBoolean(5, Boolean.TRUE.equals(f.getPermiteTroco()));
        stmt.setBoolean(6, !Boolean.FALSE.equals(f.getAtivo()));
        stmt.setInt(7, f.getIdFormaPagamento());
    }

    @Override
    protected FormaPagamento mapear(ResultSet rs) throws SQLException {
        FormaPagamento f = new FormaPagamento();
        f.setIdFormaPagamento(rs.getInt("id_forma_pagamento"));
        f.setDescricao(rs.getString("descricao"));
        f.setTipo(rs.getString("tipo"));
        f.setCodPagamentoNfe(rs.getString("cod_pagamento_nfe"));
        f.setTef(rs.getBoolean("tef"));
        f.setPermiteTroco(rs.getBoolean("permite_troco"));
        f.setAtivo(rs.getBoolean("ativo"));
        java.sql.Timestamp tsCadastro = rs.getTimestamp("data_cadastro");
        f.setDataCadastro(tsCadastro != null ? tsCadastro.toLocalDateTime() : null);
        return f;
    }
}
