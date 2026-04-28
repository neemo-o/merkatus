package main.Modal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import main.util.FXMLLoaderFactory;

public class VendaModal extends BaseModal<Object> {

    @FXML private Button btnNovaVenda;
    @FXML private Button btnVerDetalhes;
    @FXML private Button btnCancelarVenda;

    public VendaModal(Stage owner, FXMLLoaderFactory fxmlLoaderFactory) {
        super(owner, "Vendas", "/main/view/VendaModal.fxml",
              null, null, null, null, null, fxmlLoaderFactory);
    }

    @Override
    @FXML
    public void initialize() {
        btnVerDetalhes.setDisable(true);
        btnCancelarVenda.setDisable(true);

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean selected = newVal != null;
            btnVerDetalhes.setDisable(!selected);
            btnCancelarVenda.setDisable(!selected);
        });

        super.initialize();
    }

    @Override
    protected List<Object> fetchFromDatabase() {
        return new ArrayList<>();
    }

    @Override
    protected void configureColumns(TableView<Object> table) {}

    @Override
    protected boolean matchesSearch(Object v, String query) {
        return true;
    }

    @Override
    protected boolean matchesFilters(Object v) {
        return true;
    }

    @Override
    protected void resetFilters() {}

    @Override
    @FXML
    protected void abrirFormNovo() {
        new VendaFormModal(stage).show();
    }

    @Override
    @FXML
    protected void abrirFormEdicao() {
        new VendaFormModal(stage).show();
    }

    @Override
    @FXML
    protected void excluirSelecionado() {
        exibirAlerta("Funcionalidade disponível em breve.");
    }

    private void exibirAlerta(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.initOwner(stage);
        alerta.showAndWait();
    }
}
