package test;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import main.database.DAOs.FuncionarioDAO;
import main.models.Funcionario;

@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("test")
@Transactional
class FuncionarioDAOTest {

    private static final Logger log = LoggerFactory.getLogger(FuncionarioDAOTest.class);

    @Autowired
    private FuncionarioDAO funcionarioDAO;

    @BeforeEach
    void antes(TestInfo info) {
        log.info(">>> Iniciando: {}", info.getDisplayName());
    }

    @AfterEach
    void depois(TestInfo info) {
        log.info("<<< Finalizado (rollback): {}", info.getDisplayName());
    }

    // CPF tem 11 dígitos
    private String cpfUnico() {
        return UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 11);
    }

    @Test
    void deveSalvarERecuperar() {
        Funcionario f = TestFactory.funcionario(cpfUnico());
        f.setNome("Funcionario Teste");
        f.setCpf(cpfUnico());
        f.setCargo("Operador");
        f.setDataAdmissao(LocalDate.now());
        f.setAtivo(true);

        Funcionario salvo = funcionarioDAO.save(f);

        assertNotNull(salvo.getIdFuncionario());
        assertTrue(salvo.getIdFuncionario() > 0);

        Optional<Funcionario> encontrado = funcionarioDAO.findById(salvo.getIdFuncionario());
        assertTrue(encontrado.isPresent());
        assertEquals("Funcionario Teste", encontrado.get().getNome());
        assertEquals("Operador", encontrado.get().getCargo());
    }

    @Test
    void deveAtualizarFuncionario() {
        Funcionario f = TestFactory.funcionario(cpfUnico());
        f.setNome("Original");
        f.setCargo("Caixa");
        f.setDataAdmissao(LocalDate.now());
        f.setAtivo(true);

        Funcionario salvo = funcionarioDAO.save(f);
        salvo.setNome("Atualizado");
        salvo.setCargo("Gerente");

        boolean atualizado = funcionarioDAO.update(salvo);

        assertTrue(atualizado);
        Optional<Funcionario> encontrado = funcionarioDAO.findById(salvo.getIdFuncionario());
        assertTrue(encontrado.isPresent());
        assertEquals("Atualizado", encontrado.get().getNome());
        assertEquals("Gerente", encontrado.get().getCargo());
    }

    @Test
    void deveDeletar() {
        Funcionario f = TestFactory.funcionario(cpfUnico());
        f.setNome("Para Deletar");
        f.setCargo("Teste");
        f.setDataAdmissao(LocalDate.now());
        f.setAtivo(true);

        Funcionario salvo = funcionarioDAO.save(f);
        boolean deletado = funcionarioDAO.deleteById(salvo.getIdFuncionario());

        assertTrue(deletado);
        assertTrue(funcionarioDAO.findById(salvo.getIdFuncionario()).isEmpty());
    }
}
