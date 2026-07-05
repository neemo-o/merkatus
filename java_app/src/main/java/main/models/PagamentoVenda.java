package main.models;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Um pagamento da venda (uma venda pode ter vários — ex: parte cartão, parte dinheiro).
 * Persistido em vendas_pagamentos via VendaDAO.registrarPagamento.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagamentoVenda {

    private FormaPagamento formaPagamento;
    private BigDecimal valor;
}
