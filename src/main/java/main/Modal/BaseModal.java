package main.Modal;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

    public BaseModal(Stage owner, String title, String fxmlPath) {
        try {
            stage = new Stage();
            stage.initOwner(owner);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle(title);

            FXMLLoader loader = new FXMLLoader(
                getClass().getResource(fxmlPath)
            );
            loader.setController(this);

            Parent root = loader.load();
            stage.setScene(new Scene(root));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        tableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
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

    @FXML protected void confirmar() {
        stage.close();
    }

    @FXML protected void cancelar() {
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