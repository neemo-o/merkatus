package main.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.util.Duration;
import main.database.FornecedorDAO;
import main.models.Fornecedor;
import main.models.Endereco;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FornecedoresController {

    @FXML private TextField searchField;
    @FXML private TableView<Fornecedor> tableFornecedores;
    @FXML private TableColumn<Fornecedor, Integer> colId;
    @FXML private TableColumn<Fornecedor, String> colNome;
    @FXML private TableColumn<Fornecedor, String> colCnpj;
    @FXML private TableColumn<Fornecedor, String> colTelefone;
    @FXML private TableColumn<Fornecedor, String> colEmail;
    @FXML private TableColumn<Fornecedor, String> colEndereco;
    @FXML private Button btnNovo;
    @FXML private Button btnEditar;
    @FXML private Button btnExcluir;
    @FXML private Label lblTotal;
    @FXML private ScrollPane formContainer;
    @FXML private Text lblFormTitle;
    @FXML private TextField txtNome;
    @FXML private TextField txtCnpj;
    @FXML private TextField txtTelefone;
    @FXML private TextField txtEmail;
    @FXML private TextField txtLogradouro;
    @FXML private TextField txtNumero;
    @FXML private TextField txtComplemento;
    @FXML private TextField txtBairro;
    @FXML private TextField txtCidade;
    @FXML private TextField txtEstado;
    @FXML private TextField txtCep;
    @FXML private Button btnSalvar;
    @FXML private Button btnCancelar;

    private FornecedorDAO fornecedorDAO;
    private ObservableList<Fornecedor> fornecedores;
    private Fornecedor fornecedorSelecionado;
    private boolean modoEdicao = false;

    @FXML
    void initialize() {
        fornecedorDAO = new FornecedorDAO();

        configurarTableView();
        aplicarMascaras();
        aplicarValidacoes();
        
        carregarFornecedores();
        atualizarTotal();
        configurarBusca();

        formContainer.setVisible(false);
        formContainer.setManaged(false);
    }

    private void aplicarMascaras() {
        // Máscara CNPJ
        txtCnpj.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) return;
            String numbers = newVal.replaceAll("[^0-9]", "");
            if (numbers.length() > 14) numbers = numbers.substring(0, 14);
            
            String formatted = "";
            for (int i = 0; i < numbers.length(); i++) {
                if (i == 2 || i == 5) formatted += ".";
                if (i == 8) formatted += "/";
                if (i == 12) formatted += "-";
                formatted += numbers.charAt(i);
            }
            
            if (!formatted.equals(newVal)) {
                txtCnpj.setText(formatted);
                txtCnpj.positionCaret(formatted.length());
            }
        });

        // Máscara Telefone
        txtTelefone.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) return;
            String numbers = newVal.replaceAll("[^0-9]", "");
            if (numbers.length() > 11) numbers = numbers.substring(0, 11);
            
            String formatted = "";
            if (numbers.length() > 0) {
                formatted += "(";
                formatted += numbers.substring(0, Math.min(2, numbers.length()));
                if (numbers.length() > 2) {
                    formatted += ") ";
                    if (numbers.length() <= 10) {
                        formatted += numbers.substring(2, Math.min(6, numbers.length()));
                        if (numbers.length() > 6) {
                            formatted += "-";
                            formatted += numbers.substring(6);
                        }
                    } else {
                        formatted += numbers.substring(2, Math.min(7, numbers.length()));
                        if (numbers.length() > 7) {
                            formatted += "-";
                            formatted += numbers.substring(7);
                        }
                    }
                }
            }
            
            if (!formatted.equals(newVal)) {
                txtTelefone.setText(formatted);
                txtTelefone.positionCaret(formatted.length());
            }
        });

        // Máscara CEP - CORREÇÃO: Formato correto para o banco (XXXXX-XXX)
        txtCep.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) return;
            String numbers = newVal.replaceAll("[^0-9]", "");
            if (numbers.length() > 8) numbers = numbers.substring(0, 8);
            
            String formatted = "";
            for (int i = 0; i < numbers.length(); i++) {
                if (i == 5) formatted += "-";
                formatted += numbers.charAt(i);
            }
            
            if (!formatted.equals(newVal)) {
                txtCep.setText(formatted);
                txtCep.positionCaret(formatted.length());
            }
        });

        // Limites de caracteres
        limitarCaracteres(txtNome, 100);
        limitarCaracteres(txtEmail, 100);
        limitarCaracteres(txtLogradouro, 255);
        limitarCaracteres(txtNumero, 10);
        limitarCaracteres(txtComplemento, 255);
        limitarCaracteres(txtBairro, 255);
        limitarCaracteres(txtCidade, 255);
        limitarCaracteres(txtEstado, 2);
        
        // Estado em maiúsculas
        txtEstado.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && !newVal.equals(newVal.toUpperCase())) {
                txtEstado.setText(newVal.toUpperCase());
            }
        });
    }

    private void aplicarValidacoes() {
        validarCampoObrigatorio(txtNome);
        validarCampoObrigatorio(txtCnpj);
        validarCampoObrigatorio(txtTelefone);
        validarCampoObrigatorio(txtLogradouro);
        validarCampoObrigatorio(txtBairro);
        validarCampoObrigatorio(txtCidade);
        validarCampoObrigatorio(txtEstado);
        validarCampoObrigatorio(txtCep);

        // Validação email
        txtEmail.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && !getTextSafe(txtEmail).trim().isEmpty()) {
                String email = getTextSafe(txtEmail).trim();
                if (email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                    txtEmail.setStyle("-fx-border-color: #7cb342; -fx-border-width: 2; -fx-background-color: white; -fx-padding: 6;");
                } else {
                    txtEmail.setStyle("-fx-border-color: #e57373; -fx-border-width: 2; -fx-background-color: white; -fx-padding: 6;");
                }
            } else if (getTextSafe(txtEmail).trim().isEmpty()) {
                txtEmail.setStyle("-fx-background-color: white; -fx-border-color: #d0d0d0; -fx-border-width: 1; -fx-padding: 6;");
            }
        });

        // Validação CNPJ
        txtCnpj.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && !getTextSafe(txtCnpj).trim().isEmpty()) {
                String cnpj = extrairNumeros(getTextSafe(txtCnpj));
                if (cnpj.length() == 14) {
                    txtCnpj.setStyle("-fx-border-color: #7cb342; -fx-border-width: 2; -fx-background-color: white; -fx-padding: 6;");
                } else {
                    txtCnpj.setStyle("-fx-border-color: #e57373; -fx-border-width: 2; -fx-background-color: white; -fx-padding: 6;");
                }
            }
        });
    }

    private void validarCampoObrigatorio(TextField field) {
        field.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                if (getTextSafe(field).trim().isEmpty()) {
                    field.setStyle("-fx-border-color: #e57373; -fx-border-width: 2; -fx-background-color: white; -fx-padding: 6;");
                } else {
                    field.setStyle("-fx-border-color: #7cb342; -fx-border-width: 2; -fx-background-color: white; -fx-padding: 6;");
                }
            }
        });

        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty() && field.getStyle().contains("#e57373")) {
                field.setStyle("-fx-border-color: #7cb342; -fx-border-width: 2; -fx-background-color: white; -fx-padding: 6;");
            }
        });
    }

    private void limitarCaracteres(TextField field, int max) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > max) {
                field.setText(oldVal);
            }
        });
    }

    private String extrairNumeros(String texto) {
        return texto == null ? "" : texto.replaceAll("[^0-9]", "");
    }

    private void limparEstilos() {
        String estiloPadrao = "-fx-background-color: white; -fx-border-color: #d0d0d0; -fx-border-width: 1; -fx-padding: 6;";
        txtNome.setStyle(estiloPadrao);
        txtCnpj.setStyle(estiloPadrao);
        txtEmail.setStyle(estiloPadrao);
        txtLogradouro.setStyle(estiloPadrao);
        txtNumero.setStyle(estiloPadrao);
        txtComplemento.setStyle(estiloPadrao);
        txtBairro.setStyle(estiloPadrao);
        txtCidade.setStyle(estiloPadrao);
        txtEstado.setStyle(estiloPadrao);
        txtCep.setStyle(estiloPadrao);
        txtTelefone.setStyle(estiloPadrao);
    }

    private String getTextSafe(TextField field) {
        String text = field.getText();
        return text != null ? text : "";
    }

    private void mostrarNotificacao(String titulo, String mensagem, String tipo) {
        if (txtNome == null || txtNome.getScene() == null || txtNome.getScene().getWindow() == null) {
            System.out.println(tipo.toUpperCase() + ": " + titulo + " - " + mensagem);
            return;
        }
        
        Popup popup = new Popup();
        String cor = tipo.equals("sucesso") ? "#7cb342" : tipo.equals("erro") ? "#e57373" : "#ffb74d";
        String icone = tipo.equals("sucesso") ? "✓" : tipo.equals("erro") ? "✗" : "⚠";
        
        VBox container = new VBox(5);
        container.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 5;" +
            "-fx-border-color: " + cor + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 5;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);"
        );
        container.setPadding(new Insets(15));
        container.setMaxWidth(350);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label iconLabel = new Label(icone);
        iconLabel.setStyle("-fx-text-fill: " + cor + "; -fx-font-size: 20px; -fx-font-weight: bold;");
        
        Label titleLabel = new Label(titulo);
        titleLabel.setStyle("-fx-text-fill: " + cor + "; -fx-font-size: 14px; -fx-font-weight: bold;");
        titleLabel.setFont(Font.font("Segoe UI", 14));
        
        header.getChildren().addAll(iconLabel, titleLabel);

        Label messageLabel = new Label(mensagem);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-text-fill: #333; -fx-font-size: 12px;");
        messageLabel.setFont(Font.font("Segoe UI", 12));
        messageLabel.setMaxWidth(320);

        container.getChildren().addAll(header, messageLabel);
        popup.getContent().add(container);

        popup.setAutoHide(true);
        popup.show(txtNome.getScene().getWindow(), 
            txtNome.getScene().getWindow().getX() + txtNome.getScene().getWindow().getWidth() - 380, 
            txtNome.getScene().getWindow().getY() + 60
        );

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), container);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        PauseTransition pause = new PauseTransition(Duration.millis(3000));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), container);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> popup.hide());

        SequentialTransition sequence = new SequentialTransition(fadeIn, pause, fadeOut);
        sequence.play();
    }

    private void mostrarListaErros(List<String> erros) {
        StringBuilder mensagem = new StringBuilder();
        for (int i = 0; i < erros.size(); i++) {
            mensagem.append("• ").append(erros.get(i));
            if (i < erros.size() - 1) mensagem.append("\n");
        }
        mostrarNotificacao("Corrija os erros", mensagem.toString(), "erro");
    }

    private void configurarTableView() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idFornecedor"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("razaoSocial"));
        
        // CORREÇÃO: Formatar CNPJ na exibição
        colCnpj.setCellValueFactory(cellData -> {
            String cnpj = cellData.getValue().getCnpj();
            if (cnpj != null && cnpj.length() == 14) {
                String formatted = cnpj.substring(0, 2) + "." + cnpj.substring(2, 5) + "." + 
                                   cnpj.substring(5, 8) + "/" + cnpj.substring(8, 12) + "-" + cnpj.substring(12);
                return new javafx.beans.property.SimpleStringProperty(formatted);
            }
            return new javafx.beans.property.SimpleStringProperty(cnpj);
        });
        
        colTelefone.setCellValueFactory(new PropertyValueFactory<>("telefone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEndereco.setCellValueFactory(cellData -> {
            Fornecedor fornecedor = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(fornecedor.getEnderecoCompleto());
        });

        fornecedores = FXCollections.observableArrayList();
        tableFornecedores.setItems(fornecedores);
    }

    private void configurarBusca() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filtrarFornecedores(newValue);
        });
    }

    private void carregarFornecedores() {
        try {
            List<Fornecedor> listaFornecedores = fornecedorDAO.buscarTodos();
            fornecedores.clear();
            fornecedores.addAll(listaFornecedores);
        } catch (SQLException e) {
            System.err.println("Erro ao carregar fornecedores: " + e.getMessage());
            e.printStackTrace();
            mostrarNotificacao("Erro", "Erro ao carregar fornecedores: " + e.getMessage(), "erro");
        }
    }

    private void atualizarTotal() {
        try {
            int total = fornecedorDAO.contarTotal();
            lblTotal.setText("Total: " + total + " fornecedor(es)");
        } catch (SQLException e) {
            lblTotal.setText("Total: Erro ao contar");
        }
    }

    private void filtrarFornecedores(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            carregarFornecedores();
        } else {
            try {
                List<Fornecedor> listaFiltrada = fornecedorDAO.buscarPorRazaoSocial(filtro.trim());
                fornecedores.clear();
                fornecedores.addAll(listaFiltrada);
            } catch (SQLException e) {
                mostrarNotificacao("Erro", "Erro ao filtrar fornecedores", "erro");
            }
        }
    }

    @FXML
    void handleNovo() {
        modoEdicao = false;
        fornecedorSelecionado = null;
        limparFormulario();
        lblFormTitle.setText("Novo Fornecedor");
        mostrarFormulario(true);
    }

    @FXML
    void handleEditar() {
        fornecedorSelecionado = tableFornecedores.getSelectionModel().getSelectedItem();
        if (fornecedorSelecionado == null) {
            mostrarNotificacao("Atenção", "Selecione um fornecedor para editar", "aviso");
            return;
        }
        modoEdicao = true;
        preencherFormulario(fornecedorSelecionado);
        lblFormTitle.setText("Editar Fornecedor");
        mostrarFormulario(true);
    }

    @FXML
    void handleExcluir() {
        fornecedorSelecionado = tableFornecedores.getSelectionModel().getSelectedItem();
        if (fornecedorSelecionado == null) {
            mostrarNotificacao("Atenção", "Selecione um fornecedor para excluir", "aviso");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar exclusão");
        confirmacao.setHeaderText("Deseja realmente excluir este fornecedor?");
        confirmacao.setContentText("Fornecedor: " + fornecedorSelecionado.getRazaoSocial());

        if (confirmacao.showAndWait().get() == ButtonType.OK) {
            try {
                if (fornecedorDAO.excluir(fornecedorSelecionado.getIdFornecedor())) {
                    carregarFornecedores();
                    atualizarTotal();
                    mostrarNotificacao("Sucesso", "Fornecedor excluído com sucesso!", "sucesso");
                } else {
                    mostrarNotificacao("Erro", "Não foi possível excluir o fornecedor", "erro");
                }
            } catch (SQLException e) {
                mostrarNotificacao("Erro", "Erro ao excluir fornecedor: " + e.getMessage(), "erro");
            }
        }
    }

    @FXML
    void handleSalvar() {
        List<String> erros = validarFormulario();
        
        if (!erros.isEmpty()) {
            mostrarListaErros(erros);
            return;
        }

        try {
            Fornecedor fornecedor = criarFornecedorDoFormulario();
            boolean sucesso;
            
            if (modoEdicao) {
                sucesso = fornecedorDAO.atualizar(fornecedor);
            } else {
                sucesso = fornecedorDAO.inserir(fornecedor);
            }

            if (sucesso) {
                carregarFornecedores();
                atualizarTotal();
                mostrarFormulario(false);
                mostrarNotificacao("Sucesso", 
                    modoEdicao ? "Fornecedor atualizado!" : "Fornecedor cadastrado!", "sucesso");
            } else {
                mostrarNotificacao("Erro", "Não foi possível salvar o fornecedor", "erro");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao salvar fornecedor: " + e.getMessage());
            e.printStackTrace();
            mostrarNotificacao("Erro", "Erro ao salvar fornecedor: " + e.getMessage(), "erro");
        }
    }

    @FXML
    void handleCancelar() {
        mostrarFormulario(false);
    }

    private void mostrarFormulario(boolean mostrar) {
        formContainer.setVisible(mostrar);
        formContainer.setManaged(mostrar);
        tableFornecedores.setVisible(!mostrar);
        tableFornecedores.setManaged(!mostrar);
        if (!mostrar) limparEstilos();
    }

    private void limparFormulario() {
        txtNome.clear();
        txtCnpj.clear();
        txtTelefone.clear();
        txtEmail.clear();
        txtLogradouro.clear();
        txtNumero.clear();
        txtComplemento.clear();
        txtBairro.clear();
        txtCidade.clear();
        txtEstado.clear();
        txtCep.clear();
        limparEstilos();
    }

    private void preencherFormulario(Fornecedor fornecedor) {
        txtNome.setText(fornecedor.getRazaoSocial());
        
        // CORREÇÃO: Formatar CNPJ
        String cnpj = fornecedor.getCnpj();
        if (cnpj != null && cnpj.length() == 14) {
            cnpj = cnpj.substring(0, 2) + "." + cnpj.substring(2, 5) + "." + 
                   cnpj.substring(5, 8) + "/" + cnpj.substring(8, 12) + "-" + cnpj.substring(12);
        }
        txtCnpj.setText(cnpj);
        
        txtTelefone.setText(fornecedor.getTelefone());
        txtEmail.setText(fornecedor.getEmail());
        
        if (fornecedor.getEndereco() != null) {
            txtLogradouro.setText(fornecedor.getEndereco().getLogradouro());
            txtNumero.setText(fornecedor.getEndereco().getNumero());
            txtComplemento.setText(fornecedor.getEndereco().getComplemento());
            txtBairro.setText(fornecedor.getEndereco().getBairro());
            txtCidade.setText(fornecedor.getEndereco().getCidade());
            txtEstado.setText(fornecedor.getEndereco().getEstado());
            txtCep.setText(fornecedor.getEndereco().getCep());
        }
    }

    private Fornecedor criarFornecedorDoFormulario() {
        Fornecedor fornecedor = modoEdicao ? fornecedorSelecionado : new Fornecedor();

        fornecedor.setRazaoSocial(getTextSafe(txtNome).trim());
        fornecedor.setCnpj(extrairNumeros(getTextSafe(txtCnpj)));
        fornecedor.setTelefone(extrairNumeros(getTextSafe(txtTelefone)));
        fornecedor.setEmail(getTextSafe(txtEmail).trim().isEmpty() ? null : getTextSafe(txtEmail).trim());

        // CORREÇÃO: Criar endereço com CEP formatado corretamente (XXXXX-XXX)
        String cepFormatado = getTextSafe(txtCep).trim();
        String cepNumeros = extrairNumeros(cepFormatado);
        if (cepNumeros.length() == 8) {
            cepFormatado = cepNumeros.substring(0, 5) + "-" + cepNumeros.substring(5);
        }

        Endereco endereco;
        if (modoEdicao && fornecedorSelecionado.getEndereco() != null) {
            // Manter o ID do endereço existente
            endereco = fornecedorSelecionado.getEndereco();
            endereco.setLogradouro(getTextSafe(txtLogradouro).trim());
            endereco.setNumero(getTextSafe(txtNumero).trim().isEmpty() ? null : getTextSafe(txtNumero).trim());
            endereco.setComplemento(getTextSafe(txtComplemento).trim().isEmpty() ? null : getTextSafe(txtComplemento).trim());
            endereco.setBairro(getTextSafe(txtBairro).trim());
            endereco.setCidade(getTextSafe(txtCidade).trim());
            endereco.setEstado(getTextSafe(txtEstado).trim().toUpperCase());
            endereco.setCep(cepFormatado);
        } else {
            endereco = new Endereco(
                getTextSafe(txtLogradouro).trim(),
                getTextSafe(txtNumero).trim().isEmpty() ? null : getTextSafe(txtNumero).trim(),
                getTextSafe(txtComplemento).trim().isEmpty() ? null : getTextSafe(txtComplemento).trim(),
                getTextSafe(txtBairro).trim(),
                getTextSafe(txtCidade).trim(),
                getTextSafe(txtEstado).trim().toUpperCase(),
                cepFormatado
            );
        }
        fornecedor.setEndereco(endereco);

        return fornecedor;
    }

    private List<String> validarFormulario() {
        List<String> erros = new ArrayList<>();

        if (getTextSafe(txtNome).trim().isEmpty()) {
            erros.add("Razão social é obrigatória");
        }

        if (getTextSafe(txtCnpj).trim().isEmpty()) {
            erros.add("CNPJ é obrigatório");
        } else {
            String cnpj = extrairNumeros(getTextSafe(txtCnpj));
            if (cnpj.length() != 14) {
                erros.add("CNPJ deve ter 14 dígitos");
            } else {
                try {
                    Integer idExcluir = modoEdicao ? fornecedorSelecionado.getIdFornecedor() : null;
                    if (fornecedorDAO.cnpjExiste(cnpj, idExcluir)) {
                        erros.add("CNPJ já cadastrado");
                    }
                } catch (SQLException e) {
                    erros.add("Erro ao verificar CNPJ: " + e.getMessage());
                }
            }
        }

        if (!getTextSafe(txtEmail).trim().isEmpty()) {
            String email = getTextSafe(txtEmail).trim();
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                erros.add("E-mail inválido");
            }
        }

        if (getTextSafe(txtTelefone).trim().isEmpty()) {
            erros.add("Telefone é obrigatório");
        } else {
            String telefone = extrairNumeros(getTextSafe(txtTelefone));
            if (telefone.length() < 10) {
                erros.add("Telefone deve ter no mínimo 10 dígitos");
            }
        }

        // Validações de endereço (obrigatório para fornecedor)
        if (getTextSafe(txtLogradouro).trim().isEmpty()) {
            erros.add("Logradouro é obrigatório");
        }

        if (getTextSafe(txtBairro).trim().isEmpty()) {
            erros.add("Bairro é obrigatório");
        }

        if (getTextSafe(txtCidade).trim().isEmpty()) {
            erros.add("Cidade é obrigatória");
        }

        if (getTextSafe(txtEstado).trim().isEmpty()) {
            erros.add("Estado é obrigatório");
        } else if (getTextSafe(txtEstado).trim().length() != 2) {
            erros.add("Estado deve ter 2 caracteres (UF)");
        }

        if (getTextSafe(txtCep).trim().isEmpty()) {
            erros.add("CEP é obrigatório");
        } else {
            String cep = extrairNumeros(getTextSafe(txtCep));
            if (cep.length() != 8) {
                erros.add("CEP deve ter 8 dígitos");
            }
        }

        return erros;
    }
}
