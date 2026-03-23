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
import main.database.ClienteDAO;
import main.models.Cliente;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClientesController {

    @FXML private TextField searchField;
    @FXML private TableView<Cliente> tableClientes;
    @FXML private TableColumn<Cliente, String> colId;
    @FXML private TableColumn<Cliente, String> colNome;
    @FXML private TableColumn<Cliente, String> colCpfCnpj;
    @FXML private TableColumn<Cliente, String> colTelefone;
    @FXML private TableColumn<Cliente, String> colEmail;
    @FXML private TableColumn<Cliente, String> colEndereco;
    @FXML private TableColumn<Cliente, String> colStatus;
    @FXML private Button btnNovo;
    @FXML private Button btnEditar;
    @FXML private Button btnExcluir;
    @FXML private Label lblTotal;
    @FXML private ScrollPane formContainer;
    @FXML private Text lblFormTitle;
    @FXML private TextField txtNome;
    @FXML private TextField txtCpf;
    @FXML private TextField txtTelefone;
    @FXML private TextField txtEmail;
    @FXML private TextField txtLogradouro;
    @FXML private TextField txtNumero;
    @FXML private TextField txtComplemento;
    @FXML private TextField txtBairro;
    @FXML private TextField txtCidade;
    @FXML private TextField txtEstado;
    @FXML private TextField txtCep;
    @FXML private ComboBox<String> cbStatus;
    @FXML private Button btnSalvar;
    @FXML private Button btnCancelar;

    private ClienteDAO clienteDAO;
    private ObservableList<Cliente> clientes;
    private Cliente clienteSelecionado;
    private boolean modoEdicao = false;

    @FXML
    void initialize() {
        clienteDAO = new ClienteDAO();
        configurarTableView();
        aplicarMascaras();
        aplicarValidacoes();

        // CORREÇÃO: Configurar ComboBox antes de carregar dados
        cbStatus.setItems(FXCollections.observableArrayList("PAGO", "NAO_PAGO"));
        cbStatus.setValue("PAGO"); // Valor padrão

        carregarClientes();
        atualizarTotal();
        configurarBusca();

        formContainer.setVisible(false);
        formContainer.setManaged(false);
    }

    private void aplicarMascaras() {
        // Máscara CPF
        txtCpf.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) return;
            String numbers = newVal.replaceAll("[^0-9]", "");
            if (numbers.length() > 11) numbers = numbers.substring(0, 11);

            String formatted = "";
            for (int i = 0; i < numbers.length(); i++) {
                if (i == 3 || i == 6) formatted += ".";
                if (i == 9) formatted += "-";
                formatted += numbers.charAt(i);
            }

            if (!formatted.equals(newVal)) {
                txtCpf.setText(formatted);
                txtCpf.positionCaret(formatted.length());
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

        // Máscara CEP
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
        limitarCaracteres(txtLogradouro, 100);
        limitarCaracteres(txtNumero, 10);
        limitarCaracteres(txtComplemento, 50);
        limitarCaracteres(txtBairro, 50);
        limitarCaracteres(txtCidade, 50);
        limitarCaracteres(txtEstado, 2);

        // Estado em maiúsculas
        txtEstado.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && !newVal.equals(newVal.toUpperCase())) {
                txtEstado.setText(newVal.toUpperCase());
            }
        });
    }

    private void aplicarValidacoes() {
        // Validação campos obrigatórios
        validarCampoObrigatorio(txtNome);
        validarCampoObrigatorio(txtCpf);

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

    private String getTextSafe(TextField field) {
        String text = field.getText();
        return text != null ? text : "";
    }

    private void limparEstilos() {
        String estiloPadrao = "-fx-background-color: white; -fx-border-color: #d0d0d0; -fx-border-width: 1; -fx-padding: 6;";
        txtNome.setStyle(estiloPadrao);
        txtCpf.setStyle(estiloPadrao);
        txtEmail.setStyle(estiloPadrao);
    }

    private void mostrarNotificacao(String titulo, String mensagem, String tipo) {
        // Verificação de segurança
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
        // CORREÇÃO: Usar CNPJ como ID já que não existe campo id_cliente no banco
        colId.setCellValueFactory(new PropertyValueFactory<>("cnpj"));

        // Coluna Nome (colNome)
        colNome.setCellValueFactory(new PropertyValueFactory<>("razaoSocial"));

        // Coluna CPF (colCpfCnpj) - formatado
        colCpfCnpj.setCellValueFactory(cellData -> {
            String cpf = cellData.getValue().getCnpj();
            if (cpf != null && cpf.length() == 11) {
                String formatted = cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + 
                                   cpf.substring(6, 9) + "-" + cpf.substring(9);
                return new javafx.beans.property.SimpleStringProperty(formatted);
            }
            return new javafx.beans.property.SimpleStringProperty(cpf);
        });

        // Coluna Telefone
        colTelefone.setCellValueFactory(new PropertyValueFactory<>("telefoneCliente"));

        // Coluna Email
        colEmail.setCellValueFactory(new PropertyValueFactory<>("emailCliente"));

        // Coluna Endereço Completo - construída dinamicamente
        colEndereco.setCellValueFactory(cellData -> {
            Cliente cliente = cellData.getValue();
            StringBuilder endereco = new StringBuilder();

            if (cliente.getLogradouro() != null && !cliente.getLogradouro().trim().isEmpty()) {
                endereco.append(cliente.getLogradouro());
            }
            if (cliente.getNumero() != null && !cliente.getNumero().trim().isEmpty()) {
                if (endereco.length() > 0) endereco.append(", ");
                endereco.append(cliente.getNumero());
            }
            if (cliente.getComplemento() != null && !cliente.getComplemento().trim().isEmpty()) {
                if (endereco.length() > 0) endereco.append(" - ");
                endereco.append(cliente.getComplemento());
            }
            if (cliente.getBairro() != null && !cliente.getBairro().trim().isEmpty()) {
                if (endereco.length() > 0) endereco.append(", ");
                endereco.append(cliente.getBairro());
            }
            if (cliente.getCidade() != null && !cliente.getCidade().trim().isEmpty()) {
                if (endereco.length() > 0) endereco.append(" - ");
                endereco.append(cliente.getCidade());
            }
            if (cliente.getEstado() != null && !cliente.getEstado().trim().isEmpty()) {
                if (endereco.length() > 0) endereco.append("/");
                endereco.append(cliente.getEstado());
            }
            if (cliente.getCep() != null && !cliente.getCep().trim().isEmpty()) {
                if (endereco.length() > 0) endereco.append(" - CEP: ");
                endereco.append(cliente.getCep());
            }

            return new javafx.beans.property.SimpleStringProperty(endereco.toString());
        });

        // Coluna Status
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusCliente"));

        clientes = FXCollections.observableArrayList();
        tableClientes.setItems(clientes);
    }

    private void configurarBusca() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filtrarClientes(newValue);
        });
    }

    private void carregarClientes() {
        try {
            List<Cliente> listaClientes = clienteDAO.buscarTodos();
            clientes.clear();
            clientes.addAll(listaClientes);
        } catch (SQLException e) {
            System.err.println("Erro ao carregar clientes: " + e.getMessage());
            e.printStackTrace();
            mostrarNotificacao("Erro", "Erro ao carregar clientes: " + e.getMessage(), "erro");
        }
    }

    private void atualizarTotal() {
        try {
            int total = clienteDAO.contarTotal();
            lblTotal.setText("Total: " + total + " cliente(s)");
        } catch (SQLException e) {
            lblTotal.setText("Total: Erro ao contar");
        }
    }

    private void filtrarClientes(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            carregarClientes();
        } else {
            try {
                List<Cliente> listaFiltrada = clienteDAO.buscarPorRazaoSocial(filtro.trim());
                clientes.clear();
                clientes.addAll(listaFiltrada);
            } catch (SQLException e) {
                mostrarNotificacao("Erro", "Erro ao filtrar clientes", "erro");
            }
        }
    }

    @FXML
    void handleNovo() {
        modoEdicao = false;
        clienteSelecionado = null;
        limparFormulario();
        lblFormTitle.setText("Novo Cliente");
        // CORREÇÃO: Definir valor padrão no ComboBox
        cbStatus.setValue("PAGO");
        mostrarFormulario(true);
    }

    @FXML
    void handleEditar() {
        clienteSelecionado = tableClientes.getSelectionModel().getSelectedItem();
        if (clienteSelecionado == null) {
            mostrarNotificacao("Atenção", "Selecione um cliente para editar", "aviso");
            return;
        }
        modoEdicao = true;
        preencherFormulario(clienteSelecionado);
        lblFormTitle.setText("Editar Cliente");
        mostrarFormulario(true);
    }

    @FXML
    void handleExcluir() {
        clienteSelecionado = tableClientes.getSelectionModel().getSelectedItem();
        if (clienteSelecionado == null) {
            mostrarNotificacao("Atenção", "Selecione um cliente para excluir", "aviso");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar exclusão");
        confirmacao.setHeaderText("Deseja realmente excluir este cliente?");
        confirmacao.setContentText("Cliente: " + clienteSelecionado.getRazaoSocial());

        if (confirmacao.showAndWait().get() == ButtonType.OK) {
            try {
                if (clienteDAO.excluir(clienteSelecionado.getCnpj())) {
                    carregarClientes();
                    atualizarTotal();
                    mostrarNotificacao("Sucesso", "Cliente excluído com sucesso!", "sucesso");
                } else {
                    mostrarNotificacao("Erro", "Não foi possível excluir o cliente", "erro");
                }
            } catch (SQLException e) {
                mostrarNotificacao("Erro", "Erro ao excluir cliente: " + e.getMessage(), "erro");
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
            Cliente cliente = criarClienteDoFormulario();
            boolean sucesso;
            
            if (modoEdicao) {
                sucesso = clienteDAO.atualizar(cliente);
            } else {
                sucesso = clienteDAO.inserir(cliente);
            }

            if (sucesso) {
                carregarClientes();
                atualizarTotal();
                mostrarFormulario(false);
                mostrarNotificacao("Sucesso",
                    modoEdicao ? "Cliente atualizado!" : "Cliente cadastrado!", "sucesso");
            } else {
                mostrarNotificacao("Erro", "Não foi possível salvar o cliente", "erro");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao salvar cliente: " + e.getMessage());
            e.printStackTrace();
            mostrarNotificacao("Erro", "Erro ao salvar cliente: " + e.getMessage(), "erro");
        }
    }

    @FXML
    void handleCancelar() {
        mostrarFormulario(false);
    }

    private void mostrarFormulario(boolean mostrar) {
        formContainer.setVisible(mostrar);
        formContainer.setManaged(mostrar);
        tableClientes.setVisible(!mostrar);
        tableClientes.setManaged(!mostrar);
        if (!mostrar) limparEstilos();
    }

    private void limparFormulario() {
        txtNome.clear();
        txtCpf.clear();
        txtTelefone.clear();
        txtEmail.clear();
        txtLogradouro.clear();
        txtNumero.clear();
        txtComplemento.clear();
        txtBairro.clear();
        txtCidade.clear();
        txtEstado.clear();
        txtCep.clear();
        cbStatus.setValue("PAGO"); // CORREÇÃO: Valor padrão
        limparEstilos();
    }

    private void preencherFormulario(Cliente cliente) {
        txtNome.setText(cliente.getRazaoSocial());
        
        // CORREÇÃO: Formatar CPF ao preencher
        String cpf = cliente.getCnpj();
        if (cpf != null && cpf.length() == 11) {
            cpf = cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + 
                  cpf.substring(6, 9) + "-" + cpf.substring(9);
        }
        txtCpf.setText(cpf);
        
        txtTelefone.setText(cliente.getTelefoneCliente());
        txtEmail.setText(cliente.getEmailCliente());
        txtLogradouro.setText(cliente.getLogradouro());
        txtNumero.setText(cliente.getNumero());
        txtComplemento.setText(cliente.getComplemento());
        txtBairro.setText(cliente.getBairro());
        txtCidade.setText(cliente.getCidade());
        txtEstado.setText(cliente.getEstado());
        txtCep.setText(cliente.getCep());
        
        // CORREÇÃO: Verificar se status não é nulo
        String status = cliente.getStatusCliente();
        cbStatus.setValue(status != null ? status : "PAGO");
    }

    private Cliente criarClienteDoFormulario() {
        Cliente cliente = modoEdicao ? clienteSelecionado : new Cliente();
        cliente.setRazaoSocial(getTextSafe(txtNome).trim());
        cliente.setCnpj(extrairNumeros(getTextSafe(txtCpf)));
        cliente.setInscricaoEstadual(null);
        cliente.setNomeFantasia(null);
        cliente.setEmailCliente(getTextSafe(txtEmail).trim().isEmpty() ? null : getTextSafe(txtEmail).trim());
        cliente.setTelefoneCliente(getTextSafe(txtTelefone).trim().isEmpty() ? null : extrairNumeros(getTextSafe(txtTelefone)));
        cliente.setLogradouro(getTextSafe(txtLogradouro).trim().isEmpty() ? null : getTextSafe(txtLogradouro).trim());
        cliente.setNumero(getTextSafe(txtNumero).trim().isEmpty() ? null : getTextSafe(txtNumero).trim());
        cliente.setComplemento(getTextSafe(txtComplemento).trim().isEmpty() ? null : getTextSafe(txtComplemento).trim());
        cliente.setBairro(getTextSafe(txtBairro).trim().isEmpty() ? null : getTextSafe(txtBairro).trim());
        cliente.setCidade(getTextSafe(txtCidade).trim().isEmpty() ? null : getTextSafe(txtCidade).trim());
        cliente.setEstado(getTextSafe(txtEstado).trim().isEmpty() ? null : getTextSafe(txtEstado).trim());
        cliente.setCep(getTextSafe(txtCep).trim().isEmpty() ? null : extrairNumeros(getTextSafe(txtCep)));

        // CORREÇÃO: Garantir que status nunca seja nulo
        String status = cbStatus.getValue();
        cliente.setStatusCliente(status != null ? status : "PAGO");

        return cliente;
    }

    private List<String> validarFormulario() {
        List<String> erros = new ArrayList<>();

        if (getTextSafe(txtNome).trim().isEmpty()) {
            erros.add("Nome é obrigatório");
        }

        if (getTextSafe(txtCpf).trim().isEmpty()) {
            erros.add("CPF é obrigatório");
        } else {
            String cpf = extrairNumeros(getTextSafe(txtCpf));
            if (cpf.length() != 11) {
                erros.add("CPF deve ter 11 dígitos");
            } else {
                try {
                    String cpfExcluir = modoEdicao ? clienteSelecionado.getCnpj() : null;
                    if (clienteDAO.cnpjExiste(cpf, cpfExcluir)) {
                        erros.add("CPF já cadastrado");
                    }
                } catch (SQLException e) {
                    erros.add("Erro ao verificar CPF: " + e.getMessage());
                }
            }
        }

        if (!getTextSafe(txtEmail).trim().isEmpty()) {
            String email = getTextSafe(txtEmail).trim();
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                erros.add("E-mail inválido");
            }
        }

        if (!getTextSafe(txtCep).trim().isEmpty()) {
            String cep = extrairNumeros(getTextSafe(txtCep));
            if (cep.length() != 8) {
                erros.add("CEP deve ter 8 dígitos");
            } else if (!cep.matches("\\d{8}")) {
                erros.add("CEP inválido");
            }
        }

        return erros;
    }
}
