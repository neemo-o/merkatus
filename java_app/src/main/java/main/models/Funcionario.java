package main.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Funcionario {

    private Integer idFuncionario;
    private String nome;
    private String cpf;
    private String cargo;
    private LocalDate dataAdmissao;
    private LocalDate dataDemissao;
    private String telefone;
    private String email;
    private Boolean ativo;
    private LocalDateTime dataCadastro;
    private LocalDateTime dataAtualizacao;
}
