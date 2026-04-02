package main.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.stereotype.Component;

import main.models.Funcionario;

@Component
public class FuncionarioDAO extends GenericDAO<Funcionario, Integer> {

    @Override
    protected String getTabela() {
        return "funcionarios";
    }

    @Override
    protected String getColunaId() {
        return "id_funcionario";
    }

    @Override
    protected void setIdGerado(Funcionario f, ResultSet keys) throws SQLException {
        f.setIdFuncionario(keys.getInt(1));
    }

    @Override
    protected String getSqlInsert() {
        return """
                INSERT INTO funcionarios
                (nome, cpf, cargo, data_admissao, data_demissao, telefone, email, ativo, data_cadastro, data_atualizacao)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
    }

    @Override
    protected void setParametrosInsert(PreparedStatement stmt, Funcionario f) throws SQLException {
        stmt.setString(1, f.getNome());
        stmt.setString(2, f.getCpf());
        stmt.setString(3, f.getCargo());
        stmt.setObject(4, f.getDataAdmissao());
        stmt.setObject(5, f.getDataDemissao());
        stmt.setString(6, f.getTelefone());
        stmt.setString(7, f.getEmail());
        stmt.setBoolean(8, f.getAtivo() != null ? f.getAtivo() : true);
        stmt.setObject(9, f.getDataCadastro());
        stmt.setObject(10, f.getDataAtualizacao());
    }
    
    @Override
    protected void setParametrosUpdate(PreparedStatement stmt, Funcionario f) throws SQLException {
        stmt.setString(1, f.getNome());
        stmt.setString(2, f.getCpf());
        stmt.setString(3, f.getCargo());
        stmt.setObject(4, f.getDataAdmissao());
        stmt.setObject(5, f.getDataDemissao());
        stmt.setString(6, f.getTelefone());
        stmt.setString(7, f.getEmail());
        stmt.setBoolean(8, f.getAtivo() != null ? f.getAtivo() : true);
        stmt.setObject(9, f.getDataAtualizacao());
        stmt.setInt(10, f.getIdFuncionario());
    }

    @Override
    protected String getSqlUpdate() {
        return """
                UPDATE funcionarios SET
                nome = ?, cpf = ?, cargo = ?, data_admissao = ?, data_demissao = ?,
                telefone = ?, email = ?, ativo = ?, data_atualizacao = ?
                WHERE id_funcionario = ?
                """;
    }

    @Override
    protected Funcionario mapear(ResultSet rs) throws SQLException {
        Funcionario f = new Funcionario();
        f.setIdFuncionario(rs.getInt("id_funcionario"));
        f.setNome(rs.getString("nome"));
        f.setCpf(rs.getString("cpf"));
        f.setCargo(rs.getString("cargo"));
        java.sql.Date sqlAdmissao = (java.sql.Date) rs.getObject("data_admissao");
        f.setDataAdmissao(sqlAdmissao != null ? sqlAdmissao.toLocalDate() : null);
        java.sql.Date sqlDemissao = (java.sql.Date) rs.getObject("data_demissao");
        f.setDataDemissao(sqlDemissao != null ? sqlDemissao.toLocalDate() : null);
        f.setTelefone(rs.getString("telefone"));
        f.setEmail(rs.getString("email"));
        f.setAtivo(rs.getBoolean("ativo"));
        java.sql.Timestamp tsCadastro = (java.sql.Timestamp) rs.getObject("data_cadastro");
        f.setDataCadastro(tsCadastro != null ? tsCadastro.toLocalDateTime() : null);
        java.sql.Timestamp tsAtualizacao = (java.sql.Timestamp) rs.getObject("data_atualizacao");
        f.setDataAtualizacao(tsAtualizacao != null ? tsAtualizacao.toLocalDateTime() : null);
        return f;
    }
}
