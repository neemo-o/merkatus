package main.database;

import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DataSourceConfig {

    private final OfficialDataSourceProperties oficialProps;
    private final LicencasDataSourceProperties licencasProps;

    public DataSourceConfig(OfficialDataSourceProperties oficialProps,
                            LicencasDataSourceProperties licencasProps) {
        this.oficialProps = oficialProps;
        this.licencasProps = licencasProps;
    }

    @Bean
    @Primary
    public DataSource oficialDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(oficialProps.getUrl());
        config.setUsername(oficialProps.getUsername());
        config.setPassword(oficialProps.getPassword());
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setPoolName("ERP-Oficial-Pool");
        return new HikariDataSource(config);
    }

    @Bean
    @Profile("!test") 
    public DataSource licencasDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(licencasProps.getUrl());
        config.setUsername(licencasProps.getUsername());
        config.setPassword(licencasProps.getPassword());
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