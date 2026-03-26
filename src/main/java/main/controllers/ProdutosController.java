package main.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.util.Duration;
import main.database.FornecedorDAO;
import main.database.ProdutoDAO;
import main.models.Fornecedor;
import main.models.Produto;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProdutosController {

    @FXML private TextField searchField;
    @FXML private TableView<Produto> tableProdutos;
    @FXML private TableColumn<Produto, Integer> colId;
    @FXML private TableColumn<Produto, String> colCodigo;
    @FXML private TableColumn<Produto, String> colNome;
    @FXML private TableColumn<Produto, String> colCategoria;
    @FXML private TableColumn<Produto, BigDecimal> colPreco;
    @FXML private TableColumn<Produto, Integer> colEstoque;
    @FXML private TableColumn<Produto, String> colFornecedor;
    @FXML private Button btnNovo;
    @FXML private Button btnEditar;
    @FXML private Button btnExcluir;
    @FXML private Label lblTotal;
    @FXML private ScrollPane formContainer;
    @FXML private Text lblFormTitle;
    @FXML private TextField txtCodigo;
    @FXML private TextField txtNome;
    @FXML private ComboBox<String> cbCategoria;
    @FXML private TextField txtPreco;
    @FXML private TextField txtEstoque;
    @FXML private ComboBox<Fornecedor> cbFornecedor;
    @FXML private Button btnSalvar;
    @FXML private Button btnCancelar;

    private ProdutoDAO produtoDAO;
    private FornecedorDAO fornecedorDAO;
    private ObservableList<Produto> produtos;
    private ObservableList<Fornecedor> fornecedores;
    private Produto produtoSelecionado;
    private boolean modoEdicao = false;

    @FXML
    void initialize() {
        produtoDAO = new ProdutoDAO();
        fornecedorDAO = new FornecedorDAO();

        configurarTableView();
        configurarComboBoxes();
        aplicarMascaras();
        aplicarValidacoes();
        
        carregarProdutos();
        carregarFornecedores();
        atualizarTotal();
        configurarBusca();

        formContainer.setVisible(false);
        formContainer.setManaged(false);
    }

    private void aplicarMascaras() {
        txtPreco.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) return;
            String cleaned = newVal.replaceAll("[^0-9.,]", "");
            
            int commaCount = cleaned.length() - cleaned.replace(",", "").length();
            int dotCount = cleaned.length() - cleaned.replace(".", "").length();
            
            if (commaCount + dotCount > 1) {
                char separator = cleaned.contains(",") ? ',' : '.';
                int firstSep = cleaned.indexOf(separator);
                String beforeSep = cleaned.substring(0, firstSep + 1);
                String afterSep = cleaned.substring(firstSep + 1).replaceAll("[,.]", "");
                cleaned = beforeSep + afterSep;
            }
            
            if (cleaned.contains(",")) {
                String[] parts = cleaned.split(",");
                if (parts.length > 1 && parts[1].length() > 2) {
                    cleaned = parts[0] + "," + parts[1].substring(0, 2);
                }
            } else if (cleaned.contains(".")) {
                String[] parts = cleaned.split("\\.");
                if (parts.length > 1 && parts[1].length() > 2) {
                    cleaned = parts[0] + "." + parts[1].substring(0, 2);
                }
            }
            
            if (!cleaned.equals(newVal)) {
                txtPreco.setText(cleaned);
                txtPreco.positionCaret(cleaned.length());
            }
        });

        txtEstoque.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("\\d*")) {
                txtEstoque.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });

        txtCodigo.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("\\d*")) {
                txtCodigo.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });

        limitarCaracteres(txtCodigo, 20);
        limitarCaracteres(txtNome, 100);
        limitarCaracteres(txtPreco, 15);
        limitarCaracteres(txtEstoque, 10);
    }

    private void aplicarValidacoes() {
        validarCampoObrigatorio(txtNome);
        validarCampoObrigatorio(txtPreco);
        validarCampoObrigatorio(txtEstoque);

        txtPreco.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && !txtPreco.getText().trim().isEmpty()) {
                try {
                    BigDecimal preco = new BigDecimal(txtPreco.getText().replace(",", "."));
                    if (preco.compareTo(BigDecimal.ZERO) > 0) {
                        txtPreco.setStyle("-fx-border-color: #7cb342; -fx-border-width: 2; -fx-background-color: white; -fx-padding: 6;");
                    } else {
                        txtPreco.setStyle("-fx-border-color: #e57373; -fx-border-width: 2; -fx-background-color: white; -fx-padding: 6;");
                    }
                } catch (NumberFormatException e) {
                    txtPreco.setStyle("-fx-border-color: #e57373; -fx-border-width: 2; -fx-background-color: white; -fx-padding: 6;");
                }
            }
        });

        txtEstoque.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && !txtEstoque.getText().trim().isEmpty()) {
                try {
                    int estoque = Integer.parseInt(txtEstoque.getText());
                    if (estoque >= 0) {
                        txtEstoque.setStyle("-fx-border-color: #7cb342; -fx-border-width: 2; -fx-background-color: white; -fx-padding: 6;");
                    } else {
                        txtEstoque.setStyle("-fx-border-color: #e57373; -fx-border-width: 2; -fx-background-color: white; -fx-padding: 6;");
                    }
                } catch (NumberFormatException e) {
                    txtEstoque.setStyle("-fx-border-color: #e57373; -fx-border-width: 2; -fx-background-color: white; -fx-padding: 6;");
                }
            }
        });
    }

    private void validarCampoObrigatorio(TextField field) {
        field.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                if (field.getText().trim().isEmpty()) {
                    field.setStyle("-fx-border-color: #e57373; -fx-border-width: 2; -fx-background-color: white; -fx-padding: 6;");
                } else {
                    field.setStyle("-fx-border-color: #7cb342; -fx-border-width: 2; -fx-background-color: white; -fx-padding: 6;");
                }
            }
        });
        
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.trim().isEmpty() && field.getStyle().contains("#e57373")) {
                field.setStyle("-fx-border-color: #7cb342; -fx-border-width: 2; -fx-background-color: white; -fx-padding: 6;");
            }
        });
    }

    private void limitarCaracteres(TextField field, int max) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > max) {
                field.setText(oldVal);
            }
        });
    }

    private void limparEstilos() {
        String estiloPadrao = "-fx-background-color: white; -fx-border-color: #d0d0d0; -fx-border-width: 1; -fx-padding: 6;";
        txtNome.setStyle(estiloPadrao);
        txtPreco.setStyle(estiloPadrao);
        txtEstoque.setStyle(estiloPadrao);
        txtCodigo.setStyle(estiloPadrao);
    }

    private void mostrarNotificacao(String titulo, String mensagem, String tipo) {
        if (txtNome == null || txtNome.getScene() == null || txtNome.getScene().getWindow() == null) {
            System.out.println(tipo.toUpperCase() + ": " + titulo + " - " + mensagem);
            return;
        }
        
        Popup popup = new Popup();
        String cor = tipo.equals("sucesso") ? "#7cb342" : tipo.equals("erro") ? "#e57373" : "#ffb74d";
        String icone = tipo.equals("sucesso") ? "✓" : tipo.equals("erro") ? "✗" : "⚠";
        
        VBox container = new VBox(5);
        container.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-border-color: " + cor + "; -fx-border-width: 2; -fx-border-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);");
        container.setPadding(new Insets(15));
        container.setMaxWidth(350);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label iconLabel = new Label(icone);
        iconLabel.setStyle("-fx-text-fill: " + cor + "; -fx-font-size: 20px; -fx-font-weight: bold;");
        
        Label titleLabel = new Label(titulo);
        titleLabel.setStyle("-fx-text-fill: " + cor + "; -fx-font-size: 14px; -fx-font-weight: bold;");
        titleLabel.setFont(Font.font("Segoe UI", 14));
        
        header.getChildren().addAll(iconLabel, titleLabel);

        Label messageLabel = new Label(mensagem);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-text-fill: #333; -fx-font-size: 12px;");
        messageLabel.setFont(Font.font("Segoe UI", 12));
        messageLabel.setMaxWidth(320);

        container.getChildren().addAll(header, messageLabel);
        popup.getContent().add(container);

        popup.setAutoHide(true);
        popup.show(txtNome.getScene().getWindow(), 
            txtNome.getScene().getWindow().getX() + txtNome.getScene().getWindow().getWidth() - 380, 
            txtNome.getScene().getWindow().getY() + 60);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), container);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        PauseTransition pause = new PauseTransition(Duration.millis(3000));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), container);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> popup.hide());

        new SequentialTransition(fadeIn, pause, fadeOut).play();
    }

    private void mostrarListaErros(List<String> erros) {
        StringBuilder mensagem = new StringBuilder();
        for (int i = 0; i < erros.size(); i++) {
            mensagem.append("• ").append(erros.get(i));
            if (i < erros.size() - 1) mensagem.append("\n");
        }
        mostrarNotificacao("Corrija os erros", mensagem.toString(), "erro");
    }

    private void configurarTableView() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idProduto"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoBarras"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("unidadeMedida"));
        colPreco.setCellValueFactory(new PropertyValueFactory<>("precoVenda"));
        colEstoque.setCellValueFactory(new PropertyValueFactory<>("estoqueAtual"));
        colFornecedor.setCellValueFactory(cellData -> {
            Integer idFornecedor = cellData.getValue().getIdFornecedor();
            if (idFornecedor != null && fornecedores != null) {
                Fornecedor fornecedor = fornecedores.stream()
                    .filter(f -> f.getIdFornecedor() == idFornecedor)
                    .findFirst().orElse(null);
                return new javafx.beans.property.SimpleStringProperty(fornecedor != null ? fornecedor.getRazaoSocial() : "");
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        colPreco.setCellFactory(column -> new TableCell<Produto, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("R$ %.2f", item));
            }
        });

        produtos = FXCollections.observableArrayList();
        tableProdutos.setItems(produtos);
    }

    private void configurarComboBoxes() {
        cbCategoria.setItems(FXCollections.observableArrayList("UN", "KG", "L", "M", "M²", "M³", "PCT", "CX", "FD"));
        cbCategoria.setValue("UN");

        fornecedores = FXCollections.observableArrayList();
        cbFornecedor.setItems(fornecedores);
        
        cbFornecedor.setCellFactory(param -> new ListCell<Fornecedor>() {
            @Override
            protected void updateItem(Fornecedor item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Selecione (opcional)" : item.getRazaoSocial());
            }
        });
        
        cbFornecedor.setButtonCell(new ListCell<Fornecedor>() {
            @Override
            protected void updateItem(Fornecedor item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Selecione (opcional)" : item.getRazaoSocial());
            }
        });
    }

    private void configurarBusca() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filtrarProdutos(newValue));
    }

    private void carregarProdutos() {
        try {
            List<Produto> listaProdutos = produtoDAO.buscarTodos();
            produtos.clear();
            produtos.addAll(listaProdutos);
        } catch (SQLException e) {
            System.err.println("Erro ao carregar produtos: " + e.getMessage());
            e.printStackTrace();
            mostrarNotificacao("Erro", "Erro ao carregar produtos: " + e.getMessage(), "erro");
        }
    }

    private void carregarFornecedores() {
        try {
            List<Fornecedor> listaFornecedores = fornecedorDAO.buscarTodos();
            fornecedores.clear();
            fornecedores.addAll(listaFornecedores);
        } catch (SQLException e) {
            System.err.println("Erro ao carregar fornecedores: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void atualizarTotal() {
        try {
            int total = produtoDAO.contarTotal();
            lblTotal.setText("Total: " + total + " produto(s)");
        } catch (SQLException e) {
            lblTotal.setText("Total: Erro ao contar");
        }
    }

    private void filtrarProdutos(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            carregarProdutos();
        } else {
            try {
                List<Produto> listaFiltrada = produtoDAO.buscarPorDescricao(filtro.trim());
                produtos.clear();
                produtos.addAll(listaFiltrada);
            } catch (SQLException e) {
                mostrarNotificacao("Erro", "Erro ao filtrar produtos", "erro");
            }
        }
    }

    @FXML
    void handleNovo() {
        modoEdicao = false;
        produtoSelecionado = null;
        limparFormulario();
        lblFormTitle.setText("Novo Produto");
        mostrarFormulario(true);
    }

    @FXML
    void handleEditar() {
        produtoSelecionado = tableProdutos.getSelectionModel().getSelectedItem();
        if (produtoSelecionado == null) {
            mostrarNotificacao("Atenção", "Selecione um produto para editar", "aviso");
            return;
        }
        modoEdicao = true;
        preencherFormulario(produtoSelecionado);
        lblFormTitle.setText("Editar Produto");
        mostrarFormulario(true);
    }

    @FXML
    void handleExcluir() {
        produtoSelecionado = tableProdutos.getSelectionModel().getSelectedItem();
        if (produtoSelecionado == null) {
            mostrarNotificacao("Atenção", "Selecione um produto para excluir", "aviso");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar exclusão");
        confirmacao.setHeaderText("Deseja realmente excluir este produto?");
        confirmacao.setContentText("Produto: " + produtoSelecionado.getDescricao());

        if (confirmacao.showAndWait().get() == ButtonType.OK) {
            try {
                if (produtoDAO.excluir(produtoSelecionado.getIdProduto())) {
                    carregarProdutos();
                    atualizarTotal();
                    mostrarNotificacao("Sucesso", "Produto excluído com sucesso!", "sucesso");
                } else {
                    mostrarNotificacao("Erro", "Não foi possível excluir o produto", "erro");
                }
            } catch (SQLException e) {
                mostrarNotificacao("Erro", "Erro ao excluir produto: " + e.getMessage(), "erro");
            }
        }
    }

    @FXML
    void handleSalvar() {
        List<String> erros = validarFormulario();
        
        if (!erros.isEmpty()) {
            mostrarListaErros(erros);
            return;
        }

        try {
            Produto produto = criarProdutoDoFormulario();
            boolean sucesso = modoEdicao ? produtoDAO.atualizar(produto) : produtoDAO.inserir(produto);

            if (sucesso) {
                carregarProdutos();
                atualizarTotal();
                mostrarFormulario(false);
                mostrarNotificacao("Sucesso", modoEdicao ? "Produto atualizado!" : "Produto cadastrado!", "sucesso");
            } else {
                mostrarNotificacao("Erro", "Não foi possível salvar o produto", "erro");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao salvar produto: " + e.getMessage());
            e.printStackTrace();
            mostrarNotificacao("Erro", "Erro ao salvar produto: " + e.getMessage(), "erro");
        } catch (Exception e) {
            System.err.println("Erro inesperado: " + e.getMessage());
            e.printStackTrace();
            mostrarNotificacao("Erro", "Erro inesperado: " + e.getMessage(), "erro");
        }
    }

    @FXML
    void handleCancelar() {
        mostrarFormulario(false);
    }

    private void mostrarFormulario(boolean mostrar) {
        formContainer.setVisible(mostrar);
        formContainer.setManaged(mostrar);
        tableProdutos.setVisible(!mostrar);
        tableProdutos.setManaged(!mostrar);
        if (!mostrar) limparEstilos();
    }

    private void limparFormulario() {
        txtCodigo.clear();
        txtNome.clear();
        cbCategoria.setValue("UN");
        txtPreco.clear();
        txtEstoque.setText("0");
        cbFornecedor.setValue(null);
        limparEstilos();
    }

    private void preencherFormulario(Produto produto) {
        txtCodigo.setText(produto.getCodigoBarras() != null ? produto.getCodigoBarras() : "");
        txtNome.setText(produto.getDescricao());
        cbCategoria.setValue(produto.getUnidadeMedida() != null ? produto.getUnidadeMedida() : "UN");
        txtPreco.setText(produto.getPrecoVenda() != null ? produto.getPrecoVenda().toString().replace(".", ",") : "");
        txtEstoque.setText(String.valueOf(produto.getEstoqueAtual()));

        if (produto.getIdFornecedor() != null) {
            Fornecedor fornecedor = fornecedores.stream()
                .filter(f -> f.getIdFornecedor() == produto.getIdFornecedor())
                .findFirst().orElse(null);
            cbFornecedor.setValue(fornecedor);
        } else {
            cbFornecedor.setValue(null);
        }

    }

    private Produto criarProdutoDoFormulario() {
        Produto produto = modoEdicao ? produtoSelecionado : new Produto();

        produto.setDescricao(txtNome.getText().trim());
        
        String codigo = txtCodigo.getText().trim();
        produto.setCodigoBarras(codigo.isEmpty() ? null : codigo);
        
        String unidade = cbCategoria.getValue();
        produto.setUnidadeMedida(unidade != null ? unidade : "UN");
        
        String precoStr = txtPreco.getText().trim().replace(",", ".");
        BigDecimal preco = new BigDecimal(precoStr);
        produto.setPrecoCusto(preco);
        produto.setPrecoVenda(preco);
        
        String estoqueStr = txtEstoque.getText().trim();
        produto.setEstoqueAtual(estoqueStr.isEmpty() ? 0 : Integer.parseInt(estoqueStr));

        Fornecedor fornecedorSelecionado = cbFornecedor.getValue();
        produto.setIdFornecedor(fornecedorSelecionado != null ? fornecedorSelecionado.getIdFornecedor() : null);

        return produto;
    }

    private List<String> validarFormulario() {
        List<String> erros = new ArrayList<>();

        if (txtNome.getText().trim().isEmpty()) {
            erros.add("Nome do produto é obrigatório");
        }

        if (txtPreco.getText().trim().isEmpty()) {
            erros.add("Preço é obrigatório");
        } else {
            try {
                BigDecimal preco = new BigDecimal(txtPreco.getText().replace(",", "."));
                if (preco.compareTo(BigDecimal.ZERO) <= 0) {
                    erros.add("Preço deve ser maior que zero");
                }
            } catch (NumberFormatException e) {
                erros.add("Preço inválido");
            }
        }

        if (txtEstoque.getText().trim().isEmpty()) {
            erros.add("Estoque é obrigatório");
        } else {
            try {
                int estoque = Integer.parseInt(txtEstoque.getText());
                if (estoque < 0) {
                    erros.add("Estoque não pode ser negativo");
                }
            } catch (NumberFormatException e) {
                erros.add("Estoque inválido");
            }
        }

        // Código de barras é OPCIONAL, mas se preenchido, verificar duplicidade
        if (!txtCodigo.getText().trim().isEmpty()) {
            try {
                Integer idExcluir = modoEdicao ? produtoSelecionado.getIdProduto() : null;
                if (produtoDAO.codigoBarrasExiste(txtCodigo.getText().trim(), idExcluir)) {
                    erros.add("Código de barras já cadastrado");
                }
            } catch (SQLException e) {
                erros.add("Erro ao verificar código de barras");
            }
        }

        return erros;
    }
}