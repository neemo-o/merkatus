package main.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import main.database.auth.UserAuth;
import main.models.Usuario;
import main.util.FXMLLoaderFactory;
import main.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

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
    private Text statusMessage;
    @FXML
    private Label usuarioEncontradoLabel;

    private Usuario usuarioEncontrado;

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

        // Listener para buscar o usuário ao digitar o ID
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> {
            String texto = newVal.trim();

            if (texto.isEmpty()) {
                usuarioEncontradoLabel.setText("");
                usuarioEncontradoLabel.setVisible(false);
                usuarioEncontrado = null;
                return;
            }

            try {
                Integer id = Integer.parseInt(texto);
                usuarioEncontrado = userAuth.buscarPorId(id);

                if (usuarioEncontrado != null) {
                    usuarioEncontradoLabel.setText("USUÁRIO: " + usuarioEncontrado.getNome().toUpperCase());
                    usuarioEncontradoLabel.setStyle("-fx-text-fill: #2c6e3c; -fx-font-size: 12; -fx-font-weight: bold;");
                    usuarioEncontradoLabel.setVisible(true);
                    statusMessage.setVisible(false);
                } else {
                    usuarioEncontradoLabel.setText("");
                    usuarioEncontradoLabel.setVisible(false);
                    mostrarErro("Usuário não encontrado com este ID.");
                }
            } catch (NumberFormatException e) {
                mostrarErro("ID de usuário deve ser numérico.");
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
                Usuario usuario = userAuth.buscarPorId(userId);
                if (usuario != null) {
                    SessionManager.setUsuarioAtual(usuario);
                }
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

            // Pass user info to MainScreenController
            Object controller = loader.getController();
            if (controller instanceof MainScreenController mainscreenController) {
                Usuario usuario = SessionManager.getUsuarioAtual();
                if (usuario != null) {
                    mainscreenController.setUsuario(usuario.getNome());
                }
            }

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
        Scene scene = usernameField.getScene();
        if (scene != null && scene.getWindow() != null) {
            Stage stage = (Stage) scene.getWindow();
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
