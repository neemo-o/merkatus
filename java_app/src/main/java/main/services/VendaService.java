package main.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.database.DAOs.ItemVendaDAO;
import main.database.DAOs.ProdutoDAO;
import main.database.DAOs.VendaDAO;
import main.models.FormaPagamento;
import main.models.ItemVenda;
import main.models.Produto;
import main.models.TributacaoPerfil;
import main.models.Usuario;
import main.models.Venda;
import main.util.SessionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VendaService {

    private final VendaDAO vendaDAO;
    private final ItemVendaDAO itemVendaDAO;
    private final ProdutoDAO produtoDAO;
    private final TributacaoService tributacaoService;

    /**
     * Finaliza uma venda: grava a venda, os itens (com espelho fiscal),
     * o pagamento e dá baixa no estoque — tudo em uma única transação.
     *
     * @param valorRecebido usado para calcular o troco (apenas formas que permitem troco); null = valor exato
     * @param idCliente cliente vinculado à venda; null = Consumidor Final
     */
    @Transactional
    public Venda finalizarVenda(List<ItemVenda> itens, FormaPagamento formaPagamento,
                                BigDecimal desconto, String cpfNota, BigDecimal valorRecebido,
                                Integer idCliente) {

        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException("A venda precisa ter ao menos um item.");
        }
        if (formaPagamento == null) {
            throw new IllegalArgumentException("Selecione a forma de pagamento.");
        }

        BigDecimal subtotal = itens.stream()
                .map(ItemVenda::getTotalItem)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (desconto == null) desconto = BigDecimal.ZERO;
        if (desconto.compareTo(BigDecimal.ZERO) < 0 || desconto.compareTo(subtotal) > 0) {
            throw new IllegalArgumentException("Desconto inválido (deve estar entre R$ 0,00 e o subtotal).");
        }

        BigDecimal valorTotal = subtotal.subtract(desconto);

        BigDecimal troco = BigDecimal.ZERO;
        if (valorRecebido != null) {
            troco = valorRecebido.subtract(valorTotal);
            if (troco.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Valor recebido é menor que o total da venda.");
            }
        }

        Usuario operador = SessionManager.getUsuarioAtual();

        Venda venda = new Venda();
        venda.setIdOperador(operador != null ? operador.getIdUsuario() : null);
        venda.setIdCliente(idCliente);
        venda.setDataVenda(LocalDateTime.now());
        venda.setSubtotal(subtotal);
        venda.setDesconto(desconto);
        venda.setAcrescimo(BigDecimal.ZERO);
        venda.setValorTotal(valorTotal);
        venda.setTroco(troco);
        venda.setCpfNota(cpfNota != null && !cpfNota.isBlank() ? cpfNota : null);
        venda.setFormaPagamento(formaPagamento.getDescricao());
        venda.setStatus("FINALIZADA");
        venda.setDataCadastro(LocalDateTime.now());

        vendaDAO.save(venda);

        int sequencia = 1;
        for (ItemVenda item : itens) {
            Produto produto = produtoDAO.findById(item.getIdProduto())
                    .orElseThrow(() -> new IllegalStateException(
                            "Produto não encontrado: id=" + item.getIdProduto()));

            if (produto.isControlaEstoque() && produto.getEstoqueAtual() < item.getQuantidade()) {
                throw new IllegalStateException("Estoque insuficiente para '" + produto.getDescricao()
                        + "' (disponível: " + produto.getEstoqueAtual() + ").");
            }

            item.setIdVenda(venda.getIdVenda());
            item.setSequencia(sequencia++);
            espelharTributacao(item, produto);
            itemVendaDAO.save(item);

            if (produto.isControlaEstoque()) {
                produtoDAO.ajustarEstoque(produto.getIdProduto(), -item.getQuantidade());
                produtoDAO.registrarMovimentacao(produto.getIdProduto(), "SAIDA",
                        item.getQuantidade(), "VENDA", venda.getIdVenda(),
                        venda.getIdOperador());
            }
        }

        vendaDAO.registrarPagamento(venda.getIdVenda(),
                formaPagamento.getIdFormaPagamento(), valorTotal);

        log.info("Venda id={} finalizada: {} itens, total R$ {}",
                venda.getIdVenda(), itens.size(), valorTotal);

        return venda;
    }

    /**
     * Cancela uma venda finalizada: muda o status, registra o cancelamento
     * para auditoria e devolve os itens ao estoque.
     */
    @Transactional
    public void cancelarVenda(Venda venda, String motivo) {
        if (!"FINALIZADA".equals(venda.getStatus())) {
            throw new IllegalStateException("Apenas vendas finalizadas podem ser canceladas.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Informe o motivo do cancelamento.");
        }

        Usuario operador = SessionManager.getUsuarioAtual();
        if (operador == null) {
            throw new IllegalStateException("É necessário um usuário logado para cancelar a venda.");
        }

        venda.setStatus("CANCELADA");
        venda.setObservacao(venda.getObservacao() != null && !venda.getObservacao().isBlank()
                ? venda.getObservacao() + " | Cancelada: " + motivo
                : "Cancelada: " + motivo);
        vendaDAO.update(venda);

        vendaDAO.registrarCancelamento(venda.getIdVenda(), operador.getIdUsuario(), motivo);

        for (ItemVenda item : itemVendaDAO.findByVenda(venda.getIdVenda())) {
            Produto produto = produtoDAO.findByIdIncludingInactive(item.getIdProduto()).orElse(null);
            if (produto != null && produto.isControlaEstoque()) {
                produtoDAO.ajustarEstoque(produto.getIdProduto(), item.getQuantidade());
                produtoDAO.registrarMovimentacao(produto.getIdProduto(), "ENTRADA",
                        item.getQuantidade(), "CANCELAMENTO VENDA", venda.getIdVenda(),
                        operador.getIdUsuario());
            }
        }

        log.info("Venda id={} cancelada por usuário id={}: {}",
                venda.getIdVenda(), operador.getIdUsuario(), motivo);
    }

    /**
     * Copia os dados fiscais para o item no momento da venda (histórico imutável).
     * Usa o motor de tributação (produto → categoria → NCM); se nada estiver
     * configurado, cai nos campos fiscais legados do próprio produto.
     */
    private void espelharTributacao(ItemVenda item, Produto produto) {
        BigDecimal base = item.getTotalItem() != null ? item.getTotalItem() : BigDecimal.ZERO;

        try {
            TributacaoPerfil t = tributacaoService.resolverTributacao(produto.getIdProduto());

            item.setCfop(t.getCfopVenda());
            item.setNcm(t.getNcm() != null ? t.getNcm() : produto.getNcm());
            item.setCest(t.getCest() != null ? t.getCest() : produto.getCest());
            item.setCstIcms(t.getCstIcms());
            item.setCsosn(t.getCsosn());
            item.setAliqIcms(t.getAliqIcms());
            item.setCstPis(t.getCstPis());
            item.setAliqPis(t.getAliqPis());
            item.setCstCofins(t.getCstCofins());
            item.setAliqCofins(t.getAliqCofins());
        } catch (Exception e) {
            log.warn("Tributação não resolvida para produto id={} ({}). Usando campos fiscais do produto.",
                    produto.getIdProduto(), e.getMessage());

            item.setCfop(produto.getCfopVenda());
            item.setNcm(produto.getNcm());
            item.setCest(produto.getCest());
            item.setCstIcms(produto.getCstIcms());
            item.setCsosn(produto.getCsosn());
            item.setAliqIcms(produto.getAliqIcms());
            item.setCstPis(produto.getCstPis());
            item.setAliqPis(produto.getAliqPis());
            item.setCstCofins(produto.getCstCofins());
            item.setAliqCofins(produto.getAliqCofins());
        }

        item.setBaseIcms(base);
        item.setValorIcms(calcularImposto(base, item.getAliqIcms()));
        item.setValorPis(calcularImposto(base, item.getAliqPis()));
        item.setValorCofins(calcularImposto(base, item.getAliqCofins()));
    }

    private BigDecimal calcularImposto(BigDecimal base, BigDecimal aliquota) {
        if (base == null || aliquota == null) return null;
        return base.multiply(aliquota)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }
}
