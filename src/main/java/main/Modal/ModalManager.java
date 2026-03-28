package main.Modal;

import javafx.stage.Stage;
public class ModalManager {

    public static void open(ModalType type, Stage owner) {
        BaseModal<?> modal = switch (type) {
            case PRODUTO    -> new ProdutoModal(owner);
        };
        modal.show();
    }
}
