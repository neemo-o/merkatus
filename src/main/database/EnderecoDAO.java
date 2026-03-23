package main.database;

import main.models.Endereco;
import java.sql.*;

public class EnderecoDAO {

    // Método para inserir novo endereço
    public boolean inserir(Endereco endereco) throws SQLException {
        String sql = "INSERT INTO enderecos (logradouro, numero, complemento, bairro, cidade, estado, cep) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnectionMercado();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, endereco.getLogradouro());
            stmt.setString(2, endereco.getNumero());
            stmt.setString(3, endereco.getComplemento());
            stmt.setString(4, endereco.getBairro());
            stmt.setString(5, endereco.getCidade());
            stmt.setString(6, endereco.getEstado());
            stmt.setString(7, formatarCep(endereco.getCep()));

            int linhasAfetadas = stmt.executeUpdate();

            if (linhasAfetadas > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        endereco.setIdEndereco(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    // Método para buscar endereço por ID
    public Endereco buscarPorId(int idEndereco) throws SQLException {
        String sql = "SELECT * FROM enderecos WHERE id_endereco = ?";

        try (Connection conn = DatabaseConnection.getConnectionMercado();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEndereco);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Endereco endereco = new Endereco();
                    endereco.setIdEndereco(rs.getInt("id_endereco"));
                    endereco.setLogradouro(rs.getString("logradouro"));
                    endereco.setNumero(rs.getString("numero"));
                    endereco.setComplemento(rs.getString("complemento"));
                    endereco.setBairro(rs.getString("bairro"));
                    endereco.setCidade(rs.getString("cidade"));
                    endereco.setEstado(rs.getString("estado"));
                    endereco.setCep(rs.getString("cep"));
                    endereco.setDataCadastro(rs.getTimestamp("data_cadastro"));

                    return endereco;
                }
            }
        }
        return null;
    }

    // Método para atualizar endereço
    public boolean atualizar(Endereco endereco) throws SQLException {
        String sql = "UPDATE enderecos SET logradouro = ?, numero = ?, complemento = ?, " +
                    "bairro = ?, cidade = ?, estado = ?, cep = ? WHERE id_endereco = ?";

        try (Connection conn = DatabaseConnection.getConnectionMercado();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, endereco.getLogradouro());
            stmt.setString(2, endereco.getNumero());
            stmt.setString(3, endereco.getComplemento());
            stmt.setString(4, endereco.getBairro());
            stmt.setString(5, endereco.getCidade());
            stmt.setString(6, endereco.getEstado());
            stmt.setString(7, formatarCep(endereco.getCep()));
            stmt.setInt(8, endereco.getIdEndereco());

            return stmt.executeUpdate() > 0;
        }
    }

    // Método para excluir endereço
    public boolean excluir(int idEndereco) throws SQLException {
        String sql = "DELETE FROM enderecos WHERE id_endereco = ?";

        try (Connection conn = DatabaseConnection.getConnectionMercado();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEndereco);
            return stmt.executeUpdate() > 0;
        }
    }

    // Método para formatar CEP com traço
    private String formatarCep(String cep) {
        if (cep != null && cep.length() == 8) {
            return cep.substring(0, 5) + "-" + cep.substring(5);
        }
        return cep;
    }
}
