package main.database.DAOs;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.stereotype.Component;

import main.database.GenericDAO;
import main.models.Cliente;

@Component
public abstract class ClienteDAO extends GenericDAO<Cliente, Integer> {

    @Override
    protected String getTabela() {
        return "clientes";
    }

    @Override
    protected String getColunaId() {
        return "id_cliente";
    }

    
    @Override
    protected void setIdGerado(Cliente c, ResultSet keys) throws SQLException {
        c.setIdCliente(keys.getInt(1));
    }

    @Override
    protected String getSqlInsert() {
        return """
                INSERT INTO clientes
                (cnpj, razao_social, nome_fantasia, inscricao_estadual, email_cliente,
                 telefone_cliente, id_endereco_cliente, status_cliente, data_nascimento,
                 limite_credito, ativo, data_cadastro, data_atualizacao)
                """;
    }

    @Override
    protected void setParametrosInsert(PreparedStatement stmt, Cliente c) throws SQLException {
        stmt.setString(1, c.getCnpj());
        stmt.setString(2, c.getRazaoSocial());
        stmt.setString(3, c.getNomeFantasia());
        stmt.setString(4, c.getInscricaoEstadual());
        stmt.setString(5, c.getEmailCliente());
        stmt.setString(6, c.getTelefoneCliente());
        stmt.setObject(7, c.getIdEnderecoCliente()); 
        stmt.setString(8, c.getStatusCliente());
        stmt.setObject(9, c.getDataNascimento()); 
        stmt.setObject(10, c.getLimiteCredito()); 
        stmt.setBoolean(11, c.getAtivo() != null ? c.getAtivo() : false); 
        stmt.setObject(12, c.getDataCadastro()); 
        stmt.setObject(13, c.getDataAtualizacao()); 
    }
    
    @Override
    protected void setParametrosUpdate(PreparedStatement stmt, Cliente c) throws SQLException {
        stmt.setString(1, c.getCnpj());
        stmt.setString(2, c.getRazaoSocial());
        stmt.setString(3, c.getNomeFantasia());
        stmt.setString(4, c.getInscricaoEstadual());
        stmt.setString(5, c.getEmailCliente());
        stmt.setString(6, c.getTelefoneCliente());
        stmt.setObject(7, c.getIdEnderecoCliente()); 
        stmt.setString(8, c.getStatusCliente());
        stmt.setObject(9, c.getDataNascimento()); 
        stmt.setObject(10, c.getLimiteCredito()); 
        stmt.setBoolean(11, c.getAtivo() != null ? c.getAtivo() : false); 
        stmt.setObject(12, c.getDataAtualizacao()); 
        stmt.setInt(13, c.getIdCliente());
    }

    @Override
    protected String getSqlUpdate() {
        return """
                UPDATE clientes SET
                cnpj = ?, razao_social = ?, nome_fantasia = ?, inscricao_estadual = ?,
                email_cliente = ?, telefone_cliente = ?, id_endereco_cliente = ?,
                status_cliente = ?, data_nascimento = ?, limite_credito = ?,
                ativo = ?, data_atualizacao = ?
                WHERE id_cliente = ?
                """;
    }

    @Override
    protected Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setIdCliente(rs.getInt("id_cliente"));
        c.setCnpj(rs.getString("cnpj"));
        c.setRazaoSocial(rs.getString("razao_social"));
        c.setNomeFantasia(rs.getString("nome_fantasia"));
        c.setInscricaoEstadual(rs.getString("inscricao_estadual"));
        c.setEmailCliente(rs.getString("email_cliente"));
        c.setTelefoneCliente(rs.getString("telefone_cliente"));
        c.setIdEnderecoCliente((Integer) rs.getObject("id_endereco_cliente")); 
        c.setStatusCliente(rs.getString("status_cliente"));
        java.sql.Date sqlDate = (java.sql.Date) rs.getObject("data_nascimento");
        c.setDataNascimento(sqlDate != null ? sqlDate.toLocalDate() : null);
        c.setLimiteCredito((java.math.BigDecimal) rs.getObject("limite_credito")); 
        c.setAtivo(rs.getBoolean("ativo"));
        java.sql.Timestamp tsCadastro = (java.sql.Timestamp) rs.getObject("data_cadastro");
        c.setDataCadastro(tsCadastro != null ? tsCadastro.toLocalDateTime() : null);
        java.sql.Timestamp tsAtualizacao = (java.sql.Timestamp) rs.getObject("data_atualizacao");
        c.setDataAtualizacao(tsAtualizacao != null ? tsAtualizacao.toLocalDateTime() : null);
        return c;
    }
}
