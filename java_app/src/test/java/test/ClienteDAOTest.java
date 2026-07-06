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
        // Gera dígitos a partir de nanoTime (não de um UUID filtrado, que pode
        // sobrar com menos de 14 dígitos numéricos e estourar o substring)
        String digits = Long.toString(Math.abs(System.nanoTime()))
                + String.valueOf((int) (Math.random() * 1_000_000));
        return digits.substring(digits.length() - 14);
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
    void deveSalvarPessoaFisicaComCpf() {
        Cliente c = TestFactory.cliente(cnpjUnico().substring(0, 11)); // CPF: 11 dígitos
        c.setTipoPessoa("F");
        c.setRazaoSocial("João da Silva");
        c.setStatusCliente("PAGO");

        Cliente salvo = clienteDAO.save(c);

        Optional<Cliente> encontrado = clienteDAO.findById(salvo.getIdCliente());
        assertTrue(encontrado.isPresent());
        assertEquals("F", encontrado.get().getTipoPessoa());
        assertEquals("João da Silva", encontrado.get().getRazaoSocial());
        assertEquals(11, encontrado.get().getCnpj().length());
    }

    @Test
    void deveRejeitarDocumentoInconsistenteComTipoPessoa() {
        // CPF de 11 dígitos marcado como pessoa jurídica (exige 14) — deve violar o CHECK do banco
        Cliente c = TestFactory.cliente(cnpjUnico().substring(0, 11));
        c.setTipoPessoa("J");
        c.setRazaoSocial("Documento Inconsistente SA");
        c.setStatusCliente("PAGO");

        assertThrows(org.springframework.dao.DataIntegrityViolationException.class,
                () -> clienteDAO.save(c));
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
