package test;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import main.services.ProdutoService;

class ProdutoServiceTest {

    private static final Logger log = LoggerFactory.getLogger(ProdutoServiceTest.class);
    private ProdutoService service;

    @BeforeEach
    void setup(TestInfo info) {
        service = new ProdutoService();
        log.info(">>> Iniciando: {}", info.getDisplayName());
    }

    @AfterEach
    void depois(TestInfo info) {
        log.info("<<< Finalizado: {}", info.getDisplayName());
    }

    // ==============================
    // calcularMargem
    // ==============================

    @Test
    void deveCalcularMargemCorretamente() {
        // (90 - 60) / 60 * 100 = 50.00%
        BigDecimal margem = service.calcularMargem(
            new BigDecimal("60"), new BigDecimal("90"));
        assertEquals(new BigDecimal("50.00"), margem);
    }

    @Test
    void deveCalcularMargemComCasasDecimais() {
        // (10 - 7) / 7 * 100 = 42.86%
        BigDecimal margem = service.calcularMargem(
            new BigDecimal("7"), new BigDecimal("10"));
        assertEquals(new BigDecimal("42.86"), margem);
    }

    @Test
    void deveRetornarNullSeCustoZero() {
        BigDecimal margem = service.calcularMargem(
            BigDecimal.ZERO, new BigDecimal("90"));
        assertNull(margem);
    }

    @Test
    void deveRetornarNullSeCustoNull() {
        assertNull(service.calcularMargem(null, new BigDecimal("90")));
    }

    @Test
    void deveRetornarNullSeVendaNull() {
        assertNull(service.calcularMargem(new BigDecimal("60"), null));
    }

    // ==============================
    // calcularPrecoVenda
    // ==============================

    @Test
    void deveCalcularPrecoVendaCorretamente() {
        // 60 * (1 + 50/100) = 90.00
        BigDecimal venda = service.calcularPrecoVenda(
            new BigDecimal("60"), new BigDecimal("50"));
        assertEquals(new BigDecimal("90.00"), venda);
    }

    @Test
    void deveCalcularPrecoVendaComMargem30() {
        // 100 * (1 + 30/100) = 130.00
        BigDecimal venda = service.calcularPrecoVenda(
            new BigDecimal("100"), new BigDecimal("30"));
        assertEquals(new BigDecimal("130.00"), venda);
    }

    @Test
    void deveRetornarNullSeMargemNull() {
        assertNull(service.calcularPrecoVenda(new BigDecimal("60"), null));
    }
}