package main.Modal;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
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

import main.database.DAOs.EnderecoDAO;
import main.database.DAOs.FornecedorDAO;
import main.models.Endereco;
import main.models.Fornecedor;

import java.util.ArrayList;
import java.util.List;

public class FornecedorFormModal {

    private static final String AZUL         = "#194e8f";
    private static final String VERMELHO     = "#c0392b";
    private static final String ESTILO_LABEL = "-fx-font-size: 11; -fx-font-family: 'Segoe UI'; -fx-text-fill: #333333;";
    private static final String ESTILO_CAMPO = "-fx-font-size: 11; -fx-font-family: 'Segoe UI'; -fx-background-color: white; -fx-border-color: #cccccc; -fx-border-width: 1;";

    private final FornecedorDAO fornecedorDAO;
    private final EnderecoDAO   enderecoDAO;

    private Stage stage;
    private Fornecedor fornecedor;

    private TextField txtCnpj, txtRazaoSocial, txtNomeFantasia;
    private TextField txtTelefone, txtEmail;
    private CheckBox chkAtivo;

    private double xOff, yOff;

    private TextField txtlogradouro, txtnumero, txtcomplemento, txtbairro, txtcidade, txtcep;

    private ComboBox<String> cbEstado;

    public FornecedorFormModal(Stage owner, Fornecedor fornecedor,
                            FornecedorDAO fornecedorDAO, EnderecoDAO enderecoDAO) {
        this.fornecedor = fornecedor;
        this.fornecedorDAO = fornecedorDAO;
        this.enderecoDAO = enderecoDAO;

        stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle(fornecedor == null ? "Novo Fornecedor" : "Editar Fornecedor");

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

        Label titulo = new Label(fornecedor == null ? "Novo Fornecedor" : "Editar Fornecedor");
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
            new Tab("Geral",   buildAbaGeral()),
            new Tab("Endereço",  buildAbaEndereco())
        );

        VBox root = new VBox(topBar, tabPane, rodape);
        stage.setScene(new Scene(root));

        if (fornecedor != null) preencherCampos();
    }

    private GridPane criarGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));
        grid.setStyle("-fx-background-color: #f5f5f5;");

        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(110);
        labelCol.setPrefWidth(110);

        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);
        fieldCol.setFillWidth(true);

        grid.getColumnConstraints().addAll(labelCol, fieldCol);
        return grid;
    }

    private GridPane buildAbaGeral() {
        GridPane grid = criarGrid();

        txtCnpj         = criarCampo();
        txtRazaoSocial  = criarCampo();
        txtNomeFantasia = criarCampo();
        txtTelefone     = criarCampo();
        txtEmail        = criarCampo();
        

        chkAtivo = new CheckBox("Ativo");
        chkAtivo.setStyle(ESTILO_LABEL);
        chkAtivo.setSelected(true);

        aplicarMascaraCnpj(txtCnpj);
        aplicarMascaraTelefone(txtTelefone);

        grid.addRow(0, criarLabel("CNPJ:"),          txtCnpj);
        grid.addRow(1, criarLabel("Razão Social:"),  txtRazaoSocial);
        grid.addRow(2, criarLabel("Nome Fantasia:"), txtNomeFantasia);
        grid.addRow(3, criarLabel("Telefone:"),      txtTelefone);
        grid.addRow(4, criarLabel("Email:"),         txtEmail);
        grid.addRow(5, chkAtivo);

        return grid;
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


    private void exibirErroCarregamento(String contexto, Exception e) {
        exibirAlerta("Erro", "Erro ao carregar " + contexto + ": " + e.getMessage());
    }


    private void aplicarMascaraCnpj(TextField campo) {
        campo.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;

            String digits = newVal.replaceAll("[^0-9]", "");

            if (digits.length() > 14)
                digits = digits.substring(0, 14);

            String formatado = "";
            for (int i = 0; i < digits.length(); i++) {
                if (i == 2 || i == 5)  formatado += ".";
                if (i == 8)            formatado += "/";
                if (i == 12)           formatado += "-";
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
        txtCnpj.setText(fornecedor.getCnpj() != null ? fornecedor.getCnpj() : "");
        txtRazaoSocial.setText(fornecedor.getRazaoSocial() != null ? fornecedor.getRazaoSocial() : "");
        txtNomeFantasia.setText(fornecedor.getNomeFantasia() != null ? fornecedor.getNomeFantasia() : "");
        txtTelefone.setText(fornecedor.getTelefone() != null ? fornecedor.getTelefone() : "");
        txtEmail.setText(fornecedor.getEMail() != null ? fornecedor.getEMail() : "");
        chkAtivo.setSelected(fornecedor.getAtivo());

         if (fornecedor.getIdEndereco() != null) {
            try {
                Endereco endereco = enderecoDAO.findById(fornecedor.getIdEndereco()).orElse(null);
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


    private void salvar() {
        List<String> erros = new ArrayList<>();

        if (txtCnpj.getText().trim().isEmpty())
            erros.add("• CNPJ");
        if (txtRazaoSocial.getText().trim().isEmpty())
            erros.add("• Razão Social");
        if (txtNomeFantasia.getText().trim().isEmpty())
            erros.add("• Nome Fantasia");
        if (txtTelefone.getText().trim().isEmpty())
            erros.add("• Telefone");
        if (txtEmail.getText().trim().isEmpty())
            erros.add("• Email");
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

        if (!erros.isEmpty()) {
            exibirAlerta("Campos obrigatórios",
                "Preencha os campos abaixo antes de salvar:\n\n" + String.join("\n", erros));
            return;
        }

        try {
            Endereco endereco = new Endereco();
            endereco.setLogradouro(txtlogradouro.getText().trim());
            endereco.setNumero(txtnumero.getText().trim());
            endereco.setComplemento(txtcomplemento.getText().trim());
            endereco.setBairro(txtbairro.getText().trim());
            endereco.setCidade(txtcidade.getText().trim());
            endereco.setEstado(cbEstado.getValue());
            endereco.setCep(txtcep.getText().trim());
            endereco.setDataCadastro(java.time.LocalDateTime.now());

            if (fornecedor == null) {
                enderecoDAO.save(endereco);
            } else {
                endereco.setIdEndereco(fornecedor.getIdEndereco());
                enderecoDAO.update(endereco);
            }

            if (fornecedor == null) fornecedor = new Fornecedor();

            fornecedor.setCnpj(txtCnpj.getText().trim());
            fornecedor.setRazaoSocial(txtRazaoSocial.getText().trim());
            fornecedor.setNomeFantasia(txtNomeFantasia.getText().trim());
            fornecedor.setTelefone(txtTelefone.getText().trim());
            fornecedor.setEMail(txtEmail.getText().trim());
            fornecedor.setIdEndereco(endereco.getIdEndereco());
            fornecedor.setAtivo(chkAtivo.isSelected());

            if (fornecedor.getIdFornecedor() == null) {
                fornecedor.setDataCadastro(java.time.LocalDateTime.now());
                fornecedor.setDataAtualizacao(java.time.LocalDateTime.now());
                fornecedorDAO.save(fornecedor);
            } else {
                fornecedor.setDataAtualizacao(java.time.LocalDateTime.now());
                fornecedorDAO.update(fornecedor);
            }

            exibirAlerta("Fornecedor salvo", "Fornecedor salvo com sucesso!");
            stage.close();

        } catch (Exception e) {
            exibirAlerta("Erro", "Erro ao salvar o fornecedor: " + e.getMessage());
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

