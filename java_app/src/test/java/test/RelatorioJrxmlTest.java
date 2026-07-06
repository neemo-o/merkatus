package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;

/**
 * Garante que os templates .jrxml compilam — erros de estrutura ou de
 * expressão só apareceriam em runtime ao gerar o PDF.
 */
class RelatorioJrxmlTest {

    @Test
    void deveCompilarRelatorioEstoque() throws Exception {
        var stream = getClass().getResourceAsStream("/main/view/RelatorioEstoque.jrxml");
        assertNotNull(stream, "RelatorioEstoque.jrxml não encontrado no classpath");
        JasperReport report = JasperCompileManager.compileReport(stream);
        assertNotNull(report);
    }

    @Test
    void deveCompilarRelatorioVendas() throws Exception {
        var stream = getClass().getResourceAsStream("/main/view/RelatorioVendas.jrxml");
        assertNotNull(stream, "RelatorioVendas.jrxml não encontrado no classpath");
        JasperReport report = JasperCompileManager.compileReport(stream);
        assertNotNull(report);
    }
}
