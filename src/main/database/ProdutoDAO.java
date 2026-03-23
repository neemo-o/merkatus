package main.database;

import main.models.Produto;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDAO {

    // Método para buscar todos os produtos
    public List<Produto> buscarTodos() throws SQLException {
        List<Produto> produtos = new ArrayList<>();
        String sql = "SELECT * FROM produto ORDER BY descricao";

        try (Connection conn = DatabaseConnection.getConnectionMercado();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Produto produto = new Produto();
                produto.setIdProduto(rs.getInt("id_produto"));
                produto.setDescricao(rs.getString("descricao"));
                produto.setCodigoBarras(rs.getString("codigo_barras"));
                produto.setUnidadeMedida(rs.getString("unidade_medida"));
                produto.setPrecoCusto(rs.getBigDecimal("preco_custo"));
                produto.setPrecoVenda(rs.getBigDecimal("preco_venda"));
                produto.setEstoqueAtual(rs.getInt("estoque_atual"));
                produto.setIdFornecedor(rs.getObject("id_fornecedor", Integer.class));
                produto.setDataCadastro(rs.getTimestamp("data_cadastro"));
                produto.setDataAtualizacao(rs.getTimestamp("data_atualizacao"));

                produtos.add(produto);
            }
        }
        return produtos;
    }

    // Método para buscar produto por ID
    public Produto buscarPorId(int idProduto) throws SQLException {
        String sql = "SELECT * FROM produto WHERE id_produto = ?";

        try (Connection conn = DatabaseConnection.getConnectionMercado();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idProduto);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Produto produto = new Produto();
                    produto.setIdProduto(rs.getInt("id_produto"));
                    produto.setDescricao(rs.getString("descricao"));
                    produto.setCodigoBarras(rs.getString("codigo_barras"));
                    produto.setUnidadeMedida(rs.getString("unidade_medida"));
                    produto.setPrecoCusto(rs.getBigDecimal("preco_custo"));
                    produto.setPrecoVenda(rs.getBigDecimal("preco_venda"));
                    produto.setEstoqueAtual(rs.getInt("estoque_atual"));
                    produto.setIdFornecedor(rs.getObject("id_fornecedor", Integer.class));
                    produto.setDataCadastro(rs.getTimestamp("data_cadastro"));
                    produto.setDataAtualizacao(rs.getTimestamp("data_atualizacao"));

                    return produto;
                }
            }
        }
        return null;
    }

    // Método para buscar produto por código de barras
    public Produto buscarPorCodigoBarras(String codigoBarras) throws SQLException {
        String sql = "SELECT * FROM produto WHERE codigo_barras = ?";

        try (Connection conn = DatabaseConnection.getConnectionMercado();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codigoBarras);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Produto produto = new Produto();
                    produto.setIdProduto(rs.getInt("id_produto"));
                    produto.setDescricao(rs.getString("descricao"));
                    produto.setCodigoBarras(rs.getString("codigo_barras"));
                    produto.setUnidadeMedida(rs.getString("unidade_medida"));
                    produto.setPrecoCusto(rs.getBigDecimal("preco_custo"));
                    produto.setPrecoVenda(rs.getBigDecimal("preco_venda"));
                    produto.setEstoqueAtual(rs.getInt("estoque_atual"));
                    produto.setIdFornecedor(rs.getObject("id_fornecedor", Integer.class));
                    produto.setDataCadastro(rs.getTimestamp("data_cadastro"));
                    produto.setDataAtualizacao(rs.getTimestamp("data_atualizacao"));

                    return produto;
                }
            }
        }
        return null;
    }

    // Método para buscar produtos por descrição (para busca)
    public List<Produto> buscarPorDescricao(String descricao) throws SQLException {
        List<Produto> produtos = new ArrayList<>();
        String sql = "SELECT * FROM produto WHERE LOWER(descricao) LIKE LOWER(?) ORDER BY descricao";

        try (Connection conn = DatabaseConnection.getConnectionMercado();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + descricao + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Produto produto = new Produto();
                    produto.setIdProduto(rs.getInt("id_produto"));
                    produto.setDescricao(rs.getString("descricao"));
                    produto.setCodigoBarras(rs.getString("codigo_barras"));
                    produto.setUnidadeMedida(rs.getString("unidade_medida"));
                    produto.setPrecoCusto(rs.getBigDecimal("preco_custo"));
                    produto.setPrecoVenda(rs.getBigDecimal("preco_venda"));
                    produto.setEstoqueAtual(rs.getInt("estoque_atual"));
                    produto.setIdFornecedor(rs.getObject("id_fornecedor", Integer.class));
                    produto.setDataCadastro(rs.getTimestamp("data_cadastro"));
                    produto.setDataAtualizacao(rs.getTimestamp("data_atualizacao"));

                    produtos.add(produto);
                }
            }
        }
        return produtos;
    }

    // Método para inserir novo produto
    public boolean inserir(Produto produto) throws SQLException {
        String sql = "INSERT INTO produto (descricao, codigo_barras, unidade_medida, " +
                    "preco_custo, preco_venda, estoque_atual, id_fornecedor) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnectionMercado();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, produto.getDescricao());
            stmt.setString(2, produto.getCodigoBarras());
            stmt.setString(3, produto.getUnidadeMedida());
            stmt.setBigDecimal(4, produto.getPrecoCusto());
            stmt.setBigDecimal(5, produto.getPrecoVenda());
            stmt.setInt(6, produto.getEstoqueAtual());
            if (produto.getIdFornecedor() != null) {
                stmt.setInt(7, produto.getIdFornecedor());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }

            int linhasAfetadas = stmt.executeUpdate();

            if (linhasAfetadas > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        produto.setIdProduto(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    // Método para atualizar produto
    public boolean atualizar(Produto produto) throws SQLException {
        String sql = "UPDATE produto SET descricao = ?, codigo_barras = ?, unidade_medida = ?, " +
                    "preco_custo = ?, preco_venda = ?, estoque_atual = ?, id_fornecedor = ? " +
                    "WHERE id_produto = ?";

        try (Connection conn = DatabaseConnection.getConnectionMercado();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, produto.getDescricao());
            stmt.setString(2, produto.getCodigoBarras());
            stmt.setString(3, produto.getUnidadeMedida());
            stmt.setBigDecimal(4, produto.getPrecoCusto());
            stmt.setBigDecimal(5, produto.getPrecoVenda());
            stmt.setInt(6, produto.getEstoqueAtual());
            if (produto.getIdFornecedor() != null) {
                stmt.setInt(7, produto.getIdFornecedor());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }
            stmt.setInt(8, produto.getIdProduto());

            return stmt.executeUpdate() > 0;
        }
    }

    // Método para excluir produto
    public boolean excluir(int idProduto) throws SQLException {
        String sql = "DELETE FROM produto WHERE id_produto = ?";

        try (Connection conn = DatabaseConnection.getConnectionMercado();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idProduto);
            return stmt.executeUpdate() > 0;
        }
    }

    // Método para verificar se código de barras já existe
    public boolean codigoBarrasExiste(String codigoBarras, Integer idProdutoExcluir) throws SQLException {
        String sql = "SELECT COUNT(*) FROM produto WHERE codigo_barras = ?";

        if (idProdutoExcluir != null) {
            sql += " AND id_produto != ?";
        }

        try (Connection conn = DatabaseConnection.getConnectionMercado();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codigoBarras);
            if (idProdutoExcluir != null) {
                stmt.setInt(2, idProdutoExcluir);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Método para contar total de produtos
    public int contarTotal() throws SQLException {
        String sql = "SELECT COUNT(*) FROM produto";

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
