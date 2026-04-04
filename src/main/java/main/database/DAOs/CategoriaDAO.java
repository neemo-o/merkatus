package main.database.DAOs;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.stereotype.Component;

import main.database.GenericDAO;
import main.models.Categoria;

@Component
public class CategoriaDAO extends GenericDAO<Categoria, Integer> {

    public static java.util.List<Categoria> findAllStatic() {
        return GenericDAO.findAllStatic(CategoriaDAO.class);
    }

    public static Categoria findByIdStatic(Integer id) {
        return GenericDAO.findByIdStatic(CategoriaDAO.class, id);
    }

    public static Categoria insertStatic(Categoria categoria) {
        return GenericDAO.insertStatic(CategoriaDAO.class, categoria);
    }

    public static boolean updateStatic(Categoria categoria) {
        return GenericDAO.updateStatic(CategoriaDAO.class, categoria);
    }

    public static boolean deleteByIdStatic(Integer id) {
        return GenericDAO.deleteByIdStatic(CategoriaDAO.class, id);
    }

    @Override
    protected String getTabela() {
        return "categorias";
    }

    @Override
    protected String getColunaId() {
        return "id_categoria";
    }

    @Override
    protected void setIdGerado(Categoria c, ResultSet keys) throws SQLException {
        c.setIdCategoria(keys.getInt(1));
    }

    @Override
    protected String getSqlInsert() {
        return """
                INSERT INTO categorias
                (parent_id, nome, ativo, data_cadastro)
                VALUES (?, ?, ?, ?)
                """;
    }

    @Override
    protected void setParametrosInsert(PreparedStatement stmt, Categoria c) throws SQLException {
        stmt.setObject(1, c.getParentId());
        stmt.setString(2, c.getNome());
        stmt.setObject(3, c.getAtivo());
        stmt.setObject(4, c.getDataCadastro());
    }

    @Override
    protected void setParametrosUpdate(PreparedStatement stmt, Categoria c) throws SQLException {
        stmt.setObject(1, c.getParentId());
        stmt.setString(2, c.getNome());
        stmt.setObject(3, c.getAtivo());
        stmt.setObject(4, c.getIdCategoria());
    }

    @Override
    protected String getSqlUpdate() {
        return """
                UPDATE categorias SET
                    parent_id = ?,
                    nome = ?,
                    ativo = ?
                WHERE id_categoria = ?
                """;
    }

    @Override
    protected Categoria mapear(ResultSet rs) throws SQLException {
        Categoria c = new Categoria();
        c.setIdCategoria(rs.getInt("id_categoria"));
        c.setParentId(rs.getObject("parent_id", Integer.class));
        c.setNome(rs.getString("nome"));
        c.setAtivo(rs.getBoolean("ativo"));
        java.sql.Timestamp ts = rs.getTimestamp("data_cadastro");
        c.setDataCadastro(ts != null ? ts.toLocalDateTime() : null);
        return c;
    }
}

