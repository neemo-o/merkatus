package main.Modal;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import main.database.DAOs.ClienteDAO;
import main.database.DAOs.FormaPagamentoDAO;
import main.database.DAOs.ItemVendaDAO;
import main.database.DAOs.ProdutoDAO;
import main.database.DAOs.VendaDAO;
import main.models.Cliente;
import main.models.ItemVenda;
import main.models.Usuario;
import main.models.Venda;
import main.models.VendaRelatorioDTO;
import main.services.FormatacaoService;
import main.services.VendaService;
import main.util.FXMLLoaderFactory;
import main.util.SessionManager;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public class VendaModal extends BaseModal<Venda> {

    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private Button btnNovaVenda;
    @FXML private Button btnVerDetalhes;
    @FXML private Button btnCancelarVenda;
    @FXML private ComboBox<String> cbPeriodo;
    @FXML private Label lblTotalDia;
    @FXML private Label lblNumVendas;
    @FXML private Label lblTicketMedio;

    private final VendaDAO vendaDAO;
    private final ItemVendaDAO itemVendaDAO;
    private final ClienteDAO clienteDAO;
    private final ProdutoDAO produtoDAO;
    private final FormaPagamentoDAO formaPagamentoDAO;
    private final VendaService vendaService;

    private Map<Integer, Integer> itensPorVenda = Map.of();
    private Map<Integer, String> nomesClientesPorId = Map.of();

    public VendaModal(Stage owner, VendaDAO vendaDAO, ItemVendaDAO itemVendaDAO,
                      ClienteDAO clienteDAO, ProdutoDAO produtoDAO,
                      FormaPagamentoDAO formaPagamentoDAO, VendaService vendaService,
                      FXMLLoaderFactory fxmlLoaderFactory) {
        super(owner, "Vendas", "/main/view/VendaModal.fxml",
              null, null, null, null, null, fxmlLoaderFactory);
        this.vendaDAO = vendaDAO;
        this.itemVendaDAO = itemVendaDAO;
        this.clienteDAO = clienteDAO;
        this.produtoDAO = produtoDAO;
        this.formaPagamentoDAO = formaPagamentoDAO;
        this.vendaService = vendaService;
    }

    @Override
    @FXML
    public void initialize() {
        cbPeriodo.getItems().addAll("Hoje", "Últimos 7 dias", "Últimos 30 dias", "Todos");
        cbPeriodo.setValue("Todos");
        cbPeriodo.setOnAction(e -> applyFilters());

        btnVerDetalhes.setDisable(true);
        btnCancelarVenda.setDisable(true);

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            btnVerDetalhes.setDisable(newVal == null);
            btnCancelarVenda.setDisable(newVal == null || !"FINALIZADA".equals(newVal.getStatus()));
        });

        super.initialize();
    }

    @Override
    protected List<Venda> fetchFromDatabase() {
        itensPorVenda = itemVendaDAO.contarPorVenda();

        Map<Integer, String> mapaClientes = new HashMap<>();
        for (Cliente c : clienteDAO.findAll()) {
            mapaClientes.put(c.getIdCliente(), c.getRazaoSocial());
        }
        nomesClientesPorId = mapaClientes;

        List<Venda> vendas = vendaDAO.findRecentes();
        atualizarCards(vendas);
        return vendas;
    }

    private String nomeCliente(Integer idCliente) {
        if (idCliente == null) return "Consumidor Final";
        return nomesClientesPorId.getOrDefault(idCliente, "Cliente #" + idCliente);
    }

    @Override
    protected void configureColumns(TableView<Venda> table) {
        TableColumn<Venda, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idVenda"));
        colId.setPrefWidth(60);

        TableColumn<Venda, LocalDateTime> colData = new TableColumn<>("Data");
        colData.setCellValueFactory(new PropertyValueFactory<>("dataVenda"));
        colData.setCellFactory(column -> new TableCell<Venda, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(DATA_HORA));
            }
        });
        colData.setPrefWidth(150);

        TableColumn<Venda, String> colCliente = new TableColumn<>("Cliente");
        colCliente.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                nomeCliente(cellData.getValue().getIdCliente())));
        colCliente.setPrefWidth(200);

        TableColumn<Venda, Integer> colItens = new TableColumn<>("Itens");
        colItens.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(
                itensPorVenda.getOrDefault(cellData.getValue().getIdVenda(), 0)));
        colItens.setPrefWidth(80);

        TableColumn<Venda, BigDecimal> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(new PropertyValueFactory<>("valorTotal"));
        colTotal.setCellFactory(FormatacaoService.cellFactoryMoeda());
        colTotal.setPrefWidth(120);

        TableColumn<Venda, String> colPagamento = new TableColumn<>("Pagamento");
        colPagamento.setCellValueFactory(new PropertyValueFactory<>("formaPagamento"));
        colPagamento.setPrefWidth(120);

        TableColumn<Venda, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setPrefWidth(100);

        table.getColumns().add(colId);
        table.getColumns().add(colData);
        table.getColumns().add(colCliente);
        table.getColumns().add(colItens);
        table.getColumns().add(colTotal);
        table.getColumns().add(colPagamento);
        table.getColumns().add(colStatus);
    }

    @Override
    protected boolean matchesSearch(Venda v, String query) {
        if (query.isEmpty()) return true;
        return String.valueOf(v.getIdVenda()).contains(query)
            || (v.getStatus() != null && v.getStatus().toLowerCase().contains(query))
            || (v.getFormaPagamento() != null && v.getFormaPagamento().toLowerCase().contains(query));
    }

    @Override
    protected boolean matchesFilters(Venda v) {
        String periodo = cbPeriodo.getValue();
        if (periodo == null || "Todos".equals(periodo) || v.getDataVenda() == null) return true;

        LocalDate data = v.getDataVenda().toLocalDate();
        LocalDate hoje = LocalDate.now();
        return switch (periodo) {
            case "Hoje"            -> data.equals(hoje);
            case "Últimos 7 dias"  -> !data.isBefore(hoje.minusDays(7));
            case "Últimos 30 dias" -> !data.isBefore(hoje.minusDays(30));
            default                -> true;
        };
    }

    @Override
    protected void resetFilters() {
        cbPeriodo.setValue("Todos");
    }

    private void atualizarCards(List<Venda> vendas) {
        LocalDate hoje = LocalDate.now();
        List<Venda> vendasHoje = vendas.stream()
                .filter(v -> "FINALIZADA".equals(v.getStatus()))
                .filter(v -> v.getDataVenda() != null && v.getDataVenda().toLocalDate().equals(hoje))
                .toList();

        BigDecimal totalDia = vendasHoje.stream()
                .map(Venda::getValorTotal)
                .filter(t -> t != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ticketMedio = vendasHoje.isEmpty()
                ? BigDecimal.ZERO
                : totalDia.divide(BigDecimal.valueOf(vendasHoje.size()), 2, RoundingMode.HALF_UP);

        lblTotalDia.setText(MOEDA.format(totalDia));
        lblNumVendas.setText(String.valueOf(vendasHoje.size()));
        lblTicketMedio.setText(MOEDA.format(ticketMedio));
    }

    @Override
    @FXML
    protected void abrirFormNovo() {
        new VendaFormModal(stage, produtoDAO, formaPagamentoDAO, clienteDAO, vendaService).show();
        loadData();
    }

    @Override
    @FXML
    protected void abrirFormEdicao() {
        Venda selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            exibirAlerta("Selecione uma venda para ver os detalhes.");
            return;
        }
        mostrarDetalhes(selected);
    }

    @Override
    @FXML
    protected void excluirSelecionado() {
        Venda selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            exibirAlerta("Selecione uma venda para cancelar.");
            return;
        }
        if (!"FINALIZADA".equals(selected.getStatus())) {
            exibirAlerta("Apenas vendas finalizadas podem ser canceladas.");
            return;
        }

        String motivo = pedirMotivoCancelamento(selected);
        if (motivo == null) return;

        try {
            vendaService.cancelarVenda(selected, motivo);
            exibirAlerta("Venda nº " + selected.getIdVenda() + " cancelada. O estoque dos itens foi devolvido.");
        } catch (Exception e) {
            exibirAlerta("Não foi possível cancelar a venda: " + e.getMessage());
        }
        loadData();
    }

    private void mostrarDetalhes(Venda venda) {
        Stage detalhes = new Stage();
        detalhes.initOwner(stage);
        detalhes.initModality(Modality.WINDOW_MODAL);
        detalhes.initStyle(StageStyle.UNDECORATED);

        HBox topBar = new HBox();
        topBar.setMinHeight(8);
        topBar.setStyle("-fx-background-color: #194e8f;");

        Label titulo = new Label("Venda nº " + venda.getIdVenda()
                + "  —  " + (venda.getDataVenda() != null ? venda.getDataVenda().format(DATA_HORA) : ""));
        titulo.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-font-family: 'Segoe UI'; -fx-text-fill: #194e8f;");

        TableView<ItemVenda> tabela = new TableView<>();
        tabela.setPrefHeight(260);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<ItemVenda, Integer> colSeq = new TableColumn<>("Seq");
        colSeq.setCellValueFactory(new PropertyValueFactory<>("sequencia"));
        colSeq.setPrefWidth(40);

        TableColumn<ItemVenda, String> colProduto = new TableColumn<>("Produto");
        colProduto.setCellValueFactory(new PropertyValueFactory<>("descricaoProduto"));
        colProduto.setPrefWidth(240);

        TableColumn<ItemVenda, Integer> colQtd = new TableColumn<>("Qtd");
        colQtd.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        colQtd.setPrefWidth(60);

        TableColumn<ItemVenda, BigDecimal> colPreco = new TableColumn<>("Preço Unit.");
        colPreco.setCellValueFactory(new PropertyValueFactory<>("precoUnitario"));
        colPreco.setCellFactory(FormatacaoService.cellFactoryMoeda());
        colPreco.setPrefWidth(100);

        TableColumn<ItemVenda, BigDecimal> colTotalItem = new TableColumn<>("Total");
        colTotalItem.setCellValueFactory(new PropertyValueFactory<>("totalItem"));
        colTotalItem.setCellFactory(FormatacaoService.cellFactoryMoeda());
        colTotalItem.setPrefWidth(100);

        tabela.getColumns().add(colSeq);
        tabela.getColumns().add(colProduto);
        tabela.getColumns().add(colQtd);
        tabela.getColumns().add(colPreco);
        tabela.getColumns().add(colTotalItem);

        tabela.getItems().addAll(itemVendaDAO.findByVenda(venda.getIdVenda()));

        String resumoTexto = "Subtotal: " + formatarMoeda(venda.getSubtotal())
                + "    Desconto: " + formatarMoeda(venda.getDesconto())
                + "    Total: " + formatarMoeda(venda.getValorTotal())
                + "    Troco: " + formatarMoeda(venda.getTroco());
        Label resumo = new Label(resumoTexto);
        resumo.setStyle("-fx-font-size: 11; -fx-font-family: 'Segoe UI'; -fx-text-fill: #333333;");

        Label pagamento = new Label("Cliente: " + nomeCliente(venda.getIdCliente())
                + "    Pagamento: "
                + (venda.getFormaPagamento() != null ? venda.getFormaPagamento() : "-")
                + "    Status: " + venda.getStatus()
                + (venda.getObservacao() != null && !venda.getObservacao().isBlank()
                        ? "\nObservação: " + venda.getObservacao() : ""));
        pagamento.setWrapText(true);
        pagamento.setStyle("-fx-font-size: 11; -fx-font-family: 'Segoe UI'; -fx-text-fill: #555555;");

        Button btnFechar = new Button("Fechar");
        btnFechar.setStyle("-fx-background-color: #194e8f; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-family: 'Segoe UI'; -fx-cursor: hand; -fx-padding: 8 20;");
        btnFechar.setOnAction(e -> detalhes.close());

        Region espacoRodape = new Region();
        HBox.setHgrow(espacoRodape, Priority.ALWAYS);
        HBox rodape = new HBox(espacoRodape, btnFechar);
        rodape.setAlignment(Pos.CENTER_RIGHT);

        VBox content = new VBox(12, topBar, titulo, tabela, resumo, pagamento, rodape);
        content.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-width: 1;");
        content.setPrefWidth(600);
        VBox.setMargin(titulo, new Insets(8, 16, 0, 16));
        VBox.setMargin(tabela, new Insets(0, 16, 0, 16));
        VBox.setMargin(resumo, new Insets(0, 16, 0, 16));
        VBox.setMargin(pagamento, new Insets(0, 16, 0, 16));
        VBox.setMargin(rodape, new Insets(0, 16, 16, 16));

        detalhes.setScene(new Scene(content));
        detalhes.sizeToScene();
        detalhes.showAndWait();
    }

    private String pedirMotivoCancelamento(Venda venda) {
        Stage confirmStage = new Stage();
        confirmStage.initOwner(stage);
        confirmStage.initModality(Modality.WINDOW_MODAL);
        confirmStage.initStyle(StageStyle.UNDECORATED);
        confirmStage.setResizable(false);

        HBox topBar = new HBox();
        topBar.setMinHeight(8);
        topBar.setStyle("-fx-background-color: #194e8f;");

        Label mensagem = new Label("Cancelar a venda nº " + venda.getIdVenda()
                + " (" + formatarMoeda(venda.getValorTotal()) + ")?\nInforme o motivo:");
        mensagem.setWrapText(true);
        mensagem.setStyle("-fx-font-size: 12; -fx-font-family: 'Segoe UI'; -fx-text-fill: #333333;");
        mensagem.setMaxWidth(340);

        TextField txtMotivo = new TextField();
        txtMotivo.setPromptText("Motivo do cancelamento...");
        txtMotivo.setStyle("-fx-font-size: 11; -fx-font-family: 'Segoe UI'; -fx-background-color: white; -fx-border-color: #cccccc; -fx-border-width: 1;");

        Button btnConfirmar = new Button("Confirmar cancelamento");
        btnConfirmar.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 20;");
        btnConfirmar.setDisable(true);
        txtMotivo.textProperty().addListener((obs, old, val) -> btnConfirmar.setDisable(val.isBlank()));

        Button btnVoltar = new Button("Voltar");
        btnVoltar.setStyle("-fx-background-color: transparent; -fx-border-color: #194e8f; -fx-text-fill: #194e8f; -fx-font-family: 'Segoe UI'; -fx-cursor: hand; -fx-padding: 8 20; -fx-border-width: 1;");

        HBox botoes = new HBox(10, btnVoltar, btnConfirmar);
        botoes.setAlignment(Pos.CENTER);

        VBox content = new VBox(12, topBar, mensagem, txtMotivo, botoes);
        content.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 0 0 16 0;");
        content.setPrefWidth(380);
        VBox.setMargin(mensagem, new Insets(16, 16, 0, 16));
        VBox.setMargin(txtMotivo, new Insets(0, 16, 0, 16));
        VBox.setMargin(botoes, new Insets(8, 16, 0, 16));

        confirmStage.setScene(new Scene(content));
        confirmStage.sizeToScene();

        final String[] motivo = {null};
        btnConfirmar.setOnAction(e -> {
            motivo[0] = txtMotivo.getText().trim();
            confirmStage.close();
        });
        btnVoltar.setOnAction(e -> confirmStage.close());

        confirmStage.showAndWait();
        return motivo[0];
    }

    // ==============================
    // Relatório em PDF (JasperReports)
    // ==============================

    @FXML
    protected void abrirPreview() {
        try {
            JasperPrint jasperPrint = gerarRelatorio();

            File tempPdf = File.createTempFile("vendas_preview", ".pdf");
            tempPdf.deleteOnExit();
            JasperExportManager.exportReportToPdfFile(jasperPrint, tempPdf.getAbsolutePath());

            // Abre no leitor padrão do Windows via ProcessBuilder
            new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler",
                    tempPdf.getAbsolutePath())
                    .start();

        } catch (Exception e) {
            exibirAlerta("Erro ao gerar o relatório: " + e.getMessage());
        }
    }

    @FXML
    protected void exportarPdf() {
        try {
            JasperPrint jasperPrint = gerarRelatorio();
            javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            fc.setTitle("Salvar PDF");
            fc.setInitialFileName("Relatorio de Vendas" + "-" + LocalDate.now().format(fmt) + ".pdf");
            fc.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter("PDF", "*.pdf"));
            File arquivo = fc.showSaveDialog(tableView.getScene().getWindow());
            if (arquivo != null)
                JasperExportManager.exportReportToPdfFile(
                        jasperPrint, arquivo.getAbsolutePath());
        } catch (Exception e) {
            exibirAlerta("Erro ao exportar o PDF: " + e.getMessage());
        }
    }

    private JasperPrint gerarRelatorio() throws JRException {
        // Usa as linhas exibidas na tabela: busca e filtro de período já aplicados
        List<VendaRelatorioDTO> dados = new ArrayList<>();
        for (Venda v : tableView.getItems()) {
            dados.add(new VendaRelatorioDTO(v, nomeCliente(v.getIdCliente()),
                    itensPorVenda.getOrDefault(v.getIdVenda(), 0)));
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Usuario usuario = SessionManager.getUsuarioAtual();

        Map<String, Object> parametros = new HashMap<>();
        parametros.put("EMPRESA_NOME", "Merkatus");
        parametros.put("PERIODO_INICIO", periodoInicio().format(fmt));
        parametros.put("PERIODO_FIM", LocalDate.now().format(fmt));
        parametros.put("USUARIO", usuario != null ? usuario.toString() : "Administrador");
        parametros.put(JRParameter.REPORT_LOCALE, new Locale("pt", "BR"));

        var stream = getClass().getResourceAsStream("/main/view/RelatorioVendas.jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(stream);
        return JasperFillManager.fillReport(jasperReport, parametros,
                new JRBeanCollectionDataSource(dados));
    }

    private LocalDate periodoInicio() {
        LocalDate hoje = LocalDate.now();
        String periodo = cbPeriodo.getValue();
        return switch (periodo == null ? "Todos" : periodo) {
            case "Hoje"            -> hoje;
            case "Últimos 7 dias"  -> hoje.minusDays(7);
            case "Últimos 30 dias" -> hoje.minusDays(30);
            // "Todos": usa a data da venda mais antiga em exibição
            default -> tableView.getItems().stream()
                    .map(Venda::getDataVenda)
                    .filter(d -> d != null)
                    .map(LocalDateTime::toLocalDate)
                    .min(LocalDate::compareTo)
                    .orElse(hoje);
        };
    }

    private String formatarMoeda(BigDecimal valor) {
        return MOEDA.format(valor != null ? valor : BigDecimal.ZERO);
    }

    private void exibirAlerta(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.initOwner(stage);
        alerta.showAndWait();
    }
}
