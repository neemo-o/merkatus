package main.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TributacaoPerfil {

    private Integer       idTributacao;
    private String        nome;                      // ex: "Refrigerante ST ICMS SP"
    private String        descricao;

    // Classificação fiscal
    private String        ncm;                       // 8 dígitos
    private String        cest;                      // 7 dígitos

    // ICMS
    private String        cstIcms;                   // regime normal (3 dígitos)
    private String        csosn;                     // simples nacional (4 dígitos)
    private BigDecimal    aliqIcms;
    private BigDecimal    aliqIcmsSt;                // substituição tributária
    private BigDecimal    mvaSt;                     // margem de valor agregado

    // CFOP
    private String        cfopVenda;
    private String        cfopVendaInterestadual;

    // PIS / COFINS
    private String        cstPis;
    private String        cstCofins;
    private BigDecimal    aliqPis;
    private BigDecimal    aliqCofins;

    // IPI
    private String        cstIpi;
    private BigDecimal    aliqIpi;

    // Controle
    private boolean       ativo = true;
    private LocalDateTime dataCadastro;
    private LocalDateTime dataAtualizacao;
}
