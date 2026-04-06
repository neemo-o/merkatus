package main.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnidadeMedida {

    private Integer idUnidade;
    private String sigla;
    private String descricao;

    @Override
    public String toString() {
        return sigla;
    }
}
