package main.Modal;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import main.database.DAOs.CaixaDAO;
import main.models.Caixa;
import main.services.CaixaService;
import main.services.FormatacaoService;
import main.util.FXMLLoaderFactory;

public class CaixaModal extends BaseModal<Caixa> {

    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private Label lblStatusCaixa;
    @FXML private Label lblValorAbertura;
    @FXML private Label lblSaldoEsperado;
    @FXML private Button btnAbrirCaixa;
    @FXML private Button btnSuprimento;
    @FXML private Button btnSangria;
    @FXML private Button btnFecharCaixa;

    private final CaixaDAO caixaDAO;
    private final CaixaService caixaService;

    private Caixa caixaAberto;

    public CaixaModal(Stage owner, CaixaDAO caixaDAO, CaixaService caixaService,
                      FXMLLoaderFactory fxmlLoaderFactory) {
        super(owner, "Caixa", "/main/view/CaixaModal.fxml", null, null, fxmlLoaderFactory);
        this.caixaDAO = caixaDAO;
        this.caixaService = caixaService;
    }

    @Override
    protected List<Caixa> fetchFromDatabase() {
        caixaAberto = caixaService.buscarCaixaAberto().orElse(null);
        atualizarCards();
        return caixaDAO.findRecentes();
    }

    private void atualizarCards() {
        boolean aberto = caixaAberto != null;

        String estiloBase = "-fx-font-size: 16; -fx-font-weight: bold; -fx-font-family: 'Segoe UI'; -fx-text-fill: ";
        if (aberto) {
            lblStatusCaixa.setText("ABERTO desde "
                    + (caixaAberto.getDataAbertura() != null
                            ? caixaAberto.getDataAbertura().format(DATA_HORA) : "-"));
            lblStatusCaixa.setStyle(estiloBase + "#1a6b2e;");
            lblValorAbertura.setText(formatarMoeda(caixaAberto.getValorAbertura()));
            lblSaldoEsperado.setText(formatarMoeda(caixaService.calcularValorSistema(caixaAberto)));
        } else {
            lblStatusCaixa.setText("FECHADO");
            lblStatusCaixa.setStyle(estiloBase + "#c0392b;");
            lblValorAbertura.setText("-");
            lblSaldoEsperado.setText("-");
        }

        btnAbrirCaixa.setDisable(aberto);
        btnSuprimento.setDisable(!aberto);
        btnSangria.setDisable(!aberto);
        btnFecharCaixa.setDisable(!aberto);
    }

    @Override
    protected void configureColumns(TableView<Caixa> table) {
        TableColumn<Caixa, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idCaixa"));
        colId.setPrefWidth(50);

        TableColumn<Caixa, LocalDateTime> colAbertura = new TableColumn<>("Abertura");
        colAbertura.setCellValueFactory(new PropertyValueFactory<>("dataAbertura"));
        aplicarFormatoData(colAbertura);
        colAbertura.setPrefWidth(140);

        TableColumn<Caixa, LocalDateTime> colFechamento = new TableColumn<>("Fechamento");
        colFechamento.setCellValueFactory(new PropertyValueFactory<>("dataFechamento"));
        aplicarFormatoData(colFechamento);
        colFechamento.setPrefWidth(140);

        TableColumn<Caixa, Integer> colOperador = new TableColumn<>("Operador");
        colOperador.setCellValueFactory(new PropertyValueFactory<>("idOperador"));
        colOperador.setPrefWidth(80);

        TableColumn<Caixa, BigDecimal> colAberturaValor = new TableColumn<>("Vl. Abertura");
        colAberturaValor.setCellValueFactory(new PropertyValueFactory<>("valorAbertura"));
        colAberturaValor.setCellFactory(FormatacaoService.cellFactoryMoeda());
        colAberturaValor.setPrefWidth(110);

        TableColumn<Caixa, BigDecimal> colSistema = new TableColumn<>("Vl. Sistema");
        colSistema.setCellValueFactory(new PropertyValueFactory<>("valorSistema"));
        colSistema.setCellFactory(FormatacaoService.cellFactoryMoeda());
        colSistema.setPrefWidth(110);

        TableColumn<Caixa, BigDecimal> colContado = new TableColumn<>("Vl. Contado");
        colContado.setCellValueFactory(new PropertyValueFactory<>("valorFechamento"));
        colContado.setCellFactory(FormatacaoService.cellFactoryMoeda());
        colContado.setPrefWidth(110);

        TableColumn<Caixa, BigDecimal> colDiferenca = new TableColumn<>("Diferença");
        colDiferenca.setCellValueFactory(new PropertyValueFactory<>("diferenca"));
        colDiferenca.setCellFactory(FormatacaoService.cellFactoryMoeda());
        colDiferenca.setPrefWidth(100);

        TableColumn<Caixa, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setPrefWidth(90);

        table.getColumns().add(colId);
        table.getColumns().add(colAbertura);
        table.getColumns().add(colFechamento);
        table.getColumns().add(colOperador);
        table.getColumns().add(colAberturaValor);
        table.getColumns().add(colSistema);
        table.getColumns().add(colContado);
        table.getColumns().add(colDiferenca);
        table.getColumns().add(colStatus);
    }

    @Override
    protected boolean matchesSearch(Caixa c, String query) {
        if (query.isEmpty()) return true;
        return String.valueOf(c.getIdCaixa()).contains(query)
            || (c.getStatus() != null && c.getStatus().toLowerCase().contains(query));
    }

    @Override
    protected boolean matchesFilters(Caixa c) {
        return true;
    }

    @Override
    protected void resetFilters() {
    }

    /** Abrir Caixa (botão "Abrir Caixa" no FXML chama abrirFormNovo). */
    @Override
    @FXML
    protected void abrirFormNovo() {
        if (caixaAberto != null) {
            exibirAlerta("Já existe um caixa aberto. Feche-o antes de abrir outro.");
            return;
        }

        Optional<ValorMotivo> resposta = pedirValorEMotivo("Abrir Caixa",
                "Valor inicial na gaveta (fundo de troco):", null);
        if (resposta.isEmpty()) return;

        try {
            Caixa caixa = caixaService.abrirCaixa(resposta.get().valor());
            exibirAlerta("Caixa nº " + caixa.getIdCaixa() + " aberto com "
                    + formatarMoeda(caixa.getValorAbertura()) + ".");
        } catch (Exception e) {
            exibirAlerta("Não foi possível abrir o caixa: " + e.getMessage());
        }
        loadData();
    }

    @FXML
    protected void registrarSuprimento() {
        if (caixaAberto == null) {
            exibirAlerta("Não há caixa aberto.");
            return;
        }
        Optional<ValorMotivo> resposta = pedirValorEMotivo("Suprimento (entrada de dinheiro)",
                "Valor do suprimento:", "Motivo (ex: reforço de troco):");
        if (resposta.isEmpty()) return;

        try {
            caixaService.registrarSuprimento(caixaAberto, resposta.get().valor(), resposta.get().motivo());
            exibirAlerta("Suprimento de " + formatarMoeda(resposta.get().valor()) + " registrado.");
        } catch (Exception e) {
            exibirAlerta("Não foi possível registrar o suprimento: " + e.getMessage());
        }
        loadData();
    }

    @FXML
    protected void registrarSangria() {
        if (caixaAberto == null) {
            exibirAlerta("Não há caixa aberto.");
            return;
        }
        Optional<ValorMotivo> resposta = pedirValorEMotivo("Sangria (retirada de dinheiro)",
                "Valor da sangria:", "Motivo (ex: depósito, pagamento):");
        if (resposta.isEmpty()) return;

        try {
            caixaService.registrarSangria(caixaAberto, resposta.get().valor(), resposta.get().motivo());
            exibirAlerta("Sangria de " + formatarMoeda(resposta.get().valor()) + " registrada.");
        } catch (Exception e) {
            exibirAlerta("Não foi possível registrar a sangria: " + e.getMessage());
        }
        loadData();
    }

    @FXML
    protected void fecharCaixa() {
        if (caixaAberto == null) {
            exibirAlerta("Não há caixa aberto.");
            return;
        }

        BigDecimal valorSistema = caixaService.calcularValorSistema(caixaAberto);
        Optional<ValorMotivo> resposta = pedirValorEMotivo("Fechar Caixa",
                "Valor esperado: " + formatarMoeda(valorSistema) + ". Valor contado na gaveta:",
                "Observação (opcional):");
        if (resposta.isEmpty()) return;

        try {
            Caixa fechado = caixaService.fecharCaixa(caixaAberto, resposta.get().valor(), resposta.get().motivo());
            exibirAlerta("Caixa nº " + fechado.getIdCaixa() + " fechado.\n"
                    + "Sistema: " + formatarMoeda(fechado.getValorSistema())
                    + "  |  Contado: " + formatarMoeda(fechado.getValorFechamento())
                    + "  |  Diferença: " + formatarMoeda(fechado.getDiferenca()));
        } catch (Exception e) {
            exibirAlerta("Não foi possível fechar o caixa: " + e.getMessage());
        }
        loadData();
    }

    /** Duplo clique na tabela mostra o resumo do caixa selecionado. */
    @Override
    @FXML
    protected void abrirFormEdicao() {
        Caixa selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        BigDecimal suprimentos = caixaDAO.totalSangriasSuprimentos(selected.getIdCaixa(), "U");
        BigDecimal sangrias = caixaDAO.totalSangriasSuprimentos(selected.getIdCaixa(), "S");
        BigDecimal dinheiroVendas = caixaDAO.totalDinheiroVendas(selected.getIdCaixa());

        exibirAlerta("Caixa nº " + selected.getIdCaixa() + " (" + selected.getStatus() + ")\n"
                + "Abertura: " + formatarMoeda(selected.getValorAbertura()) + "\n"
                + "Vendas em dinheiro: " + formatarMoeda(dinheiroVendas) + "\n"
                + "Suprimentos: " + formatarMoeda(suprimentos) + "\n"
                + "Sangrias: " + formatarMoeda(sangrias)
                + ("FECHADO".equals(selected.getStatus())
                        ? "\nContado: " + formatarMoeda(selected.getValorFechamento())
                          + "  |  Diferença: " + formatarMoeda(selected.getDiferenca())
                        : ""));
    }

    @Override
    @FXML
    protected void excluirSelecionado() {
        exibirAlerta("Registros de caixa não podem ser excluídos (histórico de auditoria).");
    }

    // Valor + motivo digitados nos diálogos de abrir/sangria/suprimento/fechar
    private record ValorMotivo(BigDecimal valor, String motivo) {}

    private Optional<ValorMotivo> pedirValorEMotivo(String titulo, String rotuloValor, String rotuloMotivo) {
        Stage dialog = new Stage();
        dialog.initOwner(stage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setResizable(false);

        HBox topBar = new HBox();
        topBar.setMinHeight(8);
        topBar.setStyle("-fx-background-color: #194e8f;");

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-font-family: 'Segoe UI'; -fx-text-fill: #194e8f;");

        Label lblValor = new Label(rotuloValor);
        lblValor.setWrapText(true);
        lblValor.setMaxWidth(340);
        lblValor.setStyle("-fx-font-size: 11; -fx-font-family: 'Segoe UI'; -fx-text-fill: #333333;");

        TextField txtValor = new TextField();
        txtValor.setPromptText("0,00");
        txtValor.setStyle("-fx-font-size: 11; -fx-font-family: 'Segoe UI'; -fx-background-color: white; -fx-border-color: #cccccc; -fx-border-width: 1;");

        TextField txtMotivo = new TextField();
        txtMotivo.setPromptText(rotuloMotivo != null ? rotuloMotivo : "");
        txtMotivo.setStyle(txtValor.getStyle());
        txtMotivo.setVisible(rotuloMotivo != null);
        txtMotivo.setManaged(rotuloMotivo != null);

        Button btnConfirmar = new Button("Confirmar");
        btnConfirmar.setStyle("-fx-background-color: #194e8f; -fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 20;");

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setStyle("-fx-background-color: transparent; -fx-border-color: #194e8f; -fx-text-fill: #194e8f; -fx-font-family: 'Segoe UI'; -fx-cursor: hand; -fx-padding: 8 20; -fx-border-width: 1;");

        HBox botoes = new HBox(10, btnCancelar, btnConfirmar);
        botoes.setAlignment(Pos.CENTER);

        VBox content = new VBox(12, topBar, lblTitulo, lblValor, txtValor, txtMotivo, botoes);
        content.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 0 0 16 0;");
        content.setPrefWidth(380);
        VBox.setMargin(lblTitulo, new Insets(8, 16, 0, 16));
        VBox.setMargin(lblValor, new Insets(0, 16, 0, 16));
        VBox.setMargin(txtValor, new Insets(0, 16, 0, 16));
        VBox.setMargin(txtMotivo, new Insets(0, 16, 0, 16));
        VBox.setMargin(botoes, new Insets(8, 16, 0, 16));

        dialog.setScene(new Scene(content));
        dialog.sizeToScene();

        final ValorMotivo[] resultado = {null};
        btnConfirmar.setOnAction(e -> {
            BigDecimal valor = parseMoeda(txtValor.getText());
            if (valor == null) {
                Alert alerta = new Alert(Alert.AlertType.WARNING);
                alerta.setHeaderText(null);
                alerta.setContentText("Informe o valor no formato 0,00.");
                alerta.initOwner(dialog);
                alerta.showAndWait();
                return;
            }
            resultado[0] = new ValorMotivo(valor, txtMotivo.getText().trim());
            dialog.close();
        });
        btnCancelar.setOnAction(e -> dialog.close());

        dialog.showAndWait();
        return Optional.ofNullable(resultado[0]);
    }

    private BigDecimal parseMoeda(String texto) {
        if (texto == null || texto.trim().isEmpty()) return null;
        try {
            return new BigDecimal(texto.trim().replace(".", "").replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String formatarMoeda(BigDecimal valor) {
        return MOEDA.format(valor != null ? valor : BigDecimal.ZERO);
    }

    private <T> void aplicarFormatoData(TableColumn<T, LocalDateTime> coluna) {
        coluna.setCellFactory(column -> new TableCell<T, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(DATA_HORA));
            }
        });
    }

    private void exibirAlerta(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.initOwner(stage);
        alerta.showAndWait();
    }
}
