package main.Modal;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import main.database.DAOs.ClienteDAO;
import main.database.DAOs.EnderecoDAO;
import main.models.Cliente;
import main.models.Endereco;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ClienteFormModal {

    private static final String AZUL         = "#194e8f";
    private static final String VERMELHO     = "#c0392b";
    private static final String ESTILO_LABEL = "-fx-font-size: 11; -fx-font-family: 'Segoe UI'; -fx-text-fill: #333333;";
    private static final String ESTILO_CAMPO = "-fx-font-size: 11; -fx-font-family: 'Segoe UI'; -fx-background-color: white; -fx-border-color: #cccccc; -fx-border-width: 1;";

    private final ClienteDAO  clienteDAO;
    private final EnderecoDAO enderecoDAO;

    private Stage stage;
    private Cliente cliente;

    private static final String PESSOA_FISICA   = "Pessoa Física";
    private static final String PESSOA_JURIDICA = "Pessoa Jurídica";

    private ComboBox<String> cbTipoPessoa;
    private Label lblDocumento, lblNome, lblNomeFantasia, lblInscricaoEstadual;
    private TextField txtDocumento, txtRazaoSocial, txtNomeFantasia, txtInscricaoEstadual;
    private TextField txtTelefone, txtEmail, txtLimiteCredito;
    private DatePicker dpDataNascimento;
    private ComboBox<String> cbStatus;
    private CheckBox chkAtivo;

    private double xOff, yOff;

    private TextField txtlogradouro, txtnumero, txtcomplemento, txtbairro, txtcidade, txtcep;

    private ComboBox<String> cbEstado;

    public ClienteFormModal(Stage owner, Cliente cliente,
                            ClienteDAO clienteDAO, EnderecoDAO enderecoDAO) {
        this.cliente = cliente;
        this.clienteDAO = clienteDAO;
        this.enderecoDAO = enderecoDAO;

        stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle(cliente == null ? "Novo Cliente" : "Editar Cliente");

        Button btnSalvar = new Button("Salvar");
        btnSalvar.setStyle(
            "-fx-background-color: " + AZUL + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 11;" +
            "-fx-font-family: 'Segoe UI';" +
            "-fx-cursor: hand;"
        );
        btnSalvar.setPrefHeight(32);
        btnSalvar.setPrefWidth(90);

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-color: " + VERMELHO + ";" +
            "-fx-text-fill: " + VERMELHO + ";" +
            "-fx-border-width: 1;" +
            "-fx-font-size: 11;" +
            "-fx-font-family: 'Segoe UI';" +
            "-fx-cursor: hand;"
        );
        btnCancelar.setPrefHeight(32);
        btnCancelar.setPrefWidth(90);

        btnSalvar.setOnAction(e -> salvar());
        btnCancelar.setOnAction(e -> stage.close());

        HBox rodape = new HBox(10, btnSalvar, btnCancelar);
        rodape.setPadding(new Insets(10));
        rodape.setAlignment(Pos.CENTER_RIGHT);
        rodape.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #d8d8d8; -fx-border-width: 1 0 0 0;");

        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/main/resources/logoAlter.png")));
        logo.setFitHeight(22); logo.setFitWidth(22); logo.setPreserveRatio(true);

        Label titulo = new Label(cliente == null ? "Novo Cliente" : "Editar Cliente");
        titulo.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);

        Button btnFechar = new Button("X");
        btnFechar.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 12; -fx-cursor: hand; -fx-border-width: 0; -fx-padding: 0;");
        btnFechar.setOnAction(e -> stage.close());

        HBox topBar = new HBox(8, logo, titulo, espaco, btnFechar);
        topBar.setStyle("-fx-background-color: #194e8f; -fx-padding: 0 4 0 10; -fx-alignment: CENTER_LEFT;");

        // Drag
        topBar.setOnMousePressed(e -> { xOff = stage.getX() - e.getScreenX(); yOff = stage.getY() - e.getScreenY(); });
        topBar.setOnMouseDragged(e -> { stage.setX(e.getScreenX() + xOff); stage.setY(e.getScreenY() + yOff); });

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle(
            "-fx-font-size: 11;" +
            "-fx-font-family: 'Segoe UI';"
        );
        tabPane.getTabs().addAll(
            new Tab("Geral",    buildAbaGeral()),
            new Tab("Endereço", buildAbaEndereco())
        );

        VBox root = new VBox(topBar, tabPane, rodape);
        stage.setScene(new Scene(root));

        if (cliente != null) preencherCampos();
    }

    private GridPane criarGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));
        grid.setStyle("-fx-background-color: #f5f5f5;");

        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(120);
        labelCol.setPrefWidth(120);

        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);
        fieldCol.setFillWidth(true);

        grid.getColumnConstraints().addAll(labelCol, fieldCol);
        return grid;
    }

    private GridPane buildAbaGeral() {
        GridPane grid = criarGrid();

        cbTipoPessoa = new ComboBox<>();
        cbTipoPessoa.getItems().addAll(PESSOA_FISICA, PESSOA_JURIDICA);
        cbTipoPessoa.setValue(PESSOA_FISICA);
        cbTipoPessoa.setMaxWidth(Double.MAX_VALUE);
        cbTipoPessoa.setPrefHeight(24);
        cbTipoPessoa.setStyle(ESTILO_CAMPO);
        cbTipoPessoa.setOnAction(e -> atualizarCamposTipoPessoa());

        txtDocumento         = criarCampo();
        txtRazaoSocial       = criarCampo();
        txtNomeFantasia      = criarCampo();
        txtInscricaoEstadual = criarCampo();
        txtTelefone          = criarCampo();
        txtEmail             = criarCampo();
        txtLimiteCredito     = criarCampo();
        txtLimiteCredito.setPromptText("0,00");

        dpDataNascimento = new DatePicker();
        dpDataNascimento.setMaxWidth(Double.MAX_VALUE);
        dpDataNascimento.setPrefHeight(24);
        dpDataNascimento.setStyle(ESTILO_CAMPO);

        cbStatus = new ComboBox<>();
        cbStatus.getItems().addAll("PAGO", "NAO_PAGO");
        cbStatus.setValue("PAGO");
        cbStatus.setMaxWidth(Double.MAX_VALUE);
        cbStatus.setPrefHeight(24);
        cbStatus.setStyle(ESTILO_CAMPO);

        chkAtivo = new CheckBox("Ativo");
        chkAtivo.setStyle(ESTILO_LABEL);
        chkAtivo.setSelected(true);

        aplicarMascaraDocumento(txtDocumento);
        aplicarMascaraTelefone(txtTelefone);

        lblDocumento         = criarLabel("CPF:");
        lblNome              = criarLabel("Nome:");
        lblNomeFantasia      = criarLabel("Nome Fantasia:");
        lblInscricaoEstadual = criarLabel("Inscrição Estadual:");

        grid.addRow(0,  criarLabel("Tipo de Pessoa:"),    cbTipoPessoa);
        grid.addRow(1,  lblDocumento,                     txtDocumento);
        grid.addRow(2,  lblNome,                          txtRazaoSocial);
        grid.addRow(3,  lblNomeFantasia,                  txtNomeFantasia);
        grid.addRow(4,  lblInscricaoEstadual,             txtInscricaoEstadual);
        grid.addRow(5,  criarLabel("Telefone:"),          txtTelefone);
        grid.addRow(6,  criarLabel("Email:"),             txtEmail);
        grid.addRow(7,  criarLabel("Data Nascimento:"),   dpDataNascimento);
        grid.addRow(8,  criarLabel("Limite de Crédito:"), txtLimiteCredito);
        grid.addRow(9,  criarLabel("Status:"),            cbStatus);
        grid.addRow(10, chkAtivo);

        atualizarCamposTipoPessoa();

        return grid;
    }

    private boolean isPessoaFisica() {
        return PESSOA_FISICA.equals(cbTipoPessoa.getValue());
    }

    /** Ajusta rótulos, máscara e campos exclusivos de pessoa jurídica. */
    private void atualizarCamposTipoPessoa() {
        boolean pf = isPessoaFisica();

        lblDocumento.setText(pf ? "CPF:" : "CNPJ:");
        lblNome.setText(pf ? "Nome:" : "Razão Social:");
        txtDocumento.setPromptText(pf ? "000.000.000-00" : "00.000.000/0000-00");

        // Nome Fantasia e Inscrição Estadual só fazem sentido para pessoa jurídica
        for (javafx.scene.Node node : new javafx.scene.Node[]{
                lblNomeFantasia, txtNomeFantasia, lblInscricaoEstadual, txtInscricaoEstadual}) {
            node.setVisible(!pf);
            node.setManaged(!pf);
        }
        if (pf) {
            txtNomeFantasia.clear();
            txtInscricaoEstadual.clear();
        }

        // Reaplica a máscara ao documento já digitado
        String digits = txtDocumento.getText() != null ? txtDocumento.getText().replaceAll("[^0-9]", "") : "";
        txtDocumento.setText("");
        txtDocumento.setText(digits);
    }

    private GridPane buildAbaEndereco() {
        GridPane grid = criarGrid();

        txtlogradouro = criarCampo();
        txtnumero = criarCampo();
        txtcomplemento = criarCampo();
        txtbairro = criarCampo();
        txtcidade = criarCampo();
        txtcep = criarCampo();

        cbEstado = new ComboBox<>();
        cbEstado.getItems().addAll(
            "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO",
            "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI",
            "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO"
        );
        cbEstado.setMaxWidth(Double.MAX_VALUE);
        cbEstado.setPrefHeight(24);
        cbEstado.setStyle(ESTILO_CAMPO);
        cbEstado.setPromptText("Selecione...");

        aplicarMascaraCep(txtcep);

        grid.addRow(0, criarLabel("Logradouro:"), txtlogradouro);
        grid.addRow(1, criarLabel("Número:"), txtnumero);
        grid.addRow(2, criarLabel("Complemento:"), txtcomplemento);
        grid.addRow(3, criarLabel("Bairro:"), txtbairro);
        grid.addRow(4, criarLabel("Cidade:"), txtcidade);
        grid.addRow(5, criarLabel("Estado:"), cbEstado);
        grid.addRow(6, criarLabel("CEP:"), txtcep);

        return grid;
    }

    /** Máscara dinâmica: CPF (000.000.000-00) ou CNPJ (00.000.000/0000-00) conforme o tipo de pessoa. */
    private void aplicarMascaraDocumento(TextField campo) {
        campo.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;

            boolean pf = isPessoaFisica();
            int maxDigitos = pf ? 11 : 14;

            String digits = newVal.replaceAll("[^0-9]", "");

            if (digits.length() > maxDigitos)
                digits = digits.substring(0, maxDigitos);

            String formatado = "";
            for (int i = 0; i < digits.length(); i++) {
                if (pf) {
                    if (i == 3 || i == 6) formatado += ".";
                    if (i == 9)           formatado += "-";
                } else {
                    if (i == 2 || i == 5)  formatado += ".";
                    if (i == 8)            formatado += "/";
                    if (i == 12)           formatado += "-";
                }
                formatado += digits.charAt(i);
            }

            if (!formatado.equals(newVal)) {
                campo.setText(formatado);
                campo.positionCaret(formatado.length());
            }
        });
    }

    private void aplicarMascaraTelefone(TextField campo) {
        campo.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;

            String digits = newVal.replaceAll("[^0-9]", "");

            if (digits.length() > 11)
                digits = digits.substring(0, 11);

            String formatado = "";
            for (int i = 0; i < digits.length(); i++) {
                if (i == 0)  formatado += "(";
                if (i == 2)  formatado += ") ";
                if (i == 7)  formatado += "-";
                formatado += digits.charAt(i);
            }

            if (!formatado.equals(newVal)) {
                campo.setText(formatado);
                campo.positionCaret(formatado.length());
            }
        });
    }

    private void aplicarMascaraCep(TextField campo) {
        campo.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;

            String digits = newVal.replaceAll("[^0-9]", "");

            if (digits.length() > 8)
                digits = digits.substring(0, 8);

            String formatado = "";
            for (int i = 0; i < digits.length(); i++) {
                if (i == 5) formatado += "-";
                formatado += digits.charAt(i);
            }

            if (!formatado.equals(newVal)) {
                campo.setText(formatado);
                campo.positionCaret(formatado.length());
            }
        });
    }

    private void preencherCampos() {
        // Define o tipo antes do documento para a máscara correta ser aplicada
        cbTipoPessoa.setValue("F".equals(cliente.getTipoPessoa()) ? PESSOA_FISICA : PESSOA_JURIDICA);
        atualizarCamposTipoPessoa();

        txtDocumento.setText(cliente.getCnpj() != null ? cliente.getCnpj() : "");
        txtRazaoSocial.setText(cliente.getRazaoSocial() != null ? cliente.getRazaoSocial() : "");
        txtNomeFantasia.setText(cliente.getNomeFantasia() != null ? cliente.getNomeFantasia() : "");
        txtInscricaoEstadual.setText(cliente.getInscricaoEstadual() != null ? cliente.getInscricaoEstadual() : "");
        txtTelefone.setText(cliente.getTelefoneCliente() != null ? cliente.getTelefoneCliente() : "");
        txtEmail.setText(cliente.getEmailCliente() != null ? cliente.getEmailCliente() : "");
        txtLimiteCredito.setText(cliente.getLimiteCredito() != null
                ? cliente.getLimiteCredito().toPlainString().replace(".", ",") : "");
        dpDataNascimento.setValue(cliente.getDataNascimento());
        cbStatus.setValue(cliente.getStatusCliente() != null ? cliente.getStatusCliente() : "PAGO");
        chkAtivo.setSelected(Boolean.TRUE.equals(cliente.getAtivo()));

        if (cliente.getIdEnderecoCliente() != null) {
            try {
                Endereco endereco = enderecoDAO.findById(cliente.getIdEnderecoCliente()).orElse(null);
                if (endereco != null) {
                    txtlogradouro.setText(endereco.getLogradouro() != null ? endereco.getLogradouro() : "");
                    txtnumero.setText(endereco.getNumero() != null ? endereco.getNumero() : "");
                    txtcomplemento.setText(endereco.getComplemento() != null ? endereco.getComplemento() : "");
                    txtbairro.setText(endereco.getBairro() != null ? endereco.getBairro() : "");
                    txtcidade.setText(endereco.getCidade() != null ? endereco.getCidade() : "");
                    cbEstado.setValue(endereco.getEstado());
                    txtcep.setText(endereco.getCep() != null ? endereco.getCep() : "");
                }
            } catch (Exception e) {
                exibirAlerta("Erro", "Erro ao carregar endereço: " + e.getMessage());
            }
        }
    }

    private boolean enderecoPreenchido() {
        return !txtlogradouro.getText().trim().isEmpty()
            || !txtnumero.getText().trim().isEmpty()
            || !txtbairro.getText().trim().isEmpty()
            || !txtcidade.getText().trim().isEmpty()
            || cbEstado.getValue() != null
            || !txtcep.getText().trim().isEmpty();
    }

    private void salvar() {
        List<String> erros = new ArrayList<>();

        boolean pf = isPessoaFisica();

        // A coluna no banco é VARCHAR(14): valida e persiste apenas os dígitos
        String docDigits = txtDocumento.getText().replaceAll("[^0-9]", "");

        if (pf && docDigits.length() != 11)
            erros.add("• CPF (11 dígitos)");
        if (!pf && docDigits.length() != 14)
            erros.add("• CNPJ (14 dígitos)");
        if (txtRazaoSocial.getText().trim().isEmpty())
            erros.add(pf ? "• Nome" : "• Razão Social");

        // Endereço é opcional, mas se algum campo for preenchido exige os principais
        if (enderecoPreenchido()) {
            if (txtlogradouro.getText().trim().isEmpty())
                erros.add("• Logradouro");
            if (txtnumero.getText().trim().isEmpty())
                erros.add("• Número");
            if (txtbairro.getText().trim().isEmpty())
                erros.add("• Bairro");
            if (txtcidade.getText().trim().isEmpty())
                erros.add("• Cidade");
            if (cbEstado.getValue() == null)
                erros.add("• Estado");
            if (txtcep.getText().trim().isEmpty())
                erros.add("• CEP");
        }

        BigDecimal limiteCredito = null;
        String limiteTexto = txtLimiteCredito.getText().trim();
        if (!limiteTexto.isEmpty()) {
            try {
                limiteCredito = new BigDecimal(limiteTexto.replace(".", "").replace(",", "."));
            } catch (NumberFormatException ex) {
                erros.add("• Limite de Crédito (valor inválido)");
            }
        }

        if (!erros.isEmpty()) {
            exibirAlerta("Campos obrigatórios",
                "Corrija os campos abaixo antes de salvar:\n\n" + String.join("\n", erros));
            return;
        }

        try {
            Integer idEndereco = cliente != null ? cliente.getIdEnderecoCliente() : null;

            if (enderecoPreenchido()) {
                Endereco endereco = new Endereco();
                endereco.setLogradouro(txtlogradouro.getText().trim());
                endereco.setNumero(txtnumero.getText().trim());
                endereco.setComplemento(txtcomplemento.getText().trim());
                endereco.setBairro(txtbairro.getText().trim());
                endereco.setCidade(txtcidade.getText().trim());
                endereco.setEstado(cbEstado.getValue());
                endereco.setCep(txtcep.getText().trim());
                endereco.setDataCadastro(java.time.LocalDateTime.now());

                if (idEndereco == null) {
                    enderecoDAO.save(endereco);
                    idEndereco = endereco.getIdEndereco();
                } else {
                    endereco.setIdEndereco(idEndereco);
                    enderecoDAO.update(endereco);
                }
            }

            if (cliente == null) cliente = new Cliente();

            cliente.setCnpj(docDigits);
            cliente.setTipoPessoa(pf ? "F" : "J");
            cliente.setRazaoSocial(txtRazaoSocial.getText().trim());
            cliente.setNomeFantasia(pf ? null : txtNomeFantasia.getText().trim());
            cliente.setInscricaoEstadual(pf ? null : txtInscricaoEstadual.getText().trim());
            cliente.setTelefoneCliente(txtTelefone.getText().trim());
            cliente.setEmailCliente(txtEmail.getText().trim());
            cliente.setDataNascimento(dpDataNascimento.getValue());
            cliente.setLimiteCredito(limiteCredito);
            cliente.setStatusCliente(cbStatus.getValue());
            cliente.setIdEnderecoCliente(idEndereco);
            cliente.setAtivo(chkAtivo.isSelected());

            if (cliente.getIdCliente() == null) {
                cliente.setDataCadastro(java.time.LocalDateTime.now());
                cliente.setDataAtualizacao(java.time.LocalDateTime.now());
                clienteDAO.save(cliente);
            } else {
                cliente.setDataAtualizacao(java.time.LocalDateTime.now());
                clienteDAO.update(cliente);
            }

            exibirAlerta("Cliente salvo", "Cliente salvo com sucesso!");
            stage.close();

        } catch (Exception e) {
            exibirAlerta("Erro", "Erro ao salvar o cliente: " + e.getMessage());
        }
    }

    private void exibirAlerta(String titulo, String mensagem) {
        Stage dialog = new Stage();
        dialog.initOwner(stage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/main/resources/logoAlter.png")));
        logo.setFitHeight(22);
        logo.setFitWidth(22);
        logo.setPreserveRatio(true);

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-family: 'Segoe UI'; -fx-font-size: 11;");

        Region espacoTop = new Region();
        HBox.setHgrow(espacoTop, Priority.ALWAYS);

        Button btnFechar = new Button("X");
        btnFechar.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: rgba(255,255,255,0.8);" +
            "-fx-font-size: 12;" +
            "-fx-cursor: hand;" +
            "-fx-border-width: 0;" +
            "-fx-padding: 0;"
        );
        btnFechar.setOnAction(e -> dialog.close());

        HBox topBar = new HBox(8, logo, lblTitulo, espacoTop, btnFechar);
        topBar.setStyle("-fx-background-color: #194e8f; -fx-padding: 6 4 6 10; -fx-alignment: CENTER_LEFT;");

        Label lblMensagem = new Label(mensagem);
        lblMensagem.setStyle(
            "-fx-font-size: 11;" +
            "-fx-font-family: 'Segoe UI';" +
            "-fx-text-fill: #333333;" +
            "-fx-wrap-text: true;"
        );
        lblMensagem.setMaxWidth(340);
        lblMensagem.setPadding(new Insets(16));

        HBox conteudo = new HBox(lblMensagem);
        conteudo.setStyle("-fx-background-color: #f5f5f5;");

        Button btnOk = new Button("OK");
        btnOk.setStyle(
            "-fx-background-color: " + AZUL + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 11;" +
            "-fx-font-family: 'Segoe UI';" +
            "-fx-cursor: hand;"
        );
        btnOk.setPrefHeight(30);
        btnOk.setPrefWidth(80);
        btnOk.setOnAction(e -> dialog.close());

        HBox rodape = new HBox(btnOk);
        rodape.setPadding(new Insets(10));
        rodape.setAlignment(Pos.CENTER_RIGHT);
        rodape.setStyle(
            "-fx-background-color: #f5f5f5;" +
            "-fx-border-color: #d8d8d8;" +
            "-fx-border-width: 1 0 0 0;"
        );

        VBox root = new VBox(topBar, conteudo, rodape);
        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }

    private Label criarLabel(String texto) {
        Label label = new Label(texto);
        label.setStyle(ESTILO_LABEL);
        return label;
    }

    private TextField criarCampo() {
        TextField tf = new TextField();
        tf.setStyle(ESTILO_CAMPO);
        tf.setMaxWidth(Double.MAX_VALUE);
        tf.setPrefHeight(24);
        return tf;
    }

    public void show() {
        stage.showAndWait();
    }
}
