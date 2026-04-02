package main.Modal;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.math.BigDecimal;


public class ProdutoFormModal {

    private Stage stage;
    private Produto produto;

    private TextField txtDescricao, txtCodigoBarras;
    private ComboBox<UnidadeMedida> cbUnidade;
    private ComboBox<Categoria> cbCategoria;
    private ComboBox<Fornecedor> cbFornecedor;
    private CheckBox chkAtivo;

    private TextField txtPrecoCusto, txtPrecoVenda, txtMargemLucro;
    private TextField txtPesoLiquido, txtPesoBruto;
    private CheckBox chkFracionamento;

    private TextField txtEstoqueAtual, txtEstoqueMinimo, txtEstoqueMaximo;
    private CheckBox chkControlaEstoque;

    private double xOff, yOff;

    private TextField txtNcm, txtCest, txtCfop;
    private TextField txtCstIcms, txtCsosn;
    private TextField txtCstPis, txtCstCofins, txtCstIpi;
    private TextField txtAliqIcms, txtAliqPis, txtAliqCofins, txtAliqIpi;

    public ProdutoFormModal(Stage owner, Produto produto) {
        this.produto = produto;

        stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle(produto == null ? "Novo Produto" : "Editar Produto");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabPane.getTabs().addAll(
            new Tab("Geral",   buildAbaGeral()),
            new Tab("Preços",  buildAbaPrecos()),
            new Tab("Estoque", buildAbaEstoque()),
            new Tab("Fiscal",  buildAbaFiscal())
        );

        Button btnSalvar   = new Button("Salvar");
        Button btnCancelar = new Button("Cancelar");

        btnSalvar.setOnAction(e -> salvar());
        btnCancelar.setOnAction(e -> stage.close());

        HBox rodape = new HBox(10, btnSalvar, btnCancelar);
        rodape.setPadding(new Insets(10));
        rodape.setAlignment(Pos.CENTER_RIGHT);

        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/main/resources/logo.png")));
        logo.setFitHeight(22); logo.setFitWidth(22); logo.setPreserveRatio(true);

        Label titulo = new Label(produto == null ? "Novo Produto" : "Editar Produto");
        titulo.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);

        Button btnFechar = new Button("✕");
        btnFechar.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 12; -fx-cursor: hand; -fx-border-width: 0; -fx-padding: 0;");
        btnFechar.setOnAction(e -> stage.close());

        HBox topBar = new HBox(8, logo, titulo, espaco, btnFechar);
        topBar.setStyle("-fx-background-color: #194e8f; -fx-padding: 0 4 0 10; -fx-alignment: CENTER_LEFT;");

        // Drag
        topBar.setOnMousePressed(e -> { xOff = stage.getX() - e.getScreenX(); yOff = stage.getY() - e.getScreenY(); });
        topBar.setOnMouseDragged(e -> { stage.setX(e.getScreenX() + xOff); stage.setY(e.getScreenY() + yOff); });


        VBox root = new VBox(topBar, tabPane, rodape);
        stage.setScene(new Scene(root, 700, 500));

        if (produto != null) preencherCampos();
    }

    private GridPane buildAbaGeral() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        txtDescricao    = new TextField();
        txtCodigoBarras = new TextField();
        cbUnidade       = new ComboBox<>();
        cbCategoria     = new ComboBox<>();
        cbFornecedor    = new ComboBox<>();
        chkAtivo        = new CheckBox("Ativo");
        chkAtivo.setSelected(true);

        cbUnidade.getItems().addAll(UnidadeMedidaDAO.findAll());
        cbCategoria.getItems().addAll(CategoriaDAO.findAll());
        cbFornecedor.getItems().addAll(FornecedorDAO.findAll());

        txtDescricao.setPrefWidth(300);

        grid.addRow(0, new Label("Descrição:"),       txtDescricao);
        grid.addRow(1, new Label("Código de Barras:"), txtCodigoBarras);
        grid.addRow(2, new Label("Unidade:"),          cbUnidade);
        grid.addRow(3, new Label("Categoria:"),        cbCategoria);
        grid.addRow(4, new Label("Fornecedor:"),       cbFornecedor);
        grid.addRow(5, chkAtivo);

        return grid;
    }

    private GridPane buildAbaPrecos() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        txtPrecoCusto    = new TextField();
        txtPrecoVenda    = new TextField();
        txtMargemLucro   = new TextField();
        txtPesoLiquido   = new TextField();
        txtPesoBruto     = new TextField();
        chkFracionamento = new CheckBox("Permite fracionamento");

        txtMargemLucro.setEditable(false);

        txtPrecoCusto.focusedProperty().addListener((obs, old, focused) -> {
            if (!focused) calcularMargem();
        });
        txtPrecoVenda.focusedProperty().addListener((obs, old, focused) -> {
            if (!focused) calcularMargem();
        });

        grid.addRow(0, new Label("Preço custo:"),  txtPrecoCusto);
        grid.addRow(1, new Label("Preço venda:"),  txtPrecoVenda);
        grid.addRow(2, new Label("Margem (%):"),   txtMargemLucro);
        grid.addRow(3, new Label("Peso líquido:"), txtPesoLiquido);
        grid.addRow(4, new Label("Peso bruto:"),   txtPesoBruto);
        grid.addRow(5, chkFracionamento);

        return grid;
    }

    private GridPane buildAbaEstoque() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        txtEstoqueAtual    = new TextField();
        txtEstoqueMinimo   = new TextField();
        txtEstoqueMaximo   = new TextField();
        chkControlaEstoque = new CheckBox("Controla estoque");

        chkControlaEstoque.setSelected(true);

        chkControlaEstoque.selectedProperty().addListener((obs, old, selected) -> {
            txtEstoqueAtual.setDisable(!selected);
            txtEstoqueMinimo.setDisable(!selected);
            txtEstoqueMaximo.setDisable(!selected);
        });

        grid.addRow(0, new Label("Estoque atual:"),  txtEstoqueAtual);
        grid.addRow(1, new Label("Estoque mínimo:"), txtEstoqueMinimo);
        grid.addRow(2, new Label("Estoque máximo:"), txtEstoqueMaximo);
        grid.addRow(3, chkControlaEstoque);

        return grid;
    }

    private GridPane buildAbaFiscal() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        txtNcm        = new TextField();
        txtCest       = new TextField();
        txtCfop       = new TextField();
        txtCstIcms    = new TextField();
        txtCsosn      = new TextField();
        txtCstPis     = new TextField();
        txtCstCofins  = new TextField();
        txtCstIpi     = new TextField();
        txtAliqIcms   = new TextField();
        txtAliqPis    = new TextField();
        txtAliqCofins = new TextField();
        txtAliqIpi    = new TextField();

        txtNcm.setPrefWidth(120);
        txtCest.setPrefWidth(120);
        txtCfop.setPrefWidth(80);
        txtCstIcms.setPrefWidth(80);
        txtCsosn.setPrefWidth(80);
        txtCstPis.setPrefWidth(80);
        txtCstCofins.setPrefWidth(80);
        txtCstIpi.setPrefWidth(80);

        grid.addRow(0, new Label("NCM:"),         txtNcm,        new Label("CEST:"),        txtCest);
        grid.addRow(1, new Label("CFOP venda:"),  txtCfop,       new Label("CST ICMS:"),    txtCstIcms);
        grid.addRow(2, new Label("CSOSN:"),       txtCsosn,      new Label("CST PIS:"),     txtCstPis);
        grid.addRow(3, new Label("CST COFINS:"),  txtCstCofins,  new Label("CST IPI:"),     txtCstIpi);
        grid.addRow(4, new Label("Alíq. ICMS:"),  txtAliqIcms,   new Label("Alíq. PIS:"),   txtAliqPis);
        grid.addRow(5, new Label("Alíq. COFINS:"),txtAliqCofins, new Label("Alíq. IPI:"),   txtAliqIpi);

        return grid;
    }

    private void calcularMargem() {
        try {
            double custo = Double.parseDouble(txtPrecoCusto.getText().replace(",", "."));
            double venda = Double.parseDouble(txtPrecoVenda.getText().replace(",", "."));
            if (custo > 0) {
                double margem = ((venda - custo) / custo) * 100;
                txtMargemLucro.setText(String.format("%.2f", margem));
            }
        } catch (NumberFormatException ignored) {}
    }

    private void preencherCampos() {
        txtDescricao.setText(produto.getDescricao());
        txtCodigoBarras.setText(produto.getCodigoBarras());
        cbUnidade.setValue(findUnidadeMedida(produto.getUnidadeMedida()));
        cbCategoria.setValue(CategoriaDAO.findById(produto.getIdCategoria()));
        cbFornecedor.setValue(FornecedorDAO.findById(produto.getIdFornecedor()));
        chkAtivo.setSelected(produto.isAtivo());

        txtPrecoCusto.setText(produto.getPrecoCusto() != null ? produto.getPrecoCusto().toString() : "");
        txtPrecoVenda.setText(produto.getPrecoVenda() != null ? produto.getPrecoVenda().toString() : "");
        txtMargemLucro.setText(produto.getMargemLucro() != null ? produto.getMargemLucro().toString() : "");
        txtPesoLiquido.setText(produto.getPesoLiquido() != null ? produto.getPesoLiquido().toString() : "");
        txtPesoBruto.setText(produto.getPesoBruto() != null ? produto.getPesoBruto().toString() : "");
        chkFracionamento.setSelected(produto.isPermiteFracionamento());

        txtEstoqueAtual.setText(produto.getEstoqueAtual() != null ? produto.getEstoqueAtual().toString() : "");
        txtEstoqueMinimo.setText(produto.getEstoqueMinimo() != null ? produto.getEstoqueMinimo().toString() : "");
        txtEstoqueMaximo.setText(produto.getEstoqueMaximo() != null ? produto.getEstoqueMaximo().toString() : "");
        chkControlaEstoque.setSelected(produto.isControlaEstoque());

        txtNcm.setText(produto.getNcm());
        txtCest.setText(produto.getCest());
        txtCfop.setText(produto.getCfopVenda());
        txtCstIcms.setText(produto.getCstIcms());
        txtCsosn.setText(produto.getCsosn());
        txtCstPis.setText(produto.getCstPis());
        txtCstCofins.setText(produto.getCstCofins());
        txtCstIpi.setText(produto.getCstIpi());
        txtAliqIcms.setText(produto.getAliqIcms() != null ? produto.getAliqIcms().toString() : "");
        txtAliqPis.setText(produto.getAliqPis() != null ? produto.getAliqPis().toString() : "");
        txtAliqCofins.setText(produto.getAliqCofins() != null ? produto.getAliqCofins().toString() : "");
        txtAliqIpi.setText(produto.getAliqIpi() != null ? produto.getAliqIpi().toString() : "");
    }

    private UnidadeMedida findUnidadeMedida(String sigla) {
        if (sigla == null) {
            return null;
        }
        return cbUnidade.getItems().stream()
                .filter(u -> u != null && sigla.equals(u.getSigla()))
                .findFirst()
                .orElse(null);
    }

    private void salvar() {
        if (produto == null) produto = new Produto();

        produto.setDescricao(txtDescricao.getText());
        produto.setCodigoBarras(txtCodigoBarras.getText());
        produto.setUnidadeMedida(cbUnidade.getValue());
        produto.setCategoria(cbCategoria.getValue());
        produto.setFornecedor(cbFornecedor.getValue());
        produto.setAtivo(chkAtivo.isSelected());

        produto.setPrecoCusto(parseBigDecimal(txtPrecoCusto.getText()));
        produto.setPrecoVenda(parseBigDecimal(txtPrecoVenda.getText()));
        produto.setMargemLucro(parseBigDecimal(txtMargemLucro.getText()));
        produto.setPesoLiquido(parseBigDecimal(txtPesoLiquido.getText()));
        produto.setPesoBruto(parseBigDecimal(txtPesoBruto.getText()));
        produto.setPermiteFracionamento(chkFracionamento.isSelected());

        produto.setEstoqueAtual(parseInteger(txtEstoqueAtual.getText()));
        produto.setEstoqueMinimo(parseBigDecimal(txtEstoqueMinimo.getText()));
        produto.setEstoqueMaximo(parseBigDecimal(txtEstoqueMaximo.getText()));
        produto.setControlaEstoque(chkControlaEstoque.isSelected());

        produto.setNcm(txtNcm.getText());
        produto.setCest(txtCest.getText());
        produto.setCfopVenda(txtCfop.getText());
        produto.setCstIcms(txtCstIcms.getText());
        produto.setCsosn(txtCsosn.getText());
        produto.setCstPis(txtCstPis.getText());
        produto.setCstCofins(txtCstCofins.getText());
        produto.setCstIpi(txtCstIpi.getText());
        produto.setAliqIcms(parseBigDecimal(txtAliqIcms.getText()));
        produto.setAliqPis(parseBigDecimal(txtAliqPis.getText()));
        produto.setAliqCofins(parseBigDecimal(txtAliqCofins.getText()));
        produto.setAliqIpi(parseBigDecimal(txtAliqIpi.getText()));

        if (produto.getIdProduto() == null) {
            ProdutoDAO.insert(produto);
        } else {
            ProdutoDAO.update(produto);
        }

        stage.close();
    }

    private BigDecimal parseBigDecimal(String value) {
        try {
            return new BigDecimal(value.replace(",", "."));
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private Integer parseInteger(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void show() {
        stage.showAndWait();
    }
}