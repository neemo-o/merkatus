package main.Modal;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.math.BigDecimal;
import java.util.List;
import javafx.scene.control.cell.PropertyValueFactory;

public class ProdutoModal extends BaseModal<Produto> {

    @FXML private ComboBox<Categoria> cbCategoria;
    @FXML private ComboBox<Fornecedor> cbFornecedor;
    @FXML private CheckBox chkAtivo;
    @FXML private CheckBox chkControlaEstoque;
    @FXML private Button btnEditar;
    @FXML private Button btnExcluir;
    

    public ProdutoModal(Stage owner) {
        super(owner, "Produtos", "/main/view/ProdutoModal.fxml");
    }

    @Override
    @FXML
    public void initialize() {
        // Carrega os combos de filtro
        cbCategoria.getItems().add(null);
        cbCategoria.getItems().addAll(CategoriaDAO.findAll());
        cbCategoria.setOnAction(e -> applyFilters());

        cbFornecedor.getItems().add(null);
        cbFornecedor.getItems().addAll(FornecedorDAO.findAll());
        cbFornecedor.setOnAction(e -> applyFilters());

        chkAtivo.setOnAction(e -> applyFilters());
        chkControlaEstoque.setOnAction(e -> applyFilters());

        btnEditar.setDisable(true);
        btnExcluir.setDisable(true);

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            boolean enabled = newValue != null;
            btnEditar.setDisable(!enabled);
            btnExcluir.setDisable(!enabled);
        });

        // Chama o initialize da BaseModal para configurar tabela e busca
        super.initialize();
    }

    @Override
    protected List<Produto> fetchFromDatabase() {
        return ProdutoDAO.findAll();
    }

    @Override
    protected void configureColumns(TableView<Produto> table) {
        TableColumn<Produto, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idProduto"));
        colId.setPrefWidth(55);

        TableColumn<Produto, String> colCodigo = new TableColumn<>("Cód. Barras");
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoBarras"));
        colCodigo.setPrefWidth(140);

        TableColumn<Produto, String> colDescricao = new TableColumn<>("Descrição");
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colDescricao.setPrefWidth(260);

        TableColumn<Produto, String> colUnidade = new TableColumn<>("Unidade");
        colUnidade.setCellValueFactory(new PropertyValueFactory<>("unidadeMedida"));
        colUnidade.setPrefWidth(90);

        TableColumn<Produto, BigDecimal> colPrecoCusto = new TableColumn<>("Preço Custo");
        colPrecoCusto.setCellValueFactory(new PropertyValueFactory<>("precoCusto"));
        colPrecoCusto.setPrefWidth(120);

        TableColumn<Produto, BigDecimal> colPrecoVenda = new TableColumn<>("Preço Venda");
        colPrecoVenda.setCellValueFactory(new PropertyValueFactory<>("precoVenda"));
        colPrecoVenda.setPrefWidth(120);

        TableColumn<Produto, Integer> colEstoque = new TableColumn<>("Estoque");
        colEstoque.setCellValueFactory(new PropertyValueFactory<>("estoqueAtual"));
        colEstoque.setPrefWidth(90);

        TableColumn<Produto, String> colFornecedor = new TableColumn<>("Fornecedor");
        colFornecedor.setCellValueFactory(new PropertyValueFactory<>("nomeFornecedor"));
        colFornecedor.setPrefWidth(180);

        TableColumn<Produto, Boolean> colAtivo = new TableColumn<>("Ativo");
        colAtivo.setCellValueFactory(new PropertyValueFactory<>("ativo"));
        colAtivo.setPrefWidth(70);

        table.getColumns().addAll(
            colId, colCodigo, colDescricao, colUnidade,
            colPrecoCusto, colPrecoVenda,
            colEstoque, colFornecedor, colAtivo
        );
    }

    @Override
    protected boolean matchesSearch(Produto p, String query) {
        if (query.isEmpty()) return true;
        return (p.getDescricao() != null && p.getDescricao().toLowerCase().contains(query))
            || (p.getCodigoBarras() != null && p.getCodigoBarras().toLowerCase().contains(query));
    }

    @Override
    protected boolean matchesFilters(Produto p) {
        boolean categoriaOk  = cbCategoria.getValue() == null
            || (p.getIdCategoria() != null
                && p.getIdCategoria().equals(cbCategoria.getValue().getIdCategoria()));

        boolean fornecedorOk = cbFornecedor.getValue() == null
            || (p.getIdFornecedor() != null
                && p.getIdFornecedor().equals(cbFornecedor.getValue().getIdFornecedor()));

        boolean ativoOk      = !chkAtivo.isSelected()          || p.isAtivo();
        boolean estoqueOk    = !chkControlaEstoque.isSelected() || p.isControlaEstoque();

        return categoriaOk && fornecedorOk && ativoOk && estoqueOk;
    }

    @Override
    protected void resetFilters() {
        cbCategoria.setValue(null);
        cbFornecedor.setValue(null);
        chkAtivo.setSelected(false);
        chkControlaEstoque.setSelected(false);
    }

    @Override
    @FXML
    protected void abrirFormNovo() {
        ProdutoFormModal form = new ProdutoFormModal(stage, null);
        form.show();
        loadData();
    }

    @Override
    @FXML
    protected void abrirFormEdicao() {
        Produto selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            exibirAlerta("Selecione um produto para editar.");
            return;
        }
        ProdutoFormModal form = new ProdutoFormModal(stage, selected);
        form.show();
        loadData();
    }

    @Override
    @FXML
    protected void excluirSelecionado() {
        Produto selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            exibirAlerta("Selecione um produto para excluir.");
            return;
        }

        if (!mostrarConfirmacaoExclusao(selected)) {
            return;
        }

        ProdutoDAO.delete(selected.getIdProduto());
        loadData();
    }

    private boolean mostrarConfirmacaoExclusao(Produto produto) {
        try {
            Stage confirmStage = new Stage();
            confirmStage.initOwner(stage);
            confirmStage.initModality(Modality.WINDOW_MODAL);
            confirmStage.initStyle(StageStyle.UNDECORATED);
            confirmStage.setTitle("Confirmar exclusão");
            confirmStage.setResizable(false);

            HBox topBar = new HBox();
            topBar.setMinHeight(8);
            topBar.setPrefHeight(8);
            topBar.setMaxWidth(Double.MAX_VALUE);
            topBar.setStyle("-fx-background-color: #194e8f;");

            Label mensagem = new Label("Deseja excluir o produto '" +
                    (produto.getDescricao() != null ? produto.getDescricao() : "sem nome") + "'?");
            mensagem.setWrapText(true);
            mensagem.setTextAlignment(TextAlignment.CENTER);
            mensagem.setStyle("-fx-font-size: 12; -fx-font-family: 'Segoe UI'; -fx-text-fill: #333333;");
            mensagem.setMaxWidth(340);

            Button btnConfirmar = new Button("Confirmar");
            btnConfirmar.setStyle("-fx-background-color: #194e8f; -fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 20;");

            Button btnCancelar = new Button("Cancelar");
            btnCancelar.setStyle("-fx-background-color: transparent; -fx-border-color: #194e8f; -fx-text-fill: #194e8f; -fx-font-family: 'Segoe UI'; -fx-cursor: hand; -fx-padding: 8 20; -fx-border-width: 1;");

            HBox botoes = new HBox(10, btnCancelar, btnConfirmar);
            botoes.setAlignment(Pos.CENTER);
            botoes.setPadding(new Insets(12, 0, 0, 0));

            VBox content = new VBox(16, topBar, mensagem, botoes);
            content.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 0 0 16 0;");
            content.setPrefWidth(380);
            VBox.setMargin(mensagem, new Insets(16, 16, 0, 16));
            VBox.setMargin(botoes, new Insets(16, 16, 0, 16));

            Scene scene = new Scene(content);
            confirmStage.setScene(scene);
            confirmStage.sizeToScene();

            final boolean[] confirmed = {false};
            btnConfirmar.setOnAction(e -> {
                confirmed[0] = true;
                confirmStage.close();
            });
            btnCancelar.setOnAction(e -> confirmStage.close());

            confirmStage.showAndWait();
            return confirmed[0];
        } catch (Exception e) {
            Alert fallback = new Alert(Alert.AlertType.CONFIRMATION);
            fallback.setTitle("Confirmar exclusão");
            fallback.setHeaderText(null);
            fallback.setContentText("Deseja excluir o produto '" +
                    (produto.getDescricao() != null ? produto.getDescricao() : "sem nome") + "'?");
            fallback.initOwner(stage);
            return fallback.showAndWait().filter(response -> response == ButtonType.OK).isPresent();
        }
    }

    private void exibirAlerta(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.initOwner(stage);
        alerta.showAndWait();
    }
}
