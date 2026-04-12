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
import javafx.scene.layout.ColumnConstraints;
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
import java.util.ArrayList;
import java.util.List;

public class ProdutoFormModal {

    private static final String AZUL         = "#194e8f";
    private static final String VERMELHO     = "#c0392b";
    private static final String ESTILO_LABEL = "-fx-font-size: 11; -fx-font-family: 'Segoe UI'; -fx-text-fill: #333333;";
    private static final String ESTILO_CAMPO = "-fx-font-size: 11; -fx-font-family: 'Segoe UI'; -fx-background-color: white; -fx-border-color: #cccccc; -fx-border-width: 1;";

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
        tabPane.setStyle(
            "-fx-font-size: 11;" +
            "-fx-font-family: 'Segoe UI';"
        );
        tabPane.getTabs().addAll(
            new Tab("Geral",   buildAbaGeral()),
            new Tab("Preços",  buildAbaPrecos()),
            new Tab("Estoque", buildAbaEstoque()),
            new Tab("Fiscal",  buildAbaFiscal())
        );

        Button btnSalvar = new Button("Salvar");
        btnSalvar.setStyle(
            "-fx-background-color: " + AZUL + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 11;" +
            "-fx-font-family: 'Segoe UI';" +
            "-fx-cursor: hand;"
        );
        btnSalvar.setPrefHeight(32);
        btnSalvar.setPrefWidth(90);

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
        btnCancelar.setPrefWidth(90);

        btnSalvar.setOnAction(e -> salvar());
        btnCancelar.setOnAction(e -> stage.close());

        HBox rodape = new HBox(10, btnSalvar, btnCancelar);
        rodape.setPadding(new Insets(10));
        rodape.setAlignment(Pos.CENTER_RIGHT);
        rodape.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #d8d8d8; -fx-border-width: 1 0 0 0;");

        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/main/resources/logoAlter.png")));
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
        stage.setScene(new Scene(root));

        if (produto != null) preencherCampos();
    }

    private GridPane criarGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));
        grid.setStyle("-fx-background-color: #f5f5f5;");

        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(110);
        labelCol.setPrefWidth(110);

        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);
        fieldCol.setFillWidth(true);

        grid.getColumnConstraints().addAll(labelCol, fieldCol);
        return grid;
    }

    private GridPane buildAbaGeral() {
        GridPane grid = criarGrid();

        txtDescricao    = criarCampo();
        txtCodigoBarras = criarCampo();

        cbUnidade    = new ComboBox<>();
        cbCategoria  = new ComboBox<>();
        cbFornecedor = new ComboBox<>();

        for (ComboBox<?> cb : new ComboBox[]{cbUnidade, cbCategoria, cbFornecedor}) {
            cb.setMaxWidth(Double.MAX_VALUE);
            cb.setPrefHeight(24);
            cb.setStyle(ESTILO_CAMPO);
        }

        chkAtivo = new CheckBox("Ativo");
        chkAtivo.setStyle(ESTILO_LABEL);
        chkAtivo.setSelected(true);

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

        grid.addRow(0, criarLabel("Descrição:"),        txtDescricao);
        grid.addRow(1, criarLabel("Código de Barras:"), txtCodigoBarras);
        grid.addRow(2, criarLabel("Unidade:"),          cbUnidade);
        grid.addRow(3, criarLabel("Categoria:"),        cbCategoria);
        grid.addRow(4, criarLabel("Fornecedor:"),       cbFornecedor);
        grid.addRow(5, chkAtivo);

        return grid;
    }

    private void exibirErroCarregamento(String contexto, Exception e) {
        exibirAlerta("Erro", "Erro ao carregar " + contexto + ": " + e.getMessage());
    }

    private GridPane buildAbaPrecos() {
        GridPane grid = criarGrid();

        txtPrecoCusto    = criarCampo();
        txtPrecoVenda    = criarCampo();
        txtMargemLucro   = criarCampo();
        txtPesoLiquido   = criarCampo();
        txtPesoBruto     = criarCampo();

        chkFracionamento = new CheckBox("Permite fracionamento");
        chkFracionamento.setStyle(ESTILO_LABEL);

        txtMargemLucro.setEditable(false);
        txtMargemLucro.setStyle(ESTILO_CAMPO + "-fx-background-color: #eeeeee;");

        txtPrecoCusto.focusedProperty().addListener((obs, old, focused) -> {
            if (!focused) calcularMargem();
        });
        txtPrecoVenda.focusedProperty().addListener((obs, old, focused) -> {
            if (!focused) calcularMargem();
        });

        grid.addRow(0, criarLabel("Preço custo:"),  txtPrecoCusto);
        grid.addRow(1, criarLabel("Preço venda:"),  txtPrecoVenda);
        grid.addRow(2, criarLabel("Margem (%):"),   txtMargemLucro);
        grid.addRow(3, criarLabel("Peso líquido:"), txtPesoLiquido);
        grid.addRow(4, criarLabel("Peso bruto:"),   txtPesoBruto);
        grid.addRow(5, chkFracionamento);

        return grid;
    }

    private GridPane buildAbaEstoque() {
        GridPane grid = criarGrid();

        txtEstoqueAtual  = criarCampo();
        txtEstoqueMinimo = criarCampo();
        txtEstoqueMaximo = criarCampo();

        grid.addRow(0, criarLabel("Estoque atual:"),  txtEstoqueAtual);
        grid.addRow(1, criarLabel("Estoque mínimo:"), txtEstoqueMinimo);
        grid.addRow(2, criarLabel("Estoque máximo:"), txtEstoqueMaximo);

        return grid;
    }

    private GridPane buildAbaFiscal() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));
        grid.setStyle("-fx-background-color: #f5f5f5;");

        ColumnConstraints labelCol1 = new ColumnConstraints();
        labelCol1.setMinWidth(100);
        labelCol1.setPrefWidth(100);

        ColumnConstraints fieldCol1 = new ColumnConstraints();
        fieldCol1.setHgrow(Priority.ALWAYS);
        fieldCol1.setFillWidth(true);

        ColumnConstraints labelCol2 = new ColumnConstraints();
        labelCol2.setMinWidth(100);
        labelCol2.setPrefWidth(100);

        ColumnConstraints fieldCol2 = new ColumnConstraints();
        fieldCol2.setHgrow(Priority.ALWAYS);
        fieldCol2.setFillWidth(true);

        grid.getColumnConstraints().addAll(labelCol1, fieldCol1, labelCol2, fieldCol2);

        txtNcm        = criarCampo();
        txtCest       = criarCampo();
        txtCfop       = criarCampo();
        txtCstIcms    = criarCampo();
        txtCsosn      = criarCampo();
        txtCstPis     = criarCampo();
        txtCstCofins  = criarCampo();
        txtCstIpi     = criarCampo();
        txtAliqIcms   = criarCampo();
        txtAliqPis    = criarCampo();
        txtAliqCofins = criarCampo();
        txtAliqIpi    = criarCampo();

        aplicarFiltroNumerico(txtNcm,       8, false);
        aplicarFiltroNumerico(txtCest,      7, false);
        aplicarFiltroNumerico(txtCfop,      4, false);
        aplicarFiltroNumerico(txtCstIcms,   3, false);
        aplicarFiltroNumerico(txtCsosn,     3, false);
        aplicarFiltroNumerico(txtCstPis,    2, false);
        aplicarFiltroNumerico(txtCstCofins, 2, false);
        aplicarFiltroNumerico(txtCstIpi,    2, false);

        aplicarMascaraAliquota(txtAliqIcms);
        aplicarMascaraAliquota(txtAliqPis);
        aplicarMascaraAliquota(txtAliqCofins);
        aplicarMascaraAliquota(txtAliqIpi);

        txtNcm.setPromptText("00000000");
        txtCest.setPromptText("0000000");
        txtCfop.setPromptText("0000");
        txtCstIcms.setPromptText("000");
        txtCsosn.setPromptText("000");
        txtCstPis.setPromptText("00");
        txtCstCofins.setPromptText("00");
        txtCstIpi.setPromptText("00");
        txtAliqIcms.setPromptText("0.00");
        txtAliqPis.setPromptText("0.00");
        txtAliqCofins.setPromptText("0.00");
        txtAliqIpi.setPromptText("0.00");

        grid.addRow(0, criarLabel("NCM:"),           txtNcm,        criarLabel("CEST:"),         txtCest);
        grid.addRow(1, criarLabel("CFOP venda:"),    txtCfop,       criarLabel("CST ICMS:"),     txtCstIcms);
        grid.addRow(2, criarLabel("CSOSN:"),         txtCsosn,      criarLabel("CST PIS:"),      txtCstPis);
        grid.addRow(3, criarLabel("CST COFINS:"),    txtCstCofins,  criarLabel("CST IPI:"),      txtCstIpi);
        grid.addRow(4, criarLabel("Alíq. ICMS %:"),  txtAliqIcms,   criarLabel("Alíq. PIS %:"),  txtAliqPis);
        grid.addRow(5, criarLabel("Alíq. COFINS %:"),txtAliqCofins, criarLabel("Alíq. IPI %:"),  txtAliqIpi);

        return grid;
    }

    private void aplicarFiltroNumerico(TextField campo, int maxCaracteres, boolean permitirDecimal) {
        campo.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;

            String filtrado = newVal;

            if (permitirDecimal) {
                filtrado = filtrado.replaceAll("[^0-9.]", "");
                int primeiroPonto = filtrado.indexOf('.');
                if (primeiroPonto != -1) {
                    filtrado = filtrado.substring(0, primeiroPonto + 1)
                            + filtrado.substring(primeiroPonto + 1).replace(".", "");
                }
            } else {
                filtrado = filtrado.replaceAll("[^0-9]", "");
            }

            if (filtrado.length() > maxCaracteres) {
                filtrado = filtrado.substring(0, maxCaracteres);
            }

            if (!filtrado.equals(newVal)) {
                campo.setText(filtrado);
            }
        });
    }

    private void aplicarMascaraAliquota(TextField campo) {
        campo.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;

            String apenasDigitos = newVal.replaceAll("[^0-9]", "");

            if (apenasDigitos.length() > 5) {
                apenasDigitos = apenasDigitos.substring(0, 5);
            }

            String formatado;
            if (apenasDigitos.length() == 0) {
                formatado = "";
            } else if (apenasDigitos.length() == 1) {
                formatado = "0.0" + apenasDigitos;
            } else if (apenasDigitos.length() == 2) {
                formatado = "0." + apenasDigitos;
            } else {
                String inteiros  = apenasDigitos.substring(0, apenasDigitos.length() - 2);
                String decimais  = apenasDigitos.substring(apenasDigitos.length() - 2);
                formatado = inteiros + "." + decimais;
            }

            if (!formatado.equals(newVal)) {
                campo.setText(formatado);

                campo.positionCaret(formatado.length());
            }
        });
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
        String descricao = txtDescricao.getText().trim();

        List<String> erros = new ArrayList<>();

        // Aba Geral
        if (txtDescricao.getText().trim().isEmpty())
            erros.add("• Descrição  (aba Geral)");
        if (txtCodigoBarras.getText().trim().isEmpty())
            erros.add("• Código de barras  (aba Geral)");
        if (cbUnidade.getValue() == null)
            erros.add("• Unidade de medida  (aba Geral)");
        if (cbCategoria.getValue() == null)
            erros.add("• Categoria  (aba Geral)");
        if (cbFornecedor.getValue() == null)
            erros.add("• Fornecedor  (aba Geral)");

        // Aba Preços
        if (txtPrecoCusto.getText().trim().isEmpty())
            erros.add("• Preço de custo  (aba Preços)");
        if (txtPrecoVenda.getText().trim().isEmpty())
            erros.add("• Preço de venda  (aba Preços)");
        if (txtPesoLiquido.getText().trim().isEmpty())
            erros.add("• Peso líquido  (aba Preços)");
        if (txtPesoBruto.getText().trim().isEmpty())
            erros.add("• Peso bruto  (aba Preços)");

        // Aba Estoque
        if (txtEstoqueAtual.getText().trim().isEmpty())
            erros.add("• Estoque atual  (aba Estoque)");
        if (txtEstoqueMinimo.getText().trim().isEmpty())
            erros.add("• Estoque mínimo  (aba Estoque)");
        if (txtEstoqueMaximo.getText().trim().isEmpty())
            erros.add("• Estoque máximo  (aba Estoque)");

        // Aba Fiscal
        if (txtNcm.getText().trim().isEmpty())
            erros.add("• NCM  (aba Fiscal)");
        if (txtCest.getText().trim().isEmpty())
            erros.add("• CEST  (aba Fiscal)");
        if (txtCfop.getText().trim().isEmpty())
            erros.add("• CFOP  (aba Fiscal)");
        if (txtCstIcms.getText().trim().isEmpty())
            erros.add("• CST ICMS  (aba Fiscal)");
        if (txtCsosn.getText().trim().isEmpty())
            erros.add("• CSOSN  (aba Fiscal)");
        if (txtCstPis.getText().trim().isEmpty())
            erros.add("• CST PIS  (aba Fiscal)");
        if (txtCstCofins.getText().trim().isEmpty())
            erros.add("• CST COFINS  (aba Fiscal)");
        if (txtCstIpi.getText().trim().isEmpty())
            erros.add("• CST IPI  (aba Fiscal)");
        if (txtAliqIcms.getText().trim().isEmpty())
            erros.add("• Alíquota ICMS  (aba Fiscal)");
        if (txtAliqPis.getText().trim().isEmpty())
            erros.add("• Alíquota PIS  (aba Fiscal)");
        if (txtAliqCofins.getText().trim().isEmpty())
            erros.add("• Alíquota COFINS  (aba Fiscal)");
        if (txtAliqIpi.getText().trim().isEmpty())
            erros.add("• Alíquota IPI  (aba Fiscal)");

        if (!erros.isEmpty()) {
            exibirAlerta("Campos obrigatórios",
            "Preencha os campos abaixo antes de salvar:\n\n" + String.join("\n", erros));
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
            exibirAlerta("Produto salvo", "Produto salvo com sucesso!");
        } catch (Exception e) {
            exibirAlerta("Erro", "Erro ao salvar o produto: " + e.getMessage());
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
    private Label criarLabel(String texto) {
        Label label = new Label(texto);
        label.setStyle(ESTILO_LABEL);
        return label;
    }

    private TextField criarCampo() {
        TextField tf = new TextField();
        tf.setStyle(ESTILO_CAMPO);
        tf.setMaxWidth(Double.MAX_VALUE);
        tf.setPrefHeight(24);
        return tf;
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

