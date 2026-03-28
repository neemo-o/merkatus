package main.database.auth;

import main.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Auth {

  
    public static boolean validateCNPJ(String cnpj) {
        Connection db = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            db = DatabaseConnection.getConnectionLicenses();
            
            if (db != null) {
                String query = "SELECT l.status FROM clientes_licenciados c " +
                               "INNER JOIN licencas l ON c.id_cliente = l.id_cliente " +
                               "WHERE c.cnpj = ? AND c.ativo = TRUE";
                stmt = db.prepareStatement(query);
                stmt.setString(1, cnpj);
                rs = stmt.executeQuery();
                
                if (rs.next()) {
                    String status = rs.getString("status");
                    return "ATIVA".equals(status);
                }
            }
            
            return false;
            
        } catch (SQLException e) {
            System.err.println("Erro ao conectar ao banco de dados: " + e.getMessage());
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                System.err.println("Erro ao fechar recursos: " + e.getMessage());
            }
        }
    }
    
    public static boolean testConnection() {
        try {
            Connection db = DatabaseConnection.getConnectionLicenses();
            return db != null && !db.isClosed();
        } catch (SQLException e) {
            System.err.println("Erro ao testar conexão: " + e.getMessage());
            return false;
        }
    }
}