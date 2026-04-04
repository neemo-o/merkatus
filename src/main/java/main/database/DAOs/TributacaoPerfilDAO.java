package main.database.DAOs;

import main.database.GenericDAO;
import main.models.TributacaoPerfil;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Component
public class TributacaoPerfilDAO extends GenericDAO<TributacaoPerfil, Integer> {

    @Override
    protected String getTabela() { return "tributacao_perfil"; }

    @Override
    protected String getColunaId() { return "id_tributacao"; }

    @Override
    protected void setIdGerado(TributacaoPerfil t, ResultSet keys) throws SQLException {
        t.setIdTributacao(keys.getInt(1));
    }

    // ==============================
    // Busca por NCM — usada no fallback do TributacaoService
    // ==============================

    public Optional<TributacaoPerfil> findByNcm(String ncm) throws SQLException {
        String sql = """
                SELECT tp.*
                FROM tributacao_perfil tp
                INNER JOIN ncm_tributacao nt ON nt.id_tributacao = tp.id_tributacao
                WHERE nt.ncm = ?
                  AND tp.ativo = TRUE
                """;

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
    // Busca somente os ativos (para combobox de cadastro)
    // ==============================

    public List<TributacaoPerfil> findAllAtivos() throws SQLException {
        var lista = new java.util.ArrayList<TributacaoPerfil>();
        String sql = "SELECT * FROM tributacao_perfil WHERE ativo = TRUE ORDER BY nome";

        try (var conn = getConnection();
             var stmt = conn.prepareStatement(sql);
             var rs = stmt.executeQuery()) {

            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    // ==============================
    // Mapeamento ResultSet → Model
    // ==============================

    @Override
    protected TributacaoPerfil mapear(ResultSet rs) throws SQLException {
        TributacaoPerfil t = new TributacaoPerfil();

        t.setIdTributacao(rs.getInt("id_tributacao"));
        t.setNome(rs.getString("nome"));
        t.setDescricao(rs.getString("descricao"));

        t.setNcm(rs.getString("ncm"));
        t.setCest(rs.getString("cest"));

        t.setCstIcms(rs.getString("cst_icms"));
        t.setCsosn(rs.getString("csosn"));
        t.setAliqIcms(rs.getBigDecimal("aliq_icms"));
        t.setAliqIcmsSt(rs.getBigDecimal("aliq_icms_st"));
        t.setMvaSt(rs.getBigDecimal("mva_st"));

        t.setCfopVenda(rs.getString("cfop_venda"));
        t.setCfopVendaInterestadual(rs.getString("cfop_venda_interestadual"));

        t.setCstPis(rs.getString("cst_pis"));
        t.setCstCofins(rs.getString("cst_cofins"));
        t.setAliqPis(rs.getBigDecimal("aliq_pis"));
        t.setAliqCofins(rs.getBigDecimal("aliq_cofins"));

        t.setCstIpi(rs.getString("cst_ipi"));
        t.setAliqIpi(rs.getBigDecimal("aliq_ipi"));

        t.setAtivo(rs.getBoolean("ativo"));

        Timestamp cadastro = rs.getTimestamp("data_cadastro");
        if (cadastro != null) t.setDataCadastro(cadastro.toLocalDateTime());

        Timestamp atualizacao = rs.getTimestamp("data_atualizacao");
        if (atualizacao != null) t.setDataAtualizacao(atualizacao.toLocalDateTime());

        return t;
    }

    // ==============================
    // SQL Insert / Update
    // ==============================

    @Override
    protected String getSqlInsert() {
        return """
                INSERT INTO tributacao_perfil
                (nome, descricao, ncm, cest,
                 cst_icms, csosn, aliq_icms, aliq_icms_st, mva_st,
                 cfop_venda, cfop_venda_interestadual,
                 cst_pis, cst_cofins, aliq_pis, aliq_cofins,
                 cst_ipi, aliq_ipi, ativo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
    }

    @Override
    protected String getSqlUpdate() {
        return """
                UPDATE tributacao_perfil SET
                    nome = ?, descricao = ?, ncm = ?, cest = ?,
                    cst_icms = ?, csosn = ?, aliq_icms = ?, aliq_icms_st = ?, mva_st = ?,
                    cfop_venda = ?, cfop_venda_interestadual = ?,
                    cst_pis = ?, cst_cofins = ?, aliq_pis = ?, aliq_cofins = ?,
                    cst_ipi = ?, aliq_ipi = ?, ativo = ?
                WHERE id_tributacao = ?
                """;
    }

    @Override
    protected void setParametrosInsert(PreparedStatement stmt, TributacaoPerfil t) throws SQLException {
        setParametrosComuns(stmt, t);
        // sem id no insert
    }

    @Override
    protected void setParametrosUpdate(PreparedStatement stmt, TributacaoPerfil t) throws SQLException {
        setParametrosComuns(stmt, t);
        stmt.setInt(19, t.getIdTributacao()); // WHERE
    }

    // Evita duplicar os 18 setters em insert e update
    private void setParametrosComuns(PreparedStatement stmt, TributacaoPerfil t) throws SQLException {
        stmt.setString(1, t.getNome());
        stmt.setString(2, t.getDescricao());
        stmt.setString(3, t.getNcm());
        stmt.setString(4, t.getCest());
        stmt.setString(5, t.getCstIcms());
        stmt.setString(6, t.getCsosn());
        stmt.setObject(7, t.getAliqIcms());
        stmt.setObject(8, t.getAliqIcmsSt());
        stmt.setObject(9, t.getMvaSt());
        stmt.setString(10, t.getCfopVenda());
        stmt.setString(11, t.getCfopVendaInterestadual());
        stmt.setString(12, t.getCstPis());
        stmt.setString(13, t.getCstCofins());
        stmt.setObject(14, t.getAliqPis());
        stmt.setObject(15, t.getAliqCofins());
        stmt.setString(16, t.getCstIpi());
        stmt.setObject(17, t.getAliqIpi());
        stmt.setBoolean(18, t.isAtivo());
    }
}
