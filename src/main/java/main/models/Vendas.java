package main.models;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Vendas {
    private int idVenda;
    private Timestamp dataVenda;
    private BigDecimal valorTotal;
    private String formaPagamento;
    private Timestamp dataCadastro;

    // Construtores
    public Vendas() {}

    public Vendas(int idVenda, Timestamp dataVenda, BigDecimal valorTotal, String formaPagamento, Timestamp dataCadastro) {
        this.idVenda = idVenda;
        this.dataVenda = dataVenda;
        this.valorTotal = valorTotal;
        this.formaPagamento = formaPagamento;
        this.dataCadastro = dataCadastro;
    }

    // Getters e Setters
    public int getIdVenda() {
        return idVenda;
    }

    public void setIdVenda(int idVenda) {
        this.idVenda = idVenda;
    }

    public Timestamp getDataVenda() {
        return dataVenda;
    }

    public void setDataVenda(Timestamp dataVenda) {
        this.dataVenda = dataVenda;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public Timestamp getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(Timestamp dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    @Override
    public String toString() {
        return "Venda " + idVenda + " - " + valorTotal;
    }
}
