package main.models;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class VendaRelatorioDTO {

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Integer idVenda;
    private String  dataVenda;
    private String  cliente;
    private Integer itens;
    private String  formaPagamento;
    private String  status;
    private BigDecimal valorTotal;

    public VendaRelatorioDTO(Venda v, String nomeCliente, Integer qtdItens) {
        this.idVenda        = v.getIdVenda();
        this.dataVenda      = v.getDataVenda() != null ? v.getDataVenda().format(DATA_HORA) : "";
        this.cliente        = nomeCliente != null ? nomeCliente : "Consumidor Final";
        this.itens          = qtdItens != null ? qtdItens : 0;
        this.formaPagamento = v.getFormaPagamento() != null ? v.getFormaPagamento() : "";
        this.status         = v.getStatus() != null ? v.getStatus() : "";
        this.valorTotal     = v.getValorTotal() != null ? v.getValorTotal() : BigDecimal.ZERO;
    }

    public Integer    getIdVenda()        { return idVenda; }
    public String     getDataVenda()      { return dataVenda; }
    public String     getCliente()        { return cliente; }
    public Integer    getItens()          { return itens; }
    public String     getFormaPagamento() { return formaPagamento; }
    public String     getStatus()         { return status; }
    public BigDecimal getValorTotal()     { return valorTotal; }

    /** Valor considerado nos totais: apenas vendas finalizadas somam. */
    public BigDecimal getValorFinalizada() {
        return "FINALIZADA".equals(status) ? valorTotal : BigDecimal.ZERO;
    }
}
