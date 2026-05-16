package test;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import main.database.DAOs.NcmTributacaoDAO;
import main.database.DAOs.TributacaoPerfilDAO;
import main.models.NcmTributacao;
import main.models.TributacaoPerfil;

@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("test")
@Transactional
class NcmTributacaoDAOTest {

    private static final Logger log = LoggerFactory.getLogger(NcmTributacaoDAOTest.class);

    @Autowired
    private NcmTributacaoDAO ncmTributacaoDAO;

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

    // NCM tem 8 dígitos — gera um único por teste
    private String ncmUnico() {
        return String.valueOf(10000000 + (int) (Math.random() * 89999999));
    }

    @Test
    void deveSalvarERecuperar() {
        String ncm = ncmUnico();
        TributacaoPerfil perfil = tributacaoPerfilDAO.save(TestFactory.tributacaoPerfil("Perfil NCM"));
        NcmTributacao n = TestFactory.ncmTributacao(ncmUnico(), perfil.getIdTributacao());
        n.setNcm(ncm);
        n.setDescricaoNcm("Produto Teste NCM");

        NcmTributacao salvo = ncmTributacaoDAO.save(n);

        assertNotNull(salvo.getIdNcmTributacao());

        Optional<NcmTributacao> encontrado = ncmTributacaoDAO.findById(salvo.getIdNcmTributacao());
        assertTrue(encontrado.isPresent());
        assertEquals(ncm, encontrado.get().getNcm());
    }

    @Test
    void deveBuscarPorNcm() {
        TributacaoPerfil perfil = tributacaoPerfilDAO.save(TestFactory.tributacaoPerfil("Perfil Busca NCM"));

        String ncm = ncmUnico();
        NcmTributacao n = TestFactory.ncmTributacao(ncm, perfil.getIdTributacao());
        ncmTributacaoDAO.save(n);

        Optional<NcmTributacao> encontrado = ncmTributacaoDAO.findByNcm(ncm);
        assertTrue(encontrado.isPresent());
        assertEquals(ncm, encontrado.get().getNcm());
    }

    @Test
    void deveSalvarComTributacao() {
        // cria o perfil de tributação primeiro
        TributacaoPerfil perfil = tributacaoPerfilDAO.save(
                TestFactory.tributacaoPerfil("Perfil NCM Teste"));
        perfil.setNome("Perfil NCM Teste");
        perfil.setCstIcms("00");
        perfil.setCsosn("400");
        perfil.setAliqIcms(new BigDecimal("12.00"));
        perfil.setCfopVenda("5102");
        perfil.setCstPis("07");
        perfil.setCstCofins("07");
        perfil.setAliqPis(new BigDecimal("0.65"));
        perfil.setAliqCofins(new BigDecimal("3.00"));
        perfil.setCstIpi("99");
        perfil.setAliqIpi(BigDecimal.ZERO);
        perfil.setAtivo(true);
        TributacaoPerfil perfilSalvo = tributacaoPerfilDAO.save(perfil);

        String ncm = ncmUnico();
        NcmTributacao n = TestFactory.ncmTributacao(ncm, perfilSalvo.getIdTributacao());

        NcmTributacao salvo = ncmTributacaoDAO.save(n);

        Optional<NcmTributacao> encontrado = ncmTributacaoDAO.findById(salvo.getIdNcmTributacao());
        assertTrue(encontrado.isPresent());
        assertEquals(perfilSalvo.getIdTributacao(), encontrado.get().getIdTributacao());
    }
}
