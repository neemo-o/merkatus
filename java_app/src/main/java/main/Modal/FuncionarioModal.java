package main.Modal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import main.database.DAOs.FuncionarioDAO;
import main.models.Funcionario;
import main.util.DialogUtil;
import main.util.FXMLLoaderFactory;

public class FuncionarioModal extends BaseModal<Funcionario> {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private CheckBox chkAtivo;
    @FXML private Button btnEditar;
    @FXML private Button btnExcluir;

    private final FuncionarioDAO funcionarioDAO;

    public FuncionarioModal(Stage owner, FuncionarioDAO funcionarioDAO, FXMLLoaderFactory fxmlLoaderFactory) {
        super(owner, "Funcionários", "/main/view/FuncionarioModal.fxml", null, null, fxmlLoaderFactory);
        this.funcionarioDAO = funcionarioDAO;
    }

    @Override
    @FXML
    public void initialize() {
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
    protected List<Funcionario> fetchFromDatabase() {
        return funcionarioDAO.findAll();
    }

    @Override
    protected void configureColumns(TableView<Funcionario> table) {
        TableColumn<Funcionario, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idFuncionario"));
        colId.setPrefWidth(60);

        TableColumn<Funcionario, String> colNome = new TableColumn<>("Nome");
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colNome.setPrefWidth(220);

        TableColumn<Funcionario, String> colCpf = new TableColumn<>("CPF");
        colCpf.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                formatarCpf(cellData.getValue().getCpf())));
        colCpf.setPrefWidth(130);

        TableColumn<Funcionario, String> colCargo = new TableColumn<>("Cargo");
        colCargo.setCellValueFactory(new PropertyValueFactory<>("cargo"));
        colCargo.setPrefWidth(140);

        TableColumn<Funcionario, LocalDate> colAdmissao = new TableColumn<>("Admissão");
        colAdmissao.setCellValueFactory(new PropertyValueFactory<>("dataAdmissao"));
        aplicarFormatoData(colAdmissao);
        colAdmissao.setPrefWidth(100);

        TableColumn<Funcionario, LocalDate> colDemissao = new TableColumn<>("Demissão");
        colDemissao.setCellValueFactory(new PropertyValueFactory<>("dataDemissao"));
        aplicarFormatoData(colDemissao);
        colDemissao.setPrefWidth(100);

        TableColumn<Funcionario, String> colTelefone = new TableColumn<>("Telefone");
        colTelefone.setCellValueFactory(new PropertyValueFactory<>("telefone"));
        colTelefone.setPrefWidth(130);

        TableColumn<Funcionario, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setPrefWidth(160);

        TableColumn<Funcionario, Boolean> colAtivo = new TableColumn<>("Ativo");
        colAtivo.setCellValueFactory(new PropertyValueFactory<>("ativo"));
        colAtivo.setPrefWidth(60);

        table.getColumns().add(colId);
        table.getColumns().add(colNome);
        table.getColumns().add(colCpf);
        table.getColumns().add(colCargo);
        table.getColumns().add(colAdmissao);
        table.getColumns().add(colDemissao);
        table.getColumns().add(colTelefone);
        table.getColumns().add(colEmail);
        table.getColumns().add(colAtivo);
    }

    @Override
    protected boolean matchesSearch(Funcionario f, String query) {
        if (query.isEmpty()) return true;
        return (f.getNome() != null && f.getNome().toLowerCase().contains(query))
            || (f.getCpf() != null && f.getCpf().toLowerCase().contains(query))
            || (f.getCargo() != null && f.getCargo().toLowerCase().contains(query));
    }

    @Override
    protected boolean matchesFilters(Funcionario f) {
        return !chkAtivo.isSelected() || Boolean.TRUE.equals(f.getAtivo());
    }

    @Override
    protected void resetFilters() {
        chkAtivo.setSelected(false);
    }

    @Override
    @FXML
    protected void abrirFormNovo() {
        new FuncionarioFormModal(stage, null, funcionarioDAO).show();
        loadData();
    }

    @Override
    @FXML
    protected void abrirFormEdicao() {
        Funcionario selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            exibirAlerta("Selecione um funcionário para editar.");
            return;
        }
        new FuncionarioFormModal(stage, selected, funcionarioDAO).show();
        loadData();
    }

    @Override
    @FXML
    protected void excluirSelecionado() {
        Funcionario selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            exibirAlerta("Selecione um funcionário para excluir.");
            return;
        }

        String mensagem = "Deseja excluir o funcionário '" +
                (selected.getNome() != null ? selected.getNome() : "sem nome") + "'?";
        if (!DialogUtil.confirmar(stage, "Confirmar exclusão", mensagem)) {
            return;
        }

        funcionarioDAO.deleteById(selected.getIdFuncionario());
        loadData();
    }

    private String formatarCpf(String digits) {
        if (digits == null || digits.length() != 11) return digits != null ? digits : "";
        return digits.replaceFirst("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
    }

    private <T> void aplicarFormatoData(TableColumn<T, LocalDate> coluna) {
        coluna.setCellFactory(column -> new TableCell<T, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(DATA));
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
