package main.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Venda {

    private Integer idVenda;
    private Integer idCaixa;
    private Integer idTerminal;
    private Integer idOperador;
    private Integer idCliente;
    private Integer numeroVenda;
    private LocalDateTime dataVenda;
    private BigDecimal subtotal;
    private BigDecimal desconto;
    private BigDecimal acrescimo;
    private BigDecimal valorTotal;
    private BigDecimal troco;
    private String cpfNota;
    private String formaPagamento;
    private String status;
    private String observacao;
    private LocalDateTime dataCadastro;
}
