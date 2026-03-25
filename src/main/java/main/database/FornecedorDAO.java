package main.database;

import main.models.Fornecedor;
import main.models.Endereco;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FornecedorDAO {

    private EnderecoDAO enderecoDAO = new EnderecoDAO();

    // Método para buscar todos os fornecedores
    public List<Fornecedor> buscarTodos() throws SQLException {
        List<Fornecedor> fornecedores = new ArrayList<>();
        String sql = "SELECT f.id_fornecedor, f.cnpj, f.razao_social, f.telefone, f.e_mail, " +
                    "f.data_cadastro, f.data_atualizacao, " +
                    "e.id_endereco, e.logradouro, e.numero, e.complemento, e.bairro, e.cidade, e.estado, e.cep " +
                    "FROM fornecedor f " +
                    "INNER JOIN enderecos e ON f.id_endereco = e.id_endereco " +
                    "ORDER BY f.razao_social";

        try (Connection conn = DatabaseConnection.getConnectionMercado();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Fornecedor fornecedor = new Fornecedor();
                fornecedor.setIdFornecedor(rs.getInt("id_fornecedor"));
                fornecedor.setCnpj(rs.getString("cnpj"));
                fornecedor.setRazaoSocial(rs.getString("razao_social"));
                fornecedor.setTelefone(rs.getString("telefone"));
                fornecedor.setEmail(rs.getString("e_mail"));
                fornecedor.setDataCadastro(rs.getTimestamp("data_cadastro") != null ?
                    rs.getTimestamp("data_cadastro").toLocalDateTime() : null);
                fornecedor.setDataAtualizacao(rs.getTimestamp("data_atualizacao") != null ?
                    rs.getTimestamp("data_atualizacao").toLocalDateTime() : null);

                Endereco endereco = new Endereco();
                endereco.setIdEndereco(rs.getInt("id_endereco"));
                endereco.setLogradouro(rs.getString("logradouro"));
                endereco.setNumero(rs.getString("numero"));
                endereco.setComplemento(rs.getString("complemento"));
                endereco.setBairro(rs.getString("bairro"));
                endereco.setCidade(rs.getString("cidade"));
                endereco.setEstado(rs.getString("estado"));
                endereco.setCep(rs.getString("cep"));

                fornecedor.setEndereco(endereco);

                fornecedores.add(fornecedor);
            }
        }
        return fornecedores;
    }

    // Método para buscar fornecedor por ID
    public Fornecedor buscarPorId(int idFornecedor) throws SQLException {
        String sql = "SELECT f.id_fornecedor, f.cnpj, f.razao_social, f.telefone, f.e_mail, " +
                    "f.data_cadastro, f.data_atualizacao, " +
                    "e.id_endereco, e.logradouro, e.numero, e.complemento, e.bairro, e.cidade, e.estado, e.cep " +
                    "FROM fornecedor f " +
                    "INNER JOIN enderecos e ON f.id_endereco = e.id_endereco " +
                    "WHERE f.id_fornecedor = ?";

        try (Connection conn = DatabaseConnection.getConnectionMercado();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idFornecedor);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Fornecedor fornecedor = new Fornecedor();
                    fornecedor.setIdFornecedor(rs.getInt("id_fornecedor"));
                    fornecedor.setCnpj(rs.getString("cnpj"));
                    fornecedor.setRazaoSocial(rs.getString("razao_social"));
                    fornecedor.setTelefone(rs.getString("telefone"));
                    fornecedor.setEmail(rs.getString("e_mail"));
                    fornecedor.setDataCadastro(rs.getTimestamp("data_cadastro") != null ?
                        rs.getTimestamp("data_cadastro").toLocalDateTime() : null);
                    fornecedor.setDataAtualizacao(rs.getTimestamp("data_atualizacao") != null ?
                        rs.getTimestamp("data_atualizacao").toLocalDateTime() : null);

                    Endereco endereco = new Endereco();
                    endereco.setIdEndereco(rs.getInt("id_endereco"));
                    endereco.setLogradouro(rs.getString("logradouro"));
                    endereco.setNumero(rs.getString("numero"));
                    endereco.setComplemento(rs.getString("complemento"));
                    endereco.setBairro(rs.getString("bairro"));
                    endereco.setCidade(rs.getString("cidade"));
                    endereco.setEstado(rs.getString("estado"));
                    endereco.setCep(rs.getString("cep"));

                    fornecedor.setEndereco(endereco);

                    return fornecedor;
                }
            }
        }
        return null;
    }

    // Método para buscar fornecedores por razão social (para busca)
    public List<Fornecedor> buscarPorRazaoSocial(String razaoSocial) throws SQLException {
        List<Fornecedor> fornecedores = new ArrayList<>();
        String sql = "SELECT f.id_fornecedor, f.cnpj, f.razao_social, f.telefone, f.e_mail, " +
                    "f.data_cadastro, f.data_atualizacao, " +
                    "e.id_endereco, e.logradouro, e.numero, e.complemento, e.bairro, e.cidade, e.estado, e.cep " +
                    "FROM fornecedor f " +
                    "INNER JOIN enderecos e ON f.id_endereco = e.id_endereco " +
                    "WHERE UPPER(f.razao_social) LIKE UPPER(?) ORDER BY f.razao_social";

        try (Connection conn = DatabaseConnection.getConnectionMercado();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + razaoSocial + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Fornecedor fornecedor = new Fornecedor();
                    fornecedor.setIdFornecedor(rs.getInt("id_fornecedor"));
                    fornecedor.setCnpj(rs.getString("cnpj"));
                    fornecedor.setRazaoSocial(rs.getString("razao_social"));
                    fornecedor.setTelefone(rs.getString("telefone"));
                    fornecedor.setEmail(rs.getString("e_mail"));
                    fornecedor.setDataCadastro(rs.getTimestamp("data_cadastro") != null ?
                        rs.getTimestamp("data_cadastro").toLocalDateTime() : null);
                    fornecedor.setDataAtualizacao(rs.getTimestamp("data_atualizacao") != null ?
                        rs.getTimestamp("data_atualizacao").toLocalDateTime() : null);

                    Endereco endereco = new Endereco();
                    endereco.setIdEndereco(rs.getInt("id_endereco"));
                    endereco.setLogradouro(rs.getString("logradouro"));
                    endereco.setNumero(rs.getString("numero"));
                    endereco.setComplemento(rs.getString("complemento"));
                    endereco.setBairro(rs.getString("bairro"));
                    endereco.setCidade(rs.getString("cidade"));
                    endereco.setEstado(rs.getString("estado"));
                    endereco.setCep(rs.getString("cep"));

                    fornecedor.setEndereco(endereco);

                    fornecedores.add(fornecedor);
                }
            }
        }
        return fornecedores;
    }

    // Método para inserir fornecedor
    public boolean inserir(Fornecedor fornecedor) throws SQLException {
        if (fornecedor.getEndereco() == null) {
            throw new SQLException("Endereço é obrigatório para inserir fornecedor");
        }

        // Inserir endereço primeiro
        boolean enderecoInserido = enderecoDAO.inserir(fornecedor.getEndereco());
        if (!enderecoInserido) {
            return false;
        }

        int idEndereco = fornecedor.getEndereco().getIdEndereco();

        String sql = "INSERT INTO fornecedor (cnpj, razao_social, telefone, e_mail, id_endereco) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnectionMercado();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, fornecedor.getCnpj());
            stmt.setString(2, fornecedor.getRazaoSocial());
            stmt.setString(3, fornecedor.getTelefone());
            stmt.setString(4, fornecedor.getEmail());
            stmt.setInt(5, idEndereco);

            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;
        }
    }

    // Método para atualizar fornecedor
    public boolean atualizar(Fornecedor fornecedor) throws SQLException {
        if (fornecedor.getEndereco() == null || fornecedor.getEndereco().getIdEndereco() == 0) {
            throw new SQLException("Endereço inválido para atualizar fornecedor");
        }

        // Atualizar endereço
        boolean enderecoAtualizado = enderecoDAO.atualizar(fornecedor.getEndereco());
        if (!enderecoAtualizado) {
            return false;
        }

        String sql = "UPDATE fornecedor SET cnpj = ?, razao_social = ?, telefone = ?, e_mail = ? WHERE id_fornecedor = ?";

        try (Connection conn = DatabaseConnection.getConnectionMercado();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, fornecedor.getCnpj());
            stmt.setString(2, fornecedor.getRazaoSocial());
            stmt.setString(3, fornecedor.getTelefone());
            stmt.setString(4, fornecedor.getEmail());
            stmt.setInt(5, fornecedor.getIdFornecedor());

            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;
        }
    }

    // Método para excluir fornecedor
    public boolean excluir(int idFornecedor) throws SQLException {
        String sql = "DELETE FROM fornecedor WHERE id_fornecedor = ?";

        try (Connection conn = DatabaseConnection.getConnectionMercado();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idFornecedor);

            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;
        }
    }

    // Método para contar total de fornecedores
    public int contarTotal() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM fornecedor";

        try (Connection conn = DatabaseConnection.getConnectionMercado();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    // Método para verificar se CNPJ já existe
    public boolean cnpjExiste(String cnpj, Integer idExcluir) throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM fornecedor WHERE cnpj = ?";
        if (idExcluir != null) {
            sql += " AND id_fornecedor != ?";
        }

        try (Connection conn = DatabaseConnection.getConnectionMercado();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cnpj);
            if (idExcluir != null) {
                stmt.setInt(2, idExcluir);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total") > 0;
                }
            }
        }
        return false;
    }

    // Classe interna para compatibilidade com código existente
    public static class FornecedorCompatibilidade {
        private int idFornecedor;
        private String razaoSocial;

        public FornecedorCompatibilidade(int idFornecedor, String razaoSocial) {
            this.idFornecedor = idFornecedor;
            this.razaoSocial = razaoSocial;
        }

        public int getIdFornecedor() {
            return idFornecedor;
        }

        public String getRazaoSocial() {
            return razaoSocial;
        }

        @Override
        public String toString() {
            return razaoSocial;
        }
    }

    // Método para compatibilidade com código existente
    public List<FornecedorCompatibilidade> buscarTodosCompatibilidade() throws SQLException {
        List<FornecedorCompatibilidade> fornecedores = new ArrayList<>();
        String sql = "SELECT id_fornecedor, razao_social FROM fornecedor ORDER BY razao_social";

        try (Connection conn = DatabaseConnection.getConnectionMercado();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                FornecedorCompatibilidade fornecedor = new FornecedorCompatibilidade(
                    rs.getInt("id_fornecedor"),
                    rs.getString("razao_social")
                );
                fornecedores.add(fornecedor);
            }
        }
        return fornecedores;
    }
}
