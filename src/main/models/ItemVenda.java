package main.models;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class ItemVenda {
    private int idItem;
    private int idVenda;
    private int idProduto;
    private int quantidade;
    private BigDecimal precoUnitario;
    private Timestamp dataCadastro;

    // Relacionamentos
    private Produto produto;
    private Vendas venda;

    // Construtores
    public ItemVenda() {}

    public ItemVenda(int idItem, int idVenda, int idProduto, int quantidade,
                     BigDecimal precoUnitario, Timestamp dataCadastro) {
        this.idItem = idItem;
        this.idVenda = idVenda;
        this.idProduto = idProduto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.dataCadastro = dataCadastro;
    }

    // Getters e Setters
    public int getIdItem() {
        return idItem;
    }

    public void setIdItem(int idItem) {
        this.idItem = idItem;
    }

    public int getIdVenda() {
        return idVenda;
    }

    public void setIdVenda(int idVenda) {
        this.idVenda = idVenda;
    }

    public int getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(int idProduto) {
        this.idProduto = idProduto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(BigDecimal precoUnitario) {
        this.precoUnitario = precoUnitario;
    }

    public Timestamp getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(Timestamp dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public Vendas getVenda() {
        return venda;
    }

    public void setVenda(Vendas venda) {
        this.venda = venda;
    }

    // Propriedade calculada
    public BigDecimal getSubtotal() {
        if (quantidade > 0 && precoUnitario != null) {
            return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
        }
        return BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return "Item " + idItem + " - Produto " + idProduto + " x" + quantidade;
    }
}
