package main.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Caixa {

    private Integer idCaixa;
    private Integer idTerminal;
    private Integer idOperador;
    private LocalDateTime dataAbertura;
    private LocalDateTime dataFechamento;
    private BigDecimal valorAbertura;
    private BigDecimal valorFechamento;
    private BigDecimal valorSistema;
    private BigDecimal diferenca;
    // ABERTO ou FECHADO
    private String status;
    private String observacao;
    private Integer idSupervisorFechamento;
    private LocalDateTime dataCadastro;
}
