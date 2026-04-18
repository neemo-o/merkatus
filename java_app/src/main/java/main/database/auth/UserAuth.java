package main.database.auth;

import main.models.Usuario;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sql.DataSource;

@Component
public class UserAuth {

    private final JdbcTemplate oficialJdbc;
    private static final Logger logger = LoggerFactory.getLogger(UserAuth.class);

    public UserAuth(@Qualifier("oficialDataSource") DataSource oficialDataSource) {
        this.oficialJdbc = new JdbcTemplate(oficialDataSource);
    }

    public boolean authenticate(Integer idUsuario, String senha) {
        String sql = "SELECT senha_hash, bloqueado FROM usuarios WHERE id_usuario = ? AND ativo = TRUE";

        try {
            var row = oficialJdbc.queryForMap(sql, idUsuario);
            if (Boolean.TRUE.equals((Boolean) row.get("bloqueado"))) return false;
            return senha.equals(row.get("senha_hash"));
        } catch (Exception e) {
            logger.error("Erro ao autenticar usuário: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Busca um usuário pelo ID (independente de senha). Retorna null se nao existir.
     */
    public Usuario buscarPorId(Integer idUsuario) {
        String sql = "SELECT id_usuario, nome_exibicao, login FROM usuarios WHERE id_usuario = ? AND ativo = TRUE";

        try {
            var row = oficialJdbc.queryForMap(sql, idUsuario);
            Usuario u = new Usuario();
            u.setIdUsuario((Integer) row.get("id_usuario"));
            u.setNomeExibicao((String) row.get("nome_exibicao"));
            u.setLogin((String) row.get("login"));
            return u;
        } catch (Exception e) {
            logger.error("Erro ao buscar usuário: {}", e.getMessage());
        }
        return null;
    }
}
