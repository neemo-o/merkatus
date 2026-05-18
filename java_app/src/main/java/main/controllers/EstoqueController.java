package main.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import main.Modal.BaseModal;
import main.database.DAOs.ProdutoDAO;
import main.models.Produto;
import main.models.ProdutoRelatorioDTO;
import main.util.FXMLLoaderFactory;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;

import java.io.File;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class EstoqueController extends BaseModal<Produto> {

    @FXML private ComboBox<?> cbCategoria;
    @FXML private ComboBox<?> cbFornecedor;
    @FXML private DatePicker  dataInicio;
    @FXML private DatePicker  dataFim;

    public EstoqueController(Stage owner, ProdutoDAO produtoDAO,
                             FXMLLoaderFactory fxmlLoaderFactory) {
        super(owner, "Relatório de Estoque", "/main/view/Estoque.fxml",
              produtoDAO, null, null, null, fxmlLoaderFactory);
    }

    @Override
    protected List<Produto> fetchFromDatabase() {
        return produtoDAO.findAll();
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

        TableColumn<Produto, String> colCategoria = new TableColumn<>("Categoria");
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("idCategoria"));
        colCategoria.setPrefWidth(90);

        TableColumn<Produto, Integer> colQtd = new TableColumn<>("Qtd");
        colQtd.setCellValueFactory(new PropertyValueFactory<>("estoqueAtual"));
        colQtd.setPrefWidth(70);

        TableColumn<Produto, BigDecimal> colVlUnit = new TableColumn<>("Vl.Unit.");
        colVlUnit.setCellValueFactory(new PropertyValueFactory<>("precoVenda"));
        colVlUnit.setPrefWidth(100);

        TableColumn<Produto, BigDecimal> colVlTotal = new TableColumn<>("Vl.Total");
        colVlTotal.setCellValueFactory(cellData -> {
            BigDecimal preco   = cellData.getValue().getPrecoVenda()   != null ? cellData.getValue().getPrecoVenda()   : BigDecimal.ZERO;
            Integer estoque    = cellData.getValue().getEstoqueAtual() != null ? cellData.getValue().getEstoqueAtual() : 0;
            return new javafx.beans.property.SimpleObjectProperty<>(preco.multiply(new BigDecimal(estoque)));
        });
        colVlTotal.setPrefWidth(100);

        TableColumn<Produto, Boolean> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("ativo"));
        colStatus.setPrefWidth(70);

        table.getColumns().addAll(colId, colCodigo, colDescricao, colCategoria,
                                colQtd, colVlUnit, colVlTotal, colStatus);
    }

    @Override
    protected boolean matchesSearch(Produto p, String query) {
        if (query.isEmpty()) return true;
        return (p.getDescricao() != null && p.getDescricao().toLowerCase().contains(query))
            || (p.getCodigoBarras() != null && p.getCodigoBarras().toLowerCase().contains(query));
    }

    @Override
    protected boolean matchesFilters(Produto p) { return true; }

    @Override
    protected void resetFilters() {
        if (dataInicio != null) dataInicio.setValue(null);
        if (dataFim    != null) dataFim.setValue(null);
    }

    @Override @FXML protected void abrirFormNovo()      {}
    @Override @FXML protected void abrirFormEdicao()    {}
    @Override @FXML protected void excluirSelecionado() {}

    @FXML
    protected void abrirPreview() {
        try {
            JasperPrint jasperPrint = gerarRelatorio();

            // Salva em arquivo temporário
            java.io.File tempPdf = java.io.File.createTempFile("estoque_preview", ".pdf");
            tempPdf.deleteOnExit();
            JasperExportManager.exportReportToPdfFile(jasperPrint, tempPdf.getAbsolutePath());

            // Abre no leitor padrão do Windows via ProcessBuilder
            new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler",
                            tempPdf.getAbsolutePath())
                .start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void exportarPdf() {
        try {
            JasperPrint jasperPrint = gerarRelatorio();
            javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
            fc.setTitle("Salvar PDF");
            fc.setInitialFileName("relatorio_estoque.pdf");
            fc.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("PDF", "*.pdf"));
            File arquivo = fc.showSaveDialog(tableView.getScene().getWindow());
            if (arquivo != null)
                JasperExportManager.exportReportToPdfFile(
                    jasperPrint, arquivo.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JasperPrint gerarRelatorio() throws JRException {
        List<ProdutoRelatorioDTO> dados = new ArrayList<>();
        for (Produto p : tableView.getItems())
            dados.add(new ProdutoRelatorioDTO(p, ""));

        BigDecimal totalGeral = dados.stream()
            .map(ProdutoRelatorioDTO::getVlTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String inicio = dataInicio.getValue() != null
            ? dataInicio.getValue().format(fmt) : "01/01/2026";
        String fim = dataFim.getValue() != null
            ? dataFim.getValue().format(fmt)
            : java.time.LocalDate.now().format(fmt);

        Map<String, Object> parametros = new HashMap<>();
        parametros.put("EMPRESA_NOME",   "Merkatus");
        parametros.put("PERIODO_INICIO", inicio);
        parametros.put("PERIODO_FIM",    fim);
        parametros.put("USUARIO",        "Administrador");
        parametros.put("TOTAL_GERAL",    totalGeral);
        parametros.put(JRParameter.REPORT_LOCALE, new Locale("pt", "BR"));

        var stream = getClass().getResourceAsStream("/main/view/RelatorioEstoque.jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(stream);
        return JasperFillManager.fillReport(jasperReport, parametros,
            new JRBeanCollectionDataSource(dados));
    }
}
