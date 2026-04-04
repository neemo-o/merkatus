package main.database.DAOs;

import main.database.GenericDAO;
import main.models.Categoria;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class CategoriaDAO extends GenericDAO<Categoria, Integer> {

    @Override
    protected String getTabela() { return "categorias"; }

    @Override
    protected String getColunaId() { return "id_categoria"; }

    @Override
    protected void setIdGerado(Categoria c, ResultSet keys) throws SQLException {
        c.setIdCategoria(keys.getInt(1));
    }

    // ==============================
    // Busca todas as raízes (sem parent) — útil para montar árvore de categorias
    // ==============================

    public List<Categoria> findRaizes() throws SQLException {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT * FROM categorias WHERE parent_id IS NULL AND ativo = TRUE ORDER BY nome";

        try (var conn = getConnection();
             var stmt = conn.prepareStatement(sql);
             var rs = stmt.executeQuery()) {

            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    // ==============================
    // Busca filhas de uma categoria pai
    // ==============================

    public List<Categoria> findByParent(Integer parentId) throws SQLException {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT * FROM categorias WHERE parent_id = ? AND ativo = TRUE ORDER BY nome";

        try (var conn = getConnection();
             var stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, parentId);
            try (var rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    // ==============================
    // Busca categoria COM tributação padrão resolvida — usado no TributacaoService
    // Retorna a categoria com idTributacaoPadrao preenchido (se tiver)
    // ==============================

    public Optional<Categoria> findByIdComTributacao(Integer idCategoria) throws SQLException {
        String sql = """
                SELECT c.*
                FROM categorias c
                WHERE c.id_categoria = ?
                """;

        try (var conn = getConnection();
             var stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCategoria);
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
    protected Categoria mapear(ResultSet rs) throws SQLException {
        Categoria c = new Categoria();

        c.setIdCategoria(rs.getInt("id_categoria"));

        // parent_id é nullable — getObject evita retornar 0 quando o valor é NULL
        c.setParentId(rs.getObject("parent_id", Integer.class));
        c.setNome(rs.getString("nome"));

        // id_tributacao_padrao também é nullable
        c.setIdTributacaoPadrao(rs.getObject("id_tributacao_padrao", Integer.class));
        c.setAtivo(rs.getBoolean("ativo"));

        Timestamp cadastro = rs.getTimestamp("data_cadastro");
        if (cadastro != null) c.setDataCadastro(cadastro.toLocalDateTime());

        return c;
    }

    // ==============================
    // SQL Insert / Update
    // ==============================

    @Override
    protected String getSqlInsert() {
        return """
                INSERT INTO categorias (parent_id, nome, id_tributacao_padrao, ativo)
                VALUES (?, ?, ?, ?)
                """;
    }

    @Override
    protected String getSqlUpdate() {
        return """
                UPDATE categorias SET
                    parent_id = ?,
                    nome = ?,
                    id_tributacao_padrao = ?,
                    ativo = ?
                WHERE id_categoria = ?
                """;
    }

    @Override
    protected void setParametrosInsert(PreparedStatement stmt, Categoria c) throws SQLException {
        stmt.setObject(1, c.getParentId());            // nullable
        stmt.setString(2, c.getNome());
        stmt.setObject(3, c.getIdTributacaoPadrao());  // nullable
        stmt.setBoolean(4, c.isAtivo());
    }

    @Override
    protected void setParametrosUpdate(PreparedStatement stmt, Categoria c) throws SQLException {
        stmt.setObject(1, c.getParentId());
        stmt.setString(2, c.getNome());
        stmt.setObject(3, c.getIdTributacaoPadrao());
        stmt.setBoolean(4, c.isAtivo());
        stmt.setInt(5, c.getIdCategoria()); // WHERE
    }
}
