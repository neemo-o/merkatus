package main.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;
import main.Modal.ModalManager;
import main.Modal.ModalType;
import org.springframework.stereotype.Component;

/**
 * Hub de relatórios carregado na área central da MainScreen.
 * Cada card abre o módulo correspondente (que possui exportação/consulta).
 */
@Component
public class RelatoriosController {

    private final ModalManager modalManager;

    public RelatoriosController(ModalManager modalManager) {
        this.modalManager = modalManager;
    }

    @FXML
    private void abrirRelatorioEstoque(ActionEvent event) {
        modalManager.open(ModalType.ESTOQUE, stageDe(event));
    }

    @FXML
    private void abrirRelatorioVendas(ActionEvent event) {
        modalManager.open(ModalType.VENDA, stageDe(event));
    }

    private Stage stageDe(ActionEvent event) {
        return (Stage) ((Node) event.getSource()).getScene().getWindow();
    }
}
