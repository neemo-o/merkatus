package main.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SangriaSuprimento {

    private Integer idSangriaSuprimento;
    private Integer idCaixa;
    // 'S' = sangria (retirada), 'U' = suprimento (entrada)
    private String tipo;
    private BigDecimal valor;
    private String motivo;
    private Integer idOperador;
    private Integer idSupervisor;
    private LocalDateTime dataCadastro;
}
