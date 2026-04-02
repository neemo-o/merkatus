package main.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import main.models.Empresa;
import main.models.Endereco;
import main.models.Fornecedor;

@Component
public abstract class FornecedorDAO extends GenericDAO<Fornecedor, Integer> {

    @Autowired
    protected Endereco en;
    protected Empresa e;

    @Override
    protected String getTabela() {
        return "fornecedor";
    }

    @Override
    protected String getColunaId() {
        return "id_fornecedor";
    }

    @Override
    protected void setIdGerado(Fornecedor f, java.sql.ResultSet keys) throws java.sql.SQLException {
        f.setIdFornecedor(keys.getInt(1));
    }

    @Override
    protected String getSqlInsert() {
        return """
                (razao_social, nome_fantasia, cnpj, inscricao_estadual, telefone, email,
                 logradouro, numero, complemento, bairro, cidade, uf, cep)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)
                """;
    }

    @Override
    protected void setParametrosInsert(java.sql.PreparedStatement stmt, Fornecedor f) throws java.sql.SQLException {

        stmt.setString(1, f.getRazaoSocial());
        stmt.setString(2, f.getNomeFantasia());
        stmt.setString(3, f.getCnpj());
        stmt.setString(4, e.getInscricaoEstadual());
        stmt.setString(5, f.getTelefone());
        stmt.setString(6, f.getEMail());
        stmt.setString(7, en.getLogradouro());
        stmt.setString(8, en.getNumero());
        stmt.setString(9, en.getComplemento());
        stmt.setString(10, en.getBairro());
        stmt.setString(11, en.getCidade());
        stmt.setString(12, e.getUf());
        stmt.setString(13, en.getCep());
    }

    @Override
    protected void setParametrosUpdate(java.sql.PreparedStatement stmt, Fornecedor f) throws java.sql.SQLException {
        stmt.setString(1, f.getRazaoSocial());
        stmt.setString(2, f.getNomeFantasia());
        stmt.setString(3, f.getCnpj());
        stmt.setString(4, e.getInscricaoEstadual());
        stmt.setString(5, f.getTelefone());
        stmt.setString(6, f.getEMail());
        stmt.setString(7, en.getLogradouro());
        stmt.setString(8, en.getNumero());
        stmt.setString(9, en.getComplemento());
        stmt.setString(10, en.getBairro());
        stmt.setString(11, en.getCidade());
        stmt.setString(12, e.getUf());
        stmt.setString(13, en.getCep());
        stmt.setInt(14, f.getIdFornecedor()); // WHERE id_fornecedor = ?
    }

    @Override
    protected String getSqlUpdate() {
        return """
                SET razao_social = ?, nome_fantasia = ?, cnpj = ?, inscricao_estadual = ?,
                    telefone = ?, email = ?, logradouro = ?, numero = ?, complemento = ?,
                    bairro = ?, cidade = ?, uf = ?, cep = ?
                WHERE id_fornecedor = ?
                """;
    }

    @Override
    protected Fornecedor mapear(java.sql.ResultSet rs) throws java.sql.SQLException {
        Fornecedor f = new Fornecedor();
        f.setIdFornecedor(rs.getInt("id_fornecedor"));
        f.setRazaoSocial(rs.getString("razao_social"));
        f.setNomeFantasia(rs.getString("nome_fantasia"));
        f.setCnpj(rs.getString("cnpj"));
        e.setInscricaoEstadual("inscricaoEstadual");
        f.setTelefone(rs.getString("telefone"));
        f.setEMail(rs.getString("email"));
        en.setLogradouro(rs.getString("logradouro"));
        en.setNumero(rs.getString("numero"));
        en.setComplemento(rs.getString("complemento"));
        en.setBairro(rs.getString("bairro"));
        en.setCidade(rs.getString("cidade"));
        e.setUf(rs.getString("uf"));
        en.setCep(rs.getString("cep"));
        
        return f;
    }
}
