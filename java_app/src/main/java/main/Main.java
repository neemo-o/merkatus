package main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

import main.database.OfficialDataSourceProperties;
import main.database.LicencasDataSourceProperties;

@SpringBootApplication
@EnableConfigurationProperties({
    OfficialDataSourceProperties.class,
    LicencasDataSourceProperties.class
})
public class Main extends Application {

	private ConfigurableApplicationContext springContext;

	  @Override
    public void init() {
       
		try {
		 // Spring sobe ANTES da janela abrir
        springContext = SpringApplication.run(Main.class);
		} catch (Exception e) {
			Throwable cause = e.getCause() != null ? e.getCause() : e;
        if (cause.getMessage() != null && (
            cause.getMessage().contains("timeout") ||
            cause.getMessage().contains("Connection refused") ||
            cause instanceof com.zaxxer.hikari.pool.HikariPool.PoolInitializationException
        )) {
            // Mostrar alert JavaFX na thread certa
            javafx.application.Platform.startup(() -> {});
            javafx.application.Platform.runLater(() -> {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR
                );
                alert.setTitle("Erro de Conexão");
                alert.setHeaderText("Não foi possível conectar ao banco de dados.");
                alert.setContentText("Verifique se o PostgreSQL está rodando e as credenciais em application.properties.");
                alert.showAndWait();
                javafx.application.Platform.exit();
            });
        } else {
            throw e;
        }
		}
    }

	@Override
	public void start(Stage primaryStage) {
		try {
		 	FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/view/Login.fxml"));
			 // Isso permite que seus controllers JavaFX recebam @Autowired
            loader.setControllerFactory(springContext::getBean);
			Parent root = loader.load();
			Scene scene = new Scene(root);
			primaryStage.initStyle(StageStyle.UNDECORATED);
			primaryStage.centerOnScreen();
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	 @Override
    public void stop() {
        springContext.close(); // fecha o Spring quando fechar a janela
        Platform.exit();
    }


	public static void main(String[] args) {
		launch(args);
	}
}