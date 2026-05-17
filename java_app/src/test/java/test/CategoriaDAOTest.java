package test;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import main.database.DAOs.CategoriaDAO;
import main.models.Categoria;

@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("test")
@Transactional
class CategoriaDAOTest {

    private static final Logger log = LoggerFactory.getLogger(CategoriaDAOTest.class);

    @Autowired
    private CategoriaDAO categoriaDAO;

    @BeforeEach
    void antes(TestInfo info) {
        log.info(">>> Iniciando: {}", info.getDisplayName());
    }

    @AfterEach
    void depois(TestInfo info) {
        log.info("<<< Finalizado (rollback): {}", info.getDisplayName());
    }

    @Test
    void deveSalvarERecuperar() {
        Categoria c = new Categoria();
        c.setNome("Categoria Teste");
        c.setAtivo(true);

        Categoria salvo = categoriaDAO.save(c);

        assertNotNull(salvo.getIdCategoria());
        assertTrue(salvo.getIdCategoria() > 0);

        Optional<Categoria> encontrado = categoriaDAO.findById(salvo.getIdCategoria());
        assertTrue(encontrado.isPresent());
        assertEquals("Categoria Teste", encontrado.get().getNome());
    }

    @Test
    void deveSalvarSubcategoria() {
        // cria categoria pai
        Categoria pai = new Categoria();
        pai.setNome("Pai Teste");
        pai.setAtivo(true);
        Categoria paiSalvo = categoriaDAO.save(pai);

        // cria subcategoria apontando para o pai
        Categoria filho = new Categoria();
        filho.setNome("Filho Teste");
        filho.setParentId(paiSalvo.getIdCategoria());
        filho.setAtivo(true);
        Categoria filhoSalvo = categoriaDAO.save(filho);

        assertNotNull(filhoSalvo.getIdCategoria());
        Optional<Categoria> encontrado = categoriaDAO.findById(filhoSalvo.getIdCategoria());
        assertTrue(encontrado.isPresent());
        assertEquals(paiSalvo.getIdCategoria(), encontrado.get().getParentId());
    }

    @Test
    void deveBuscarRaizes() {
        Categoria raiz = new Categoria();
        raiz.setNome("Raiz Teste");
        raiz.setAtivo(true);
        // parent_id null = raiz
        categoriaDAO.save(raiz);

        List<Categoria> raizes = categoriaDAO.findRaizes();
        assertFalse(raizes.isEmpty());
        // todas as raizes retornadas devem ter parent_id nulo
        assertTrue(raizes.stream().allMatch(r -> r.getParentId() == null));
    }

    @Test
    void deveDeletar() {
        Categoria c = new Categoria();
        c.setNome("Para Deletar");
        c.setAtivo(true);

        Categoria salvo = categoriaDAO.save(c);
        boolean deletado = categoriaDAO.deleteById(salvo.getIdCategoria());

        assertTrue(deletado);
        assertTrue(categoriaDAO.findById(salvo.getIdCategoria()).isEmpty());
    }
}
