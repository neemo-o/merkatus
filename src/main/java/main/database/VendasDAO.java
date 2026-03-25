package main.database;

import main.models.Vendas;
import java.sql.*;

public class VendasDAO {

    // Método para salvar venda
    public int salvar(Vendas venda) throws SQLException {
        String sql = "INSERT INTO venda (valor_total, forma_pagamento) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnectionMercado();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setBigDecimal(1, venda.getValorTotal());
            stmt.setString(2, venda.getFormaPagamento());

            int linhasAfetadas = stmt.executeUpdate();

            if (linhasAfetadas > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
            return 0;
        }
    }
}
