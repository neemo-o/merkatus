package main.Modal;

import javafx.stage.Stage;
import main.database.DAOs.CategoriaDAO;
import main.database.DAOs.EnderecoDAO;
import main.database.DAOs.FornecedorDAO;
import main.database.DAOs.ProdutoDAO;
import main.database.DAOs.UnidadeMedidaDAO;
import main.util.FXMLLoaderFactory;
import main.services.ProdutoService;

public class ModalManager {

    public static void open(ModalType type, Stage owner,
                           ProdutoDAO produtoDAO, CategoriaDAO categoriaDAO,
                           FornecedorDAO fornecedorDAO, UnidadeMedidaDAO unidadeMedidaDAO, EnderecoDAO enderecoDAO, FXMLLoaderFactory fxmlLoaderFactory, ProdutoService produtoService) {
        BaseModal<?> modal = switch (type) {
            case PRODUTO        -> new ProdutoModal(owner, produtoDAO, categoriaDAO, fornecedorDAO, unidadeMedidaDAO, fxmlLoaderFactory, produtoService);
            case FORNECEDOR     -> new FornecedorModal(owner, fornecedorDAO, enderecoDAO, fxmlLoaderFactory);
            case UNIDADE_MEDIDA -> new UnidadeMedidaModal(owner, unidadeMedidaDAO, fxmlLoaderFactory);
            case CATEGORIA      -> new CategoriaModal(owner, categoriaDAO, fxmlLoaderFactory);
            case VENDA -> new VendaModal(owner, fxmlLoaderFactory);
        };
        modal.show();
    }
}
