package main.models;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormaPagamento {

    private Integer idFormaPagamento;
    private String descricao;
    private String tipo;
    private String codPagamentoNfe;
    private Boolean tef;
    private Boolean permiteTroco;
    private Boolean ativo;
    private LocalDateTime dataCadastro;

    @Override
    public String toString() {
        // Usado pelo ComboBox de forma de pagamento
        return descricao != null ? descricao : "";
    }
}
