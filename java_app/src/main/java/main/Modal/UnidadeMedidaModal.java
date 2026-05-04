package main.Modal;

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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import main.database.DAOs.UnidadeMedidaDAO;
import main.models.UnidadeMedida;
import main.util.FXMLLoaderFactory;

import java.util.List;

public class UnidadeMedidaModal extends BaseModal<UnidadeMedida> {

    @FXML private Button btnEditar;
    @FXML private Button btnExcluir;

    public UnidadeMedidaModal(Stage owner, UnidadeMedidaDAO unidadeMedidaDAO, FXMLLoaderFactory fxmlLoaderFactory) {
        super(owner, "Unidades de Medida", "/main/view/UnidadeMedidaModal.fxml", unidadeMedidaDAO, fxmlLoaderFactory);
    }

    @Override
    @FXML
    public void initialize() {
        btnEditar.setDisable(true);
        btnExcluir.setDisable(true);

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean enabled = newVal != null;
            btnEditar.setDisable(!enabled);
            btnExcluir.setDisable(!enabled);
        });

        super.initialize();
    }

    @Override
    protected List<UnidadeMedida> fetchFromDatabase() {
        return unidadeMedidaDAO.findAll();
    }

    @Override
    protected void configureColumns(TableView<UnidadeMedida> table) {
        TableColumn<UnidadeMedida, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idUnidade"));
        colId.setPrefWidth(60);

        TableColumn<UnidadeMedida, String> colSigla = new TableColumn<>("Sigla");
        colSigla.setCellValueFactory(new PropertyValueFactory<>("sigla"));
        colSigla.setPrefWidth(120);

        TableColumn<UnidadeMedida, String> colDescricao = new TableColumn<>("Descrição");
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colDescricao.setPrefWidth(400);

        table.getColumns().addAll(colId, colSigla, colDescricao);
    }

    @Override
    protected boolean matchesSearch(UnidadeMedida u, String query) {
        if (query.isEmpty()) return true;
        return (u.getSigla() != null && u.getSigla().toLowerCase().contains(query))
            || (u.getDescricao() != null && u.getDescricao().toLowerCase().contains(query));
    }

    @Override
    protected boolean matchesFilters(UnidadeMedida u) {
        return true;
    }

    @Override
    protected void resetFilters() {}

    @Override
    @FXML
    protected void abrirFormNovo() {
        new UnidadeMedidaFormModal(stage, null, unidadeMedidaDAO).show();
        loadData();
    }

    @Override
    @FXML
    protected void abrirFormEdicao() {
        UnidadeMedida selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            exibirAlerta("Selecione uma unidade de medida para editar.");
            return;
        }
        new UnidadeMedidaFormModal(stage, selected, unidadeMedidaDAO).show();
        loadData();
    }

    @Override
    @FXML
    protected void excluirSelecionado() {
        UnidadeMedida selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            exibirAlerta("Selecione uma unidade de medida para excluir.");
            return;
        }
        if (!mostrarConfirmacaoExclusao(selected)) return;
        try {
            unidadeMedidaDAO.deleteById(selected.getIdUnidade());
            loadData();
        } catch (Exception e) {
            exibirAlerta("Não foi possível excluir: a unidade pode estar em uso por algum produto.");
        }
    }

    private boolean mostrarConfirmacaoExclusao(UnidadeMedida u) {
        try {
            Stage confirmStage = new Stage();
            confirmStage.initOwner(stage);
            confirmStage.initModality(Modality.WINDOW_MODAL);
            confirmStage.initStyle(StageStyle.UNDECORATED);
            confirmStage.setResizable(false);

            HBox topBar = new HBox();
            topBar.setMinHeight(8);
            topBar.setMaxWidth(Double.MAX_VALUE);
            topBar.setStyle("-fx-background-color: #194e8f;");

            Label mensagem = new Label("Deseja excluir a unidade '" + u.getSigla() + " – " + u.getDescricao() + "'?");
            mensagem.setWrapText(true);
            mensagem.setStyle("-fx-font-size: 12; -fx-font-family: 'Segoe UI'; -fx-text-fill: #333333;");
            mensagem.setMaxWidth(340);

            Button btnConfirmar = new Button("Confirmar");
            btnConfirmar.setStyle("-fx-background-color: #194e8f; -fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 20;");

            Button btnCancelar = new Button("Cancelar");
            btnCancelar.setStyle("-fx-background-color: transparent; -fx-border-color: #194e8f; -fx-text-fill: #194e8f; -fx-font-family: 'Segoe UI'; -fx-cursor: hand; -fx-padding: 8 20; -fx-border-width: 1;");

            HBox botoes = new HBox(10, btnCancelar, btnConfirmar);
            botoes.setAlignment(Pos.CENTER);
            botoes.setPadding(new Insets(12, 0, 0, 0));

            VBox content = new VBox(16, topBar, mensagem, botoes);
            content.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 0 0 16 0;");
            content.setPrefWidth(380);
            VBox.setMargin(mensagem, new Insets(16, 16, 0, 16));
            VBox.setMargin(botoes, new Insets(16, 16, 0, 16));

            confirmStage.setScene(new Scene(content));
            confirmStage.sizeToScene();

            final boolean[] confirmed = {false};
            btnConfirmar.setOnAction(e -> { confirmed[0] = true; confirmStage.close(); });
            btnCancelar.setOnAction(e -> confirmStage.close());

            confirmStage.showAndWait();
            return confirmed[0];
        } catch (Exception e) {
            Alert fallback = new Alert(Alert.AlertType.CONFIRMATION);
            fallback.setTitle("Confirmar exclusão");
            fallback.setHeaderText(null);
            fallback.setContentText("Deseja excluir a unidade '" + u.getSigla() + "'?");
            fallback.initOwner(stage);
            return fallback.showAndWait().filter(r -> r == ButtonType.OK).isPresent();
        }
    }

    private void exibirAlerta(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.initOwner(stage);
        alerta.showAndWait();
    }
}
