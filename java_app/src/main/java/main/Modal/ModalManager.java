package main.Modal;

import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import main.controllers.EstoqueController;
import main.database.DAOs.CaixaDAO;
import main.database.DAOs.CategoriaDAO;
import main.database.DAOs.ClienteDAO;
import main.database.DAOs.EnderecoDAO;
import main.database.DAOs.FormaPagamentoDAO;
import main.database.DAOs.FornecedorDAO;
import main.database.DAOs.FuncionarioDAO;
import main.database.DAOs.ItemVendaDAO;
import main.database.DAOs.ProdutoDAO;
import main.database.DAOs.UnidadeMedidaDAO;
import main.database.DAOs.VendaDAO;
import main.util.FXMLLoaderFactory;
import main.services.CaixaService;
import main.services.ProdutoService;
import main.services.VendaService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ModalManager {

    private final ProdutoDAO produtoDAO;
    private final CategoriaDAO categoriaDAO;
    private final FornecedorDAO fornecedorDAO;
    private final UnidadeMedidaDAO unidadeMedidaDAO;
    private final EnderecoDAO enderecoDAO;
    private final ClienteDAO clienteDAO;
    private final FuncionarioDAO funcionarioDAO;
    private final VendaDAO vendaDAO;
    private final ItemVendaDAO itemVendaDAO;
    private final FormaPagamentoDAO formaPagamentoDAO;
    private final CaixaDAO caixaDAO;
    private final FXMLLoaderFactory fxmlLoaderFactory;
    private final ProdutoService produtoService;
    private final VendaService vendaService;
    private final CaixaService caixaService;

    public void open(ModalType type, Stage owner) {
        BaseModal<?> modal = switch (type) {
            case PRODUTO        -> new ProdutoModal(owner, produtoDAO, categoriaDAO, fornecedorDAO, unidadeMedidaDAO, fxmlLoaderFactory, produtoService);
            case FORNECEDOR     -> new FornecedorModal(owner, fornecedorDAO, enderecoDAO, fxmlLoaderFactory);
            case UNIDADE_MEDIDA -> new UnidadeMedidaModal(owner, unidadeMedidaDAO, fxmlLoaderFactory);
            case CATEGORIA      -> new CategoriaModal(owner, categoriaDAO, fxmlLoaderFactory);
            case CLIENTE        -> new ClienteModal(owner, clienteDAO, enderecoDAO, fxmlLoaderFactory);
            case FUNCIONARIO    -> new FuncionarioModal(owner, funcionarioDAO, fxmlLoaderFactory);
            case VENDA          -> new VendaModal(owner, vendaDAO, itemVendaDAO, clienteDAO, produtoDAO, formaPagamentoDAO, vendaService, fxmlLoaderFactory);
            case CAIXA          -> new CaixaModal(owner, caixaDAO, caixaService, fxmlLoaderFactory);
            case ESTOQUE        -> new EstoqueController(owner, produtoDAO, fxmlLoaderFactory, categoriaDAO);
        };
        modal.show();
    }
}
