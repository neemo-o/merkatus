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
import javafx.stage.Popup;
import javafx.util.Duration;
import main.database.ClienteDAO;
import main.database.ItemVendaDAO;
import main.database.ProdutoDAO;
import main.database.VendasDAO;
import main.models.Cliente;
import main.models.ItemVenda;
import main.models.Produto;
import main.models.Vendas;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VendasController {

    @FXML
    private TextField txtCodigoBarras;

    @FXML
    private TextField txtQuantidade;

    @FXML
    private Button btnAdicionar;

    @FXML
    private TableView<ItemVenda> tableItens;

    @FXML
    private TableColumn<ItemVenda, String> colCodigo;

    @FXML
    private TableColumn<ItemVenda, String> colDescricao;

    @FXML
    private TableColumn<ItemVenda, Integer> colQuantidade;

    @FXML
    private TableColumn<ItemVenda, BigDecimal> colPrecoUnit;

    @FXML
    private TableColumn<ItemVenda, BigDecimal> colTotal;

    @FXML
    private Button btnRemoverItem;

    @FXML
    private Button btnLimparVenda;

    @FXML
    private Label lblSubtotal;

    @FXML
    private Label lblDesconto;

    @FXML
    private Label lblTotal;

    @FXML
    private ComboBox<String> cbFormaPagamento;

    @FXML
    private ComboBox<Cliente> cbCliente;

    @FXML
    private Button btnFinalizarVenda;

    @FXML
    private Button btnCancelarVenda;

    @FXML
    private Label lblVendaAtual;

    @FXML
    private Label lblOperador;

    @FXML
    private ListView<Produto> listSugestoesProdutos;

    private VendasDAO vendasDAO;
    private ItemVendaDAO itemVendaDAO;
    private ProdutoDAO produtoDAO;
    private ClienteDAO clienteDAO;
    private ObservableList<ItemVenda> itensVenda;
    private ObservableList<Cliente> clientes;
    private ObservableList<String> formasPagamento;
    private ObservableList<Produto> sugestoesProdutos;
    private Vendas vendaAtual;
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal desconto = BigDecimal.ZERO;
    
    private PauseTransition searchDebounce;
    private List<Produto> todosOsProdutos;

    @FXML
    void initialize() {
        vendasDAO = new VendasDAO();
        itemVendaDAO = new ItemVendaDAO();
        produtoDAO = new ProdutoDAO();
        clienteDAO = new ClienteDAO();

        itensVenda = FXCollections.observableArrayList();
        clientes = FXCollections.observableArrayList();
        formasPagamento = FXCollections.observableArrayList();
        sugestoesProdutos = FXCollections.observableArrayList();
        todosOsProdutos = new ArrayList<>();

        configurarTableView();
        configurarComboBoxes();
        aplicarMascaras();
        aplicarValidacoes();
        configurarBuscaProdutos();

        carregarClientes();
        carregarTodosProdutos();
        configurarFormasPagamento();
        iniciarNovaVenda();

        configurarFocoCampo();
        atualizarTotais();

        lblOperador.setText("Operador: Sistema");
    }

    private void carregarTodosProdutos() {
        try {
            todosOsProdutos = produtoDAO.buscarTodos();
            System.out.println("✅ " + todosOsProdutos.size() + " produtos carregados!");
        } catch (SQLException e) {
            System.err.println("❌ Erro ao carregar produtos: " + e.getMessage());
            mostrarNotificacao("Erro", "Erro ao carregar produtos", "erro");
        }
    }

    private void configurarBuscaProdutos() {
        System.out.println("🔧 Configurando busca de produtos...");
        
        if (listSugestoesProdutos != null) {
            System.out.println("✅ ListView encontrado!");
            listSugestoesProdutos.setVisible(false);
            listSugestoesProdutos.setMaxHeight(200);
            
            listSugestoesProdutos.setCellFactory(param -> new ListCell<Produto>() {
                @Override
                protected void updateItem(Produto produto, boolean empty) {
                    super.updateItem(produto, empty);
                    if (empty || produto == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(String.format("%s - %s | Estoque: %d | R$ %.2f",
                            produto.getCodigoBarras(),
                            produto.getDescricao(),
                            produto.getEstoqueAtual(),
                            produto.getPrecoVenda()));
                        
                        setStyle("-fx-padding: 8; -fx-font-size: 11px;");
                    }
                }
            });

            listSugestoesProdutos.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    Produto produtoSelecionado = listSugestoesProdutos.getSelectionModel().getSelectedItem();
                    if (produtoSelecionado != null) {
                        selecionarProduto(produtoSelecionado);
                    }
                }
            });

            listSugestoesProdutos.setOnKeyPressed(event -> {
                switch (event.getCode()) {
                    case ENTER:
                        Produto produtoSelecionado = listSugestoesProdutos.getSelectionModel().getSelectedItem();
                        if (produtoSelecionado != null) {
                            selecionarProduto(produtoSelecionado);
                        }
                        break;
                    case ESCAPE:
                        listSugestoesProdutos.setVisible(false);
                        txtCodigoBarras.requestFocus();
                        break;
                }
            });
        } else {
            System.err.println("❌ ListView NÃO encontrado no FXML!");
        }

        searchDebounce = new PauseTransition(Duration.millis(200));
        searchDebounce.setOnFinished(event -> buscarProdutosPreditivo());

        txtCodigoBarras.textProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("📝 Digitado: '" + newVal + "'");
            
            if (newVal != null && newVal.trim().length() >= 1) {
                System.out.println("🔍 Iniciando busca...");
                searchDebounce.playFromStart();
            } else {
                if (listSugestoesProdutos != null) {
                    listSugestoesProdutos.setVisible(false);
                }
                sugestoesProdutos.clear();
            }
        });

        txtCodigoBarras.setOnKeyPressed(event -> {
            if (listSugestoesProdutos != null && listSugestoesProdutos.isVisible()) {
                switch (event.getCode()) {
                    case DOWN:
                        event.consume();
                        listSugestoesProdutos.requestFocus();
                        listSugestoesProdutos.getSelectionModel().selectFirst();
                        break;
                    case ESCAPE:
                        event.consume();
                        listSugestoesProdutos.setVisible(false);
                        break;
                }
            }
        });
    }

    private void buscarProdutosPreditivo() {
        String termo = txtCodigoBarras.getText().trim().toLowerCase();
        
        System.out.println("🔎 Buscando por: '" + termo + "'");
        
        if (termo.isEmpty()) {
            if (listSugestoesProdutos != null) {
                listSugestoesProdutos.setVisible(false);
            }
            return;
        }

        try {
            List<Produto> resultados = todosOsProdutos.stream()
                .filter(p -> {
                    String codigo = p.getCodigoBarras() != null ? p.getCodigoBarras().toLowerCase() : "";
                    String descricao = p.getDescricao() != null ? p.getDescricao().toLowerCase() : "";
                    
                    return codigo.contains(termo) || descricao.contains(termo);
                })
                .limit(15)
                .collect(Collectors.toList());

            System.out.println("📊 Encontrados " + resultados.size() + " produtos");

            sugestoesProdutos.clear();
            
            if (!resultados.isEmpty()) {
                sugestoesProdutos.addAll(resultados);
                
                if (listSugestoesProdutos != null) {
                    listSugestoesProdutos.setItems(sugestoesProdutos);
                    listSugestoesProdutos.setVisible(true);
                    System.out.println("✅ Lista exibida!");
                }
            } else {
                if (listSugestoesProdutos != null) {
                    listSugestoesProdutos.setVisible(false);
                }
                System.out.println("⚠️ Nenhum produto encontrado");
            }

        } catch (Exception e) {
            System.err.println("❌ Erro na busca: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void selecionarProduto(Produto produto) {
        System.out.println("✅ Produto selecionado: " + produto.getDescricao());
        txtCodigoBarras.setText(produto.getCodigoBarras());
        if (listSugestoesProdutos != null) {
            listSugestoesProdutos.setVisible(false);
        }
        txtQuantidade.requestFocus();
        txtQuantidade.selectAll();
    }

    private void configurarTableView() {
        colCodigo.setCellValueFactory(cellData -> {
            ItemVenda item = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                item.getProduto() != null ? item.getProduto().getCodigoBarras() : "");
        });

        colDescricao.setCellValueFactory(cellData -> {
            ItemVenda item = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                item.getProduto() != null ? item.getProduto().getDescricao() : "");
        });

        colQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        colPrecoUnit.setCellValueFactory(new PropertyValueFactory<>("precoUnitario"));

        colTotal.setCellValueFactory(cellData -> {
            ItemVenda item = cellData.getValue();
            BigDecimal subtotal = item.getSubtotal();
            return new javafx.beans.property.SimpleObjectProperty<>(subtotal);
        });

        colPrecoUnit.setCellFactory(column -> new TableCell<ItemVenda, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("R$ %.2f", item));
            }
        });

        colTotal.setCellFactory(column -> new TableCell<ItemVenda, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("R$ %.2f", item));
            }
        });

        tableItens.setItems(itensVenda);
        
        tableItens.setOnKeyPressed(event -> {
            ItemVenda itemSelecionado = tableItens.getSelectionModel().getSelectedItem();
            if (itemSelecionado != null) {
                switch (event.getCode()) {
                    case DELETE:
                        handleRemoverItem();
                        break;
                    case ADD:
                    case PLUS:
                        itemSelecionado.setQuantidade(itemSelecionado.getQuantidade() + 1);
                        tableItens.refresh();
                        atualizarTotais();
                        break;
                    case SUBTRACT:
                    case MINUS:
                        if (itemSelecionado.getQuantidade() > 1) {
                            itemSelecionado.setQuantidade(itemSelecionado.getQuantidade() - 1);
                            tableItens.refresh();
                            atualizarTotais();
                        }
                        break;
                }
            }
        });
    }

    private void configurarComboBoxes() {
        formasPagamento.addAll("PIX", "CARTAO", "DINHEIRO");
        cbFormaPagamento.setItems(formasPagamento);
        cbFormaPagamento.setValue("DINHEIRO");

        cbCliente.setItems(clientes);

        cbCliente.setCellFactory(param -> new ListCell<Cliente>() {
            @Override
            protected void updateItem(Cliente item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Sem cliente" : item.getRazaoSocial());
            }
        });

        cbCliente.setButtonCell(new ListCell<Cliente>() {
            @Override
            protected void updateItem(Cliente item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Sem cliente" : item.getRazaoSocial());
            }
        });
    }

    private void aplicarMascaras() {
        txtQuantidade.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("\\d*")) {
                txtQuantidade.setText(newVal.replaceAll("[^\\d]", ""));
            }
            if (!newVal.isEmpty()) {
                try {
                    int quantidade = Integer.parseInt(newVal);
                    if (quantidade <= 0) {
                        txtQuantidade.setText(oldVal);
                    }
                } catch (NumberFormatException e) {
                    txtQuantidade.setText(oldVal);
                }
            }
        });
    }

    private void aplicarValidacoes() {
        validarCampoObrigatorio(txtQuantidade);
        validarCampoObrigatorio(txtCodigoBarras);
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

    private void mostrarNotificacao(String titulo, String mensagem, String tipo) {
        if (txtCodigoBarras.getScene() == null || txtCodigoBarras.getScene().getWindow() == null) return;

        Popup popup = new Popup();
        String cor = tipo.equals("sucesso") ? "#7cb342" : tipo.equals("erro") ? "#e57373" : "#ffb74d";
        String icone = tipo.equals("sucesso") ? "✓" : tipo.equals("erro") ? "✗" : "⚠";

        VBox container = new VBox(5);
        container.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 5;" +
            "-fx-border-color: " + cor + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 5;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);"
        );
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
        popup.show(txtCodigoBarras.getScene().getWindow(),
            txtCodigoBarras.getScene().getWindow().getX() + txtCodigoBarras.getScene().getWindow().getWidth() - 380,
            txtCodigoBarras.getScene().getWindow().getY() + 60
        );

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), container);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        PauseTransition pause = new PauseTransition(Duration.millis(3000));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), container);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> popup.hide());

        SequentialTransition sequence = new SequentialTransition(fadeIn, pause, fadeOut);
        sequence.play();
    }

    private void configurarFocoCampo() {
        txtCodigoBarras.setOnAction(event -> {
            if (!txtCodigoBarras.getText().trim().isEmpty()) {
                if (listSugestoesProdutos != null && 
                    listSugestoesProdutos.isVisible() && 
                    sugestoesProdutos.size() == 1) {
                    selecionarProduto(sugestoesProdutos.get(0));
                } else {
                    txtQuantidade.requestFocus();
                }
            }
        });

        txtQuantidade.setOnAction(event -> {
            btnAdicionar.fire();
        });
    }

    private void carregarClientes() {
        try {
            List<Cliente> listaClientes = clienteDAO.buscarTodos();
            clientes.clear();
            clientes.addAll(listaClientes);
        } catch (SQLException e) {
            mostrarNotificacao("Erro", "Erro ao carregar clientes", "erro");
        }
    }

    private void configurarFormasPagamento() {
        cbFormaPagamento.setItems(formasPagamento);
    }

    private void iniciarNovaVenda() {
        itensVenda.clear();
        vendaAtual = new Vendas();
        vendaAtual.setDataVenda(new Timestamp(System.currentTimeMillis()));
        subtotal = BigDecimal.ZERO;
        desconto = BigDecimal.ZERO;
        lblVendaAtual.setText("Nova Venda");
        limparCampos();
        atualizarTotais();
    }

    private void limparCampos() {
        txtCodigoBarras.clear();
        txtQuantidade.setText("1");
        cbCliente.setValue(null);
        cbFormaPagamento.setValue("DINHEIRO");

        if (listSugestoesProdutos != null) {
            listSugestoesProdutos.setVisible(false);
        }

        txtCodigoBarras.setStyle("-fx-background-color: white; -fx-border-color: #d0d0d0; -fx-border-width: 1; -fx-padding: 6;");
        txtQuantidade.setStyle("-fx-background-color: white; -fx-border-color: #d0d0d0; -fx-border-width: 1; -fx-padding: 6;");
    }

    private void atualizarTotais() {
        BigDecimal totalItens = itensVenda.stream()
            .map(ItemVenda::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        subtotal = totalItens;
        BigDecimal totalFinal = subtotal.subtract(desconto);

        lblSubtotal.setText(String.format("R$ %.2f", subtotal));
        lblDesconto.setText(String.format("R$ %.2f", desconto));
        lblTotal.setText(String.format("R$ %.2f", totalFinal));

        vendaAtual.setValorTotal(totalFinal);
    }

    private boolean validarAdicaoProduto() {
        String codigoBarras = txtCodigoBarras.getText().trim();
        String quantidadeStr = txtQuantidade.getText().trim();
        List<String> erros = new ArrayList<>();

        if (codigoBarras.isEmpty()) {
            erros.add("Código de barras é obrigatório");
        }

        if (quantidadeStr.isEmpty()) {
            erros.add("Quantidade é obrigatória");
        } else {
            try {
                int quantidade = Integer.parseInt(quantidadeStr);
                if (quantidade <= 0) {
                    erros.add("Quantidade deve ser maior que zero");
                } else if (quantidade > 1000) {
                    erros.add("Quantidade muito alta");
                }
            } catch (NumberFormatException e) {
                erros.add("Quantidade inválida");
            }
        }

        if (!erros.isEmpty()) {
            mostrarNotificacao("Corrija os erros", String.join("\n", erros), "erro");
            return false;
        }

        return true;
    }

    @FXML
    void handleAdicionar() {
        if (!validarAdicaoProduto()) {
            return;
        }

        try {
            Produto produto = produtoDAO.buscarPorCodigoBarras(txtCodigoBarras.getText().trim());

            if (produto == null) {
                mostrarNotificacao("Erro", "Produto não encontrado", "erro");
                txtCodigoBarras.requestFocus();
                return;
            }

            int quantidade = Integer.parseInt(txtQuantidade.getText().trim());

            if (produto.getEstoqueAtual() < quantidade) {
                mostrarNotificacao("Erro", "Estoque insuficiente. Disponível: " + produto.getEstoqueAtual(), "erro");
                txtQuantidade.requestFocus();
                return;
            }

            ItemVenda itemExistente = itensVenda.stream()
                .filter(item -> item.getIdProduto() == produto.getIdProduto())
                .findFirst()
                .orElse(null);

            if (itemExistente != null) {
                int novaQuantidade = itemExistente.getQuantidade() + quantidade;
                if (produto.getEstoqueAtual() < novaQuantidade) {
                    mostrarNotificacao("Erro", "Estoque insuficiente para esta quantidade total", "erro");
                    return;
                }
                itemExistente.setQuantidade(novaQuantidade);
                tableItens.refresh();
            } else {
                ItemVenda novoItem = new ItemVenda();
                novoItem.setIdProduto(produto.getIdProduto());
                novoItem.setProduto(produto);
                novoItem.setQuantidade(quantidade);
                novoItem.setPrecoUnitario(produto.getPrecoVenda());
                novoItem.setDataCadastro(new Timestamp(System.currentTimeMillis()));
                itensVenda.add(novoItem);
            }

            mostrarNotificacao("Sucesso", "Produto adicionado à venda", "sucesso");
            limparCampos();
            atualizarTotais();
            txtCodigoBarras.requestFocus();

        } catch (SQLException e) {
            mostrarNotificacao("Erro", "Erro ao buscar produto", "erro");
        }
    }

    @FXML
    void handleRemoverItem() {
        ItemVenda itemSelecionado = tableItens.getSelectionModel().getSelectedItem();
        if (itemSelecionado == null) {
            mostrarNotificacao("Atenção", "Selecione um item para remover", "aviso");
            return;
        }

        itensVenda.remove(itemSelecionado);
        atualizarTotais();
        mostrarNotificacao("Sucesso", "Item removido da venda", "sucesso");
    }

    @FXML
    void handleLimparVenda() {
        if (!itensVenda.isEmpty()) {
            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacao.setTitle("Confirmar limpeza");
            confirmacao.setHeaderText("Deseja limpar toda a venda?");
            confirmacao.setContentText("Todos os itens serão removidos.");

            if (confirmacao.showAndWait().get() == ButtonType.OK) {
                itensVenda.clear();
                atualizarTotais();
                mostrarNotificacao("Sucesso", "Venda limpa", "sucesso");
            }
        }
    }

    @FXML
    void handleFinalizarVenda() {
        System.out.println("=== INICIANDO FINALIZAÇÃO DA VENDA ===");
        
        // Validação 1: Verificar se há itens
        if (itensVenda.isEmpty()) {
            System.out.println("❌ Erro: Nenhum item na venda");
            mostrarNotificacao("Atenção", "Adicione pelo menos um produto à venda", "aviso");
            return;
        }

        // Validação 2: Verificar forma de pagamento
        if (cbFormaPagamento.getValue() == null || cbFormaPagamento.getValue().trim().isEmpty()) {
            System.out.println("❌ Erro: Forma de pagamento não selecionada");
            mostrarNotificacao("Atenção", "Selecione a forma de pagamento", "aviso");
            cbFormaPagamento.requestFocus();
            return;
        }

        try {
            System.out.println("→ Configurando dados da venda...");
            
            // Configurar dados da venda
            vendaAtual.setFormaPagamento(cbFormaPagamento.getValue());
            vendaAtual.setDataVenda(new Timestamp(System.currentTimeMillis()));
            
            System.out.println("→ Valor total da venda: R$ " + vendaAtual.getValorTotal());
            System.out.println("→ Forma de pagamento: " + vendaAtual.getFormaPagamento());
            System.out.println("→ Total de itens: " + itensVenda.size());
            
            // Salvar venda no banco
            System.out.println("→ Salvando venda no banco de dados...");
            int idVenda = vendasDAO.salvar(vendaAtual);

            if (idVenda <= 0) {
                System.err.println("❌ Erro: ID da venda retornou 0 ou negativo");
                mostrarNotificacao("Erro", "Erro ao salvar venda no banco de dados", "erro");
                return;
            }

            System.out.println("✅ Venda salva com sucesso! ID: " + idVenda);
            vendaAtual.setIdVenda(idVenda);

            // Salvar itens da venda
            System.out.println("→ Salvando itens da venda...");
            int itemCount = 0;
            for (ItemVenda item : itensVenda) {
                item.setIdVenda(idVenda);
                
                System.out.println("  → Item " + (++itemCount) + ": " + item.getProduto().getDescricao() + 
                                 " (Qtd: " + item.getQuantidade() + ", Preço: R$ " + item.getPrecoUnitario() + ")");
                
                int idItem = itemVendaDAO.salvar(item);
                if (idItem <= 0) {
                    System.err.println("  ❌ Erro ao salvar item da venda");
                }

                // Atualizar estoque do produto
                Produto produto = item.getProduto();
                if (produto != null) {
                    int novoEstoque = produto.getEstoqueAtual() - item.getQuantidade();
                    System.out.println("  → Atualizando estoque: " + produto.getEstoqueAtual() + " → " + novoEstoque);
                    produto.setEstoqueAtual(novoEstoque);
                    boolean estoqueAtualizado = produtoDAO.atualizar(produto);
                    
                    if (!estoqueAtualizado) {
                        System.err.println("  ⚠️ Aviso: Estoque não foi atualizado para o produto ID " + produto.getIdProduto());
                    }
                }
            }

            System.out.println("✅ Todos os itens salvos com sucesso!");
            System.out.println("✅ VENDA FINALIZADA COM SUCESSO! ID: " + idVenda);
            
            mostrarNotificacao("Sucesso", "Venda #" + idVenda + " finalizada com sucesso!", "sucesso");
            lblVendaAtual.setText("Venda #" + idVenda);
            
            // Recarregar produtos após venda (estoque atualizado)
            carregarTodosProdutos();
            iniciarNovaVenda();

        } catch (SQLException e) {
            System.err.println("❌ ERRO AO FINALIZAR VENDA:");
            System.err.println("   Mensagem: " + e.getMessage());
            System.err.println("   Código SQL: " + e.getErrorCode());
            e.printStackTrace();
            
            mostrarNotificacao("Erro", "Erro ao finalizar venda: " + e.getMessage(), "erro");
        } catch (Exception e) {
            System.err.println("❌ ERRO INESPERADO:");
            e.printStackTrace();
            mostrarNotificacao("Erro", "Erro inesperado: " + e.getMessage(), "erro");
        }
    }

    @FXML
    void handleCancelarVenda() {
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Cancelar venda");
        confirmacao.setHeaderText("Deseja cancelar a venda atual?");
        confirmacao.setContentText("Todos os itens serão perdidos.");

        if (confirmacao.showAndWait().get() == ButtonType.OK) {
            iniciarNovaVenda();
            mostrarNotificacao("Cancelado", "Venda cancelada", "aviso");
        }
    }
}