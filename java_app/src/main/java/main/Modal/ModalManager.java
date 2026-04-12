package main.Modal;

import javafx.stage.Stage;
import main.database.DAOs.CategoriaDAO;
import main.database.DAOs.FornecedorDAO;
import main.database.DAOs.ProdutoDAO;
import main.database.DAOs.UnidadeMedidaDAO;

public class ModalManager {

    public static void open(ModalType type, Stage owner,
                           ProdutoDAO produtoDAO, CategoriaDAO categoriaDAO,
                           FornecedorDAO fornecedorDAO, UnidadeMedidaDAO unidadeMedidaDAO) {
        BaseModal<?> modal = switch (type) {
            case PRODUTO -> new ProdutoModal(owner, produtoDAO, categoriaDAO, fornecedorDAO, unidadeMedidaDAO);
            case FORNECEDOR -> new FornecedorModal(owner, fornecedorDAO);
        };
        modal.show();
    }
}
