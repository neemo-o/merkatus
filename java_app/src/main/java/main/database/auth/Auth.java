package main.database.auth;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Profile("!test")  //← adiciona isso
public class Auth {

    private final JdbcTemplate licencasJdbc;
    private static final Logger logger = LoggerFactory.getLogger(Auth.class);

    public Auth(@Qualifier("licencasDataSource") DataSource licencasDataSource) {
        this.licencasJdbc = new JdbcTemplate(licencasDataSource);
    }

    public boolean validateCNPJ(String cnpj) {
        logger.debug("Validando CNPJ: '{}'", cnpj);
        String sql = """
            SELECT l.status
            FROM clientes_licenciados c
            INNER JOIN licencas l ON c.id_cliente = l.id_cliente
            WHERE c.cnpj = ? AND c.ativo = TRUE
            """;

        try {
            String status = licencasJdbc.queryForObject(sql, String.class, cnpj);
            return "ATIVA".equals(status);
        } catch (Exception e) {
            logger.error("Erro ao validar CNPJ: {}", e.getMessage());
        }
        return false;
    }

    public boolean testConnection() {
        try {
            licencasJdbc.queryForObject("SELECT 1", Integer.class);
            return true;
        } catch (Exception e) {
            logger.error("Erro ao testar conexão: {}", e.getMessage());
            return false;
        }
    }
}
