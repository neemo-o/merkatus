package test;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import main.database.DAOs.ClienteDAO;
import main.models.Cliente;

@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("test")
@Transactional
class ClienteDAOTest {

    private static final Logger log = LoggerFactory.getLogger(ClienteDAOTest.class);

    @Autowired
    private ClienteDAO clienteDAO;

    @BeforeEach
    void antes(TestInfo info) {
        log.info(">>> Iniciando: {}", info.getDisplayName());
    }

    @AfterEach
    void depois(TestInfo info) {
        log.info("<<< Finalizado (rollback): {}", info.getDisplayName());
    }

    private String cnpjUnico() {
        return UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 14);
    }

    @Test
    void deveSalvarERecuperar() {
        Cliente c = TestFactory.cliente(cnpjUnico());
        c.setRazaoSocial("Cliente Teste SA");
        c.setNomeFantasia("Cliente Teste");
        c.setStatusCliente("PAGO");
        c.setAtivo(true);

        Cliente salvo = clienteDAO.save(c);

        assertNotNull(salvo.getIdCliente());
        assertTrue(salvo.getIdCliente() > 0);

        Optional<Cliente> encontrado = clienteDAO.findById(salvo.getIdCliente());
        assertTrue(encontrado.isPresent());
        assertEquals("Cliente Teste SA", encontrado.get().getRazaoSocial());
    }

    @Test
    void deveAtualizarCliente() {
        Cliente c = TestFactory.cliente(cnpjUnico());
        c.setRazaoSocial("Original SA");
        c.setStatusCliente("PAGO");
        c.setAtivo(true);

        Cliente salvo = clienteDAO.save(c);
        salvo.setRazaoSocial("Atualizada SA");
        salvo.setEmailCliente("teste@teste.com");

        boolean atualizado = clienteDAO.update(salvo);

        assertTrue(atualizado);
        Optional<Cliente> encontrado = clienteDAO.findById(salvo.getIdCliente());
        assertTrue(encontrado.isPresent());
        assertEquals("Atualizada SA", encontrado.get().getRazaoSocial());
    }

    @Test
    void deveDeletar() {
        Cliente c = TestFactory.cliente(cnpjUnico());
        c.setRazaoSocial("Para Deletar SA");
        c.setStatusCliente("PAGO");
        c.setAtivo(true);

        Cliente salvo = clienteDAO.save(c);
        boolean deletado = clienteDAO.deleteById(salvo.getIdCliente());

        assertTrue(deletado);
        assertTrue(clienteDAO.findById(salvo.getIdCliente()).isEmpty());
    }
}
