package main.Modal;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import main.database.DAOs.ClienteDAO;
import main.database.DAOs.EnderecoDAO;
import main.models.Cliente;
import main.models.Endereco;
import main.services.FormatacaoService;
import main.util.DialogUtil;
import main.util.FXMLLoaderFactory;

public class ClienteModal extends BaseModal<Cliente> {

    @FXML private CheckBox chkAtivo;
    @FXML private Button btnEditar;
    @FXML private Button btnExcluir;

    private final ClienteDAO clienteDAO;
    private Map<Integer, Endereco> enderecosPorId = Map.of();

    public ClienteModal(Stage owner,
                        ClienteDAO clienteDAO, EnderecoDAO enderecoDAO, FXMLLoaderFactory fxmlLoaderFactory) {
        super(owner, "Clientes", "/main/view/ClienteModal.fxml", null, enderecoDAO, fxmlLoaderFactory);
        this.clienteDAO = clienteDAO;
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
    protected List<Cliente> fetchFromDatabase() {
        Map<Integer, Endereco> mapa = new HashMap<>();
        for (Endereco e : enderecoDAO.findAll()) {
            mapa.put(e.getIdEndereco(), e);
        }
        enderecosPorId = mapa;
        return clienteDAO.findAll();
    }

    @Override
    protected void configureColumns(TableView<Cliente> table) {
        TableColumn<Cliente, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idCliente"));
        colId.setPrefWidth(60);

        TableColumn<Cliente, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                "F".equals(cellData.getValue().getTipoPessoa()) ? "PF" : "PJ"));
        colTipo.setPrefWidth(50);

        TableColumn<Cliente, String> colCnpj = new TableColumn<>("CPF/CNPJ");
        colCnpj.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                formatarDocumento(cellData.getValue().getCnpj())));
        colCnpj.setPrefWidth(140);

        TableColumn<Cliente, String> colRazaoSocial = new TableColumn<>("Nome / Razão Social");
        colRazaoSocial.setCellValueFactory(new PropertyValueFactory<>("razaoSocial"));
        colRazaoSocial.setPrefWidth(180);

        TableColumn<Cliente, String> colNomeFantasia = new TableColumn<>("Nome Fantasia");
        colNomeFantasia.setCellValueFactory(new PropertyValueFactory<>("nomeFantasia"));
        colNomeFantasia.setPrefWidth(160);

        TableColumn<Cliente, String> colTelefone = new TableColumn<>("Telefone");
        colTelefone.setCellValueFactory(new PropertyValueFactory<>("telefoneCliente"));
        colTelefone.setPrefWidth(130);

        TableColumn<Cliente, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("emailCliente"));
        colEmail.setPrefWidth(160);

        TableColumn<Cliente, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusCliente"));
        colStatus.setPrefWidth(90);

        TableColumn<Cliente, BigDecimal> colLimiteCredito = new TableColumn<>("Limite Crédito");
        colLimiteCredito.setCellValueFactory(new PropertyValueFactory<>("limiteCredito"));
        colLimiteCredito.setCellFactory(FormatacaoService.cellFactoryMoeda());
        colLimiteCredito.setPrefWidth(110);

        TableColumn<Cliente, String> colEndereco = new TableColumn<>("Endereço");
        colEndereco.setCellValueFactory(cellData -> {
            Integer id = cellData.getValue().getIdEnderecoCliente();
            Endereco e = id != null ? enderecosPorId.get(id) : null;
            return new javafx.beans.property.SimpleStringProperty(e != null ? e.toString() : "");
        });
        colEndereco.setPrefWidth(200);

        TableColumn<Cliente, LocalDateTime> colDataCadastro = new TableColumn<>("Data de Cadastro");
        colDataCadastro.setCellValueFactory(new PropertyValueFactory<>("dataCadastro"));
        colDataCadastro.setPrefWidth(150);

        aplicarFormatoData(colDataCadastro);

        table.getColumns().add(colId);
        table.getColumns().add(colTipo);
        table.getColumns().add(colCnpj);
        table.getColumns().add(colRazaoSocial);
        table.getColumns().add(colNomeFantasia);
        table.getColumns().add(colTelefone);
        table.getColumns().add(colEmail);
        table.getColumns().add(colStatus);
        table.getColumns().add(colLimiteCredito);
        table.getColumns().add(colEndereco);
        table.getColumns().add(colDataCadastro);
    }

    @Override
    protected boolean matchesSearch(Cliente c, String query) {
        if (query.isEmpty()) return true;
        return (c.getRazaoSocial() != null && c.getRazaoSocial().toLowerCase().contains(query))
            || (c.getCnpj() != null && c.getCnpj().toLowerCase().contains(query))
            || (c.getNomeFantasia() != null && c.getNomeFantasia().toLowerCase().contains(query));
    }

    @Override
    protected boolean matchesFilters(Cliente c) {
        boolean ativoOk = !chkAtivo.isSelected() || Boolean.TRUE.equals(c.getAtivo());
        return ativoOk;
    }

    @Override
    protected void resetFilters() {
        chkAtivo.setSelected(false);
    }

    @Override
    @FXML
    protected void abrirFormNovo() {
        ClienteFormModal form = new ClienteFormModal(stage, null,
            clienteDAO, enderecoDAO);
        form.show();
        loadData();
    }

    @Override
    @FXML
    protected void abrirFormEdicao() {
        Cliente selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            exibirAlerta("Selecione um cliente para editar.");
            return;
        }
        ClienteFormModal form = new ClienteFormModal(stage, selected,
            clienteDAO, enderecoDAO);
        form.show();
        loadData();
    }

    @Override
    @FXML
    protected void excluirSelecionado() {
        Cliente selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            exibirAlerta("Selecione um cliente para excluir.");
            return;
        }

        String mensagem = "Deseja excluir o cliente '" +
                (selected.getRazaoSocial() != null ? selected.getRazaoSocial() : "sem nome") + "'?";
        if (!DialogUtil.confirmar(stage, "Confirmar exclusão", mensagem)) {
            return;
        }

        clienteDAO.deleteById(selected.getIdCliente());
        loadData();
    }

    /** Formata o documento pelo tamanho: 11 dígitos = CPF, 14 = CNPJ. */
    private String formatarDocumento(String digits) {
        if (digits == null) return "";
        if (digits.length() == 11) {
            return digits.replaceFirst("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
        }
        if (digits.length() == 14) {
            return digits.replaceFirst("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
        }
        return digits;
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
