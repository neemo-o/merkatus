package main.Modal;

import java.sql.Time;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.scene.control.TableCell;

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
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import main.database.DAOs.FornecedorDAO;
import main.database.DAOs.EnderecoDAO;
import main.models.Endereco;
import main.models.Fornecedor;
import main.util.FXMLLoaderFactory;

public class FornecedorModal extends BaseModal<Fornecedor> {

    @FXML private CheckBox chkAtivo;
    @FXML private Button btnEditar;
    @FXML private Button btnExcluir;


    public FornecedorModal(Stage owner,
                       FornecedorDAO fornecedorDAO, EnderecoDAO enderecoDAO, FXMLLoaderFactory fxmlLoaderFactory) {
        super(owner, "Fornecedores", "/main/view/FornecedorModal.fxml",fornecedorDAO, enderecoDAO, fxmlLoaderFactory);
    }

    @Override
    @FXML
    public void initialize() {
        // Carrega os combos de filtro

        chkAtivo.setOnAction(e -> applyFilters());

        btnEditar.setDisable(true);
        btnExcluir.setDisable(true);

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            boolean enabled = newValue != null;
            btnEditar.setDisable(!enabled);
            btnExcluir.setDisable(!enabled);
        });

        // Chama o initialize da BaseModal para configurar tabela e busca
        super.initialize();
    }

    @Override
    protected List<Fornecedor> fetchFromDatabase() {
        return fornecedorDAO.findAll();
    }

    @Override
    protected void configureColumns(TableView<Fornecedor> table) {
        TableColumn<Fornecedor, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idFornecedor"));
        colId.setPrefWidth(150);

        TableColumn<Fornecedor, String> colCnpj = new TableColumn<>("Cnpj");
        colCnpj.setCellValueFactory(new PropertyValueFactory<>("cnpj"));
        colCnpj.setPrefWidth(150);

        TableColumn<Fornecedor, String> colRazaoSocial = new TableColumn<>("Razão Social");
        colRazaoSocial.setCellValueFactory(new PropertyValueFactory<>("razaoSocial"));
        colRazaoSocial.setPrefWidth(150);

        TableColumn<Fornecedor, String> colNomeFantasia = new TableColumn<>("Nome Fantasia");
        colNomeFantasia.setCellValueFactory(new PropertyValueFactory<>("nomeFantasia"));
        colNomeFantasia.setPrefWidth(150);

        TableColumn<Fornecedor, String> colTelefone = new TableColumn<>("Telefone");
        colTelefone.setCellValueFactory(new PropertyValueFactory<>("telefone"));
        colTelefone.setPrefWidth(150);

        TableColumn<Fornecedor, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("eMail"));
        colEmail.setPrefWidth(150);

        TableColumn<Fornecedor, String> colEndereco = new TableColumn<>("Endereço");
        colEndereco.setCellValueFactory(cellData -> {
            Integer id = cellData.getValue().getIdEndereco();
            if (id != null) {
                Endereco e = enderecoDAO.findById(id).orElse(null);
                if (e != null) {
                    return new javafx.beans.property.SimpleStringProperty(e.toString());
                }
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        colEndereco.setPrefWidth(200);

        TableColumn<Fornecedor, LocalDateTime> colDataCadastro = new TableColumn<>("Data de Cadastro");
        colDataCadastro.setCellValueFactory(new PropertyValueFactory<>("dataCadastro"));
        colDataCadastro.setPrefWidth(150);

        TableColumn<Fornecedor, LocalDateTime> colDataAtualizacao = new TableColumn<>("Data de Atualização");
        colDataAtualizacao.setCellValueFactory(new PropertyValueFactory<>("dataAtualizacao"));
        colDataAtualizacao.setPrefWidth(150);

        aplicarFormatoData(colDataCadastro);
        aplicarFormatoData(colDataAtualizacao);

        table.getColumns().add(colId);
        table.getColumns().add(colCnpj);
        table.getColumns().add(colRazaoSocial);
        table.getColumns().add(colNomeFantasia);
        table.getColumns().add(colTelefone);
        table.getColumns().add(colEmail);
        table.getColumns().add(colEndereco);
        table.getColumns().add(colDataCadastro);
        table.getColumns().add(colDataAtualizacao);

    }

    @Override
    protected boolean matchesSearch(Fornecedor p, String query) {
        if (query.isEmpty()) return true;
        return (p.getRazaoSocial() != null && p.getRazaoSocial().toLowerCase().contains(query))
            || (p.getCnpj() != null && p.getCnpj().toLowerCase().contains(query))
            || (p.getNomeFantasia() != null && p.getNomeFantasia().toLowerCase().contains(query));
    }

    @Override
    protected boolean matchesFilters(Fornecedor p) {

        boolean ativoOk = !chkAtivo.isSelected() || p.getAtivo();
        return ativoOk;
    }

    @Override
    protected void resetFilters() {
        chkAtivo.setSelected(false);
    }

    @Override
    @FXML
    protected void abrirFormNovo() {
        FornecedorFormModal form = new FornecedorFormModal(stage, null,
            fornecedorDAO, enderecoDAO);
        form.show();
        loadData();
    }

    @Override
    @FXML
    protected void abrirFormEdicao() {
        Fornecedor selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            exibirAlerta("Selecione um fornecedor para editar.");
            return;
        }
        FornecedorFormModal form = new FornecedorFormModal(stage, selected,
            fornecedorDAO, enderecoDAO);
        form.show();
        loadData();
    }

    @Override
    @FXML
    protected void excluirSelecionado() {
        Fornecedor selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            exibirAlerta("Selecione um fornecedor para excluir.");
            return;
        }

        if (!mostrarConfirmacaoExclusao(selected)) {
            return;
        }

        fornecedorDAO.deleteById(selected.getIdFornecedor());
        loadData();
    }

    private boolean mostrarConfirmacaoExclusao(Fornecedor fornecedor) {
        try {
            Stage confirmStage = new Stage();
            confirmStage.initOwner(stage);
            confirmStage.initModality(Modality.WINDOW_MODAL);
            confirmStage.initStyle(StageStyle.UNDECORATED);
            confirmStage.setTitle("Confirmar exclusão");
            confirmStage.setResizable(false);

            HBox topBar = new HBox();
            topBar.setMinHeight(8);
            topBar.setPrefHeight(8);
            topBar.setMaxWidth(Double.MAX_VALUE);
            topBar.setStyle("-fx-background-color: #194e8f;");

            Label mensagem = new Label("Deseja excluir o fornecedor '" +
                    (fornecedor.getRazaoSocial() != null ? fornecedor.getRazaoSocial() : "sem nome") + "'?");
            mensagem.setWrapText(true);
            mensagem.setTextAlignment(TextAlignment.CENTER);
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

            Scene scene = new Scene(content);
            confirmStage.setScene(scene);
            confirmStage.sizeToScene();

            final boolean[] confirmed = {false};
            btnConfirmar.setOnAction(e -> {
                confirmed[0] = true;
                confirmStage.close();
            });
            btnCancelar.setOnAction(e -> confirmStage.close());

            confirmStage.showAndWait();
            return confirmed[0];
        } catch (Exception e) {
            Alert fallback = new Alert(Alert.AlertType.CONFIRMATION);
            fallback.setTitle("Confirmar exclusão");
            fallback.setHeaderText(null);
            fallback.setContentText("Deseja excluir o fornecedor '" +
                    (fornecedor.getRazaoSocial() != null ? fornecedor.getRazaoSocial() : "sem nome") + "'?");
            fallback.initOwner(stage);
            return fallback.showAndWait().filter(response -> response == ButtonType.OK).isPresent();
        }
    }

    private <T> void aplicarFormatoData(TableColumn<T, LocalDateTime> coluna) {
    coluna.setCellFactory(column -> new TableCell<T, LocalDateTime>() {
        private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        @Override
        protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(dtf));
                }
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

