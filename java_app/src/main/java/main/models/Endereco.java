package main.models;


import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Endereco {

    private Integer idEndereco;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
    private String cep;
    private LocalDateTime dataCadastro;

    @Override
    public String toString() {
        return logradouro + ", " + numero + " - " + estado;
    }
}