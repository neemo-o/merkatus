package main.Modal;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import main.database.DAOs.CategoriaDAO;
import main.models.Categoria;
import main.util.FXMLLoaderFactory;

import java.util.List;

public class CategoriaModal extends BaseModal<Categoria> {

    @FXML private CheckBox chkAtivo;
    @FXML private Button   btnEditar;
    @FXML private Button   btnExcluir;

    public CategoriaModal(Stage owner, CategoriaDAO categoriaDAO, FXMLLoaderFactory fxmlLoaderFactory) {
        super(owner, "Categorias", "/main/view/CategoriaModal.fxml", categoriaDAO, fxmlLoaderFactory);
    }

    @Override
    @FXML
    public void initialize() {
        chkAtivo.setOnAction(e -> applyFilters());

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
    protected List<Categoria> fetchFromDatabase() {
        return categoriaDAO.findAll();
    }

    @Override
    protected void configureColumns(TableView<Categoria> table) {
        TableColumn<Categoria, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idCategoria"));
        colId.setPrefWidth(60);

        TableColumn<Categoria, String> colNome = new TableColumn<>("Nome");
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colNome.setPrefWidth(280);

        TableColumn<Categoria, String> colPai = new TableColumn<>("Categoria Pai");
        colPai.setCellValueFactory(cellData -> {
            Integer parentId = cellData.getValue().getParentId();
            if (parentId == null) return new SimpleStringProperty("—");
            return categoriaDAO.findById(parentId)
                .map(p -> new SimpleStringProperty(p.getNome()))
                .orElse(new SimpleStringProperty("ID " + parentId));
        });
        colPai.setPrefWidth(200);

        TableColumn<Categoria, Boolean> colAtivo = new TableColumn<>("Ativo");
        colAtivo.setCellValueFactory(new PropertyValueFactory<>("ativo"));
        colAtivo.setPrefWidth(80);

        table.getColumns().addAll(colId, colNome, colPai, colAtivo);
    }

    @Override
    protected boolean matchesSearch(Categoria c, String query) {
        if (query.isEmpty()) return true;
        return c.getNome() != null && c.getNome().toLowerCase().contains(query);
    }

    @Override
    protected boolean matchesFilters(Categoria c) {
        return !chkAtivo.isSelected() || c.isAtivo();
    }

    @Override
    protected void resetFilters() {
        chkAtivo.setSelected(false);
    }

    @Override
    @FXML
    protected void abrirFormNovo() {
        new CategoriaFormModal(stage, null, categoriaDAO).show();
        loadData();
    }

    @Override
    @FXML
    protected void abrirFormEdicao() {
        Categoria selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            exibirAlerta("Selecione uma categoria para editar.");
            return;
        }
        new CategoriaFormModal(stage, selected, categoriaDAO).show();
        loadData();
    }

    @Override
    @FXML
    protected void excluirSelecionado() {
        Categoria selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            exibirAlerta("Selecione uma categoria para excluir.");
            return;
        }
        if (!mostrarConfirmacaoExclusao(selected)) return;
        try {
            categoriaDAO.deleteById(selected.getIdCategoria());
            loadData();
        } catch (Exception e) {
            exibirAlerta("Não foi possível excluir: a categoria pode estar em uso ou possuir subcategorias.");
        }
    }

    private boolean mostrarConfirmacaoExclusao(Categoria c) {
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

            Label mensagem = new Label("Deseja excluir a categoria '" + c.getNome() + "'?");
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
            fallback.setContentText("Deseja excluir a categoria '" + c.getNome() + "'?");
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
