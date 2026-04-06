package main.Modal;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import main.database.DAOs.CategoriaDAO;
import main.database.DAOs.FornecedorDAO;
import main.database.DAOs.ProdutoDAO;
import main.database.DAOs.UnidadeMedidaDAO;
import main.models.Categoria;
import main.models.Fornecedor;
import main.models.Produto;
import main.models.UnidadeMedida;

import java.math.BigDecimal;

public class ProdutoFormModal {

    private final ProdutoDAO produtoDAO;
    private final CategoriaDAO categoriaDAO;
    private final FornecedorDAO fornecedorDAO;
    private final UnidadeMedidaDAO unidadeMedidaDAO;

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

    public ProdutoFormModal(Stage owner, Produto produto,
                            ProdutoDAO produtoDAO, CategoriaDAO categoriaDAO,
                            FornecedorDAO fornecedorDAO, UnidadeMedidaDAO unidadeMedidaDAO) {
        this.produto = produto;
        this.produtoDAO = produtoDAO;
        this.categoriaDAO = categoriaDAO;
        this.fornecedorDAO = fornecedorDAO;
        this.unidadeMedidaDAO = unidadeMedidaDAO;

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

        Button btnFechar = new Button("X");
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

        // Carrega combos com try/catch — em caso de erro, combo fica vazio, app não trava
        try {
            cbUnidade.getItems().addAll(unidadeMedidaDAO.findAll());
        } catch (Exception e) {
            exibirErroCarregamento("Unidade de Medida", e);
        }
        try {
            cbCategoria.getItems().addAll(categoriaDAO.findAll());
        } catch (Exception e) {
            exibirErroCarregamento("Categoria", e);
        }
        try {
            cbFornecedor.getItems().addAll(fornecedorDAO.findAll());
        } catch (Exception e) {
            exibirErroCarregamento("Fornecedor", e);
        }

        txtDescricao.setPrefWidth(300);

        grid.addRow(0, new Label("Descrição:"),       txtDescricao);
        grid.addRow(1, new Label("Código de Barras:"), txtCodigoBarras);
        grid.addRow(2, new Label("Unidade:"),          cbUnidade);
        grid.addRow(3, new Label("Categoria:"),        cbCategoria);
        grid.addRow(4, new Label("Fornecedor:"),       cbFornecedor);
        grid.addRow(5, chkAtivo);

        return grid;
    }

    private void exibirErroCarregamento(String contexto, Exception e) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setHeaderText(null);
        alerta.setContentText("Erro ao carregar " + contexto + ": " + e.getMessage());
        alerta.initOwner(stage);
        alerta.showAndWait();
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
        txtDescricao.setText(produto.getDescricao() != null ? produto.getDescricao() : "");
        txtCodigoBarras.setText(produto.getCodigoBarras() != null ? produto.getCodigoBarras() : "");
        cbUnidade.setValue(findUnidadeMedida(produto.getUnidadeMedida()));
        cbCategoria.setValue(categoriaDAO.findById(produto.getIdCategoria()).orElse(null));
        cbFornecedor.setValue(fornecedorDAO.findById(produto.getIdFornecedor()).orElse(null));
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

        txtNcm.setText(produto.getNcm() != null ? produto.getNcm() : "");
        txtCest.setText(produto.getCest() != null ? produto.getCest() : "");
        txtCfop.setText(produto.getCfopVenda() != null ? produto.getCfopVenda() : "");
        txtCstIcms.setText(produto.getCstIcms() != null ? produto.getCstIcms() : "");
        txtCsosn.setText(produto.getCsosn() != null ? produto.getCsosn() : "");
        txtCstPis.setText(produto.getCstPis() != null ? produto.getCstPis() : "");
        txtCstCofins.setText(produto.getCstCofins() != null ? produto.getCstCofins() : "");
        txtCstIpi.setText(produto.getCstIpi() != null ? produto.getCstIpi() : "");
        txtAliqIcms.setText(produto.getAliqIcms() != null ? produto.getAliqIcms().toString() : "");
        txtAliqPis.setText(produto.getAliqPis() != null ? produto.getAliqPis().toString() : "");
        txtAliqCofins.setText(produto.getAliqCofins() != null ? produto.getAliqCofins().toString() : "");
        txtAliqIpi.setText(produto.getAliqIpi() != null ? produto.getAliqIpi().toString() : "");

        calcularMargem();
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
        // Validação de campos obrigatórios
        String descricao = txtDescricao.getText().trim();
        if (descricao.isEmpty()) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setHeaderText(null);
            alerta.setContentText("A descrição do produto é obrigatória.");
            alerta.initOwner(stage);
            alerta.showAndWait();
            return;
        }

        if (cbUnidade.getValue() == null) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setHeaderText(null);
            alerta.setContentText("Selecione uma unidade de medida.");
            alerta.initOwner(stage);
            alerta.showAndWait();
            return;
        }

        if (produto == null) produto = new Produto();

        produto.setDescricao(descricao);
        produto.setCodigoBarras(txtCodigoBarras.getText().trim());
        produto.setUnidadeMedida(cbUnidade.getValue().getSigla());
        produto.setIdUnidadeMedida(cbUnidade.getValue().getIdUnidade());
        produto.setIdCategoria(cbCategoria.getValue() != null ? cbCategoria.getValue().getIdCategoria() : null);
        produto.setIdFornecedor(cbFornecedor.getValue() != null ? cbFornecedor.getValue().getIdFornecedor() : null);
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

        produto.setNcm(txtNcm.getText().trim());
        produto.setCest(txtCest.getText().trim());
        produto.setCfopVenda(txtCfop.getText().trim());
        produto.setCstIcms(txtCstIcms.getText().trim());
        produto.setCsosn(txtCsosn.getText().trim());
        produto.setCstPis(txtCstPis.getText().trim());
        produto.setCstCofins(txtCstCofins.getText().trim());
        produto.setCstIpi(txtCstIpi.getText().trim());
        produto.setAliqIcms(parseBigDecimal(txtAliqIcms.getText()));
        produto.setAliqPis(parseBigDecimal(txtAliqPis.getText()));
        produto.setAliqCofins(parseBigDecimal(txtAliqCofins.getText()));
        produto.setAliqIpi(parseBigDecimal(txtAliqIpi.getText()));

        try {
            if (produto.getIdProduto() == null) {
                produtoDAO.save(produto);
            } else {
                produtoDAO.update(produto);
            }
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setHeaderText(null);
            info.setContentText("Produto salvo com sucesso!");
            info.initOwner(stage);
            info.showAndWait();
            stage.close();
        } catch (Exception e) {
            Alert erro = new Alert(Alert.AlertType.ERROR);
            erro.setHeaderText(null);
            erro.setContentText("Erro ao salvar o produto: " + e.getMessage());
            erro.initOwner(stage);
            erro.showAndWait();
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null) return null;

        String trimmed = value.trim();
        if (trimmed.isEmpty()) return null;

        try {
            return new BigDecimal(trimmed.replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInteger(String value) {
        if (value == null) return null;

        String trimmed = value.trim();
        if (trimmed.isEmpty()) return null;

        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void show() {
        stage.showAndWait();
    }
}

