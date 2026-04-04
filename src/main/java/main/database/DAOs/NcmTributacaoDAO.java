package main.database.DAOs;

import main.database.GenericDAO;
import main.models.NcmTributacao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
public class NcmTributacaoDAO extends GenericDAO<NcmTributacao, Integer> {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    protected String getTabela() { return "ncm_tributacao"; }

    @Override
    protected String getColunaId() { return "id_ncm_tributacao"; }

    @Override
    protected void setGeneratedId(NcmTributacao n, Number id) {
        n.setIdNcmTributacao(id.intValue());
    }

    public Optional<NcmTributacao> findByNcm(String ncm) {
        String sql = "SELECT * FROM ncm_tributacao WHERE ncm = ?";
        List<NcmTributacao> result = jdbcTemplate.query(sql, (rs, rowNum) -> mapear(rs), ncm);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    protected NcmTributacao mapear(ResultSet rs) throws SQLException {
        NcmTributacao n = new NcmTributacao();
        n.setIdNcmTributacao(rs.getInt("id_ncm_tributacao"));
        n.setNcm(rs.getString("ncm"));
        n.setDescricaoNcm(rs.getString("descricao_ncm"));
        n.setIdTributacao(rs.getInt("id_tributacao"));
        java.sql.Timestamp cadastro = rs.getTimestamp("data_cadastro");
        if (cadastro != null) n.setDataCadastro(cadastro.toLocalDateTime());
        return n;
    }

    @Override
    protected String getSqlInsert() {
        return "INSERT INTO ncm_tributacao (ncm, descricao_ncm, id_tributacao) VALUES (?, ?, ?)";
    }

    @Override
    protected String getSqlUpdate() {
        return "UPDATE ncm_tributacao SET ncm = ?, descricao_ncm = ?, id_tributacao = ? WHERE id_ncm_tributacao = ?";
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
        stmt.setInt(4, n.getIdNcmTributacao());
    }
}
