package main.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import main.database.DAOs.CategoriaDAO;
import main.database.DAOs.ProdutoDAO;
import main.database.DAOs.TributacaoPerfilDAO;
import main.models.Categoria;
import main.models.Produto;
import main.models.TributacaoPerfil;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ProdutoService {

    private final ProdutoDAO produtoDAO;
    private final CategoriaDAO categoriaDAO;
    private final TributacaoPerfilDAO tributacaoPerfilDAO;

    // Construtor atualizado com as novas dependências
    public ProdutoService(ProdutoDAO produtoDAO, 
                          CategoriaDAO categoriaDAO, 
                          TributacaoPerfilDAO tributacaoPerfilDAO) {
        this.produtoDAO = produtoDAO;
        this.categoriaDAO = categoriaDAO;
        this.tributacaoPerfilDAO = tributacaoPerfilDAO;
    }

    /**
     * Busca o perfil de tributação associado à categoria padrão.
     * Usado para auto-preenchimento no formulário de produto.
     */
    public TributacaoPerfil buscarTributacaoPorCategoria(Integer idCategoria) {
        if (idCategoria == null) return null;
        
        Categoria categoria = categoriaDAO.findById(idCategoria).orElse(null);
        if (categoria == null || categoria.getIdTributacaoPadrao() == null) {
            return null;
        }
        
        return tributacaoPerfilDAO.findById(categoria.getIdTributacaoPadrao()).orElse(null);
    }

    // ==============================
    // MÉTODOS EXISTENTES (mantidos)
    // ==============================

    public BigDecimal calcularMargem(BigDecimal precoCusto, BigDecimal precoVenda) {
        if (precoCusto == null || precoVenda == null) return null;
        if (precoCusto.compareTo(BigDecimal.ZERO) == 0) return null;

        return precoVenda.subtract(precoCusto)
                .divide(precoCusto, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calcularPrecoVenda(BigDecimal precoCusto, BigDecimal margem) {
        if (precoCusto == null || margem == null) return null;
        if (precoCusto.compareTo(BigDecimal.ZERO) == 0) return null;

        return precoCusto.multiply(
                BigDecimal.ONE.add(margem.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP))
        ).setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional
    public boolean excluirProduto(Integer id) {
        Produto produto = produtoDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado ou já inativo"));

        validarSePodeSerExcluido(produto);
        produto.marcarComoInativo();
        return produtoDAO.update(produto);
    }

    @Transactional
    public boolean reativarProduto(Integer id) {
        Produto produto = produtoDAO.findByIdIncludingInactive(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        if (produto.isAtivo()) {
            throw new RuntimeException("Produto já está ativo");
        }

        produto.marcarComoAtivo();
        return produtoDAO.update(produto);
    }

    private void validarSePodeSerExcluido(Produto produto) {
        if (produto.getEstoqueAtual() > 0) {
            throw new RuntimeException("Não é possível excluir o produto '" + produto.getDescricao() + 
                    "' pois possui estoque atual: " + produto.getEstoqueAtual());
        }
    }

    public boolean podeSerExcluido(Integer id) {
        try {
            Produto produto = produtoDAO.findById(id).orElse(null);
            if (produto == null) return false;
            validarSePodeSerExcluido(produto);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
}