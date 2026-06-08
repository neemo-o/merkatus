package main.Modal;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import main.database.DAOs.CategoriaDAO;
import main.database.DAOs.FornecedorDAO;
import main.database.DAOs.ProdutoDAO;
import main.database.DAOs.UnidadeMedidaDAO;
import main.models.Categoria;
import main.models.Fornecedor;
import main.models.Produto;
import main.services.FormatacaoService;
import main.services.ProdutoService;
import main.util.DialogUtil;
import main.util.FXMLLoaderFactory;
import main.util.TableViewUtils;

public class ProdutoModal extends BaseModal<Produto> {

    @FXML
    private ComboBox<Categoria> cbCategoria;
    @FXML
    private ComboBox<Fornecedor> cbFornecedor;
    @FXML
    private CheckBox chkAtivo;
    @FXML
    private Button btnEditar;
    @FXML
    private Button btnExcluir;
    @FXML
    private Button btnLixeira; // 👈 Botão da lixeira

    private final ProdutoService produtoService;
    private static final DecimalFormat FORMATADOR_MOEDA = new DecimalFormat("R$#,##0.00");

    public ProdutoModal(Stage owner,
            ProdutoDAO produtoDAO, CategoriaDAO categoriaDAO,
            FornecedorDAO fornecedorDAO, UnidadeMedidaDAO unidadeMedidaDAO,
            FXMLLoaderFactory fxmlLoaderFactory,
            ProdutoService produtoService) {
        super(owner, "Produtos", "/main/view/ProdutoModal.fxml",
                produtoDAO, categoriaDAO, fornecedorDAO, unidadeMedidaDAO, fxmlLoaderFactory);
        this.produtoService = produtoService;
    }

    @Override
    @FXML
    public void initialize() {
        // Carrega os combos de filtro
        cbCategoria.getItems().add(null);
        cbCategoria.getItems().addAll(categoriaDAO.findAll());
        cbCategoria.setOnAction(e -> applyFilters());

        cbFornecedor.getItems().add(null);
        cbFornecedor.getItems().addAll(fornecedorDAO.findAll());
        cbFornecedor.setOnAction(e -> applyFilters());

        chkAtivo.setOnAction(e -> applyFilters());

        // Estado inicial dos botões
        btnEditar.setDisable(true);
        btnExcluir.setDisable(true);

        // 👇 Ação do botão da lixeira
        if (btnLixeira != null) {
            btnLixeira.setOnAction(e -> abrirLixeira());
        }

        // Listener para habilitar botões ao selecionar item
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
        return produtoDAO.findAll();
    }

    @Override
    protected void configureColumns(TableView<Produto> table) {
        TableColumn<Produto, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idProduto"));
        colId.setMinWidth(35);
        colId.setMaxWidth(35);
        colId.setPrefWidth(35);

        TableColumn<Produto, String> colCodigo = new TableColumn<>("Cód. Barras");
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoBarras"));
        colCodigo.setMinWidth(100);
        colCodigo.setMaxWidth(100);
        colCodigo.setPrefWidth(100);

        TableColumn<Produto, String> colDescricao = new TableColumn<>("Descrição");
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colDescricao.setMinWidth(150);
        colDescricao.setMaxWidth(Double.MAX_VALUE); // ✅ Flexível
        colDescricao.setPrefWidth(200);

        TableColumn<Produto, String> colUnidade = new TableColumn<>("UN");
        colUnidade.setCellValueFactory(cellData -> {
            String sigla = cellData.getValue().getUnidadeMedida();
            return new javafx.beans.property.SimpleStringProperty(sigla != null ? sigla : "");
        });
        colUnidade.setMinWidth(50);
        colUnidade.setMaxWidth(50);
        colUnidade.setPrefWidth(50);
        colUnidade.setStyle("-fx-alignment: CENTER;");

        TableColumn<Produto, BigDecimal> colPrecoCusto = new TableColumn<>("Preço Custo");
        colPrecoCusto.setCellValueFactory(new PropertyValueFactory<>("precoCusto"));
        colPrecoCusto.setPrefWidth(120);
        colPrecoCusto.setStyle("-fx-alignment: CENTER-RIGHT;");
        colPrecoCusto.setCellFactory(FormatacaoService.cellFactoryMoeda());

        TableColumn<Produto, BigDecimal> colPrecoVenda = new TableColumn<>("Preço Venda");
        colPrecoVenda.setCellValueFactory(new PropertyValueFactory<>("precoVenda"));
        colPrecoVenda.setPrefWidth(120);
        colPrecoVenda.setStyle("-fx-alignment: CENTER-RIGHT;");
        colPrecoVenda.setCellFactory(FormatacaoService.cellFactoryMoeda());

        TableColumn<Produto, Integer> colEstoque = new TableColumn<>("Estoque");
        colEstoque.setCellValueFactory(new PropertyValueFactory<>("estoqueAtual"));
        colEstoque.setPrefWidth(90);
        colEstoque.setStyle("-fx-alignment: CENTER;");

        TableColumn<Produto, String> colFornecedor = new TableColumn<>("Fornecedor");
        colFornecedor.setCellValueFactory(cellData -> {
            Integer idFornecedor = cellData.getValue().getIdFornecedor();
            if (idFornecedor != null) {
                var fornecedorOpt = fornecedorDAO.findById(idFornecedor);
                if (fornecedorOpt.isPresent()) {
                    Fornecedor f = fornecedorOpt.get();
                    String nome = f.getNomeFantasia() != null ? f.getNomeFantasia() : f.getRazaoSocial();
                    return new javafx.beans.property.SimpleStringProperty(nome);
                }
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        colFornecedor.setMinWidth(80);
        colFornecedor.setMaxWidth(Double.MAX_VALUE); // ✅ Flexível
        colFornecedor.setPrefWidth(180);

        TableColumn<Produto, String> colAtivo = new TableColumn<>("Ativo");
        colAtivo.setCellValueFactory(cellData -> {
            Boolean ativo = cellData.getValue().isAtivo();
            String texto = (ativo != null && ativo) ? "Sim" : "Não";
            return new javafx.beans.property.SimpleStringProperty(texto);
        });
        colAtivo.setPrefWidth(70);
        colAtivo.setStyle("-fx-alignment: CENTER;");

        table.getColumns().addAll(colId, colCodigo, colDescricao, colUnidade,
                colPrecoCusto, colPrecoVenda, colEstoque, colFornecedor, colAtivo);

        table.setItems(filteredItems);

        // Auto-ajusta APENAS as colunas que devem ter tamanho fixo
        TableViewUtils.autoFitColumns(table,
                colId,
                colCodigo,
                colUnidade,
                colPrecoCusto,
                colPrecoVenda,
                colEstoque,
                colAtivo);

        // Deixa "Descrição" e "Fornecedor" flexíveis para preencher o espaço restante
        // Isso elimina completamente a barra de rolagem horizontal.
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Oculta a barra de rolagem horizontal (opcional, via CSS)
        table.setStyle("-fx-padding: 0;");
    }

    @Override
    protected boolean matchesSearch(Produto p, String query) {
        if (query.isEmpty())
            return true;
        return (p.getDescricao() != null && p.getDescricao().toLowerCase().contains(query))
                || (p.getCodigoBarras() != null && p.getCodigoBarras().toLowerCase().contains(query));
    }

    @Override
    protected boolean matchesFilters(Produto p) {
        boolean categoriaOk = cbCategoria.getValue() == null
                || (p.getIdCategoria() != null && p.getIdCategoria().equals(cbCategoria.getValue().getIdCategoria()));

        boolean fornecedorOk = cbFornecedor.getValue() == null
                || (p.getIdFornecedor() != null
                        && p.getIdFornecedor().equals(cbFornecedor.getValue().getIdFornecedor()));

        boolean ativoOk = !chkAtivo.isSelected() || p.isAtivo();
        return categoriaOk && fornecedorOk && ativoOk;
    }

    @Override
    protected void resetFilters() {
        cbCategoria.setValue(null);
        cbFornecedor.setValue(null);
        chkAtivo.setSelected(false);
    }

    @Override
    protected void abrirFormNovo() {
        ProdutoFormModal form = new ProdutoFormModal(stage, null,
                produtoDAO, categoriaDAO, fornecedorDAO, unidadeMedidaDAO, produtoService);
        form.show();
        loadData();
    }

    @Override
    protected void abrirFormEdicao() {
        Produto selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            exibirAlerta("Selecione um produto para editar.");
            return;
        }
        ProdutoFormModal form = new ProdutoFormModal(stage, selected,
                produtoDAO, categoriaDAO, fornecedorDAO, unidadeMedidaDAO, produtoService);
        form.show();
        loadData();
    }

    // Método para abrir a lixeira
    private void abrirLixeira() {
        ProdutoLixeiraModal lixeira = new ProdutoLixeiraModal(
                stage,
                produtoDAO,
                categoriaDAO,
                fornecedorDAO,
                unidadeMedidaDAO,
                fxmlLoaderFactory,
                produtoService);

        lixeira.show(); // Aguarda fechar (BaseModal já usa showAndWait)
        loadData(); // Recarrega a tabela principal após fechar
    }

    @Override
    @FXML
    protected void excluirSelecionado() {
        Produto selected = tableView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            exibirAlerta("Selecione um produto para excluir.");
            return;
        }

        String nome = selected.getDescricao() != null ? selected.getDescricao() : "sem nome";
        boolean confirmado = DialogUtil.confirmar(stage, "Confirmar exclusão",
                "Deseja marcar o produto '" + nome + "' como inativo?");

        if (!confirmado)
            return;

        try {
            produtoService.excluirProduto(selected.getIdProduto());
            exibirAlerta("Produto marcado como inativo com sucesso.");
            loadData();
        } catch (RuntimeException e) {
            exibirAlerta(e.getMessage());
        }
    }

    private void exibirAlerta(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.initOwner(stage);
        alerta.getDialogPane().setStyle("-fx-font-family: 'Segoe UI';");
        alerta.showAndWait();
    }
}