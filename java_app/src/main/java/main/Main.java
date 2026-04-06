package main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

@SpringBootApplication
public class Main extends Application {

	private ConfigurableApplicationContext springContext;

	  @Override
    public void init() {
        // Spring sobe ANTES da janela abrir
        springContext = SpringApplication.run(Main.class);
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