package test;

import main.database.DAOs.ProdutoDAO;
import main.models.Produto;

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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("test")
@Transactional
class ProdutoDAOTest {

    @Autowired
    private ProdutoDAO produtoDAO;
    
    @Test
    void deveSalvarERecuperarProduto() {
        Produto p = new Produto();
        p.setDescricao("Produto Teste CI");
        p.setEstoqueAtual(0);
        p.setAtivo(true);
        p.setPermiteFracionamento(false);
        p.setControlaEstoque(true);
        p.setBalanca(false);

        Produto salvo = produtoDAO.save(p);

        assertNotNull(salvo.getIdProduto(), "O ID deve ser gerado pelo banco");
        assertTrue(salvo.getIdProduto() > 0);
        
        Optional<Produto> encontrado = produtoDAO.findById(salvo.getIdProduto());
        assertTrue(encontrado.isPresent());
        assertEquals("Produto Teste CI", encontrado.get().getDescricao());
    }

    @Test
    void deveDeletarProduto() {
        Produto p = new Produto();
        p.setDescricao("Produto Para Deletar");
        p.setEstoqueAtual(0);
        p.setAtivo(true);
        p.setPermiteFracionamento(false);
        p.setControlaEstoque(false);
        p.setBalanca(false);
        
        Produto salvo = produtoDAO.save(p);
        boolean deletado = produtoDAO.deleteById(salvo.getIdProduto());
        
        assertTrue(deletado);
        assertTrue(produtoDAO.findById(salvo.getIdProduto()).isEmpty());
    }
    
    private static final Logger log = LoggerFactory.getLogger(ProdutoDAOTest.class);

    @BeforeEach
    void antes(TestInfo info) {
        log.info(">>> Iniciando: {}", info.getDisplayName());
    }
    
    @AfterEach
    void depois(TestInfo info) {
        log.info("<<< Finalizado (rollback): {}", info.getDisplayName());
    }
}