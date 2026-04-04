package main.database.DAOs;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import main.database.GenericDAO;
import main.models.Categoria;

@Component
public class CategoriaDAO extends GenericDAO<Categoria, Integer> {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public static java.util.List<Categoria> findAllStatic() {
        return GenericDAO.findAllStatic(CategoriaDAO.class);
    }

    public static Categoria findByIdStatic(Integer id) {
        return GenericDAO.findByIdStatic(CategoriaDAO.class, id);
    }

    @Override
    protected String getTabela() { return "categorias"; }

    @Override
    protected String getColunaId() { return "id_categoria"; }

    @Override
    protected void setGeneratedId(Categoria c, Number id) {
        c.setIdCategoria(id.intValue());
    }

    public List<Categoria> findRaizes() {
        String sql = "SELECT * FROM categorias WHERE parent_id IS NULL AND ativo = TRUE ORDER BY nome";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapear(rs));
    }

    public List<Categoria> findByParent(Integer parentId) {
        String sql = "SELECT * FROM categorias WHERE parent_id = ? AND ativo = TRUE ORDER BY nome";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapear(rs), parentId);
    }

    public Optional<Categoria> findByIdComTributacao(Integer idCategoria) {
        String sql = "SELECT * FROM categorias WHERE id_categoria = ?";
        List<Categoria> result = jdbcTemplate.query(sql, (rs, rowNum) -> mapear(rs), idCategoria);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    protected Categoria mapear(ResultSet rs) throws SQLException {
        Categoria c = new Categoria();
        c.setIdCategoria(rs.getInt("id_categoria"));
        c.setParentId(rs.getObject("parent_id", Integer.class));
        c.setNome(rs.getString("nome"));
        c.setIdTributacaoPadrao(rs.getObject("id_tributacao_padrao", Integer.class));
        c.setAtivo(rs.getBoolean("ativo"));
        java.sql.Timestamp cadastro = rs.getTimestamp("data_cadastro");
        if (cadastro != null) c.setDataCadastro(cadastro.toLocalDateTime());
        return c;
    }

    @Override
    protected String getSqlInsert() {
        return "INSERT INTO categorias (parent_id, nome, id_tributacao_padrao, ativo) VALUES (?, ?, ?, ?)";
    }

    @Override
    protected String getSqlUpdate() {
        return "UPDATE categorias SET parent_id = ?, nome = ?, id_tributacao_padrao = ?, ativo = ? WHERE id_categoria = ?";
    }

    @Override
    protected void setParametrosInsert(PreparedStatement stmt, Categoria c) throws SQLException {
        stmt.setObject(1, c.getParentId());
        stmt.setString(2, c.getNome());
        stmt.setObject(3, c.getIdTributacaoPadrao());
        stmt.setBoolean(4, c.isAtivo());
    }

    @Override
    protected void setParametrosUpdate(PreparedStatement stmt, Categoria c) throws SQLException {
        stmt.setObject(1, c.getParentId());
        stmt.setString(2, c.getNome());
        stmt.setObject(3, c.getIdTributacaoPadrao());
        stmt.setBoolean(4, c.isAtivo());
        stmt.setInt(5, c.getIdCategoria());
    }
}
