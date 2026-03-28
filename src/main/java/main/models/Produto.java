package main.models;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "produto")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_produto")
    private int idProduto;

    @Column(name = "descricao", nullable = false)
    private String descricao;

    @Column(name = "codigo_barras", unique = true, length = 50)
    private String codigoBarras;
    
    @Column(name = "unidade_medida")
    private String unidadeMedida;

    @Column(name = "preco_custo", precision = 12, scale = 4)
    private BigDecimal precoCusto;

    @Column(name = "preco_venda", precision = 12, scale = 4)
    private BigDecimal precoVenda;

    @Column(name = "margem_lucro", precision = 5, scale = 2)
    private BigDecimal margemLucro;

    @ManyToOne
    @JoinColumn(name = "id_unidade_medida")
    private UnidadeMedida unidadeMedidaRef;

    @ManyToOne
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "id_fornecedor")
    private Fornecedor fornecedor;

    @Column(name = "estoque_atual")
    private Integer estoqueAtual = 0;

    @Column(name = "estoque_minimo", precision = 12, scale = 3)
    private BigDecimal estoqueMinimo;

    @Column(name = "estoque_maximo", precision = 12, scale = 3)
    private BigDecimal estoqueMaximo;

    private String ncm;
    private String cest;

    @Column(name = "cfop_venda", length = 4)
    private String cfopVenda;

    @Column(name = "cst_icms", length = 3)
    private String cstIcms;

    private String csosn;

    @Column(name = "cst_pis", length = 2)
    private String cstPis;

    @Column(name = "cst_cofins", length = 2)
    private String cstCofins;

    @Column(name = "cst_ipi", length = 2)
    private String cstIpi;

    @Column(name = "aliq_icms", precision = 5, scale = 2)
    private BigDecimal aliqIcms;

    @Column(name = "aliq_pis", precision = 5, scale = 4)
    private BigDecimal aliqPis;

    @Column(name = "aliq_cofins", precision = 5, scale = 4)
    private BigDecimal aliqCofins;

    @Column(name = "aliq_ipi", precision = 5, scale = 2)
    private BigDecimal aliqIpi;

    @Column(name = "peso_liquido", precision = 10, scale = 3)
    private BigDecimal pesoLiquido;

    @Column(name = "peso_bruto", precision = 10, scale = 3)
    private BigDecimal pesoBruto;

    @Column(name = "permite_fracionamento", nullable = false)
    private Boolean permiteFracionamento = false;

    @Column(name = "controla_estoque", nullable = false)
    private Boolean controlaEstoque = true;

    @Column(nullable = false)
    private Boolean balanca = false;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(name = "data_cadastro", nullable = false)
    private LocalDateTime dataCadastro;

    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    
    @Override
    public String toString() {
        return descricao;
    }
}
