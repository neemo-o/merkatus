package main.Modal;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
import main.database.DAOs.UnidadeMedidaDAO;
import main.models.UnidadeMedida;

import java.util.ArrayList;
import java.util.List;

public class UnidadeMedidaFormModal {

    private static final String AZUL         = "#194e8f";
    private static final String VERMELHO     = "#c0392b";
    private static final String ESTILO_LABEL = "-fx-font-size: 11; -fx-font-family: 'Segoe UI'; -fx-text-fill: #333333;";
    private static final String ESTILO_CAMPO = "-fx-font-size: 11; -fx-font-family: 'Segoe UI'; -fx-background-color: white; -fx-border-color: #cccccc; -fx-border-width: 1;";

    private final UnidadeMedidaDAO unidadeMedidaDAO;
    private Stage stage;
    private UnidadeMedida unidade;

    private TextField txtSigla;
    private TextField txtDescricao;

    private double xOff, yOff;

    public UnidadeMedidaFormModal(Stage owner, UnidadeMedida unidade, UnidadeMedidaDAO unidadeMedidaDAO) {
        this.unidade = unidade;
        this.unidadeMedidaDAO = unidadeMedidaDAO;

        stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setResizable(false);
        stage.setTitle(unidade == null ? "Nova Unidade de Medida" : "Editar Unidade de Medida");

        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/main/resources/logoAlter.png")));
        logo.setFitHeight(22);
        logo.setFitWidth(22);
        logo.setPreserveRatio(true);

        Label titulo = new Label(unidade == null ? "Nova Unidade de Medida" : "Editar Unidade de Medida");
        titulo.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-family: 'Segoe UI'; -fx-font-size: 11;");

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);

        Button btnFechar = new Button("X");
        btnFechar.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 12; -fx-cursor: hand; -fx-border-width: 0; -fx-padding: 0;");
        btnFechar.setOnAction(e -> stage.close());

        HBox topBar = new HBox(8, logo, titulo, espaco, btnFechar);
        topBar.setStyle("-fx-background-color: #194e8f; -fx-padding: 0 4 0 10; -fx-alignment: CENTER_LEFT;");
        topBar.setMinHeight(36);

        topBar.setOnMousePressed(e -> { xOff = stage.getX() - e.getScreenX(); yOff = stage.getY() - e.getScreenY(); });
        topBar.setOnMouseDragged(e -> { stage.setX(e.getScreenX() + xOff); stage.setY(e.getScreenY() + yOff); });

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

        VBox root = new VBox(topBar, buildFormGrid(), rodape);
        root.setStyle("-fx-background-color: white;");
        stage.setScene(new Scene(root));
        stage.sizeToScene();

        if (unidade != null) preencherCampos();
    }

    private GridPane buildFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));
        grid.setStyle("-fx-background-color: #f5f5f5;");
        grid.setPrefWidth(400);

        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(100);
        labelCol.setPrefWidth(110);

        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);
        fieldCol.setFillWidth(true);

        grid.getColumnConstraints().addAll(labelCol, fieldCol);

        txtSigla = criarCampo();
        txtSigla.setPromptText("Ex: KG, UN, LT...");
        aplicarLimiteCaracteres(txtSigla, 10);

        txtDescricao = criarCampo();
        txtDescricao.setPromptText("Ex: Quilograma, Unidade, Litro...");

        grid.addRow(0, criarLabel("Sigla *:"),     txtSigla);
        grid.addRow(1, criarLabel("Descrição:"),   txtDescricao);

        return grid;
    }

    private void preencherCampos() {
        txtSigla.setText(unidade.getSigla() != null ? unidade.getSigla() : "");
        txtDescricao.setText(unidade.getDescricao() != null ? unidade.getDescricao() : "");
    }

    private void salvar() {
        List<String> erros = new ArrayList<>();

        if (txtSigla.getText().trim().isEmpty())
            erros.add("• Sigla");

        if (!erros.isEmpty()) {
            exibirAlerta("Campos obrigatórios", "Preencha os campos abaixo antes de salvar:\n\n" + String.join("\n", erros));
            return;
        }

        try {
            if (unidade == null) unidade = new UnidadeMedida();

            unidade.setSigla(txtSigla.getText().trim().toUpperCase());
            unidade.setDescricao(txtDescricao.getText().trim());

            if (unidade.getIdUnidade() == null) {
                unidadeMedidaDAO.save(unidade);
            } else {
                unidadeMedidaDAO.update(unidade);
            }

            exibirAlerta("Sucesso", "Unidade de medida salva com sucesso!");
            stage.close();
        } catch (Exception e) {
            exibirAlerta("Erro", "Erro ao salvar a unidade de medida: " + e.getMessage());
        }
    }

    private void aplicarLimiteCaracteres(TextField campo, int limite) {
        campo.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > limite)
                campo.setText(oldVal);
        });
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
        btnFechar.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 12; -fx-cursor: hand; -fx-border-width: 0; -fx-padding: 0;");
        btnFechar.setOnAction(e -> dialog.close());

        HBox topBar = new HBox(8, logo, lblTitulo, espacoTop, btnFechar);
        topBar.setStyle("-fx-background-color: #194e8f; -fx-padding: 6 4 6 10; -fx-alignment: CENTER_LEFT;");

        Label lblMensagem = new Label(mensagem);
        lblMensagem.setStyle("-fx-font-size: 11; -fx-font-family: 'Segoe UI'; -fx-text-fill: #333333; -fx-wrap-text: true;");
        lblMensagem.setMaxWidth(340);
        lblMensagem.setPadding(new Insets(16));

        Button btnOk = new Button("OK");
        btnOk.setStyle("-fx-background-color: " + AZUL + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11; -fx-font-family: 'Segoe UI'; -fx-cursor: hand;");
        btnOk.setPrefHeight(30);
        btnOk.setPrefWidth(80);
        btnOk.setOnAction(e -> dialog.close());

        HBox rodape = new HBox(btnOk);
        rodape.setPadding(new Insets(10));
        rodape.setAlignment(Pos.CENTER_RIGHT);
        rodape.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #d8d8d8; -fx-border-width: 1 0 0 0;");

        VBox root = new VBox(topBar, new HBox(lblMensagem), rodape);
        root.setStyle("-fx-background-color: #f5f5f5;");
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
