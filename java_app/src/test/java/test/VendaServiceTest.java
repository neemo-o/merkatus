package test;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import main.database.DAOs.FormaPagamentoDAO;
import main.database.DAOs.ItemVendaDAO;
import main.database.DAOs.ProdutoDAO;
import main.models.FormaPagamento;
import main.models.ItemVenda;
import main.models.PagamentoVenda;
import main.models.Produto;
import main.models.Venda;
import main.services.VendaService;
import main.util.SessionManager;

/**
 * Teste de integração do fluxo de venda (o mesmo executado pelo menu Vendas):
 * finalização com baixa de estoque, troco, pagamento dividido e validações.
 */
@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("test")
@Transactional
class VendaServiceTest {

    @Autowired private VendaService vendaService;
    @Autowired private ProdutoDAO produtoDAO;
    @Autowired private ItemVendaDAO itemVendaDAO;
    @Autowired private FormaPagamentoDAO formaPagamentoDAO;

    @BeforeEach
    void setup() {
        // Venda sem operador logado (id_operador é opcional na venda)
        SessionManager.clear();
    }

    private Produto criarProduto(int estoque) {
        Produto p = new Produto();
        p.setDescricao("Produto Venda Teste");
        p.setPrecoVenda(new BigDecimal("10.00"));
        p.setEstoqueAtual(estoque);
        p.setControlaEstoque(true);
        return produtoDAO.save(p);
    }

    private FormaPagamento dinheiro() {
        return formaPagamentoDAO.findAll().stream()
                .filter(f -> "DINHEIRO".equals(f.getTipo()))
                .findFirst()
                .orElseGet(() -> {
                    FormaPagamento f = new FormaPagamento();
                    f.setDescricao("Dinheiro Teste");
                    f.setTipo("DINHEIRO");
                    f.setPermiteTroco(true);
                    f.setAtivo(true);
                    return formaPagamentoDAO.save(f);
                });
    }

    private FormaPagamento cartaoDebito() {
        return formaPagamentoDAO.findAll().stream()
                .filter(f -> "DEBITO".equals(f.getTipo()))
                .findFirst()
                .orElseGet(() -> {
                    FormaPagamento f = new FormaPagamento();
                    f.setDescricao("Débito Teste");
                    f.setTipo("DEBITO");
                    f.setPermiteTroco(false);
                    f.setAtivo(true);
                    return formaPagamentoDAO.save(f);
                });
    }

    private ItemVenda item(Produto p, int quantidade) {
        ItemVenda i = new ItemVenda();
        i.setIdProduto(p.getIdProduto());
        i.setQuantidade(quantidade);
        i.setPrecoUnitario(p.getPrecoVenda());
        i.setDesconto(BigDecimal.ZERO);
        i.setAcrescimo(BigDecimal.ZERO);
        i.setTotalItem(p.getPrecoVenda().multiply(BigDecimal.valueOf(quantidade)));
        return i;
    }

    @Test
    void deveFinalizarVendaComBaixaDeEstoqueETroco() {
        Produto p = criarProduto(10);

        Venda venda = vendaService.finalizarVenda(
                List.of(item(p, 2)),
                List.of(new PagamentoVenda(dinheiro(), new BigDecimal("50.00"))),
                BigDecimal.ZERO, null, null);

        assertNotNull(venda.getIdVenda());
        assertEquals("FINALIZADA", venda.getStatus());
        assertEquals(0, new BigDecimal("20.00").compareTo(venda.getValorTotal()));
        assertEquals(0, new BigDecimal("30.00").compareTo(venda.getTroco()));

        // Baixa de estoque: 10 - 2 = 8
        assertEquals(8, produtoDAO.findById(p.getIdProduto()).orElseThrow().getEstoqueAtual());

        // Item gravado com espelho da venda
        assertEquals(1, itemVendaDAO.findByVenda(venda.getIdVenda()).size());
    }

    @Test
    void deveFinalizarVendaComPagamentoDividido() {
        Produto p = criarProduto(10);

        // Total 30: débito 20 + dinheiro 15 → troco 5 (sai do dinheiro)
        Venda venda = vendaService.finalizarVenda(
                List.of(item(p, 3)),
                List.of(new PagamentoVenda(cartaoDebito(), new BigDecimal("20.00")),
                        new PagamentoVenda(dinheiro(), new BigDecimal("15.00"))),
                BigDecimal.ZERO, null, null);

        assertEquals(0, new BigDecimal("30.00").compareTo(venda.getValorTotal()));
        assertEquals(0, new BigDecimal("5.00").compareTo(venda.getTroco()));
        assertTrue(venda.getFormaPagamento().contains("+"),
                "Forma de pagamento legada deve listar as formas combinadas");
    }

    @Test
    void deveRecusarEstoqueInsuficiente() {
        Produto p = criarProduto(1);

        assertThrows(IllegalStateException.class, () -> vendaService.finalizarVenda(
                List.of(item(p, 5)),
                List.of(new PagamentoVenda(dinheiro(), new BigDecimal("100.00"))),
                BigDecimal.ZERO, null, null));
    }

    @Test
    void deveRecusarPagamentoInsuficiente() {
        Produto p = criarProduto(10);

        assertThrows(IllegalArgumentException.class, () -> vendaService.finalizarVenda(
                List.of(item(p, 2)),
                List.of(new PagamentoVenda(dinheiro(), new BigDecimal("5.00"))),
                BigDecimal.ZERO, null, null));
    }

    @Test
    void deveRecusarTrocoEmFormaQueNaoPermite() {
        Produto p = criarProduto(10);

        // Pagou 50 no débito para um total de 20 — débito não devolve troco
        assertThrows(IllegalArgumentException.class, () -> vendaService.finalizarVenda(
                List.of(item(p, 2)),
                List.of(new PagamentoVenda(cartaoDebito(), new BigDecimal("50.00"))),
                BigDecimal.ZERO, null, null));
    }
}
