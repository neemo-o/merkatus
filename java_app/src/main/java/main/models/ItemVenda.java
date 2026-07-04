package main.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemVenda {

    private Integer idItem;
    private Integer idVenda;
    private Integer idProduto;
    private Integer sequencia;
    private Integer quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal desconto;
    private BigDecimal acrescimo;
    private BigDecimal totalItem;

    // Campos fiscais espelhados no momento da venda (não mudam retroativamente)
    private String cfop;
    private String ncm;
    private String cest;
    private String cstIcms;
    private String csosn;
    private BigDecimal aliqIcms;
    private BigDecimal valorIcms;
    private BigDecimal baseIcms;
    private String cstPis;
    private BigDecimal aliqPis;
    private BigDecimal valorPis;
    private String cstCofins;
    private BigDecimal aliqCofins;
    private BigDecimal valorCofins;

    private Boolean cancelado;
    private String motivoCancelamento;
    private LocalDateTime dataCadastro;

    // Apenas para exibição em tela (não persistido na tabela item_venda)
    private String descricaoProduto;
}
