package main.database.DAOs;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.stereotype.Component;

import main.database.GenericDAO;
import main.models.Endereco;

@Component
public class EnderecoDAO extends GenericDAO<Endereco, Integer> {

    public static java.util.List<Endereco> findAllStatic() {
        return GenericDAO.findAllStatic(EnderecoDAO.class);
    }

    public static Endereco findByIdStatic(Integer id) {
        return GenericDAO.findByIdStatic(EnderecoDAO.class, id);
    }

    public static boolean deleteByIdStatic(Integer id) {
        return GenericDAO.deleteByIdStatic(EnderecoDAO.class, id);
    }

    public static Endereco insertStatic(Endereco endereco) {
        return GenericDAO.insertStatic(EnderecoDAO.class, endereco);
    }

    public static boolean updateStatic(Endereco endereco) {
        return GenericDAO.updateStatic(EnderecoDAO.class, endereco);
    }

    @Override
    protected String getTabela() {
        return "enderecos";
    }

    @Override
    protected String getColunaId() {
        return "id_endereco";
    }

    @Override
    protected void setGeneratedId(Endereco e, Number id) {
        e.setIdEndereco(id.intValue());
    }

    @Override
    protected String getSqlInsert() {
        return """
                INSERT INTO enderecos
                (logradouro, numero, complemento, bairro, cidade, estado, cep, data_cadastro)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
    }

    @Override
    protected void setParametrosInsert(PreparedStatement stmt, Endereco e) throws SQLException {
        stmt.setString(1, e.getLogradouro());
        stmt.setString(2, e.getNumero());
        stmt.setString(3, e.getComplemento());
        stmt.setString(4, e.getBairro());
        stmt.setString(5, e.getCidade());
        stmt.setString(6, e.getEstado());
        stmt.setString(7, e.getCep());
        stmt.setObject(8, e.getDataCadastro());
    }

    @Override
    protected void setParametrosUpdate(PreparedStatement stmt, Endereco e) throws SQLException {
        stmt.setString(1, e.getLogradouro());
        stmt.setString(2, e.getNumero());
        stmt.setString(3, e.getComplemento());
        stmt.setString(4, e.getBairro());
        stmt.setString(5, e.getCidade());
        stmt.setString(6, e.getEstado());
        stmt.setString(7, e.getCep());
        stmt.setInt(8, e.getIdEndereco());
    }

    @Override
    protected String getSqlUpdate() {
        return """
                UPDATE enderecos SET
                logradouro = ?, numero = ?, complemento = ?, bairro = ?,
                cidade = ?, estado = ?, cep = ?
                WHERE id_endereco = ?
                """;
    }

    @Override
    protected Endereco mapear(ResultSet rs) throws SQLException {
        Endereco e = new Endereco();
        e.setIdEndereco(rs.getInt("id_endereco"));
        e.setLogradouro(rs.getString("logradouro"));
        e.setNumero(rs.getString("numero"));
        e.setComplemento(rs.getString("complemento"));
        e.setBairro(rs.getString("bairro"));
        e.setCidade(rs.getString("cidade"));
        e.setEstado(rs.getString("estado"));
        e.setCep(rs.getString("cep"));
        java.sql.Timestamp tsCadastro = (java.sql.Timestamp) rs.getObject("data_cadastro");
        e.setDataCadastro(tsCadastro != null ? tsCadastro.toLocalDateTime() : null);
        return e;
    }
}
