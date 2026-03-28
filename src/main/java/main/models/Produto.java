package main.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Produto {


    private Integer       idProduto;
    private String        descricao;
    private String        codigoBarras;


    private String        unidadeMedida;      
    private Integer       idUnidadeMedida;


    private Integer       idCategoria;
    private Integer       idFornecedor;


    private BigDecimal    precoCusto;         
    private BigDecimal    precoVenda;         
    private BigDecimal    margemLucro;        

  
    private Integer       estoqueAtual;       
    private BigDecimal    estoqueMinimo;      
    private BigDecimal    estoqueMaximo;      

  
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

  
    private BigDecimal    pesoLiquido;      
    private BigDecimal    pesoBruto;        

   
    private boolean       permiteFracionamento; 
    private boolean       controlaEstoque;      
    private boolean       balanca;            
    private boolean       ativo;                 

   
    private LocalDateTime dataCadastro;
    private LocalDateTime dataAtualizacao;


   
    // Construtores
  

    public Produto() {
        this.estoqueAtual          = 0;
        this.permiteFracionamento  = false;
        this.controlaEstoque       = true;
        this.balanca               = false;
        this.ativo                 = true;
        this.dataCadastro          = LocalDateTime.now();
    }

    public Produto(String descricao, BigDecimal precoVenda) {
        this();
        this.descricao  = descricao;
        this.precoVenda = precoVenda;
    }



    // Getters e Setters
    

    public Integer       getIdProduto()              { return idProduto; }
    public void          setIdProduto(Integer v)       { this.idProduto = v; }

    public String        getDescricao()               { return descricao; }
    public void          setDescricao(String v)        { this.descricao = v; }

    public String        getCodigoBarras()             { return codigoBarras; }
    public void          setCodigoBarras(String v)      { this.codigoBarras = v; }

    public String        getUnidadeMedida()            { return unidadeMedida; }
    public void          setUnidadeMedida(String v)     { this.unidadeMedida = v; }

    public Integer       getIdUnidadeMedida()          { return idUnidadeMedida; }
    public void          setIdUnidadeMedida(Integer v)  { this.idUnidadeMedida = v; }

    public Integer       getIdCategoria()              { return idCategoria; }
    public void          setIdCategoria(Integer v)      { this.idCategoria = v; }

    public Integer       getIdFornecedor()             { return idFornecedor; }
    public void          setIdFornecedor(Integer v)     { this.idFornecedor = v; }

    public BigDecimal    getPrecoCusto()               { return precoCusto; }
    public void          setPrecoCusto(BigDecimal v)    { this.precoCusto = v; }

    public BigDecimal    getPrecoVenda()               { return precoVenda; }
    public void          setPrecoVenda(BigDecimal v)    { this.precoVenda = v; }

    public BigDecimal    getMargemLucro()              { return margemLucro; }
    public void          setMargemLucro(BigDecimal v)   { this.margemLucro = v; }

    public Integer       getEstoqueAtual()             { return estoqueAtual; }
    public void          setEstoqueAtual(Integer v)     { this.estoqueAtual = v; }

    public BigDecimal    getEstoqueMinimo()            { return estoqueMinimo; }
    public void          setEstoqueMinimo(BigDecimal v) { this.estoqueMinimo = v; }

    public BigDecimal    getEstoqueMaximo()            { return estoqueMaximo; }
    public void          setEstoqueMaximo(BigDecimal v) { this.estoqueMaximo = v; }

    public String        getNcm()                      { return ncm; }
    public void          setNcm(String v)               { this.ncm = v; }

    public String        getCest()                     { return cest; }
    public void          setCest(String v)              { this.cest = v; }

    public String        getCfopVenda()                { return cfopVenda; }
    public void          setCfopVenda(String v)         { this.cfopVenda = v; }

    public String        getCstIcms()                  { return cstIcms; }
    public void          setCstIcms(String v)           { this.cstIcms = v; }

    public String        getCsosn()                    { return csosn; }
    public void          setCsosn(String v)             { this.csosn = v; }

    public String        getCstPis()                   { return cstPis; }
    public void          setCstPis(String v)            { this.cstPis = v; }

    public String        getCstCofins()                { return cstCofins; }
    public void          setCstCofins(String v)         { this.cstCofins = v; }

    public String        getCstIpi()                   { return cstIpi; }
    public void          setCstIpi(String v)            { this.cstIpi = v; }

    public BigDecimal    getAliqIcms()                 { return aliqIcms; }
    public void          setAliqIcms(BigDecimal v)      { this.aliqIcms = v; }

    public BigDecimal    getAliqPis()                  { return aliqPis; }
    public void          setAliqPis(BigDecimal v)       { this.aliqPis = v; }

    public BigDecimal    getAliqCofins()               { return aliqCofins; }
    public void          setAliqCofins(BigDecimal v)    { this.aliqCofins = v; }

    public BigDecimal    getAliqIpi()                  { return aliqIpi; }
    public void          setAliqIpi(BigDecimal v)       { this.aliqIpi = v; }

    public BigDecimal    getPesoLiquido()              { return pesoLiquido; }
    public void          setPesoLiquido(BigDecimal v)   { this.pesoLiquido = v; }

    public BigDecimal    getPesoBruto()                { return pesoBruto; }
    public void          setPesoBruto(BigDecimal v)     { this.pesoBruto = v; }

    public boolean       isPermiteFracionamento()      { return permiteFracionamento; }
    public void          setPermiteFracionamento(boolean v) { this.permiteFracionamento = v; }

    public boolean       isControlaEstoque()           { return controlaEstoque; }
    public void          setControlaEstoque(boolean v)  { this.controlaEstoque = v; }

    public boolean       isBalanca()                   { return balanca; }
    public void          setBalanca(boolean v)          { this.balanca = v; }

    public boolean       isAtivo()                     { return ativo; }
    public void          setAtivo(boolean v)            { this.ativo = v; }

    public LocalDateTime getDataCadastro()             { return dataCadastro; }
    public void          setDataCadastro(LocalDateTime v){ this.dataCadastro = v; }

    public LocalDateTime getDataAtualizacao()          { return dataAtualizacao; }
    public void          setDataAtualizacao(LocalDateTime v){ this.dataAtualizacao = v; }




    @Override
    public String toString() {
        return "Produto{" +
            "id="          + idProduto   +
            ", descricao='" + descricao  + '\'' +
            ", ativo="     + ativo       +
            '}';
    }
}
