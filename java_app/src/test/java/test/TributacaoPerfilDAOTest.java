package test;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import main.database.DAOs.TributacaoPerfilDAO;
import main.models.TributacaoPerfil;

@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("test")
@Transactional
class TributacaoPerfilDAOTest {

    private static final Logger log = LoggerFactory.getLogger(TributacaoPerfilDAOTest.class);

    @Autowired
    private TributacaoPerfilDAO tributacaoPerfilDAO;

    @BeforeEach
    void antes(TestInfo info) {
        log.info(">>> Iniciando: {}", info.getDisplayName());
    }

    @AfterEach
    void depois(TestInfo info) {
        log.info("<<< Finalizado (rollback): {}", info.getDisplayName());
    }

    private TributacaoPerfil novoPerfil(String nome) {
        TributacaoPerfil t = TestFactory.tributacaoPerfil("Perfil Teste Save");
        t.setNome(nome);
        t.setDescricao("Perfil de teste");
        t.setCstIcms("00");
        t.setCsosn("400");
        t.setAliqIcms(new BigDecimal("12.00"));
        t.setCfopVenda("5102");
        t.setCstPis("07");
        t.setCstCofins("07");
        t.setAliqPis(new BigDecimal("0.65"));
        t.setAliqCofins(new BigDecimal("3.00"));
        t.setCstIpi("99");
        t.setAliqIpi(BigDecimal.ZERO);
        t.setAtivo(true);
        return t;
    }

    @Test
    void deveSalvarERecuperar() {
        TributacaoPerfil salvo = tributacaoPerfilDAO.save(novoPerfil("Perfil Teste Save"));

        assertNotNull(salvo.getIdTributacao());
        assertTrue(salvo.getIdTributacao() > 0);

        Optional<TributacaoPerfil> encontrado = tributacaoPerfilDAO.findById(salvo.getIdTributacao());
        assertTrue(encontrado.isPresent());
        assertEquals("Perfil Teste Save", encontrado.get().getNome());
    }

    @Test
    void deveBuscarAtivos() {
        tributacaoPerfilDAO.save(novoPerfil("Ativo Teste"));

        List<TributacaoPerfil> ativos = tributacaoPerfilDAO.findAllAtivos();
        assertFalse(ativos.isEmpty());
        assertTrue(ativos.stream().allMatch(TributacaoPerfil::isAtivo));
    }

    @Test
    void deveDeletar() {
        TributacaoPerfil salvo = tributacaoPerfilDAO.save(novoPerfil("Para Deletar"));

        boolean deletado = tributacaoPerfilDAO.deleteById(salvo.getIdTributacao());

        assertTrue(deletado);
        assertTrue(tributacaoPerfilDAO.findById(salvo.getIdTributacao()).isEmpty());
    }
}
