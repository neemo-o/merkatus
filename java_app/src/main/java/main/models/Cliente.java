package main.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    private Integer idCliente;
    // Documento: CPF (11 dígitos) para pessoa física, CNPJ (14) para jurídica
    private String cnpj;
    // 'F' = pessoa física, 'J' = pessoa jurídica
    private String tipoPessoa = "J";
    // Para pessoa física guarda o nome completo
    private String razaoSocial;
    private String nomeFantasia;
    private String inscricaoEstadual;
    private String emailCliente;
    private String telefoneCliente;
    private Integer idEnderecoCliente;
    private String statusCliente;
    private LocalDate dataNascimento;
    private BigDecimal limiteCredito;
    private Boolean ativo;
    private LocalDateTime dataCadastro;
    private LocalDateTime dataAtualizacao;
}