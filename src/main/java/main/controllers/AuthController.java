package main.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import main.database.auth.Auth;

@Component
public class AuthController {

    private double xOffset = 0;
    private double yOffset = 0;

    @Autowired
    private Auth auth;

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

    private boolean isFormatting = false;

    @FXML
    public void initialize() {
        documentField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (isFormatting || newValue == null)
                return;

            isFormatting = true;

            Platform.runLater(() -> {
                try {
                    String numbers = newValue.replaceAll("[^0-9]", "");

                    if (numbers.length() > 14) {
                        numbers = numbers.substring(0, 14);
                    }

                    String formatted = formatCNPJ(numbers);

                    if (!formatted.equals(documentField.getText())) {
                        documentField.setText(formatted);
                        documentField.positionCaret(formatted.length());
                    }
                } finally {
                    isFormatting = false;
                }
            });
        });
    }

    @FXML
    private void handleAccessButton() {
        String doc = documentField.getText().trim();

        if (doc.length() != 18) {
            statusMessage.setText("CNPJ deve ter exatamente 18 caracteres.");
            statusMessage.setVisible(true);
            return;
        }

        if (!doc.matches("\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}")) {
            statusMessage.setText("Formato de CNPJ inválido. Use: XX.XXX.XXX/XXXX-XX");
            statusMessage.setVisible(true);
            return;
        }

        if (auth.validateCNPJ(doc)) {
            statusMessage.setText("CNPJ conectado com sucesso.");
            statusMessage.setVisible(true);

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/view/Login2.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                Stage stage = (Stage) accessButton.getScene().getWindow();
                stage.setScene(scene);
                stage.setResizable(false);

                javafx.application.Platform.runLater(() -> {
                    stage.centerOnScreen();
                });
                stage.show();
            } catch (Exception e) {
                System.out.println();
                statusMessage.setText("Erro ao carregar a próxima tela: " + e.getMessage());
                statusMessage.setVisible(true);
            }
        } else {
            statusMessage.setText("CNPJ não encontrado ou sem licença ativa.");
            statusMessage.setVisible(true);
        }
    }

    @FXML
    private String formatCNPJ(String numbers) {
        if (numbers.isEmpty())
            return "";

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