package main.database.auth;

import main.database.DatabaseManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class Auth {

    @Autowired
    private DatabaseManager databaseManager;

    public boolean validateCNPJ(String cnpj) {
        String sql = """
            SELECT l.status
            FROM clientes_licenciados c
            INNER JOIN licencas l ON c.id_cliente = l.id_cliente
            WHERE c.cnpj = ? AND c.ativo = TRUE
            """;

        try (Connection conn = databaseManager.getLicencasConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cnpj);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return "ATIVA".equals(rs.getString("status"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao validar CNPJ: " + e.getMessage());
        }
        return false;
    }

    public boolean testConnection() {
        try (Connection conn = databaseManager.getLicencasConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Erro ao testar conexão: " + e.getMessage());
            return false;
        }
    }
}