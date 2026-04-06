package main.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Produto {

    private Integer       idProduto;
    private String        descricao;
    private String        codigoBarras;

    private String        unidadeMedida;       // campo legado (compatibilidade com DAO atual)
    private Integer       idUnidadeMedida;

    private Integer       idCategoria;
    private Integer       idFornecedor;

    private BigDecimal    precoCusto;
    private BigDecimal    precoVenda;
    private BigDecimal    margemLucro;

    private Integer       estoqueAtual  = 0;
    private BigDecimal    estoqueMinimo;
    private BigDecimal    estoqueMaximo;

    // Campos fiscais legados (mantidos por compatibilidade com ProdutoDAO existente)
    private String        ncm;
    private String        cest;
    private String        cfopVenda;
    private String        cstIcms;
    private String        csosn;
    private String        cstPis;
    private String        cstCofins;
    private String        cstIpi;
    private BigDecimal    aliqIcms;
    private BigDecimal    aliqPis;
    private BigDecimal    aliqCofins;
    private BigDecimal    aliqIpi;

    // NOVO: FK para tributacao_perfil (null = herda de categoria ou NCM)
    private Integer       idTributacao;

    private BigDecimal    pesoLiquido;
    private BigDecimal    pesoBruto;

    private boolean       permiteFracionamento = false;
    private boolean       controlaEstoque      = true;
    private boolean       balanca              = false;
    private boolean       ativo                = true;

    private LocalDateTime dataCadastro     = LocalDateTime.now();
    private LocalDateTime dataAtualizacao;

    // Construtor de conveniência mantido para compatibilidade
    public Produto(String descricao, BigDecimal precoVenda) {
        this();
        this.descricao  = descricao;
        this.precoVenda = precoVenda;
    }
}