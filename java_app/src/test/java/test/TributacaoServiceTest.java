package test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import main.database.DAOs.*;
import main.models.*;
import main.services.TributacaoService;

@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("test")
@Transactional
class TributacaoServiceTest {

    private static final Logger log = LoggerFactory.getLogger(TributacaoServiceTest.class);

    @Autowired private TributacaoService tributacaoService;
    @Autowired private ProdutoDAO produtoDAO;
    @Autowired private TributacaoPerfilDAO tributacaoPerfilDAO;
    @Autowired private CategoriaDAO categoriaDAO;
    @Autowired private NcmTributacaoDAO ncmTributacaoDAO;

    @BeforeEach
    void antes(TestInfo info) {
        log.info(">>> Iniciando: {}", info.getDisplayName());
    }

    @AfterEach
    void depois(TestInfo info) {
        log.info("<<< Finalizado (rollback): {}", info.getDisplayName());
    }

    // ==============================
    // Prioridade 1 — tributação direta no produto
    // ==============================

    @Test
    void deveResolverTributacaoDiretaDoProduto() {
        TributacaoPerfil perfil = tributacaoPerfilDAO.save(
            TestFactory.tributacaoPerfil("Perfil Direto"));

        Produto p = new Produto();
        p.setDescricao("Produto com tributação direta");
        p.setEstoqueAtual(0);
        p.setAtivo(true);
        p.setPermiteFracionamento(false);
        p.setControlaEstoque(false);
        p.setBalanca(false);
        p.setIdTributacao(perfil.getIdTributacao()); // prioridade 1
        Produto salvo = produtoDAO.save(p);

        TributacaoPerfil resultado = tributacaoService.resolverTributacao(salvo.getIdProduto());

        assertNotNull(resultado);
        assertEquals(perfil.getIdTributacao(), resultado.getIdTributacao());
    }

    // ==============================
    // Prioridade 2 — tributação pela categoria
    // ==============================

    @Test
    void deveResolverTributacaoPelaCategoria() {
        TributacaoPerfil perfil = tributacaoPerfilDAO.save(
            TestFactory.tributacaoPerfil("Perfil Categoria"));

        Categoria categoria = new Categoria();
        categoria.setNome("Categoria Teste");
        categoria.setAtivo(true);
        categoria.setIdTributacaoPadrao(perfil.getIdTributacao());
        Categoria categoriaSalva = categoriaDAO.save(categoria);

        Produto p = new Produto();
        p.setDescricao("Produto sem tributação direta");
        p.setEstoqueAtual(0);
        p.setAtivo(true);
        p.setPermiteFracionamento(false);
        p.setControlaEstoque(false);
        p.setBalanca(false);
        p.setIdTributacao(null);   // sem tributação direta
        p.setIdCategoria(categoriaSalva.getIdCategoria()); // fallback categoria
        Produto salvo = produtoDAO.save(p);

        TributacaoPerfil resultado = tributacaoService.resolverTributacao(salvo.getIdProduto());

        assertNotNull(resultado);
        assertEquals(perfil.getIdTributacao(), resultado.getIdTributacao());
    }

    // ==============================
    // Prioridade 3 — tributação pelo NCM
    // ==============================

    @Test
    void deveResolverTributacaoPeloNcm() {
        TributacaoPerfil perfil = tributacaoPerfilDAO.save(
            TestFactory.tributacaoPerfil("Perfil NCM"));

        String ncm = "99887766";
        NcmTributacao ncmTributacao = TestFactory.ncmTributacao(ncm, perfil.getIdTributacao());
        ncmTributacaoDAO.save(ncmTributacao);

        Produto p = new Produto();
        p.setDescricao("Produto só com NCM");
        p.setEstoqueAtual(0);
        p.setAtivo(true);
        p.setPermiteFracionamento(false);
        p.setControlaEstoque(false);
        p.setBalanca(false);
        p.setIdTributacao(null);    // sem tributação direta
        p.setIdCategoria(null);     // sem categoria
        p.setNcm(ncm);              // fallback NCM
        Produto salvo = produtoDAO.save(p);

        TributacaoPerfil resultado = tributacaoService.resolverTributacao(salvo.getIdProduto());

        assertNotNull(resultado);
        assertEquals(perfil.getIdTributacao(), resultado.getIdTributacao());
    }

    // ==============================
    // Sem tributação — deve lançar exceção
    // ==============================

    @Test
    void deveLancarExcecaoSemTributacao() {
        Produto p = new Produto();
        p.setDescricao("Produto sem tributação nenhuma");
        p.setEstoqueAtual(0);
        p.setAtivo(true);
        p.setPermiteFracionamento(false);
        p.setControlaEstoque(false);
        p.setBalanca(false);
        p.setIdTributacao(null);
        p.setIdCategoria(null);
        p.setNcm(null);
        Produto salvo = produtoDAO.save(p);

        assertThrows(IllegalStateException.class, () ->
            tributacaoService.resolverTributacao(salvo.getIdProduto())
        );
    }
}