package main.Modal;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import main.database.DAOs.CategoriaDAO;
import main.database.DAOs.FornecedorDAO;
import main.database.DAOs.ProdutoDAO;
import main.database.DAOs.UnidadeMedidaDAO;
import main.util.FXMLLoaderFactory;
import main.database.DAOs.EnderecoDAO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseModal<T> {

    protected Stage stage;
    protected List<T> allItems = new ArrayList<>();
    protected ObservableList<T> filteredItems = FXCollections.observableArrayList();

    @FXML protected TextField txtBusca;
    @FXML protected TableView<T> tableView;
    @FXML protected Label lblTotalRegistros;
    @FXML protected Label lblStatus;

    protected ProdutoDAO produtoDAO;
    protected CategoriaDAO categoriaDAO;
    protected FornecedorDAO fornecedorDAO;
    protected UnidadeMedidaDAO unidadeMedidaDAO;
    protected EnderecoDAO enderecoDAO;
    protected FXMLLoaderFactory fxmlLoaderFactory;

    public BaseModal(Stage owner, String title, String fxmlPath,
                    ProdutoDAO produtoDAO, CategoriaDAO categoriaDAO,
                    FornecedorDAO fornecedorDAO, UnidadeMedidaDAO unidadeMedidaDAO, FXMLLoaderFactory fxmlLoaderFactory) {
        this(owner, title, fxmlPath, produtoDAO, categoriaDAO, fornecedorDAO, unidadeMedidaDAO,null,fxmlLoaderFactory);
    }

    public BaseModal(Stage owner, String title, String fxmlPath,
                    FornecedorDAO fornecedorDAO, EnderecoDAO enderecoDAO, FXMLLoaderFactory fxmlLoaderFactory) {
        this(owner, title, fxmlPath, null, null, fornecedorDAO, null, enderecoDAO, fxmlLoaderFactory);
    }

    // base modal temporario par o layout visual do modal de vendas.

    public BaseModal(Stage owner, String title, String fxmlPath) {
       this(owner, title, fxmlPath, null, null, null, null, null, null);
    }

    public BaseModal(Stage owner, String title, String fxmlPath,
                    UnidadeMedidaDAO unidadeMedidaDAO, FXMLLoaderFactory fxmlLoaderFactory) {
        this(owner, title, fxmlPath, null, null, null, unidadeMedidaDAO, null, fxmlLoaderFactory);
    }

    public BaseModal(Stage owner, String title, String fxmlPath,
                    CategoriaDAO categoriaDAO, FXMLLoaderFactory fxmlLoaderFactory) {
        this(owner, title, fxmlPath, null, categoriaDAO, null, null, null, fxmlLoaderFactory);
    }

    public BaseModal(Stage owner, String title, String fxmlPath,
                     ProdutoDAO produtoDAO, CategoriaDAO categoriaDAO,
                     FornecedorDAO fornecedorDAO, UnidadeMedidaDAO unidadeMedidaDAO, EnderecoDAO enderecoDAO, FXMLLoaderFactory fxmlLoaderFactory) {
        this.produtoDAO = produtoDAO;
        this.categoriaDAO = categoriaDAO;
        this.fornecedorDAO = fornecedorDAO;
        this.unidadeMedidaDAO = unidadeMedidaDAO;
        this.enderecoDAO = enderecoDAO;
        this.fxmlLoaderFactory = fxmlLoaderFactory;
        

        try {
            stage = new Stage();
            stage.initOwner(owner);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle(title);

            stage.getIcons().add(new Image(getClass().getResourceAsStream("/main/resources/logo.png")));

            FXMLLoader loader = fxmlLoaderFactory.create(fxmlPath);
            loader.setController(this);

            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.setResizable(true);

            if (owner != null) {
                double maxWidth = owner.getWidth() * 0.95;
                double maxHeight = owner.getHeight() * 0.9;
                double prefWidth = Math.min(1100, Math.max(800, maxWidth * 0.9));
                double prefHeight = Math.min(760, Math.max(600, maxHeight * 0.9));

                stage.setMaxWidth(maxWidth);
                stage.setMaxHeight(maxHeight);
                stage.setWidth(prefWidth);
                stage.setHeight(prefHeight);
                stage.setX(owner.getX() + (owner.getWidth() - stage.getWidth()) / 2);
                stage.setY(owner.getY() + (owner.getHeight() - stage.getHeight()) / 2);
            } else {
                stage.sizeToScene();
                stage.centerOnScreen();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        tableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        tableView.getColumns().clear();
        configureColumns(tableView);

        txtBusca.textProperty().addListener((obs, old, val) -> applyFilters());

        filteredItems.addListener((javafx.collections.ListChangeListener<T>) c ->
            lblTotalRegistros.setText("Registros: " + filteredItems.size())
        );

        tableView.setItems(filteredItems);

        tableView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) abrirFormEdicao();
        });
    }

    protected void applyFilters() {
        String query = txtBusca.getText().toLowerCase();
        filteredItems.setAll(
            allItems.stream()
                .filter(item -> matchesSearch(item, query))
                .filter(this::matchesFilters)
                .collect(Collectors.toList())
        );
    }

    public void loadData() {
        allItems = fetchFromDatabase();
        applyFilters();
    }

    public void show() {
        loadData();
        stage.showAndWait();
    }

    @FXML protected void limparFiltros() {
        txtBusca.clear();
        resetFilters();
        applyFilters();
    }

    @FXML protected void fechar() {
        stage.close();
    }


    @FXML
    protected void minimizar() {
        ((Stage) tableView.getScene().getWindow()).setIconified(true);
    }

    // Métodos abstratos que cada modal filho implementa
    protected abstract List<T>  fetchFromDatabase();
    protected abstract void     configureColumns(TableView<T> table);
    protected abstract boolean  matchesSearch(T item, String query);
    protected abstract boolean  matchesFilters(T item);
    protected abstract void     resetFilters();

    @FXML protected abstract void abrirFormNovo();
    @FXML protected abstract void abrirFormEdicao();
    @FXML protected abstract void excluirSelecionado();
}