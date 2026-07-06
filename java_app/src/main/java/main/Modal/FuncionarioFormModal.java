package main.Modal;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import main.database.DAOs.FuncionarioDAO;
import main.models.Funcionario;

import java.util.ArrayList;
import java.util.List;

public class FuncionarioFormModal {

    private static final String AZUL         = "#194e8f";
    private static final String VERMELHO     = "#c0392b";
    private static final String ESTILO_LABEL = "-fx-font-size: 11; -fx-font-family: 'Segoe UI'; -fx-text-fill: #333333;";
    private static final String ESTILO_CAMPO = "-fx-font-size: 11; -fx-font-family: 'Segoe UI'; -fx-background-color: white; -fx-border-color: #cccccc; -fx-border-width: 1;";

    private final FuncionarioDAO funcionarioDAO;

    private Stage stage;
    private Funcionario funcionario;

    private TextField txtNome, txtCpf, txtCargo, txtTelefone, txtEmail;
    private DatePicker dpDataAdmissao, dpDataDemissao;
    private CheckBox chkAtivo;

    private double xOff, yOff;

    public FuncionarioFormModal(Stage owner, Funcionario funcionario, FuncionarioDAO funcionarioDAO) {
        this.funcionario = funcionario;
        this.funcionarioDAO = funcionarioDAO;

        stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle(funcionario == null ? "Novo Funcionário" : "Editar Funcionário");

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

        Label titulo = new Label(funcionario == null ? "Novo Funcionário" : "Editar Funcionário");
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

        VBox root = new VBox(topBar, buildFormulario(), rodape);
        stage.setScene(new Scene(root));

        if (funcionario != null) preencherCampos();
    }

    private GridPane buildFormulario() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));
        grid.setStyle("-fx-background-color: #f5f5f5;");
        grid.setPrefWidth(440);

        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(110);
        labelCol.setPrefWidth(110);

        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);
        fieldCol.setFillWidth(true);

        grid.getColumnConstraints().addAll(labelCol, fieldCol);

        txtNome     = criarCampo();
        txtCpf      = criarCampo();
        txtCargo    = criarCampo();
        txtTelefone = criarCampo();
        txtEmail    = criarCampo();

        dpDataAdmissao = new DatePicker(java.time.LocalDate.now());
        dpDataAdmissao.setMaxWidth(Double.MAX_VALUE);
        dpDataAdmissao.setPrefHeight(24);
        dpDataAdmissao.setStyle(ESTILO_CAMPO);

        dpDataDemissao = new DatePicker();
        dpDataDemissao.setMaxWidth(Double.MAX_VALUE);
        dpDataDemissao.setPrefHeight(24);
        dpDataDemissao.setStyle(ESTILO_CAMPO);
        dpDataDemissao.setPromptText("Opcional");

        chkAtivo = new CheckBox("Ativo");
        chkAtivo.setStyle(ESTILO_LABEL);
        chkAtivo.setSelected(true);

        aplicarMascaraCpf(txtCpf);
        aplicarMascaraTelefone(txtTelefone);

        grid.addRow(0, criarLabel("Nome:"),           txtNome);
        grid.addRow(1, criarLabel("CPF:"),            txtCpf);
        grid.addRow(2, criarLabel("Cargo:"),          txtCargo);
        grid.addRow(3, criarLabel("Data Admissão:"),  dpDataAdmissao);
        grid.addRow(4, criarLabel("Data Demissão:"),  dpDataDemissao);
        grid.addRow(5, criarLabel("Telefone:"),       txtTelefone);
        grid.addRow(6, criarLabel("Email:"),          txtEmail);
        grid.addRow(7, chkAtivo);

        return grid;
    }

    private void aplicarMascaraCpf(TextField campo) {
        campo.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;

            String digits = newVal.replaceAll("[^0-9]", "");

            if (digits.length() > 11)
                digits = digits.substring(0, 11);

            String formatado = "";
            for (int i = 0; i < digits.length(); i++) {
                if (i == 3 || i == 6) formatado += ".";
                if (i == 9)           formatado += "-";
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

    private void preencherCampos() {
        txtNome.setText(funcionario.getNome() != null ? funcionario.getNome() : "");
        txtCpf.setText(funcionario.getCpf() != null ? funcionario.getCpf() : "");
        txtCargo.setText(funcionario.getCargo() != null ? funcionario.getCargo() : "");
        txtTelefone.setText(funcionario.getTelefone() != null ? funcionario.getTelefone() : "");
        txtEmail.setText(funcionario.getEmail() != null ? funcionario.getEmail() : "");
        dpDataAdmissao.setValue(funcionario.getDataAdmissao());
        dpDataDemissao.setValue(funcionario.getDataDemissao());
        chkAtivo.setSelected(Boolean.TRUE.equals(funcionario.getAtivo()));
    }

    private void salvar() {
        List<String> erros = new ArrayList<>();

        String cpfDigits = txtCpf.getText().replaceAll("[^0-9]", "");

        if (txtNome.getText().trim().isEmpty())
            erros.add("• Nome");
        if (cpfDigits.length() != 11)
            erros.add("• CPF (11 dígitos)");
        if (dpDataAdmissao.getValue() == null)
            erros.add("• Data de Admissão");
        if (dpDataDemissao.getValue() != null && dpDataAdmissao.getValue() != null
                && dpDataDemissao.getValue().isBefore(dpDataAdmissao.getValue()))
            erros.add("• Data de Demissão (anterior à admissão)");

        if (!erros.isEmpty()) {
            exibirAlerta("Campos obrigatórios",
                "Corrija os campos abaixo antes de salvar:\n\n" + String.join("\n", erros));
            return;
        }

        try {
            if (funcionario == null) funcionario = new Funcionario();

            funcionario.setNome(txtNome.getText().trim());
            funcionario.setCpf(cpfDigits);
            funcionario.setCargo(txtCargo.getText().trim());
            funcionario.setDataAdmissao(dpDataAdmissao.getValue());
            funcionario.setDataDemissao(dpDataDemissao.getValue());
            funcionario.setTelefone(txtTelefone.getText().trim());
            funcionario.setEmail(txtEmail.getText().trim());
            funcionario.setAtivo(chkAtivo.isSelected());

            if (funcionario.getIdFuncionario() == null) {
                funcionario.setDataCadastro(java.time.LocalDateTime.now());
                funcionario.setDataAtualizacao(java.time.LocalDateTime.now());
                funcionarioDAO.save(funcionario);
            } else {
                funcionario.setDataAtualizacao(java.time.LocalDateTime.now());
                funcionarioDAO.update(funcionario);
            }

            exibirAlerta("Funcionário salvo", "Funcionário salvo com sucesso!");
            stage.close();

        } catch (Exception e) {
            exibirAlerta("Erro", "Erro ao salvar o funcionário: " + e.getMessage());
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
