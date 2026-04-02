package main.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import main.Modal.Categoria; 
import main.Modal.Fornecedor;
import main.Modal.FornecedorDAO;
import main.Modal.Produto;
import main.Modal.ProdutoDAO;
import main.Modal.UnidadeMedida;
import main.Modal.UnidadeMedidaDAO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ProdutosController {

    @FXML
    private TextField searchField;

    @FXML
    private TableView<Produto> tableProdutos;

    @FXML
    private TableColumn<Produto, Integer> colId;

    @FXML
    private TableColumn<Produto, String> colCodigo;

    @FXML
    private TableColumn<Produto, String> colNome;

    @FXML
    private TableColumn<Produto, String> colCategoria;

    @FXML
    private TableColumn<Produto, BigDecimal> colPreco;

    @FXML
    private TableColumn<Produto, Integer> colEstoque;

    @FXML
    private TableColumn<Produto, String> colFornecedor;

    @FXML
    private Label lblTotal;

    @FXML
    private Button btnNovo;

    @FXML
    private Button btnEditar;

    @FXML
    private Button btnExcluir;

    @FXML
    private ScrollPane formContainer;

    @FXML
    private Text lblFormTitle;

    @FXML
    private TextField txtCodigo;

    @FXML
    private TextField txtNome;

    @FXML
    private ComboBox<UnidadeMedida> cbCategoria;

    @FXML
    private TextField txtPreco;

    @FXML
    private TextField txtEstoque;

    @FXML
    private ComboBox<Fornecedor> cbFornecedor;

    private ObservableList<Produto> produtos = FXCollections.observableArrayList();

    private Produto produtoEditando;

    @FXML
    public void initialize() {
        configurarColunas();
        carregarProdutos();
        carregarCombos();
        esconderFormulario();

        searchField.textProperty().addListener((obs, oldValue, newValue) -> aplicarFiltro(newValue));

        tableProdutos.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            btnEditar.setDisable(newValue == null);
            btnExcluir.setDisable(newValue == null);
        });

        btnEditar.setDisable(true);
        btnExcluir.setDisable(true);
    }

    private void configurarColunas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idProduto"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoBarras"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("unidadeMedida"));
        colPreco.setCellValueFactory(new PropertyValueFactory<>("precoVenda"));
        colEstoque.setCellValueFactory(new PropertyValueFactory<>("estoqueAtual"));
        colFornecedor.setCellValueFactory(new PropertyValueFactory<>("nomeFornecedor"));
    }

    private void carregarProdutos() {
        produtos.setAll(ProdutoDAO.findAll());
        tableProdutos.setItems(produtos);
        atualizarTotal();
    }

    private void carregarCombos() {
        cbCategoria.getItems().setAll(UnidadeMedidaDAO.findAll());
        cbFornecedor.getItems().setAll(FornecedorDAO.findAll());
    }

    private void aplicarFiltro(String filtro) {
        if (filtro == null || filtro.isBlank()) {
            tableProdutos.setItems(produtos);
        } else {
            String termo = filtro.toLowerCase().trim();
            tableProdutos.setItems(produtos.filtered(p ->
                (p.getDescricao() != null && p.getDescricao().toLowerCase().contains(termo))
                || (p.getCodigoBarras() != null && p.getCodigoBarras().toLowerCase().contains(termo))
                || (p.getNomeFornecedor() != null && p.getNomeFornecedor().toLowerCase().contains(termo))
            ));
        }
        atualizarTotal();
    }

    private void atualizarTotal() {
        lblTotal.setText("Total: " + tableProdutos.getItems().size() + " registros");
    }

    @FXML
    private void handleNovo() {
        produtoEditando = new Produto();
        lblFormTitle.setText("Cadastro de Produto");
        limparFormulario();
        mostrarFormulario();
    }

    @FXML
    private void handleEditar() {
        Produto selecionado = tableProdutos.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            exibirAlerta("Selecione um produto para editar.", Alert.AlertType.INFORMATION);
            return;
        }

        produtoEditando = selecionado;
        lblFormTitle.setText("Editar Produto");
        preencherFormulario(produtoEditando);
        mostrarFormulario();
    }

    @FXML
    private void handleExcluir() {
        Produto selecionado = tableProdutos.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            exibirAlerta("Selecione um produto para excluir.", Alert.AlertType.INFORMATION);
            return;
        }

        if (!mostrarConfirmacaoExclusao(selecionado)) {
            return;
        }

        ProdutoDAO.delete(selecionado.getIdProduto());
        carregarProdutos();
        if (!tableProdutos.getItems().contains(selecionado)) {
            btnEditar.setDisable(true);
            btnExcluir.setDisable(true);
        }
    }

    private boolean mostrarConfirmacaoExclusao(Produto produto) {
        try {
            Stage confirmStage = new Stage();
            confirmStage.initOwner(getStage());
            confirmStage.initModality(Modality.WINDOW_MODAL);
            confirmStage.initStyle(StageStyle.UNDECORATED);
            confirmStage.setTitle("Confirmar exclusão");
            confirmStage.setResizable(false);

            HBox topBar = new HBox();
            topBar.setMinHeight(18);
            topBar.setPrefHeight(18);
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
            fallback.initOwner(getStage());
            return fallback.showAndWait().filter(response -> response == ButtonType.OK).isPresent();
        }
    }

    @FXML
    private void handleSalvar() {
        if (produtoEditando == null) {
            produtoEditando = new Produto();
        }

        if (!validarFormulario()) {
            return;
        }

        produtoEditando.setCodigoBarras(txtCodigo.getText());
        produtoEditando.setDescricao(txtNome.getText());
        produtoEditando.setUnidadeMedida(cbCategoria.getValue());
        produtoEditando.setPrecoVenda(parseBigDecimal(txtPreco.getText()));
        produtoEditando.setEstoqueAtual(parseInteger(txtEstoque.getText()));
        produtoEditando.setFornecedor(cbFornecedor.getValue());

        if (produtoEditando.getIdProduto() == null) {
            ProdutoDAO.insert(produtoEditando);
        } else {
            ProdutoDAO.update(produtoEditando);
        }

        carregarProdutos();
        esconderFormulario();
        produtoEditando = null;
    }

    @FXML
    private void handleCancelar() {
        esconderFormulario();
        produtoEditando = null;
    }

    private void mostrarFormulario() {
        formContainer.setVisible(true);
        formContainer.setManaged(true);
    }

    private void esconderFormulario() {
        formContainer.setVisible(false);
        formContainer.setManaged(false);
    }

    private void preencherFormulario(Produto produto) {
        txtCodigo.setText(produto.getCodigoBarras());
        txtNome.setText(produto.getDescricao());
        cbCategoria.setValue(findUnidadeMedida(produto.getUnidadeMedida()));
        txtPreco.setText(produto.getPrecoVenda() != null ? produto.getPrecoVenda().toString() : "");
        txtEstoque.setText(produto.getEstoqueAtual() != null ? produto.getEstoqueAtual().toString() : "");
        cbFornecedor.setValue(findFornecedor(produto.getIdFornecedor()));
    }

    private void limparFormulario() {
        txtCodigo.clear();
        txtNome.clear();
        cbCategoria.setValue(null);
        txtPreco.clear();
        txtEstoque.clear();
        cbFornecedor.setValue(null);
    }

    private boolean validarFormulario() {
        if (txtNome.getText() == null || txtNome.getText().isBlank()) {
            exibirAlerta("O nome do produto é obrigatório.", Alert.AlertType.WARNING);
            return false;
        }
        if (txtPreco.getText() == null || txtPreco.getText().isBlank()) {
            exibirAlerta("O preço do produto é obrigatório.", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private UnidadeMedida findUnidadeMedida(String sigla) {
        if (sigla == null) {
            return null;
        }
        return cbCategoria.getItems().stream()
            .filter(u -> u != null && sigla.equals(u.getSigla()))
            .findFirst()
            .orElse(null);
    }

    private Fornecedor findFornecedor(Integer idFornecedor) {
        if (idFornecedor == null) {
            return null;
        }
        return cbFornecedor.getItems().stream()
            .filter(f -> f != null && idFornecedor.equals(f.getIdFornecedor()))
            .findFirst()
            .orElse(null);
    }

    private BigDecimal parseBigDecimal(String value) {
        try {
            return new BigDecimal(value.trim().replace(",", "."));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private Integer parseInteger(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private Stage getStage() {
        return (Stage) searchField.getScene().getWindow();
    }

    private void exibirAlerta(String mensagem, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.initOwner(getStage());
        alerta.showAndWait();
    }
}
