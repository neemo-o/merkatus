package main.Modal;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import main.database.DAOs.*;
import main.models.Produto;
import main.services.ProdutoService;
import main.util.DialogUtil;
import main.util.FXMLLoaderFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ProdutoLixeiraModal extends BaseModal<Produto> {

    private final ProdutoService produtoService;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private Button btnEditar;
    @FXML
    private Button btnExcluir;

    public ProdutoLixeiraModal(Stage owner,
            ProdutoDAO produtoDAO, CategoriaDAO categoriaDAO,
            FornecedorDAO fornecedorDAO, UnidadeMedidaDAO unidadeMedidaDAO,
            FXMLLoaderFactory fxmlLoaderFactory,
            ProdutoService produtoService) {
        super(owner, "Lixeira de Produtos", "/main/view/ProdutoLixeiraModal.fxml",
                produtoDAO, categoriaDAO, fornecedorDAO, unidadeMedidaDAO, fxmlLoaderFactory);
        this.produtoService = produtoService;
    }

    @Override
    @FXML
    public void initialize() {

        if (btnEditar != null) {
            btnEditar.setText("Restaurar");
            btnEditar.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
            btnEditar.setOnAction(e -> restaurarSelecionado());
        }

        if (btnExcluir != null) {
            btnExcluir.setText("Excluir Permanentemente");
            btnExcluir.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
            btnExcluir.setOnAction(e -> excluirPermanentemente());
        }

        // Listener para habilitar botões
        if (tableView != null) {
            tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            tableView.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
                boolean enabled = selected != null;
                if (btnEditar != null)
                    btnEditar.setDisable(!enabled);
                if (btnExcluir != null)
                    btnExcluir.setDisable(!enabled);
            });
        }

        super.initialize();
    }

    @Override
    protected List<Produto> fetchFromDatabase() {
        return produtoDAO.findAllIncludingInactive().stream()
                .filter(p -> !p.isAtivo())
                .collect(Collectors.toList());
    }

    @Override
    protected void configureColumns(TableView<Produto> table) {
        TableColumn<Produto, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idProduto"));
        colId.setPrefWidth(40);
        colId.setStyle("-fx-alignment: CENTER;");

        TableColumn<Produto, String> colDescricao = new TableColumn<>("Descrição");
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colDescricao.setPrefWidth(200);

        TableColumn<Produto, String> colCodigo = new TableColumn<>("Cód. Barras");
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoBarras"));
        colCodigo.setPrefWidth(100);

        // Coluna: Data da Exclusão (auditoria)
        TableColumn<Produto, String> colDataExclusao = new TableColumn<>("Excluído em");
        colDataExclusao.setCellValueFactory(cellData -> {
            var data = cellData.getValue().getDataAtualizacao();
            String formatted = (data != null) ? data.format(DATE_FORMAT) : "-";
            return new javafx.beans.property.SimpleStringProperty(formatted);
        });
        colDataExclusao.setPrefWidth(130);
        colDataExclusao.setStyle("-fx-alignment: CENTER;");

        // Coluna: Estoque (para referência antes de restaurar/excluir)
        TableColumn<Produto, Integer> colEstoque = new TableColumn<>("Estoque");
        colEstoque.setCellValueFactory(new PropertyValueFactory<>("estoqueAtual"));
        colEstoque.setPrefWidth(70);
        colEstoque.setStyle("-fx-alignment: CENTER;");

        table.getColumns().addAll(colId, colDescricao, colCodigo, colDataExclusao, colEstoque);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Estilo visual para diferenciar a lixeira
        table.setStyle("-fx-control-inner-background: #fff9f9;");
    }

    // ==============================
    // AÇÕES DA LIXEIRA
    // ==============================

    @FXML
    private void restaurarSelecionado() {
        Produto selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            exibirAlerta("Selecione um produto para restaurar.", Alert.AlertType.WARNING);
            return;
        }

        String nome = selected.getDescricao() != null ? selected.getDescricao() : "sem nome";
        if (!DialogUtil.confirmar(stage, "Confirmar restauração",
                "Deseja reativar o produto '" + nome + "'?")) {
            return;
        }

        try {
            boolean sucesso = produtoService.reativarProduto(selected.getIdProduto());
            if (sucesso) {
                exibirAlerta("Produto restaurado com sucesso!", Alert.AlertType.INFORMATION);
                loadData();
            }
        } catch (RuntimeException e) {
            exibirAlerta(e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void excluirPermanentemente() {
        Produto selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            exibirAlerta("Selecione um produto para exclusão permanente.", Alert.AlertType.WARNING);
            return;
        }

        String nome = selected.getDescricao() != null ? selected.getDescricao() : "sem nome";

        if (!DialogUtil.confirmar(stage, "Exclusão Permanente",
                "ATENÇÃO: Esta ação NÃO pode ser desfeita!\n\n" +
                        "Deseja EXCLUIR PERMANENTEMENTE o produto '" + nome + "'?\n" +
                        "Todos os dados serão perdidos.")) {
            return;
        }

        try {
            boolean sucesso = produtoDAO.deletePermanente(selected.getIdProduto());
            if (sucesso) {
                exibirAlerta("Produto excluído permanentemente.", Alert.AlertType.INFORMATION);
                loadData();
            }
        } catch (RuntimeException e) {
            exibirAlerta("Erro: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @Override
    protected boolean matchesSearch(Produto p, String query) {
        if (query.isEmpty())
            return true;
        String q = query.toLowerCase();
        return (p.getDescricao() != null && p.getDescricao().toLowerCase().contains(q))
                || (p.getCodigoBarras() != null && p.getCodigoBarras().toLowerCase().contains(q));
    }

    @Override
    protected boolean matchesFilters(Produto p) {
        return true; // Na lixeira, não aplica filtros extras
    }

    @Override
    protected void resetFilters() {
        // Não necessário
    }

    @Override
    protected void abrirFormNovo() {
        exibirAlerta("Para cadastrar um novo produto, use a tela principal.", Alert.AlertType.INFORMATION);
    }

    @Override
    protected void abrirFormEdicao() {
        restaurarSelecionado();
    }

    @Override
    protected void excluirSelecionado() {
        excluirPermanentemente();
    }

    private void exibirAlerta(String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(
                tipo == Alert.AlertType.ERROR ? "Erro" : tipo == Alert.AlertType.WARNING ? "Atenção" : "Informação");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.initOwner(stage);
        alert.getDialogPane().setStyle("-fx-font-family: 'Segoe UI';");
        alert.showAndWait();
    }
}