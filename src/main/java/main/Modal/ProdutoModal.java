package main.Modal;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.math.BigDecimal;
import java.util.List;
public class ProdutoModal extends BaseModal<Produto> {

    @FXML private ComboBox<Categoria> cbCategoria;
    @FXML private ComboBox<Fornecedor> cbFornecedor;
    @FXML private CheckBox chkAtivo;
    @FXML private CheckBox chkControlaEstoque;
    @FXML private CheckBox chkBalanca;

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
        chkBalanca.setOnAction(e -> applyFilters());

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
        boolean balancaOk    = !chkBalanca.isSelected()         || p.isBalanca();

        return categoriaOk && fornecedorOk && ativoOk && estoqueOk && balancaOk;
    }

    @Override
    protected void resetFilters() {
        cbCategoria.setValue(null);
        cbFornecedor.setValue(null);
        chkAtivo.setSelected(false);
        chkControlaEstoque.setSelected(false);
        chkBalanca.setSelected(false);
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
        if (selected != null) {
            ProdutoFormModal form = new ProdutoFormModal(stage, selected);
            form.show();
            loadData();
        }
    }

    @Override
    @FXML
    protected void excluirSelecionado() {
        Produto selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            ProdutoDAO.delete(selected.getIdProduto());
            loadData();
        }
    }
}
