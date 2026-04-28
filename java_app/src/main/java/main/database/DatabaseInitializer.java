package main.database;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class DatabaseInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final JdbcTemplate jdbc;

    public DatabaseInitializer(@Qualifier("oficialDataSource") DataSource oficialDataSource) {
        this.jdbc = new JdbcTemplate(oficialDataSource);
    }

    @PostConstruct
    public void init() {
        if (!tabelasExistem()) {
            logger.warn("DatabaseInitializer: schema não encontrado, seed ignorado. " +
                        "Rode erp-oficial.sql antes de iniciar o sistema.");
            return;
        }

        if (jaFoiSemeado()) {
            logger.debug("DatabaseInitializer: seed já aplicado anteriormente, pulando.");
            return;
        }

        logger.info("DatabaseInitializer: banco limpo detectado, aplicando seed...");
        aplicarSeed();
        logger.info("DatabaseInitializer: seed aplicado com sucesso.");
    }

    // -------------------------------------------------------
    // Verifica se o schema já existe (tabela configuracoes)
    // -------------------------------------------------------
    private boolean tabelasExistem() {
        try {
            String sql = """
                SELECT COUNT(*) FROM information_schema.tables
                WHERE table_schema = 'public'
                AND table_name = 'configuracoes'
                """;
            Integer count = jdbc.queryForObject(sql, Integer.class);
            return count != null && count > 0;
        } catch (Exception e) {
            logger.error("DatabaseInitializer: erro ao verificar schema — {}", e.getMessage());
            return false;
        }
    }

    // -------------------------------------------------------
    // Verifica a flag SEED_APLICADO na tabela configuracoes
    // -------------------------------------------------------
    private boolean jaFoiSemeado() {
        try {
            String sql = """
                SELECT COUNT(*) FROM configuracoes
                WHERE modulo = 'SISTEMA' AND chave = 'SEED_APLICADO' AND valor = 'true'
                """;
            Integer count = jdbc.queryForObject(sql, Integer.class);
            return count != null && count > 0;
        } catch (Exception e) {
            // tabela existe mas vazia — não foi semeado
            return false;
        }
    }

    // -------------------------------------------------------
    // Lê seed.sql do classpath e executa statement por statement
    // -------------------------------------------------------
    private void aplicarSeed() {
        try {
            ClassPathResource resource = new ClassPathResource("db/scripts/seed.sql");
            String sql = resource.getContentAsString(StandardCharsets.UTF_8);

            // Divide nos ponto-e-vírgula, ignorando linhas de comentário
            String[] statements = sql.split(";");

            int executados = 0;
            for (String statement : statements) {
                String trimmed = statement.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("--")) continue;

                // Remove linhas de comentário inline antes de executar
                String limpo = removerComentarios(trimmed);
                if (limpo.isBlank()) continue;

                try {
                    jdbc.execute(limpo);
                    executados++;
                } catch (Exception e) {
                    logger.warn("DatabaseInitializer: statement ignorado com erro — {}", e.getMessage());
                }
            }

            logger.info("DatabaseInitializer: {} statements executados.", executados);

        } catch (IOException e) {
            logger.error("DatabaseInitializer: não foi possível ler seed.sql — {}", e.getMessage());
            throw new RuntimeException("Falha ao carregar seed.sql do classpath", e);
        }
    }

    // -------------------------------------------------------
    // Remove linhas que começam com -- dentro de um statement
    // -------------------------------------------------------
    private String removerComentarios(String sql) {
        StringBuilder sb = new StringBuilder();
        for (String linha : sql.split("\n")) {
            String t = linha.trim();
            if (!t.startsWith("--")) {
                sb.append(linha).append("\n");
            }
        }
        return sb.toString().trim();
    }
}