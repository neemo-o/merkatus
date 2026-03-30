package main.database.auth;

import main.database.DatabaseManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class UserAuth {

    @Autowired
    private DatabaseManager databaseManager;

    public boolean authenticate(Integer idUsuario, String senha) {
        String sql = """
            SELECT senha_hash, bloqueado
            FROM usuarios
            WHERE id_usuario = ? AND ativo = TRUE
            """;

        try (Connection conn = databaseManager.getOficialConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    if (rs.getBoolean("bloqueado")) return false;
                    return senha.equals(rs.getString("senha_hash"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao autenticar usuário: " + e.getMessage());
        }
        return false;
    }
}