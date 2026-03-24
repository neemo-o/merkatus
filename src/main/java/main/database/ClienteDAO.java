package main.database;

import main.models.Cliente;
import main.models.Endereco;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    // Método para buscar todos os clientes
    public List<Cliente> buscarTodos() throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT c.*, e.logradouro, e.numero, e.complemento, e.bairro, e.cidade, e.estado, e.cep " +
                "FROM clientes c LEFT JOIN enderecos e ON c.id_endereco_cliente = e.id_endereco " +
                "ORDER BY c.razao_social";

        try (Connection conn = DatabaseConnection.getConnectionMercado();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Cliente cliente = new Cliente();
                cliente.setCnpj(rs.getString("cnpj"));
                cliente.setRazaoSocial(rs.getString("razao_social"));
                cliente.setNomeFantasia(rs.getString("nome_fantasia"));
                cliente.setInscricaoEstadual(rs.getString("inscricao_estadual"));
                cliente.setEmailCliente(rs.getString("email_cliente"));
                cliente.setTelefoneCliente(rs.getString("telefone_cliente"));
                cliente.setIdEnderecoCliente(rs.getObject("id_endereco_cliente", Integer.class));
                cliente.setStatusCliente(rs.getString("status_cliente"));
                

                // Campos de endereço via JOIN
                cliente.setLogradouro(rs.getString("logradouro"));
                cliente.setNumero(rs.getString("numero"));
                cliente.setComplemento(rs.getString("complemento"));
                cliente.setBairro(rs.getString("bairro"));
                cliente.setCidade(rs.getString("cidade"));
                cliente.setEstado(rs.getString("estado"));
                cliente.setCep(rs.getString("cep"));

                clientes.add(cliente);
            }
        }
        return clientes;
    }

    // Método para buscar cliente por CNPJ
    public Cliente buscarPorCnpj(String cnpj) throws SQLException {
        String sql = "SELECT c.*, e.logradouro, e.numero, e.complemento, e.bairro, e.cidade, e.estado, e.cep " +
                "FROM clientes c LEFT JOIN enderecos e ON c.id_endereco_cliente = e.id_endereco " +
                "WHERE c.cnpj = ?";

        try (Connection conn = DatabaseConnection.getConnectionMercado();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cnpj);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Cliente cliente = new Cliente();
                    cliente.setCnpj(rs.getString("cnpj"));
                    cliente.setRazaoSocial(rs.getString("razao_social"));
                    cliente.setNomeFantasia(rs.getString("nome_fantasia"));
                    cliente.setInscricaoEstadual(rs.getString("inscricao_estadual"));
                    cliente.setEmailCliente(rs.getString("email_cliente"));
                    cliente.setTelefoneCliente(rs.getString("telefone_cliente"));
                    cliente.setIdEnderecoCliente(rs.getObject("id_endereco_cliente", Integer.class));
                    cliente.setStatusCliente(rs.getString("status_cliente"));
                    

                    // Campos de endereço via JOIN
                    cliente.setLogradouro(rs.getString("logradouro"));
                    cliente.setNumero(rs.getString("numero"));
                    cliente.setComplemento(rs.getString("complemento"));
                    cliente.setBairro(rs.getString("bairro"));
                    cliente.setCidade(rs.getString("cidade"));
                    cliente.setEstado(rs.getString("estado"));
                    cliente.setCep(rs.getString("cep"));

                    return cliente;
                }
            }
        }
        return null;
    }

    // Método para buscar clientes por razão social (para busca)
    public List<Cliente> buscarPorRazaoSocial(String razaoSocial) throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT c.*, e.logradouro, e.numero, e.complemento, e.bairro, e.cidade, e.estado, e.cep " +
                "FROM clientes c LEFT JOIN enderecos e ON c.id_endereco_cliente = e.id_endereco " +
                "WHERE LOWER(c.razao_social) LIKE LOWER(?) ORDER BY c.razao_social";

        try (Connection conn = DatabaseConnection.getConnectionMercado();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + razaoSocial + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Cliente cliente = new Cliente();
                    cliente.setCnpj(rs.getString("cnpj"));
                    cliente.setRazaoSocial(rs.getString("razao_social"));
                    cliente.setNomeFantasia(rs.getString("nome_fantasia"));
                    cliente.setInscricaoEstadual(rs.getString("inscricao_estadual"));
                    cliente.setEmailCliente(rs.getString("email_cliente"));
                    cliente.setTelefoneCliente(rs.getString("telefone_cliente"));
                    cliente.setIdEnderecoCliente(rs.getObject("id_endereco_cliente", Integer.class));
                    cliente.setStatusCliente(rs.getString("status_cliente"));
                    

                    // Campos de endereço via JOIN
                    cliente.setLogradouro(rs.getString("logradouro"));
                    cliente.setNumero(rs.getString("numero"));
                    cliente.setComplemento(rs.getString("complemento"));
                    cliente.setBairro(rs.getString("bairro"));
                    cliente.setCidade(rs.getString("cidade"));
                    cliente.setEstado(rs.getString("estado"));
                    cliente.setCep(rs.getString("cep"));

                    clientes.add(cliente);
                }
            }
        }
        return clientes;
    }

    // Método para inserir novo cliente
    public boolean inserir(Cliente cliente) throws SQLException {
        EnderecoDAO enderecoDAO = new EnderecoDAO();

        // Primeiro, inserir o endereço se houver dados
        Integer idEndereco = null;
        if (cliente.getLogradouro() != null && !cliente.getLogradouro().trim().isEmpty()) {
            Endereco endereco = new Endereco(
                    cliente.getLogradouro(),
                    cliente.getNumero(),
                    cliente.getComplemento(),
                    cliente.getBairro(),
                    cliente.getCidade(),
                    cliente.getEstado(),
                    cliente.getCep());

            if (enderecoDAO.inserir(endereco)) {
                idEndereco = endereco.getIdEndereco();
            }
        }

        // Agora inserir o cliente com a foreign key do endereço
        String sql = "INSERT INTO clientes (cnpj, razao_social, nome_fantasia, inscricao_estadual, " +
                "email_cliente, telefone_cliente, id_endereco_cliente, status_cliente) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnectionMercado();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cliente.getCnpj());
            stmt.setString(2, cliente.getRazaoSocial());
            stmt.setString(3, cliente.getNomeFantasia());
            stmt.setString(4, cliente.getInscricaoEstadual());
            stmt.setString(5, cliente.getEmailCliente());
            stmt.setString(6, cliente.getTelefoneCliente());
            if (idEndereco != null) {
                stmt.setInt(7, idEndereco);
            } else {
                stmt.setNull(7, Types.INTEGER);
            }
            stmt.setString(8, cliente.getStatusCliente() != null ? cliente.getStatusCliente() : "PAGO");

            return stmt.executeUpdate() > 0;
        }
    }

    // Método para atualizar cliente
    public boolean atualizar(Cliente cliente) throws SQLException {
        EnderecoDAO enderecoDAO = new EnderecoDAO();

        // Gerenciar o endereço
        Integer idEndereco = cliente.getIdEnderecoCliente();
        if (cliente.getLogradouro() != null && !cliente.getLogradouro().trim().isEmpty()) {
            Endereco endereco = new Endereco(
                    cliente.getLogradouro(),
                    cliente.getNumero(),
                    cliente.getComplemento(),
                    cliente.getBairro(),
                    cliente.getCidade(),
                    cliente.getEstado(),
                    cliente.getCep());

            if (idEndereco != null) {
                // Atualizar endereço existente
                endereco.setIdEndereco(idEndereco);
                enderecoDAO.atualizar(endereco);
            } else {
                // Inserir novo endereço
                if (enderecoDAO.inserir(endereco)) {
                    idEndereco = endereco.getIdEndereco();
                }
            }
        }

        // Atualizar o cliente com a foreign key do endereço
        String sql = "UPDATE clientes SET razao_social = ?, nome_fantasia = ?, inscricao_estadual = ?, " +
                "email_cliente = ?, telefone_cliente = ?, id_endereco_cliente = ?, status_cliente = ? " +
                "WHERE cnpj = ?";

        try (Connection conn = DatabaseConnection.getConnectionMercado();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cliente.getRazaoSocial());
            stmt.setString(2, cliente.getNomeFantasia());
            stmt.setString(3, cliente.getInscricaoEstadual());
            stmt.setString(4, cliente.getEmailCliente());
            stmt.setString(5, cliente.getTelefoneCliente());
            if (idEndereco != null) {
                stmt.setInt(6, idEndereco);
            } else {
                stmt.setNull(6, Types.INTEGER);
            }
            stmt.setString(7, cliente.getStatusCliente());
            stmt.setString(8, cliente.getCnpj());

            return stmt.executeUpdate() > 0;
        }
    }

    // Método para excluir cliente
    public boolean excluir(String cnpj) throws SQLException {
        String sql = "DELETE FROM clientes WHERE cnpj = ?";

        try (Connection conn = DatabaseConnection.getConnectionMercado();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cnpj);
            return stmt.executeUpdate() > 0;
        }
    }

    // Método para verificar se CNPJ já existe
    public boolean cnpjExiste(String cnpj, String cnpjExcluir) throws SQLException {
        String sql = "SELECT COUNT(*) FROM clientes WHERE cnpj = ?";

        if (cnpjExcluir != null) {
            sql += " AND cnpj != ?";
        }

        try (Connection conn = DatabaseConnection.getConnectionMercado();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cnpj);
            if (cnpjExcluir != null) {
                stmt.setString(2, cnpjExcluir);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Método para contar total de clientes
    public int contarTotal() throws SQLException {
        String sql = "SELECT COUNT(*) FROM clientes";

        try (Connection conn = DatabaseConnection.getConnectionMercado();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}
