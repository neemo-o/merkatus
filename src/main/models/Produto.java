package main.models;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Produto {
    private int idProduto;
    private String descricao;
    private String codigoBarras;
    private String unidadeMedida;
    private BigDecimal precoCusto;
    private BigDecimal precoVenda;
    private int estoqueAtual;
    private Integer idFornecedor; // Pode ser null
    private Timestamp dataCadastro;
    private Timestamp dataAtualizacao;

    // Construtores
    public Produto() {}

    public Produto(int idProduto, String descricao, String codigoBarras,
                   String unidadeMedida, BigDecimal precoCusto, BigDecimal precoVenda,
                   int estoqueAtual, Integer idFornecedor) {
        this.idProduto = idProduto;
        this.descricao = descricao;
        this.codigoBarras = codigoBarras;
        this.unidadeMedida = unidadeMedida;
        this.precoCusto = precoCusto;
        this.precoVenda = precoVenda;
        this.estoqueAtual = estoqueAtual;
        this.idFornecedor = idFornecedor;
    }

    // Getters e Setters
    public int getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(int idProduto) {
        this.idProduto = idProduto;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public String getUnidadeMedida() {
        return unidadeMedida;
    }

    public void setUnidadeMedida(String unidadeMedida) {
        this.unidadeMedida = unidadeMedida;
    }

    public BigDecimal getPrecoCusto() {
        return precoCusto;
    }

    public void setPrecoCusto(BigDecimal precoCusto) {
        this.precoCusto = precoCusto;
    }

    public BigDecimal getPrecoVenda() {
        return precoVenda;
    }

    public void setPrecoVenda(BigDecimal precoVenda) {
        this.precoVenda = precoVenda;
    }

    public int getEstoqueAtual() {
        return estoqueAtual;
    }

    public void setEstoqueAtual(int estoqueAtual) {
        this.estoqueAtual = estoqueAtual;
    }

    public Integer getIdFornecedor() {
        return idFornecedor;
    }

    public void setIdFornecedor(Integer idFornecedor) {
        this.idFornecedor = idFornecedor;
    }

    public Timestamp getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(Timestamp dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public Timestamp getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(Timestamp dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
