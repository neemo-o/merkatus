package main.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import main.database.auth.UserAuth;
import main.util.FXMLLoaderFactory;

@Component
public class Login2Controller {

    @Autowired
    private UserAuth userAuth;

    @Autowired
    private FXMLLoaderFactory loaderFactory;

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Button loginButton1;
    @FXML
    private Text statusMessage;

    @FXML
    public void initialize() {
        usernameField.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Stage stage = (Stage) newScene.getWindow();
                if (stage != null) {
                    stage.setResizable(false);
                    Platform.runLater(stage::centerOnScreen);
                }
            }
        });
    }

    @FXML
    private void handleLoginButton() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty()) {
            mostrarErro("ID de usuário não pode estar vazio.");
            return;
        }

        if (password.isEmpty()) {
            mostrarErro("Senha não pode estar vazia.");
            return;
        }

        try {
            Integer userId = Integer.parseInt(username);

            if (userAuth.authenticate(userId, password)) {
                navegarPara("/main/view/MainScreen.fxml", true);
            } else {
                mostrarErro("Credenciais inválidas ou usuário bloqueado.");
            }

        } catch (NumberFormatException e) {
            mostrarErro("ID de usuário deve ser numérico.");
        }
    }

    @FXML
    private void handleMainScreenLink() {
        navegarPara("/main/view/MainScreen.fxml", true);
    }

    private void navegarPara(String fxml, boolean maximizado) {
        try {
            FXMLLoader loader = loaderFactory.create(fxml);
            Parent root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            if (maximizado)
                stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            mostrarErro("Erro ao carregar tela: " + e.getMessage());
        }
    }

    private void mostrarErro(String mensagem) {
        statusMessage.setText(mensagem);
        statusMessage.setVisible(true);
    }

    @FXML
    private void handleCloseButton() {
        if (loginButton1.getScene() != null && loginButton1.getScene().getWindow() != null) {
            Stage stage = (Stage) loginButton1.getScene().getWindow();
            stage.close();
        } else {
            System.exit(0);
        }
    }

    @FXML
    private void handleMinimize() {
        if (loginButton.getScene() != null && loginButton.getScene().getWindow() != null) {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setIconified(true);
        }
    }

    @FXML
    private void handleMousePressed(javafx.scene.input.MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    @FXML
    private void handleMouseDragged(javafx.scene.input.MouseEvent event) {
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
    }
}