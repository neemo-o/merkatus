package main.database;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.database.DAOs.CategoriaDAO;
import main.database.DAOs.NcmTributacaoDAO;
import main.database.DAOs.ProdutoDAO;
import main.database.DAOs.TributacaoPerfilDAO;
import main.models.Produto;
import main.models.TributacaoPerfil;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TributacaoService {

    private final ProdutoDAO           produtoDAO;
    private final TributacaoPerfilDAO  tributacaoPerfilDAO;
    private final CategoriaDAO         categoriaDAO;
    private final NcmTributacaoDAO     ncmTributacaoDAO;

    /**
     * Resolve a tributação de um produto seguindo a cadeia de prioridade:
     *
     * 1. Tributação específica do produto (id_tributacao no produto)
     * 2. Tributação padrão da categoria do produto
     * 3. Lookup pelo NCM do produto na tabela ncm_tributacao
     * 4. Lança exceção com mensagem clara
     */
    public TributacaoPerfil resolverTributacao(Integer idProduto) {

        Produto produto = produtoDAO.findById(idProduto)
            .orElseThrow(() -> new IllegalArgumentException(
                "Produto não encontrado: id=" + idProduto
            ));

        log.debug("Resolvendo tributação — produto id={} descricao='{}'",
            idProduto, produto.getDescricao());

        return resolverPorProduto(produto)
            .or(() -> resolverPorCategoria(produto))
            .or(() -> resolverPorNcm(produto))
            .orElseThrow(() -> new IllegalStateException(
                "Produto '%s' (id=%d) não possui tributação definida. ".formatted(
                    produto.getDescricao(), idProduto) +
                "Configure a tributação do produto, da categoria, ou mapeie o NCM '%s'."
                .formatted(produto.getNcm())
            ));
    }

    // ==============================
    // Prioridade 1 — tributação direta no produto
    // ==============================

    private Optional<TributacaoPerfil> resolverPorProduto(Produto produto) {
        if (produto.getIdTributacao() == null) {
            return Optional.empty();
        }

        try {
            return tributacaoPerfilDAO.findById(produto.getIdTributacao());
        } catch (Exception e) {
            log.warn("Erro ao buscar tributação do produto id={}: {}",
                produto.getIdProduto(), e.getMessage());
            return Optional.empty();
        }
    }

    // ==============================
    // Prioridade 2 — tributação padrão da categoria
    // ==============================

    private Optional<TributacaoPerfil> resolverPorCategoria(Produto produto) {
        if (produto.getIdCategoria() == null) {
            log.debug("Produto id={} sem categoria, pulando fallback de categoria.",
                produto.getIdProduto());
            return Optional.empty();
        }

        try {
            return categoriaDAO.findByIdComTributacao(produto.getIdCategoria())
                .flatMap(categoria -> {
                    if (categoria.getIdTributacaoPadrao() == null) {
                        log.debug("Categoria id={} sem tributação padrão definida.",
                            categoria.getIdCategoria());
                        return Optional.empty();
                    }

                    try {
                        Optional<TributacaoPerfil> resultado =
                            tributacaoPerfilDAO.findById(categoria.getIdTributacaoPadrao());

                        resultado.ifPresent(t ->
                            log.debug("Tributação resolvida pela origem: CATEGORIA id={} nome='{}'",
                                categoria.getIdCategoria(), categoria.getNome())
                        );
                        return resultado;

                    } catch (Exception e) {
                        log.warn("Erro ao buscar tributação da categoria: {}", e.getMessage());
                        return Optional.empty();
                    }
                });

        } catch (Exception e) {
            log.warn("Erro ao buscar categoria id={}: {}", produto.getIdCategoria(), e.getMessage());
            return Optional.empty();
        }
    }

    // ==============================
    // Prioridade 3 — lookup pelo NCM
    // ==============================

    private Optional<TributacaoPerfil> resolverPorNcm(Produto produto) {
        if (produto.getNcm() == null || produto.getNcm().isBlank()) {
            log.debug("Produto id={} sem NCM, fallback por NCM indisponível.",
                produto.getIdProduto());
            return Optional.empty();
        }

        log.debug("Tentando fallback por NCM='{}'", produto.getNcm());

        try {
            return ncmTributacaoDAO.findByNcm(produto.getNcm())
                .flatMap(ncmTributacao -> {
                    try {
                        Optional<TributacaoPerfil> resultado =
                            tributacaoPerfilDAO.findById(ncmTributacao.getIdTributacao());

                        resultado.ifPresent(t ->
                            log.debug("Tributação resolvida pela origem: NCM='{}'", produto.getNcm())
                        );
                        return resultado;

                    } catch (Exception e) {
                        log.warn("Erro ao buscar tributação por NCM '{}': {}",
                            produto.getNcm(), e.getMessage());
                        return Optional.empty();
                    }
                });

        } catch (Exception e) {
            log.warn("Erro ao consultar ncm_tributacao para NCM='{}': {}",
                produto.getNcm(), e.getMessage());
            return Optional.empty();
        }
    }
}