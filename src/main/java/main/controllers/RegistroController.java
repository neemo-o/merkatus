package main.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import main.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.stereotype.Component;

@Component
public class RegistroController {

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private Button minimizeButton;

    @FXML
    private Button closeButton;

    @FXML
    private TextField cnpjField;

    @FXML
    private TextField razaoSocialField;

    @FXML
    private TextField nomeFantasiaField;

    @FXML
    private TextField inscricaoEstadualField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField telefoneField;

    @FXML
    private TextField cepField;

    @FXML
    private TextField ruaField;

    @FXML
    private TextField numeroField;

    @FXML
    private TextField bairroField;

    @FXML
    private TextField cidadeField;

    @FXML
    private TextField estadoField;

    @FXML
    private Button registroButton;

    @FXML
    private Text statusMessage;

    @FXML
    public void initialize() {
        // Desabilitar resize da janela
        cnpjField.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Stage stage = (Stage) newScene.getWindow();
                if (stage != null) {
                    stage.setResizable(false);
                }
            }
        });

        // Configurar formatação automática e validações em tempo real
        setupCNPJField();
        setupTelefoneField();
        setupCEPField();
        setupEmailField();
        setupTextFields();
        setupNumeroField();
        setupEstadoField();
    }

    // ==================== FORMATAÇÃO AUTOMÁTICA ====================

    private void setupCNPJField() {
        cnpjField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                String digits = newValue.replaceAll("\\D", "");
                
                if (digits.length() > 14) {
                    digits = digits.substring(0, 14);
                }
                
                String formatted = formatCNPJ(digits);
                
                if (!formatted.equals(newValue)) {
                    cnpjField.setText(formatted);
                    cnpjField.positionCaret(formatted.length());
                }
                
                validateCNPJVisual(digits);
            }
        });
        
        cnpjField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                cnpjField.setStyle(cnpjField.getStyle().replaceAll("-fx-border-color: [^;]+;", "-fx-border-color: #87ceeb;"));
            }
        });
    }

    private void setupTelefoneField() {
        telefoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                String digits = newValue.replaceAll("\\D", "");
                
                if (digits.length() > 11) {
                    digits = digits.substring(0, 11);
                }
                
                String formatted = formatTelefone(digits);
                
                if (!formatted.equals(newValue)) {
                    telefoneField.setText(formatted);
                    telefoneField.positionCaret(formatted.length());
                }
                
                validateTelefoneVisual(digits);
            }
        });
        
        telefoneField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                telefoneField.setStyle(telefoneField.getStyle().replaceAll("-fx-border-color: [^;]+;", "-fx-border-color: #87ceeb;"));
            }
        });
    }

    private void setupCEPField() {
        cepField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                String digits = newValue.replaceAll("\\D", "");
                
                if (digits.length() > 8) {
                    digits = digits.substring(0, 8);
                }
                
                String formatted = formatCEP(digits);
                
                if (!formatted.equals(newValue)) {
                    cepField.setText(formatted);
                    cepField.positionCaret(formatted.length());
                }
                
                validateCEPVisual(digits);
            }
        });
        
        cepField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                cepField.setStyle(cepField.getStyle().replaceAll("-fx-border-color: [^;]+;", "-fx-border-color: #87ceeb;"));
            }
        });
    }

    private void setupEmailField() {
        emailField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && !emailField.getText().trim().isEmpty()) {
                validateEmailVisual(emailField.getText().trim());
            } else if (isNowFocused) {
                emailField.setStyle(emailField.getStyle().replaceAll("-fx-border-color: [^;]+;", "-fx-border-color: #87ceeb;"));
            }
        });
    }

    private void setupTextFields() {
        razaoSocialField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && newVal.length() > 100) {
                razaoSocialField.setText(old);
            }
        });
        
        nomeFantasiaField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && newVal.length() > 100) {
                nomeFantasiaField.setText(old);
            }
        });
        
        inscricaoEstadualField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && newVal.length() > 20) {
                inscricaoEstadualField.setText(old);
            }
        });
        
        ruaField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && newVal.length() > 100) {
                ruaField.setText(old);
            }
        });
        
        bairroField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && newVal.length() > 50) {
                bairroField.setText(old);
            }
        });
        
        cidadeField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && newVal.length() > 50) {
                cidadeField.setText(old);
            }
        });
    }

    private void setupNumeroField() {
        numeroField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                if (newVal.length() > 10) {
                    numeroField.setText(old);
                }
            }
        });
    }

    private void setupEstadoField() {
        estadoField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                String upper = newVal.toUpperCase().replaceAll("[^A-Z]", "");
                if (upper.length() > 2) {
                    upper = upper.substring(0, 2);
                }
                if (!upper.equals(newVal)) {
                    estadoField.setText(upper);
                    estadoField.positionCaret(upper.length());
                }
            }
        });
    }

    // ==================== FORMATADORES ====================

    private String formatCNPJ(String digits) {
        if (digits.isEmpty()) return "";
        
        StringBuilder formatted = new StringBuilder();
        
        for (int i = 0; i < digits.length(); i++) {
            if (i == 2 || i == 5) {
                formatted.append(".");
            } else if (i == 8) {
                formatted.append("/");
            } else if (i == 12) {
                formatted.append("-");
            }
            formatted.append(digits.charAt(i));
        }
        
        return formatted.toString();
    }

    private String formatTelefone(String digits) {
        if (digits.isEmpty()) return "";
        
        StringBuilder formatted = new StringBuilder();
        
        formatted.append("(");
        
        for (int i = 0; i < digits.length(); i++) {
            if (i == 2) {
                formatted.append(") ");
            } else if ((digits.length() == 11 && i == 7) || (digits.length() == 10 && i == 6)) {
                formatted.append("-");
            }
            formatted.append(digits.charAt(i));
        }
        
        return formatted.toString();
    }

    private String formatCEP(String digits) {
        if (digits.isEmpty()) return "";
        
        StringBuilder formatted = new StringBuilder();
        
        for (int i = 0; i < digits.length(); i++) {
            if (i == 5) {
                formatted.append("-");
            }
            formatted.append(digits.charAt(i));
        }
        
        return formatted.toString();
    }

    // ==================== VALIDAÇÕES VISUAIS ====================

    private void validateCNPJVisual(String digits) {
        String baseStyle = "-fx-background-color: white; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-width: 2; -fx-padding: 5; -fx-font-size: 11;";
        
        if (digits.isEmpty()) {
            cnpjField.setStyle(baseStyle + " -fx-border-color: #87ceeb;");
        } else if (digits.length() == 14) {
            cnpjField.setStyle(baseStyle + " -fx-border-color: #4caf50;");
        } else {
            cnpjField.setStyle(baseStyle + " -fx-border-color: #ff9800;");
        }
    }

    private void validateTelefoneVisual(String digits) {
        String baseStyle = "-fx-background-color: white; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-width: 2; -fx-padding: 5; -fx-font-size: 11;";
        
        if (digits.isEmpty()) {
            telefoneField.setStyle(baseStyle + " -fx-border-color: #87ceeb;");
        } else if (digits.length() == 10 || digits.length() == 11) {
            telefoneField.setStyle(baseStyle + " -fx-border-color: #4caf50;");
        } else {
            telefoneField.setStyle(baseStyle + " -fx-border-color: #ff9800;");
        }
    }

    private void validateCEPVisual(String digits) {
        String baseStyle = "-fx-background-color: white; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-width: 2; -fx-padding: 5; -fx-font-size: 11;";
        
        if (digits.isEmpty()) {
            cepField.setStyle(baseStyle + " -fx-border-color: #87ceeb;");
        } else if (digits.length() == 8) {
            cepField.setStyle(baseStyle + " -fx-border-color: #4caf50;");
        } else {
            cepField.setStyle(baseStyle + " -fx-border-color: #ff9800;");
        }
    }

    private void validateEmailVisual(String email) {
        String baseStyle = "-fx-background-color: white; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-width: 2; -fx-padding: 5; -fx-font-size: 11;";
        
        if (email.isEmpty()) {
            emailField.setStyle(baseStyle + " -fx-border-color: #87ceeb;");
        } else if (email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            emailField.setStyle(baseStyle + " -fx-border-color: #4caf50;");
        } else {
            emailField.setStyle(baseStyle + " -fx-border-color: #f44336;");
        }
    }

    // ==================== VALIDAÇÃO NO SUBMIT ====================

    private boolean validarCampos() {
        boolean valido = true;
        String mensagemErro = "";

        String cnpjDigits = cnpjField.getText().replaceAll("\\D", "");
        if (cnpjDigits.isEmpty()) {
            mensagemErro = "CNPJ é obrigatório";
            valido = false;
            setFieldError(cnpjField);
        } else if (cnpjDigits.length() != 14) {
            mensagemErro = "CNPJ deve conter 14 dígitos";
            valido = false;
            setFieldError(cnpjField);
        }

        if (razaoSocialField.getText().trim().isEmpty()) {
            if (!mensagemErro.isEmpty()) mensagemErro += " | ";
            mensagemErro += "Razão Social é obrigatória";
            valido = false;
            setFieldError(razaoSocialField);
        }

        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            if (!mensagemErro.isEmpty()) mensagemErro += " | ";
            mensagemErro += "E-mail é obrigatório";
            valido = false;
            setFieldError(emailField);
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            if (!mensagemErro.isEmpty()) mensagemErro += " | ";
            mensagemErro += "E-mail inválido";
            valido = false;
            setFieldError(emailField);
        }

        String telefoneDigits = telefoneField.getText().replaceAll("\\D", "");
        if (!telefoneDigits.isEmpty() && telefoneDigits.length() != 10 && telefoneDigits.length() != 11) {
            if (!mensagemErro.isEmpty()) mensagemErro += " | ";
            mensagemErro += "Telefone deve ter 10 ou 11 dígitos";
            valido = false;
            setFieldError(telefoneField);
        }

        String cepDigits = cepField.getText().replaceAll("\\D", "");
        if (!cepDigits.isEmpty() && cepDigits.length() != 8) {
            if (!mensagemErro.isEmpty()) mensagemErro += " | ";
            mensagemErro += "CEP deve ter 8 dígitos";
            valido = false;
            setFieldError(cepField);
        }

        if (!valido) {
            statusMessage.setFill(javafx.scene.paint.Color.web("#f44336"));
            statusMessage.setText(mensagemErro);
            statusMessage.setVisible(true);
        }

        return valido;
    }

    private void setFieldError(TextField field) {
        String baseStyle = "-fx-background-color: white; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-width: 2; -fx-padding: 5; -fx-font-size: 11;";
        field.setStyle(baseStyle + " -fx-border-color: #f44336;");
    }

    // ==================== CADASTRO ====================

    @FXML
    private void handleRegistro() {
        System.out.println("=== INICIANDO CADASTRO DE EMPRESA ===");
        
        statusMessage.setVisible(false);
        
        if (!validarCampos()) {
            System.out.println("Validação falhou");
            return;
        }

        String cnpj = cnpjField.getText().trim();
        String razaoSocial = razaoSocialField.getText().trim();
        String nomeFantasia = nomeFantasiaField.getText().trim();
        String inscricaoEstadual = inscricaoEstadualField.getText().trim();
        String email = emailField.getText().trim();
        String telefone = telefoneField.getText().trim();
        String cep = cepField.getText().trim();
        String rua = ruaField.getText().trim();
        String numero = numeroField.getText().trim();
        String bairro = bairroField.getText().trim();
        String cidade = cidadeField.getText().trim();
        String estado = estadoField.getText().trim();

        System.out.println("Dados validados — CNPJ: " + cnpj);

        Connection db = null;
        try {
            db = DatabaseConnection.getConnectionLicenses();
            
            if (db == null) {
                System.err.println("Falha ao conectar ao banco de dados");
                statusMessage.setFill(javafx.scene.paint.Color.web("#f44336"));
                statusMessage.setText("Falha na conexão com o banco.");
                statusMessage.setVisible(true);
                return;
            }

            System.out.println("Conexão estabelecida com o banco");
            db.setAutoCommit(false);

            // ATUALIZADO: Inserir na tabela clientes_licenciados (substitui a antiga licencas)
            String queryCliente = "INSERT INTO clientes_licenciados (cnpj, razao_social, nome_fantasia, " +
                                "inscricao_estadual, telefone, email, logradouro, numero, bairro, " +
                                "cidade, estado, cep, ativo) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE)";
            
            PreparedStatement stmtCliente = db.prepareStatement(queryCliente, Statement.RETURN_GENERATED_KEYS);
            stmtCliente.setString(1, cnpj);
            stmtCliente.setString(2, razaoSocial);
            stmtCliente.setString(3, nomeFantasia.isEmpty() ? null : nomeFantasia);
            stmtCliente.setString(4, inscricaoEstadual.isEmpty() ? null : inscricaoEstadual);
            stmtCliente.setString(5, telefone.isEmpty() ? null : telefone);
            stmtCliente.setString(6, email);
            stmtCliente.setString(7, rua.isEmpty() ? null : rua);
            stmtCliente.setString(8, numero.isEmpty() ? null : numero);
            stmtCliente.setString(9, bairro.isEmpty() ? null : bairro);
            stmtCliente.setString(10, cidade.isEmpty() ? null : cidade);
            stmtCliente.setString(11, estado.isEmpty() ? null : estado);
            stmtCliente.setString(12, cep.isEmpty() ? null : cep);

            System.out.println("Executando INSERT em clientes_licenciados...");
            int clienteRows = stmtCliente.executeUpdate();

            if (clienteRows > 0) {
                // Recuperar o ID do cliente inserido
                ResultSet generatedKeys = stmtCliente.getGeneratedKeys();
                int idCliente = 0;
                if (generatedKeys.next()) {
                    idCliente = generatedKeys.getInt(1);
                }
                generatedKeys.close();
                stmtCliente.close();

                // NOVO: Criar a licença para este cliente (padrão: 1 PDV, 1 Gerenciador, 1 ano)
                String queryLicenca = "INSERT INTO licencas (id_cliente, chave_ativacao, " +
                                    "qtd_pdv_incluso, qtd_gerenciador_incluso, qtd_pdv_total, " +
                                    "qtd_gerenciador_total, data_validade, status) " +
                                    "VALUES (?, ?, 1, 1, 1, 1, CURRENT_DATE + INTERVAL '1 year', 'ATIVA')";
                
                // Gerar chave de ativação simples (TODO: melhorar geração de chave)
                String chaveAtivacao = java.util.UUID.randomUUID().toString().substring(0, 20).toUpperCase();

                PreparedStatement stmtLicenca = db.prepareStatement(queryLicenca);
                stmtLicenca.setInt(1, idCliente);
                stmtLicenca.setString(2, chaveAtivacao);
                stmtLicenca.executeUpdate();
                stmtLicenca.close();

                db.commit();
                System.out.println("CADASTRO REALIZADO COM SUCESSO!");
                
                statusMessage.setFill(javafx.scene.paint.Color.web("#4caf50"));
                statusMessage.setText("Cadastro realizado com sucesso!");
                statusMessage.setVisible(true);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Cadastro Concluído");
                alert.setHeaderText("Empresa cadastrada com sucesso!");
                alert.setContentText(
                    "Sua empresa foi cadastrada no sistema.\n\n" +
                    "CNPJ: " + cnpj + "\n" +
                    "Razão Social: " + razaoSocial + "\n" +
                    "Chave de Ativação: " + chaveAtivacao + "\n\n" +
                    "Guarde a chave de ativação! Ela será usada para ativar o sistema.\n\n" +
                    "Você já pode fazer login no sistema usando seu CNPJ."
                );
                alert.showAndWait();

                Platform.runLater(() -> {
                    try {
                        Thread.sleep(500);
                        handleLoginLink();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            } else {
                stmtCliente.close();
                db.rollback();
                System.err.println("Erro: Nenhuma linha foi inserida");
                statusMessage.setFill(javafx.scene.paint.Color.web("#f44336"));
                statusMessage.setText("Erro ao cadastrar empresa.");
                statusMessage.setVisible(true);
            }

        } catch (SQLException e) {
            System.err.println("ERRO SQL: " + e.getMessage());
            e.printStackTrace();
            
            if (db != null) {
                try {
                    db.rollback();
                    System.out.println("Rollback executado");
                } catch (SQLException rollbackEx) {
                    System.err.println("Erro no rollback: " + rollbackEx.getMessage());
                }
            }
            
            String errorMsg = e.getMessage();
            if (errorMsg.contains("duplicate key") || errorMsg.contains("já existe")) {
                statusMessage.setText("CNPJ já cadastrado no sistema");
            } else {
                statusMessage.setText("Erro: " + e.getMessage());
            }
            statusMessage.setFill(javafx.scene.paint.Color.web("#f44336"));
            statusMessage.setVisible(true);
            
        } finally {
            if (db != null) {
                try {
                    db.setAutoCommit(true);
                    db.close();
                    System.out.println("Conexão fechada");
                } catch (SQLException closeEx) {
                    System.err.println("Erro ao fechar conexão: " + closeEx.getMessage());
                }
            }
        }
    }

    @FXML
    private void handleLoginLink() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/view/Login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) registroButton.getScene().getWindow();
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            statusMessage.setText("Erro ao voltar para login: " + e.getMessage());
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