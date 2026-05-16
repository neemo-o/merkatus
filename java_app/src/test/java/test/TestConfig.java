package test;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import main.database.OfficialDataSourceProperties;
import main.database.LicencasDataSourceProperties;

@SpringBootApplication(scanBasePackages = "main")
@EnableConfigurationProperties({
    OfficialDataSourceProperties.class,
    LicencasDataSourceProperties.class
})
public class TestConfig {
    // Classe vazia — só serve para o Spring saber o que escanear nos testes
}