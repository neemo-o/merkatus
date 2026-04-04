package main.database.DAOs;

import main.database.GenericDAO;
import main.models.NcmTributacao;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

@Component
public class NcmTributacaoDAO extends GenericDAO<NcmTributacao, Integer> {

    @Override
    protected String getTabela() { return "ncm_tributacao"; }

    @Override
    protected String getColunaId() { return "id_ncm_tributacao"; }

    @Override
    protected void setIdGerado(NcmTributacao n, ResultSet keys) throws SQLException {
        n.setIdNcmTributacao(keys.getInt(1));
    }

    // ==============================
    // Busca por NCM — ponto central do fallback
    // ==============================

    public Optional<NcmTributacao> findByNcm(String ncm) throws SQLException {
        String sql = "SELECT * FROM ncm_tributacao WHERE ncm = ?";

        try (var conn = getConnection();
             var stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ncm);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapear(rs));
            }
        }
        return Optional.empty();
    }

    // ==============================
    // Mapeamento ResultSet → Model
    // ==============================

    @Override
    protected NcmTributacao mapear(ResultSet rs) throws SQLException {
        NcmTributacao n = new NcmTributacao();

        n.setIdNcmTributacao(rs.getInt("id_ncm_tributacao"));
        n.setNcm(rs.getString("ncm"));
        n.setDescricaoNcm(rs.getString("descricao_ncm"));
        n.setIdTributacao(rs.getInt("id_tributacao"));

        Timestamp cadastro = rs.getTimestamp("data_cadastro");
        if (cadastro != null) n.setDataCadastro(cadastro.toLocalDateTime());

        return n;
    }

    // ==============================
    // SQL Insert / Update
    // ==============================

    @Override
    protected String getSqlInsert() {
        return """
                INSERT INTO ncm_tributacao (ncm, descricao_ncm, id_tributacao)
                VALUES (?, ?, ?)
                """;
    }

    @Override
    protected String getSqlUpdate() {
        return """
                UPDATE ncm_tributacao SET
                    ncm = ?,
                    descricao_ncm = ?,
                    id_tributacao = ?
                WHERE id_ncm_tributacao = ?
                """;
    }

    @Override
    protected void setParametrosInsert(PreparedStatement stmt, NcmTributacao n) throws SQLException {
        stmt.setString(1, n.getNcm());
        stmt.setString(2, n.getDescricaoNcm());
        stmt.setInt(3, n.getIdTributacao());
    }

    @Override
    protected void setParametrosUpdate(PreparedStatement stmt, NcmTributacao n) throws SQLException {
        stmt.setString(1, n.getNcm());
        stmt.setString(2, n.getDescricaoNcm());
        stmt.setInt(3, n.getIdTributacao());
        stmt.setInt(4, n.getIdNcmTributacao()); // WHERE
    }
}
