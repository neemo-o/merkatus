package main.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.database.DAOs.CaixaDAO;
import main.models.Caixa;
import main.models.Usuario;
import main.util.SessionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CaixaService {

    private final CaixaDAO caixaDAO;

    public Optional<Caixa> buscarCaixaAberto() {
        return caixaDAO.findAberto();
    }

    @Transactional
    public Caixa abrirCaixa(BigDecimal valorAbertura) {
        if (valorAbertura == null || valorAbertura.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Informe um valor de abertura válido (pode ser R$ 0,00).");
        }
        if (caixaDAO.findAberto().isPresent()) {
            throw new IllegalStateException("Já existe um caixa aberto. Feche-o antes de abrir outro.");
        }

        Usuario operador = exigirOperador();

        Caixa caixa = new Caixa();
        caixa.setIdTerminal(caixaDAO.obterOuCriarTerminalPadrao());
        caixa.setIdOperador(operador.getIdUsuario());
        caixa.setDataAbertura(LocalDateTime.now());
        caixa.setValorAbertura(valorAbertura);
        caixa.setStatus("ABERTO");

        caixaDAO.save(caixa);
        log.info("Caixa id={} aberto por usuário id={} com R$ {}",
                caixa.getIdCaixa(), operador.getIdUsuario(), valorAbertura);
        return caixa;
    }

    /** Sangria: retirada de dinheiro do caixa (ex: depósito, pagamento de entrega). */
    @Transactional
    public void registrarSangria(Caixa caixa, BigDecimal valor, String motivo) {
        registrarMovimento(caixa, "S", valor, motivo);
    }

    /** Suprimento: entrada de dinheiro no caixa (ex: reforço de troco). */
    @Transactional
    public void registrarSuprimento(Caixa caixa, BigDecimal valor, String motivo) {
        registrarMovimento(caixa, "U", valor, motivo);
    }

    private void registrarMovimento(Caixa caixa, String tipo, BigDecimal valor, String motivo) {
        validarCaixaAberto(caixa);
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Informe um valor maior que zero.");
        }
        Usuario operador = exigirOperador();
        caixaDAO.registrarSangriaSuprimento(caixa.getIdCaixa(), tipo, valor, motivo,
                operador.getIdUsuario());
    }

    /**
     * Valor esperado em dinheiro na gaveta:
     * abertura + suprimentos - sangrias + vendas em dinheiro do caixa.
     */
    public BigDecimal calcularValorSistema(Caixa caixa) {
        BigDecimal abertura = caixa.getValorAbertura() != null ? caixa.getValorAbertura() : BigDecimal.ZERO;
        BigDecimal suprimentos = caixaDAO.totalSangriasSuprimentos(caixa.getIdCaixa(), "U");
        BigDecimal sangrias = caixaDAO.totalSangriasSuprimentos(caixa.getIdCaixa(), "S");
        BigDecimal dinheiroVendas = caixaDAO.totalDinheiroVendas(caixa.getIdCaixa());
        return abertura.add(suprimentos).subtract(sangrias).add(dinheiroVendas);
    }

    /**
     * Fecha o caixa registrando o valor contado na gaveta e a diferença
     * em relação ao valor calculado pelo sistema.
     */
    @Transactional
    public Caixa fecharCaixa(Caixa caixa, BigDecimal valorContado, String observacao) {
        validarCaixaAberto(caixa);
        if (valorContado == null || valorContado.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Informe o valor contado na gaveta.");
        }
        exigirOperador();

        BigDecimal valorSistema = calcularValorSistema(caixa);

        caixa.setDataFechamento(LocalDateTime.now());
        caixa.setValorFechamento(valorContado);
        caixa.setValorSistema(valorSistema);
        caixa.setDiferenca(valorContado.subtract(valorSistema));
        caixa.setStatus("FECHADO");
        if (observacao != null && !observacao.isBlank()) {
            caixa.setObservacao(observacao);
        }

        caixaDAO.update(caixa);
        log.info("Caixa id={} fechado: sistema R$ {}, contado R$ {}, diferença R$ {}",
                caixa.getIdCaixa(), valorSistema, valorContado, caixa.getDiferenca());
        return caixa;
    }

    private void validarCaixaAberto(Caixa caixa) {
        if (caixa == null || !"ABERTO".equals(caixa.getStatus())) {
            throw new IllegalStateException("Não há caixa aberto.");
        }
    }

    private Usuario exigirOperador() {
        Usuario operador = SessionManager.getUsuarioAtual();
        if (operador == null || operador.getIdUsuario() == null) {
            throw new IllegalStateException("É necessário um usuário logado para operar o caixa.");
        }
        return operador;
    }
}
