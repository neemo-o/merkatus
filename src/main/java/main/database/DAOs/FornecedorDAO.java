package main.database.DAOs;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.stereotype.Component;

import main.database.GenericDAO;
import main.models.Fornecedor;

@Component
public class FornecedorDAO extends GenericDAO<Fornecedor, Integer> {

    @Override
    protected String getTabela() {
        return "fornecedor";
    }

    @Override
    protected String getColunaId() {
        return "id_fornecedor";
    }

    @Override
    protected void setGeneratedId(Fornecedor f, Number id) {
        f.setIdFornecedor(id.intValue());
    }

    @Override
    protected String getSqlInsert() {
        return """
                INSERT INTO fornecedor (razao_social, nome_fantasia, cnpj, telefone, e_mail, id_endereco, ativo, data_cadastro, data_atualizacao)
                VALUES (?,?,?,?,?,?,?,?,?)
                """;
    }

    @Override
    protected void setParametrosInsert(PreparedStatement stmt, Fornecedor f) throws SQLException {
        stmt.setString(1, f.getRazaoSocial());
        stmt.setString(2, f.getNomeFantasia());
        stmt.setString(3, f.getCnpj());
        stmt.setString(4, f.getTelefone());
        stmt.setString(5, f.getEMail());
        stmt.setObject(6, f.getIdEndereco());
        stmt.setBoolean(7, f.getAtivo() != null ? f.getAtivo() : false);
        stmt.setObject(8, f.getDataCadastro());
        stmt.setObject(9, f.getDataAtualizacao());
    }

    @Override
    protected void setParametrosUpdate(PreparedStatement stmt, Fornecedor f) throws SQLException {
        stmt.setString(1, f.getRazaoSocial());
        stmt.setString(2, f.getNomeFantasia());
        stmt.setString(3, f.getCnpj());
        stmt.setString(4, f.getTelefone());
        stmt.setString(5, f.getEMail());
        stmt.setObject(6, f.getIdEndereco());
        stmt.setBoolean(7, f.getAtivo() != null ? f.getAtivo() : false);
        stmt.setObject(8, f.getDataAtualizacao());
        stmt.setInt(9, f.getIdFornecedor());
    }

    @Override
    protected String getSqlUpdate() {
        return """
                UPDATE fornecedor SET razao_social = ?, nome_fantasia = ?, cnpj = ?, telefone = ?, e_mail = ?, id_endereco = ?, ativo = ?, data_atualizacao = ?
                WHERE id_fornecedor = ?
                """;
    }

    @Override
    protected Fornecedor mapear(ResultSet rs) throws SQLException {
        Fornecedor f = new Fornecedor();
        f.setIdFornecedor(rs.getInt("id_fornecedor"));
        f.setRazaoSocial(rs.getString("razao_social"));
        f.setNomeFantasia(rs.getString("nome_fantasia"));
        f.setCnpj(rs.getString("cnpj"));
        f.setTelefone(rs.getString("telefone"));
        f.setEMail(rs.getString("e_mail"));

        f.setIdEndereco(rs.getObject("id_endereco", Integer.class));
        f.setAtivo(rs.getBoolean("ativo"));
        java.sql.Timestamp tsCadastro = rs.getTimestamp("data_cadastro");
        if (tsCadastro != null) f.setDataCadastro(tsCadastro.toLocalDateTime());
        java.sql.Timestamp tsAtualizacao = rs.getTimestamp("data_atualizacao");
        if (tsAtualizacao != null) f.setDataAtualizacao(tsAtualizacao.toLocalDateTime());

        return f;
    }
}
