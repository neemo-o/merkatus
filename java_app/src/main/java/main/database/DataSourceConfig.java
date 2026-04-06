package main.database;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DataSourceConfig {

    @Value("${db.oficial.url}")
    private String oficialUrl;

    @Value("${db.oficial.username}")
    private String oficialUsername;

    @Value("${db.oficial.password}")
    private String oficialPassword;

    @Value("${db.licencas.url}")
    private String licencasUrl;

    @Value("${db.licencas.username}")
    private String licencasUsername;

    @Value("${db.licencas.password}")
    private String licencasPassword;

    @Bean
    @Primary
    public DataSource oficialDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(oficialUrl);
        config.setUsername(oficialUsername);
        config.setPassword(oficialPassword);
        config.setDriverClassName("org.postgresql.Driver");

        // Pool
        config.setMaximumPoolSize(10); // máximo de conexões abertas
        config.setMinimumIdle(2); // mínimo mantido em espera
        config.setConnectionTimeout(30000); // 30s pra conseguir uma conexão
        config.setIdleTimeout(600000); // fecha conexão ociosa após 10min
        config.setMaxLifetime(1800000); // recicla conexão após 30min

        config.setPoolName("ERP-Oficial-Pool");

        return new HikariDataSource(config);
    }

    @Bean
    public DataSource licencasDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(licencasUrl);
        config.setUsername(licencasUsername);
        config.setPassword(licencasPassword);
        config.setDriverClassName("org.postgresql.Driver");
        config.addDataSourceProperty("ssl", "true");
        config.addDataSourceProperty("sslmode", "require");
        config.setMaximumPoolSize(3);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000);
        config.setPoolName("ERP-Licencas-Pool");
        return new HikariDataSource(config);
    }
}