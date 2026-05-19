package main.models;

import java.math.BigDecimal;

public class ProdutoRelatorioDTO {

    private Integer idProduto;
    private String  codigoBarras;
    private String  descricao;
    private String  categoria;
    private Integer estoqueAtual;
    private BigDecimal precoVenda;
    private BigDecimal vlTotal;
    private Boolean ativo;

    public ProdutoRelatorioDTO(Produto p, String nomeCategoria) {
        this.idProduto    = p.getIdProduto();
        this.codigoBarras = p.getCodigoBarras();
        this.descricao    = p.getDescricao();
        this.categoria    = nomeCategoria != null ? nomeCategoria : "";
        this.estoqueAtual = p.getEstoqueAtual() != null ? p.getEstoqueAtual() : 0;
        this.precoVenda   = p.getPrecoVenda() != null ? p.getPrecoVenda() : BigDecimal.ZERO;
        this.vlTotal      = this.precoVenda.multiply(new BigDecimal(this.estoqueAtual));
        this.ativo        = p.isAtivo();
    }

    public Integer    getIdProduto()    { return idProduto; }
    public String     getCodigoBarras() { return codigoBarras; }
    public String     getDescricao()    { return descricao; }
    public String     getCategoria()    { return categoria; }
    public Integer    getEstoqueAtual() { return estoqueAtual; }
    public BigDecimal getPrecoVenda()   { return precoVenda; }
    public BigDecimal getVlTotal()      { return vlTotal; }
    public Boolean    getAtivo()        { return ativo; }
}
