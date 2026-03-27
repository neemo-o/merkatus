package main.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import main.database.DatabaseConnection;

public class Login2Controller {

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private TextField documentField1;

    @FXML
    private PasswordField documentField11;

    @FXML
    private Button loginButton;

    @FXML
    private Button minimizeButton;

    @FXML
    private Text statusMessage;

    @FXML
    public void initialize() {
        documentField1.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Stage stage = (Stage) newScene.getWindow();
                if (stage != null) {
                    stage.setResizable(false);
                    javafx.application.Platform.runLater(() -> {
                        stage.centerOnScreen();
                    });
                }
            }
        });
    }

    private boolean authenticateUser(Integer username, String password) {
        try {
            Connection db = DatabaseConnection.getConnectionMercado();

            if (db != null) {
                String query = "SELECT * FROM licencas WHERE id_usuario = ?";
                PreparedStatement stmt = db.prepareStatement(query);
                stmt.setInt(1, username);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String dbPassword = rs.getString("senha_usuario");

                    if (dbPassword.equals(password)) {
                        statusMessage.setText("Login realizado com sucesso!");
                        statusMessage.setVisible(true);
                        return true;
                    } else {
                        statusMessage.setText("Credenciais inválidas.");
                        statusMessage.setVisible(true);
                    }
                }
            }

        } catch (SQLException e) {
            statusMessage.setText("Erro ao conectar ao banco de dados: " + e.getMessage());
            statusMessage.setVisible(true);
        }

        return false;
    }

    @FXML
    private void handleLoginButton() {
        String username = documentField1.getText().trim();
        String password = documentField11.getText().trim();

        if (username.isEmpty()) {
            statusMessage.setText("Nome de usuário não pode estar vazio.");
            statusMessage.setVisible(true);
            return;
        }

        if (password.isEmpty()) {
            statusMessage.setText("Senha não pode estar vazia.");
            statusMessage.setVisible(true);
            return;
        }

        try {
            Integer userId = Integer.parseInt(username);

            if (authenticateUser(userId, password)) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/view/MainScreen.fxml"));
                    Parent root = loader.load();
                    Scene scene = new Scene(root);

                    if (loginButton.getScene() != null && loginButton.getScene().getWindow() != null) {
                        Stage stage = (Stage) loginButton.getScene().getWindow();
                        stage.setScene(scene);
                        stage.setResizable(false);
                        stage.show();
                    } else {
                        statusMessage.setText("Erro: Janela não está disponível.");
                        statusMessage.setVisible(true);
                    }
                } catch (Exception e) {
                    statusMessage.setText("Erro ao carregar a tela principal: " + e.getMessage());
                    statusMessage.setVisible(true);
                    e.printStackTrace();
                }
            }

        } catch (NumberFormatException e) {
            statusMessage.setText("ID de usuário inválido.");
            statusMessage.setVisible(true);
        }
    }

    @FXML
    private void handleCloseButton() {
        Stage stage = null;

        if (loginButton != null && loginButton.getScene() != null) {
            stage = (Stage) loginButton.getScene().getWindow();
        } else if (minimizeButton != null && minimizeButton.getScene() != null) {
            stage = (Stage) minimizeButton.getScene().getWindow();
        }

        if (stage != null) {
            stage.close();
        } else {
            System.exit(0);
        }
    }

    @FXML
    private void handleMinimize() {
        if (minimizeButton.getScene() != null && minimizeButton.getScene().getWindow() != null) {
            Stage stage = (Stage) minimizeButton.getScene().getWindow();
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