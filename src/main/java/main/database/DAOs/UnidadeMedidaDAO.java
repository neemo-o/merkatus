package main.database.DAOs;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.stereotype.Component;

import main.database.GenericDAO;
import main.models.UnidadeMedida;

@Component
public class UnidadeMedidaDAO extends GenericDAO<UnidadeMedida, Integer> {

    @Override
    protected String getTabela() {
        return "unidades_medida";
    }

    @Override
    protected String getColunaId() {
        return "id_unidade";
    }

    @Override
    protected void setGeneratedId(UnidadeMedida u, Number id) {
        u.setIdUnidade(id.intValue());
    }

    @Override
    protected String getSqlInsert() {
        return """
                INSERT INTO unidades_medida
                (sigla, descricao)
                VALUES (?, ?)
                """;
    }

    @Override
    protected void setParametrosInsert(PreparedStatement stmt, UnidadeMedida u) throws SQLException {
        stmt.setString(1, u.getSigla());
        stmt.setString(2, u.getDescricao());
    }

    @Override
    protected void setParametrosUpdate(PreparedStatement stmt, UnidadeMedida u) throws SQLException {
        stmt.setString(1, u.getSigla());
        stmt.setString(2, u.getDescricao());
        stmt.setInt(3, u.getIdUnidade());
    }

    @Override
    protected String getSqlUpdate() {
        return """
                UPDATE unidades_medida SET
                    sigla = ?,
                    descricao = ?
                WHERE id_unidade = ?
                """;
    }

    @Override
    protected UnidadeMedida mapear(ResultSet rs) throws SQLException {
        UnidadeMedida u = new UnidadeMedida();
        u.setIdUnidade(rs.getInt("id_unidade"));
        u.setSigla(rs.getString("sigla"));
        u.setDescricao(rs.getString("descricao"));
        return u;
    }
}

