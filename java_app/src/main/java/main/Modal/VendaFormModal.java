package main.Modal;

import java.math.BigDecimal;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class VendaFormModal {

    private static final String AZUL     = "#194e8f";
    private static final String VERMELHO = "#c0392b";
    private static final String ESTILO_LABEL = "-fx-font-size: 11; -fx-font-family: 'Segoe UI'; -fx-text-fill: #333333;";
    private static final String ESTILO_CAMPO = "-fx-font-size: 11; -fx-font-family: 'Segoe UI'; -fx-background-color: white; -fx-border-color: #cccccc; -fx-border-width: 1;";

    private Stage stage;
    private double xOff, yOff;

    private TextField txtBuscaProduto;
    private TableView<ItemVendaTemp> tabelaItens;
    private ObservableList<ItemVendaTemp> itens = FXCollections.observableArrayList();
    private Label lblTotal;

    public static class ItemVendaTemp {
        private String produto;
        private Integer quantidade;
        private BigDecimal precoUnitario;

        public ItemVendaTemp(String produto, Integer quantidade, BigDecimal precoUnitario) {
            this.produto = produto;
            this.quantidade = quantidade;
            this.precoUnitario = precoUnitario;
        }

        public String getProduto() { return produto; }
        public Integer getQuantidade() { return quantidade; }
        public BigDecimal getPrecoUnitario() { return precoUnitario; }
        public BigDecimal getSubtotal() {
            if (precoUnitario == null || quantidade == null) return BigDecimal.ZERO;
            return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
        }
    }

    public VendaFormModal(Stage owner) {
        stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);

        // TopBar
        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/main/resources/logoAlter.png")));
        logo.setFitHeight(22); logo.setFitWidth(22); logo.setPreserveRatio(true);

        Label titulo = new Label("Nova Venda");
        titulo.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");

        Region espacoTop = new Region();
        HBox.setHgrow(espacoTop, Priority.ALWAYS);

        Button btnFechar = new Button("X");
        btnFechar.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 12; -fx-cursor: hand; -fx-border-width: 0; -fx-padding: 0;");
        btnFechar.setOnAction(e -> stage.close());

        HBox topBar = new HBox(8, logo, titulo, espacoTop, btnFechar);
        topBar.setStyle("-fx-background-color: #194e8f; -fx-padding: 0 4 0 10; -fx-alignment: CENTER_LEFT;");
        topBar.setOnMousePressed(e -> { xOff = stage.getX() - e.getScreenX(); yOff = stage.getY() - e.getScreenY(); });
        topBar.setOnMouseDragged(e -> { stage.setX(e.getScreenX() + xOff); stage.setY(e.getScreenY() + yOff); });

        // Busca de produto
        Label lblBusca = new Label("Buscar produto:");
        lblBusca.setStyle(ESTILO_LABEL);

        txtBuscaProduto = new TextField();
        txtBuscaProduto.setPromptText("Nome ou código de barras...");
        txtBuscaProduto.setStyle(ESTILO_CAMPO);
        txtBuscaProduto.setPrefHeight(24);
        HBox.setHgrow(txtBuscaProduto, Priority.ALWAYS);

        HBox barraBusca = new HBox(10, lblBusca, txtBuscaProduto);
        barraBusca.setAlignment(Pos.CENTER_LEFT);
        barraBusca.setPadding(new Insets(10, 12, 10, 12));
        barraBusca.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #d8d8d8; -fx-border-width: 0 0 1 0;");

        // Tabela de itens
        tabelaItens = new TableView<>(itens);
        tabelaItens.setStyle("-fx-background-color: white;");
        VBox.setVgrow(tabelaItens, Priority.ALWAYS);

        TableColumn<ItemVendaTemp, String> colProduto = new TableColumn<>("Produto");
        colProduto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProduto()));
        colProduto.setPrefWidth(250);

        TableColumn<ItemVendaTemp, Integer> colQtd = new TableColumn<>("Qtd");
        colQtd.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getQuantidade()));
        colQtd.setPrefWidth(70);

        TableColumn<ItemVendaTemp, BigDecimal> colPreco = new TableColumn<>("Preço Unit.");
        colPreco.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getPrecoUnitario()));
        colPreco.setPrefWidth(120);

        TableColumn<ItemVendaTemp, BigDecimal> colSubtotal = new TableColumn<>("Subtotal");
        colSubtotal.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getSubtotal()));
        colSubtotal.setPrefWidth(120);

        tabelaItens.getColumns().add(colProduto);
        tabelaItens.getColumns().add(colQtd);
        tabelaItens.getColumns().add(colPreco);
        tabelaItens.getColumns().add(colSubtotal);

        Separator sep = new Separator();

        // Rodapé com total e botões
        lblTotal = new Label("R$ 0,00");
        lblTotal.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-font-family: 'Segoe UI'; -fx-text-fill: " + AZUL + ";");

        Label lblTotalLabel = new Label("Total:");
        lblTotalLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-font-family: 'Segoe UI'; -fx-text-fill: #333333;");

        Region espacoRodape = new Region();
        HBox.setHgrow(espacoRodape, Priority.ALWAYS);

        Button btnFinalizar = new Button("Finalizar Venda");
        btnFinalizar.setStyle(
            "-fx-background-color: " + AZUL + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 11;" +
            "-fx-font-family: 'Segoe UI';" +
            "-fx-cursor: hand;"
        );
        btnFinalizar.setPrefHeight(32);
        btnFinalizar.setOnAction(e -> finalizar());

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
        btnCancelar.setOnAction(e -> stage.close());

        HBox rodape = new HBox(12, lblTotalLabel, lblTotal, espacoRodape, btnFinalizar, btnCancelar);
        rodape.setAlignment(Pos.CENTER_LEFT);
        rodape.setPadding(new Insets(10, 12, 10, 12));
        rodape.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #d8d8d8; -fx-border-width: 1 0 0 0;");

        VBox root = new VBox(topBar, barraBusca, tabelaItens, sep, rodape);
        stage.setScene(new Scene(root, 620, 480));
    }

    private void finalizar() {
        // implementação futura
        stage.close();
    }

    public void show() {
        stage.showAndWait();
    }
}
