package main.database.DAOs;

import org.springframework.stereotype.Component;

import main.database.GenericDAO;
import main.models.Fornecedor;
import main.util.SpringContextProvider;

@Component
public class FornecedorDAO extends GenericDAO<Fornecedor, Integer> {

    public static java.util.List<Fornecedor> findAllStatic() {
        try {
            return SpringContextProvider.getBean(FornecedorDAO.class).findAll();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Fornecedor findByIdStatic(Integer id) {
        try {
            return SpringContextProvider.getBean(FornecedorDAO.class).findById(id).orElse(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Fornecedor insertStatic(Fornecedor fornecedor) {
        try {
            return SpringContextProvider.getBean(FornecedorDAO.class).save(fornecedor);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean updateStatic(Fornecedor fornecedor) {
        try {
            return SpringContextProvider.getBean(FornecedorDAO.class).update(fornecedor);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean deleteByIdStatic(Integer id) {
        try {
            return SpringContextProvider.getBean(FornecedorDAO.class).deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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
        stmt.setString(4, null); // inscricao_estadual não disponível no model atual
        stmt.setString(5, f.getTelefone());
        stmt.setString(6, f.getEMail());
        stmt.setString(7, null); // logradouro
        stmt.setString(8, null); // numero
        stmt.setString(9, null); // complemento
        stmt.setString(10, null); // bairro
        stmt.setString(11, null); // cidade
        stmt.setString(12, null); // uf
        stmt.setString(13, null); // cep
    }

    @Override
    protected void setParametrosUpdate(java.sql.PreparedStatement stmt, Fornecedor f) throws java.sql.SQLException {
        stmt.setString(1, f.getRazaoSocial());
        stmt.setString(2, f.getNomeFantasia());
        stmt.setString(3, f.getCnpj());
        stmt.setString(4, null); // inscricao_estadual
        stmt.setString(5, f.getTelefone());
        stmt.setString(6, f.getEMail());
        stmt.setString(7, null); // logradouro
        stmt.setString(8, null); // numero
        stmt.setString(9, null); // complemento
        stmt.setString(10, null); // bairro
        stmt.setString(11, null); // cidade
        stmt.setString(12, null); // uf
        stmt.setString(13, null); // cep
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
        f.setTelefone(rs.getString("telefone"));
        f.setEMail(rs.getString("email"));

        // Não temos campos de endereço no modelo Fornecedor atual, apenas idEndereco
        f.setIdEndereco(rs.getObject("id_endereco", Integer.class));

        return f;
    }
}
