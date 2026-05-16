package main.services;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ProdutoService {

    /**
     * Calcula a margem de lucro percentual.
     * margem = ((precoVenda - precoCusto) / precoCusto) * 100
     *
     * @return margem em percentual com 2 casas decimais, ou null se inválido
     */
    public BigDecimal calcularMargem(BigDecimal precoCusto, BigDecimal precoVenda) {
        if (precoCusto == null || precoVenda == null) return null;
        if (precoCusto.compareTo(BigDecimal.ZERO) == 0) return null;

        return precoVenda.subtract(precoCusto)
                .divide(precoCusto, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula o preço de venda a partir do custo e margem desejada.
     * precoVenda = precoCusto * (1 + margem / 100)
     *
     * @return preço de venda com 2 casas decimais, ou null se inválido
     */
    public BigDecimal calcularPrecoVenda(BigDecimal precoCusto, BigDecimal margem) {
        if (precoCusto == null || margem == null) return null;
        if (precoCusto.compareTo(BigDecimal.ZERO) == 0) return null;

        return precoCusto.multiply(
                BigDecimal.ONE.add(
                        margem.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)
                )
        ).setScale(2, RoundingMode.HALF_UP);
    }
}