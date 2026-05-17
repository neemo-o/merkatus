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
import main.database.DAOs.FornecedorDAO;
import main.models.Fornecedor;

@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("test")
@Transactional
class FornecedorDAOTest {

    private static final Logger log = LoggerFactory.getLogger(FornecedorDAOTest.class);

    @Autowired
    private FornecedorDAO fornecedorDAO;

    @BeforeEach
    void antes(TestInfo info) {
        log.info(">>> Iniciando: {}", info.getDisplayName());
    }

    @AfterEach
    void depois(TestInfo info) {
        log.info("<<< Finalizado (rollback): {}", info.getDisplayName());
    }

    // CNPJ tem 14 dígitos — gera um único por teste para evitar unique constraint
    private String cnpjUnico() {
        return UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 14);
    }

    @Test
    void deveSalvarERecuperar() {
        Fornecedor f = TestFactory.fornecedor(cnpjUnico());
        f.setRazaoSocial("Fornecedor Teste SA");
        f.setNomeFantasia("Fornecedor Teste");
        f.setCnpj(cnpjUnico());
        f.setAtivo(true);

        Fornecedor salvo = fornecedorDAO.save(f);

        assertNotNull(salvo.getIdFornecedor());
        assertTrue(salvo.getIdFornecedor() > 0);

        Optional<Fornecedor> encontrado = fornecedorDAO.findById(salvo.getIdFornecedor());
        assertTrue(encontrado.isPresent());
        assertEquals("Fornecedor Teste SA", encontrado.get().getRazaoSocial());
    }

    @Test
    void deveAtualizarFornecedor() {
        Fornecedor f = TestFactory.fornecedor(cnpjUnico());
f.setRazaoSocial("Fornecedor Teste SA");
        f.setCnpj(cnpjUnico());
        f.setAtivo(true);

        Fornecedor salvo = fornecedorDAO.save(f);
        salvo.setRazaoSocial("Atualizada SA");
        salvo.setTelefone("7599999999");

        boolean atualizado = fornecedorDAO.update(salvo);

        assertTrue(atualizado);
        Optional<Fornecedor> encontrado = fornecedorDAO.findById(salvo.getIdFornecedor());
        assertTrue(encontrado.isPresent());
        assertEquals("Atualizada SA", encontrado.get().getRazaoSocial());
    }

    @Test
    void deveDeletar() {
        Fornecedor f = TestFactory.fornecedor(cnpjUnico());
        f.setRazaoSocial("Para Deletar SA");
        f.setAtivo(true);

        Fornecedor salvo = fornecedorDAO.save(f);
        boolean deletado = fornecedorDAO.deleteById(salvo.getIdFornecedor());

        assertTrue(deletado);
        assertTrue(fornecedorDAO.findById(salvo.getIdFornecedor()).isEmpty());
    }
}
