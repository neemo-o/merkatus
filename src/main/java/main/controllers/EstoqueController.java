package main.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class EstoqueController {

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<?> cbFiltroCategoria;

    @FXML
    private ComboBox<?> cbFiltroSituacao;

    @FXML
    private Button btnFiltrar;

    @FXML
    private Button btnLimparFiltro;

    @FXML
    private TableView<?> tableEstoque;

    @FXML
    private TableColumn<?, ?> colId;

    @FXML
    private TableColumn<?, ?> colCodigo;

    @FXML
    private TableColumn<?, ?> colNome;

    @FXML
    private TableColumn<?, ?> colCategoria;

    @FXML
    private TableColumn<?, ?> colEstoqueAtual;

    @FXML
    private TableColumn<?, ?> colEstoqueMin;

    @FXML
    private TableColumn<?, ?> colSituacao;

    @FXML
    private TableColumn<?, ?> colUltimaAtualizacao;

    @FXML
    private Label lblTotal;

    @FXML
    private Label lblBaixoEstoque;

    @FXML
    void initialize() {
        // Código de inicialização aqui
        // Carregar produtos do estoque
        // Configurar filtros de categoria
        // Configurar filtros de situação (Normal, Baixo, Crítico)
    }

    @FXML
    void handleFiltrar() {
        // Implementar lógica de filtrar estoque
    }

    @FXML
    void handleLimparFiltro() {
        // Implementar lógica de limpar filtros
    }

}
