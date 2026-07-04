package main.Modal;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
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

import main.database.DAOs.ClienteDAO;
import main.database.DAOs.FormaPagamentoDAO;
import main.database.DAOs.ProdutoDAO;
import main.models.Cliente;
import main.models.FormaPagamento;
import main.models.ItemVenda;
import main.models.Produto;
import main.models.Venda;
import main.services.VendaService;

public class VendaFormModal {

    private static final String AZUL     = "#194e8f";
    private static final String VERMELHO = "#c0392b";
    private static final String ESTILO_LABEL = "-fx-font-size: 11; -fx-font-family: 'Segoe UI'; -fx-text-fill: #333333;";
    private static final String ESTILO_CAMPO = "-fx-font-size: 11; -fx-font-family: 'Segoe UI'; -fx-background-color: white; -fx-border-color: #cccccc; -fx-border-width: 1;";
    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    private final ProdutoDAO produtoDAO;
    private final FormaPagamentoDAO formaPagamentoDAO;
    private final ClienteDAO clienteDAO;
    private final VendaService vendaService;

    private Stage stage;
    private double xOff, yOff;

    private TextField txtBuscaProduto;
    private Spinner<Integer> spQuantidade;
    private TableView<ItemVenda> tabelaItens;
    private ObservableList<ItemVenda> itens = FXCollections.observableArrayList();
    private ComboBox<Cliente> cbCliente;
    private ComboBox<FormaPagamento> cbFormaPagamento;
    private TextField txtDesconto, txtCpfNota, txtValorRecebido;
    private Label lblSubtotal, lblTotal, lblTroco;
    private Button btnRemover;

    public VendaFormModal(Stage owner, ProdutoDAO produtoDAO, FormaPagamentoDAO formaPagamentoDAO,
                          ClienteDAO clienteDAO, VendaService vendaService) {
        this.produtoDAO = produtoDAO;
        this.formaPagamentoDAO = formaPagamentoDAO;
        this.clienteDAO = clienteDAO;
        this.vendaService = vendaService;

        stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Nova Venda");

        VBox root = new VBox(buildTopBar(), buildBarraBusca(), buildTabelaItens(),
                new Separator(), buildBarraPagamento(), buildRodape());
        stage.setScene(new Scene(root, 760, 560));
    }

    private HBox buildTopBar() {
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
        return topBar;
    }

    private HBox buildBarraBusca() {
        Label lblBusca = new Label("Produto:");
        lblBusca.setStyle(ESTILO_LABEL);

        txtBuscaProduto = new TextField();
        txtBuscaProduto.setPromptText("Nome ou código de barras (Enter adiciona)...");
        txtBuscaProduto.setStyle(ESTILO_CAMPO);
        txtBuscaProduto.setPrefHeight(24);
        txtBuscaProduto.setOnAction(e -> adicionarProduto());
        HBox.setHgrow(txtBuscaProduto, Priority.ALWAYS);

        Label lblQtd = new Label("Qtd:");
        lblQtd.setStyle(ESTILO_LABEL);

        spQuantidade = new Spinner<>(1, 9999, 1);
        spQuantidade.setEditable(true);
        spQuantidade.setPrefWidth(80);
        spQuantidade.setPrefHeight(24);

        Button btnAdicionar = new Button("Adicionar");
        btnAdicionar.setStyle(
            "-fx-background-color: " + AZUL + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 11;" +
            "-fx-font-family: 'Segoe UI';" +
            "-fx-cursor: hand;"
        );
        btnAdicionar.setPrefHeight(24);
        btnAdicionar.setOnAction(e -> adicionarProduto());

        btnRemover = new Button("Remover Item");
        btnRemover.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-color: " + VERMELHO + ";" +
            "-fx-text-fill: " + VERMELHO + ";" +
            "-fx-border-width: 1;" +
            "-fx-font-size: 11;" +
            "-fx-font-family: 'Segoe UI';" +
            "-fx-cursor: hand;"
        );
        btnRemover.setPrefHeight(24);
        btnRemover.setDisable(true);
        btnRemover.setOnAction(e -> removerItemSelecionado());

        HBox barraBusca = new HBox(10, lblBusca, txtBuscaProduto, lblQtd, spQuantidade, btnAdicionar, btnRemover);
        barraBusca.setAlignment(Pos.CENTER_LEFT);
        barraBusca.setPadding(new Insets(10, 12, 10, 12));
        barraBusca.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #d8d8d8; -fx-border-width: 0 0 1 0;");
        return barraBusca;
    }

    private TableView<ItemVenda> buildTabelaItens() {
        tabelaItens = new TableView<>(itens);
        tabelaItens.setStyle("-fx-background-color: white;");
        VBox.setVgrow(tabelaItens, Priority.ALWAYS);

        TableColumn<ItemVenda, String> colProduto = new TableColumn<>("Produto");
        colProduto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescricaoProduto()));
        colProduto.setPrefWidth(300);

        TableColumn<ItemVenda, Integer> colQtd = new TableColumn<>("Qtd");
        colQtd.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getQuantidade()));
        colQtd.setPrefWidth(70);

        TableColumn<ItemVenda, String> colPreco = new TableColumn<>("Preço Unit.");
        colPreco.setCellValueFactory(c -> new SimpleStringProperty(MOEDA.format(c.getValue().getPrecoUnitario())));
        colPreco.setPrefWidth(120);

        TableColumn<ItemVenda, String> colSubtotal = new TableColumn<>("Subtotal");
        colSubtotal.setCellValueFactory(c -> new SimpleStringProperty(MOEDA.format(c.getValue().getTotalItem())));
        colSubtotal.setPrefWidth(120);

        tabelaItens.getColumns().add(colProduto);
        tabelaItens.getColumns().add(colQtd);
        tabelaItens.getColumns().add(colPreco);
        tabelaItens.getColumns().add(colSubtotal);

        tabelaItens.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, val) -> btnRemover.setDisable(val == null));

        return tabelaItens;
    }

    private HBox buildBarraPagamento() {
        Label lblCliente = new Label("Cliente:");
        lblCliente.setStyle(ESTILO_LABEL);

        cbCliente = new ComboBox<>();
        cbCliente.setPrefHeight(24);
        cbCliente.setPrefWidth(180);
        cbCliente.setStyle(ESTILO_CAMPO);
        cbCliente.getItems().add(null); // null = Consumidor Final
        try {
            cbCliente.getItems().addAll(clienteDAO.findAll());
        } catch (Exception e) {
            exibirAlerta("Erro", "Erro ao carregar clientes: " + e.getMessage());
        }
        cbCliente.setValue(null);
        cbCliente.setButtonCell(criarCelulaCliente());
        cbCliente.setCellFactory(list -> criarCelulaCliente());

        Label lblForma = new Label("Pagamento:");
        lblForma.setStyle(ESTILO_LABEL);

        cbFormaPagamento = new ComboBox<>();
        cbFormaPagamento.setPrefHeight(24);
        cbFormaPagamento.setPrefWidth(150);
        cbFormaPagamento.setStyle(ESTILO_CAMPO);
        cbFormaPagamento.setPromptText("Selecione...");
        try {
            cbFormaPagamento.getItems().addAll(formaPagamentoDAO.findAll());
        } catch (Exception e) {
            exibirAlerta("Erro", "Erro ao carregar formas de pagamento: " + e.getMessage());
        }
        cbFormaPagamento.setOnAction(e -> atualizarCampoRecebido());

        Label lblDesconto = new Label("Desconto:");
        lblDesconto.setStyle(ESTILO_LABEL);

        txtDesconto = new TextField();
        txtDesconto.setPromptText("0,00");
        txtDesconto.setPrefWidth(80);
        txtDesconto.setPrefHeight(24);
        txtDesconto.setStyle(ESTILO_CAMPO);
        txtDesconto.textProperty().addListener((obs, old, val) -> atualizarTotais());

        Label lblCpf = new Label("CPF na nota:");
        lblCpf.setStyle(ESTILO_LABEL);

        txtCpfNota = new TextField();
        txtCpfNota.setPromptText("Opcional");
        txtCpfNota.setPrefWidth(120);
        txtCpfNota.setPrefHeight(24);
        txtCpfNota.setStyle(ESTILO_CAMPO);
        aplicarMascaraCpf(txtCpfNota);

        Label lblRecebido = new Label("Recebido:");
        lblRecebido.setStyle(ESTILO_LABEL);

        txtValorRecebido = new TextField();
        txtValorRecebido.setPromptText("0,00");
        txtValorRecebido.setPrefWidth(80);
        txtValorRecebido.setPrefHeight(24);
        txtValorRecebido.setStyle(ESTILO_CAMPO);
        txtValorRecebido.setDisable(true);
        txtValorRecebido.textProperty().addListener((obs, old, val) -> atualizarTotais());

        lblTroco = new Label("Troco: " + MOEDA.format(BigDecimal.ZERO));
        lblTroco.setStyle(ESTILO_LABEL);

        HBox barra = new HBox(8, lblCliente, cbCliente, lblForma, cbFormaPagamento, lblDesconto, txtDesconto,
                lblCpf, txtCpfNota, lblRecebido, txtValorRecebido, lblTroco);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(10, 12, 10, 12));
        barra.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #d8d8d8; -fx-border-width: 0 0 1 0;");
        return barra;
    }

    private HBox buildRodape() {
        Label lblSubtotalLabel = new Label("Subtotal:");
        lblSubtotalLabel.setStyle("-fx-font-size: 12; -fx-font-family: 'Segoe UI'; -fx-text-fill: #555555;");

        lblSubtotal = new Label(MOEDA.format(BigDecimal.ZERO));
        lblSubtotal.setStyle("-fx-font-size: 12; -fx-font-family: 'Segoe UI'; -fx-text-fill: #555555;");

        Label lblTotalLabel = new Label("Total:");
        lblTotalLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-font-family: 'Segoe UI'; -fx-text-fill: #333333;");

        lblTotal = new Label(MOEDA.format(BigDecimal.ZERO));
        lblTotal.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-font-family: 'Segoe UI'; -fx-text-fill: " + AZUL + ";");

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

        HBox rodape = new HBox(12, lblSubtotalLabel, lblSubtotal, lblTotalLabel, lblTotal,
                espacoRodape, btnFinalizar, btnCancelar);
        rodape.setAlignment(Pos.CENTER_LEFT);
        rodape.setPadding(new Insets(10, 12, 10, 12));
        rodape.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #d8d8d8; -fx-border-width: 1 0 0 0;");
        return rodape;
    }

    // ==============================
    // Itens
    // ==============================

    private void adicionarProduto() {
        String busca = txtBuscaProduto.getText().trim();
        if (busca.isEmpty()) return;

        int quantidade = spQuantidade.getValue() != null ? spQuantidade.getValue() : 1;

        List<Produto> encontrados;
        try {
            encontrados = produtoDAO.findByFiltros(busca, null, null);
        } catch (Exception e) {
            exibirAlerta("Erro", "Erro ao buscar produtos: " + e.getMessage());
            return;
        }

        // Código de barras exato tem prioridade sobre busca por nome
        Produto produto = encontrados.stream()
                .filter(p -> busca.equals(p.getCodigoBarras()))
                .findFirst()
                .orElse(null);

        if (produto == null) {
            if (encontrados.isEmpty()) {
                exibirAlerta("Produto não encontrado", "Nenhum produto ativo encontrado para '" + busca + "'.");
                return;
            }
            if (encontrados.size() == 1) {
                produto = encontrados.get(0);
            } else {
                produto = escolherProduto(encontrados);
                if (produto == null) return;
            }
        }

        if (produto.getPrecoVenda() == null) {
            exibirAlerta("Produto sem preço", "O produto '" + produto.getDescricao()
                    + "' não possui preço de venda cadastrado.");
            return;
        }

        final Integer idProduto = produto.getIdProduto();
        ItemVenda existente = itens.stream()
                .filter(i -> i.getIdProduto().equals(idProduto))
                .findFirst()
                .orElse(null);

        int quantidadeFinal = quantidade + (existente != null ? existente.getQuantidade() : 0);

        if (produto.isControlaEstoque() && produto.getEstoqueAtual() < quantidadeFinal) {
            exibirAlerta("Estoque insuficiente", "O produto '" + produto.getDescricao()
                    + "' possui apenas " + produto.getEstoqueAtual() + " em estoque.");
            return;
        }

        if (existente != null) {
            existente.setQuantidade(quantidadeFinal);
            // Atualiza também o preço unitário para o valor atual do produto,
            // mantendo precoUnitario * quantidade == totalItem consistente.
            existente.setPrecoUnitario(produto.getPrecoVenda());
            existente.setTotalItem(produto.getPrecoVenda().multiply(BigDecimal.valueOf(quantidadeFinal)));
            tabelaItens.refresh();
        } else {
            ItemVenda item = new ItemVenda();
            item.setIdProduto(produto.getIdProduto());
            item.setDescricaoProduto(produto.getDescricao());
            item.setQuantidade(quantidade);
            item.setPrecoUnitario(produto.getPrecoVenda());
            item.setDesconto(BigDecimal.ZERO);
            item.setAcrescimo(BigDecimal.ZERO);
            item.setTotalItem(produto.getPrecoVenda().multiply(BigDecimal.valueOf(quantidade)));
            itens.add(item);
        }

        txtBuscaProduto.clear();
        spQuantidade.getValueFactory().setValue(1);
        txtBuscaProduto.requestFocus();
        atualizarTotais();
    }

    private Produto escolherProduto(List<Produto> encontrados) {
        // Prefixa com o ID para garantir chave única mesmo quando dois produtos
        // têm a mesma descrição/preço e nenhum código de barras cadastrado.
        Map<String, Produto> opcoes = new LinkedHashMap<>();
        for (Produto p : encontrados) {
            opcoes.put("#" + p.getIdProduto() + " - " + p.getDescricao() + " — "
                    + (p.getPrecoVenda() != null ? MOEDA.format(p.getPrecoVenda()) : "sem preço")
                    + (p.getCodigoBarras() != null ? " (cód: " + p.getCodigoBarras() + ")" : ""), p);
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(opcoes.keySet().iterator().next(), opcoes.keySet());
        dialog.setTitle("Selecionar produto");
        dialog.setHeaderText("Mais de um produto encontrado. Selecione:");
        dialog.setContentText("Produto:");
        dialog.initOwner(stage);

        Optional<String> escolha = dialog.showAndWait();
        return escolha.map(opcoes::get).orElse(null);
    }

    private void removerItemSelecionado() {
        ItemVenda selecionado = tabelaItens.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            itens.remove(selecionado);
            atualizarTotais();
        }
    }

    // ==============================
    // Totais e pagamento
    // ==============================

    private BigDecimal calcularSubtotal() {
        return itens.stream()
                .map(ItemVenda::getTotalItem)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void atualizarCampoRecebido() {
        FormaPagamento forma = cbFormaPagamento.getValue();
        boolean permiteTroco = forma != null && Boolean.TRUE.equals(forma.getPermiteTroco());
        txtValorRecebido.setDisable(!permiteTroco);
        if (!permiteTroco) txtValorRecebido.clear();
        atualizarTotais();
    }

    private void atualizarTotais() {
        BigDecimal subtotal = calcularSubtotal();
        BigDecimal desconto = parseMoedaSilencioso(txtDesconto.getText());
        BigDecimal total = subtotal.subtract(desconto != null ? desconto : BigDecimal.ZERO);
        if (total.compareTo(BigDecimal.ZERO) < 0) total = BigDecimal.ZERO;

        lblSubtotal.setText(MOEDA.format(subtotal));
        lblTotal.setText(MOEDA.format(total));

        BigDecimal recebido = parseMoedaSilencioso(txtValorRecebido.getText());
        BigDecimal troco = recebido != null ? recebido.subtract(total) : BigDecimal.ZERO;
        if (troco.compareTo(BigDecimal.ZERO) < 0) troco = BigDecimal.ZERO;
        lblTroco.setText("Troco: " + MOEDA.format(troco));
    }

    private BigDecimal parseMoedaSilencioso(String texto) {
        if (texto == null || texto.trim().isEmpty()) return null;
        try {
            return new BigDecimal(texto.trim().replace(".", "").replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private javafx.scene.control.ListCell<Cliente> criarCelulaCliente() {
        return new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Cliente item, boolean empty) {
                super.updateItem(item, empty);
                // "empty" também é true quando o valor selecionado é null (Consumidor Final),
                // então o texto é decidido só pelo item, não pela flag empty.
                setText(item == null ? "Consumidor Final" : item.getRazaoSocial());
            }
        };
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

    // ==============================
    // Finalização
    // ==============================

    private void finalizar() {
        if (itens.isEmpty()) {
            exibirAlerta("Venda vazia", "Adicione ao menos um produto antes de finalizar.");
            return;
        }

        FormaPagamento forma = cbFormaPagamento.getValue();
        if (forma == null) {
            exibirAlerta("Forma de pagamento", "Selecione a forma de pagamento.");
            return;
        }

        String textoDesconto = txtDesconto.getText().trim();
        BigDecimal desconto = parseMoedaSilencioso(textoDesconto);
        if (!textoDesconto.isEmpty() && desconto == null) {
            exibirAlerta("Desconto inválido", "Informe o desconto no formato 0,00.");
            return;
        }

        BigDecimal valorRecebido = null;
        if (Boolean.TRUE.equals(forma.getPermiteTroco())) {
            String textoRecebido = txtValorRecebido.getText().trim();
            if (!textoRecebido.isEmpty()) {
                valorRecebido = parseMoedaSilencioso(textoRecebido);
                if (valorRecebido == null) {
                    exibirAlerta("Valor recebido inválido", "Informe o valor recebido no formato 0,00.");
                    return;
                }
            }
        }

        Cliente cliente = cbCliente.getValue();

        try {
            Venda venda = vendaService.finalizarVenda(new ArrayList<>(itens), forma,
                    desconto, txtCpfNota.getText().trim(), valorRecebido,
                    cliente != null ? cliente.getIdCliente() : null);

            String mensagem = "Venda nº " + venda.getIdVenda() + " finalizada!\n"
                    + "Total: " + MOEDA.format(venda.getValorTotal());
            if (venda.getTroco() != null && venda.getTroco().compareTo(BigDecimal.ZERO) > 0) {
                mensagem += "\nTroco: " + MOEDA.format(venda.getTroco());
            }
            exibirAlerta("Venda finalizada", mensagem);
            stage.close();

        } catch (Exception e) {
            exibirAlerta("Não foi possível finalizar", e.getMessage());
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

    public void show() {
        stage.showAndWait();
    }
}
