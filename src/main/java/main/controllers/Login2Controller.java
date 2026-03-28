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

import org.springframework.stereotype.Component;

@Component
public class Login2Controller {

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button loginButton1; // Botão Fechar

    @FXML
    private Text statusMessage;

    @FXML
    public void initialize() {

        usernameField.sceneProperty().addListener((obs, oldScene, newScene) -> {
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

                // ATUALIZADO: agora consulta tabela 'usuarios' em vez de 'licencas'
                String query = "SELECT * FROM usuarios WHERE id_usuario = ? AND ativo = TRUE";
                PreparedStatement stmt = db.prepareStatement(query);

                stmt.setInt(1, username);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    // ATUALIZADO: campo agora é 'senha_hash' em vez de 'senha_usuario'
                    String dbPassword = rs.getString("senha_hash");

                    // TODO: Quando implementar hash de verdade (bcrypt/argon2),
                    // substituir esta comparação por BCrypt.checkpw(password, dbPassword)
                    if (dbPassword.equals(password)) {
                        statusMessage.setText("Login realizado com sucesso!");
                        statusMessage.setVisible(true);

                        // Verificar se usuário está bloqueado
                        if (rs.getBoolean("bloqueado")) {
                            statusMessage.setText("Usuário bloqueado. Contate o administrador.");
                            statusMessage.setVisible(true);
                            return false;
                        }

                        return true;
                    } else {
                        statusMessage.setText("Credenciais inválidas.");
                        statusMessage.setVisible(true);
                    }
                } else {
                    statusMessage.setText("Usuário não encontrado ou inativo.");
                    statusMessage.setVisible(true);
                }

                rs.close();
                stmt.close();
            }

        } catch (SQLException e) {
            statusMessage.setText("Erro ao conectar ao banco de dados: " + e.getMessage());
            statusMessage.setVisible(true);
        }

        return false;
    }

    @FXML
    private void handleLoginButton() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

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
            return;
        }
    }

    @FXML
    private void handleMainScreenLink() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/view/MainScreen.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            if (loginButton.getScene() != null && loginButton.getScene().getWindow() != null) {
                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setResizable(false);
                stage.centerOnScreen();
                stage.setScene(scene);

                stage.setMaximized(true);
                stage.show();
            } else {
                statusMessage.setText("Erro: Janela não está disponível.");
                statusMessage.setVisible(true);
            }
        } catch (Exception e) {
            statusMessage.setText("Erro ao voltar para login: " + e.getMessage());
            statusMessage.setVisible(true);
        }
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