package main.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import main.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.UnaryOperator;

public class AuthController {

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private Button minimizeButton;

    @FXML
    private Button closeButton;

    @FXML
    private TextField documentField;

    @FXML
    private Button accessButton;

    @FXML
    private Text statusMessage;

    @FXML
    public void initialize() {
        // Aplicar máscara de CNPJ com formatação automática
        documentField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) return;
            
            // Remove todos os caracteres não numéricos
            String numbers = newValue.replaceAll("[^0-9]", "");
            
            // Limita a 14 dígitos
            if (numbers.length() > 14) {
                numbers = numbers.substring(0, 14);
            }
            
            // Formata o CNPJ: XX.XXX.XXX/XXXX-XX
            String formatted = formatCNPJ(numbers);
            
            // Atualiza o campo apenas se o texto formatado for diferente
            if (!formatted.equals(newValue)) {
                documentField.setText(formatted);
                documentField.positionCaret(formatted.length());
            }
        });
    }

    @FXML
    private void handleAccessButton() {
        String doc = documentField.getText().trim();
        
        // Validar se o CNPJ tem exatamente 18 caracteres
        if (doc.length() != 18) {
            statusMessage.setText("CNPJ deve ter exatamente 18 caracteres.");
            statusMessage.setVisible(true);
            return;
        }

        
        // Validar formato básico do CNPJ (exemplo: XX.XXX.XXX/XXXX-XX)
        if (!doc.matches("\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}")) {
            statusMessage.setText("Formato de CNPJ inválido. Use: XX.XXX.XXX/XXXX-XX");
            statusMessage.setVisible(true);
            return;
        }

        // Só conectar ao banco se os dados forem válidos
        try {
            Connection db = DatabaseConnection.getConnectionLicenses();

            if (db != null) {
                // Verificar se o CNPJ existe e está pago
                String query = "SELECT status FROM licencas WHERE cnpj = ?";
                PreparedStatement stmt = db.prepareStatement(query);
                stmt.setString(1, doc);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String status = rs.getString("status");
                    if ("PAGO".equals(status)) {
                        statusMessage.setText("CNPJ conectado com sucesso.");
                        statusMessage.setVisible(true);

                        // Navegar para Login2.fxml
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/view/Login2.fxml"));
                            Parent root = loader.load();
                            Scene scene = new Scene(root);
                            Stage stage = (Stage) accessButton.getScene().getWindow();
                            stage.setScene(scene);
                            stage.setResizable(false); // Garantir que a nova janela também não seja redimensionável

                            javafx.application.Platform.runLater(() -> {
                            stage.centerOnScreen();
                        });
                            stage.show();
                        } catch (Exception e) {
                            statusMessage.setText("Erro ao carregar a próxima tela: " + e.getMessage());
                            statusMessage.setVisible(true);
                        }
                    } else {
                        statusMessage.setText("CNPJ não pago. Acesso negado.");
                        statusMessage.setVisible(true);
                    }
                } else {
                    statusMessage.setText("CNPJ não encontrado no banco de dados.");
                    statusMessage.setVisible(true);
                }

                rs.close();
                stmt.close();
            } else {
                statusMessage.setText("Falha na conexão com o banco de dados.");
                statusMessage.setVisible(true);
            }

        } catch (SQLException e) {
            statusMessage.setText("Erro ao conectar ao banco de dados: " + e.getMessage());
            statusMessage.setVisible(true);
        }
    }
    @FXML
     private String formatCNPJ(String numbers) {
        if (numbers.isEmpty()) return "";
        
        StringBuilder formatted = new StringBuilder();
        
        for (int i = 0; i < numbers.length(); i++) {
            if (i == 2 || i == 5) {
                formatted.append(".");
            } else if (i == 8) {
                formatted.append("/");
            } else if (i == 12) {
                formatted.append("-");
            }
            formatted.append(numbers.charAt(i));
        }
        
        return formatted.toString();
    }

    @FXML
    private void handleCadastroLink() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/view/Registro1.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) accessButton.getScene().getWindow();
            stage.setResizable(false); 
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            statusMessage.setText("Erro ao carregar a tela de cadastro: " + e.getMessage());
            statusMessage.setVisible(true);
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
    private void handleClose() {
        Platform.exit();
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