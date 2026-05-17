package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import main.database.DAOs.UnidadeMedidaDAO;
import main.models.UnidadeMedida;

@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("test")
@Transactional
class UnidadeMedidaDAOTest {

    private static final Logger log = LoggerFactory.getLogger(UnidadeMedidaDAOTest.class);

    @Autowired
    private UnidadeMedidaDAO unidadeMedidaDAO;

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
        String siglaUnica = "T" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        // gera algo como "T3AF2" — diferente a cada execução

        UnidadeMedida u = new UnidadeMedida();
        u.setSigla(siglaUnica);
        u.setDescricao("Teste unitário");

        UnidadeMedida salvo = unidadeMedidaDAO.save(u);

        assertNotNull(salvo.getIdUnidade());
        Optional<UnidadeMedida> encontrado = unidadeMedidaDAO.findById(salvo.getIdUnidade());
        assertTrue(encontrado.isPresent());
        assertEquals(siglaUnica, encontrado.get().getSigla());
    }

    @Test
    void deveListarTodos() {
        String s1 = "T" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String s2 = "T" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        UnidadeMedida u1 = new UnidadeMedida();
        u1.setSigla(s1);
        u1.setDescricao("Teste lista 1");

        UnidadeMedida u2 = new UnidadeMedida();
        u2.setSigla(s2);
        u2.setDescricao("Teste lista 2");

        unidadeMedidaDAO.save(u1);
        unidadeMedidaDAO.save(u2);

        List<UnidadeMedida> todos = unidadeMedidaDAO.findAll();
        assertTrue(todos.size() >= 2);
    }

    @Test
    void deveDeletar() {
        String siglaUnica = "T" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        UnidadeMedida u = new UnidadeMedida();
        u.setSigla(siglaUnica);
        u.setDescricao("Teste delete");

        UnidadeMedida salvo = unidadeMedidaDAO.save(u);
        boolean deletado = unidadeMedidaDAO.deleteById(salvo.getIdUnidade());

        assertTrue(deletado);
        assertTrue(unidadeMedidaDAO.findById(salvo.getIdUnidade()).isEmpty());
    }
}
