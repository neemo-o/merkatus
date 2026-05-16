package test;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import main.database.DAOs.EnderecoDAO;
import main.models.Endereco;

@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("test")
@Transactional
class EnderecoDAOTest {

    private static final Logger log = LoggerFactory.getLogger(EnderecoDAOTest.class);

    @Autowired
    private EnderecoDAO enderecoDAO;

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
        Endereco e = TestFactory.endereco();
        e.setLogradouro("Rua Teste");
        e.setNumero("123");
        e.setBairro("Centro");
        e.setCidade("Alagoinhas");
        e.setEstado("BA");
        e.setCep("48400000");

        Endereco salvo = enderecoDAO.save(e);

        assertNotNull(salvo.getIdEndereco());
        assertTrue(salvo.getIdEndereco() > 0);

        Optional<Endereco> encontrado = enderecoDAO.findById(salvo.getIdEndereco());
        assertTrue(encontrado.isPresent());
        assertEquals("Rua Teste", encontrado.get().getLogradouro());
        assertEquals("Alagoinhas", encontrado.get().getCidade());
    }

    @Test
    void deveAtualizarEndereco() {
        Endereco e = TestFactory.endereco();
        e.setLogradouro("Rua Original");
        e.setNumero("1");
        e.setBairro("Bairro");
        e.setCidade("Cidade");
        e.setEstado("BA");
        e.setCep("48000000");

        Endereco salvo = enderecoDAO.save(e);
        salvo.setLogradouro("Rua Atualizada");
        salvo.setNumero("999");

        boolean atualizado = enderecoDAO.update(salvo);

        assertTrue(atualizado);
        Optional<Endereco> encontrado = enderecoDAO.findById(salvo.getIdEndereco());
        assertTrue(encontrado.isPresent());
        assertEquals("Rua Atualizada", encontrado.get().getLogradouro());
        assertEquals("999", encontrado.get().getNumero());
    }

    @Test
    void deveDeletar() {
        Endereco e = TestFactory.endereco();
        e.setLogradouro("Rua Delete");
        e.setNumero("0");
        e.setBairro("Bairro");
        e.setCidade("Cidade");
        e.setEstado("BA");
        e.setCep("00000000");

        Endereco salvo = enderecoDAO.save(e);
        boolean deletado = enderecoDAO.deleteById(salvo.getIdEndereco());

        assertTrue(deletado);
        assertTrue(enderecoDAO.findById(salvo.getIdEndereco()).isEmpty());
    }
}
