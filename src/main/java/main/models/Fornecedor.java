package main.models;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Fornecedor {

    private Integer idFornecedor;
    private String cnpj;
    private String razaoSocial;
    private String nomeFantasia;
    private String telefone;
    private String eMail;
    private Integer idEndereco;
    private Boolean ativo;
    private LocalDateTime dataCadastro;
    private LocalDateTime dataAtualizacao;

    public Fornecedor() {
        // Construtor padrão com o Lombok @NoArgsConstructor, mas adicionado
        // explicitamente para garantir que seja mantido mesmo após a adição de outros construtores
    }

    @Override
    public String toString() {
        return nomeFantasia != null ? nomeFantasia : razaoSocial;
    }
}
