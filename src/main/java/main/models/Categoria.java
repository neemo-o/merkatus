package main.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Categoria {

    private Integer       idCategoria;
    private Integer       parentId;                  // hierarquia — null = categoria raiz
    private String        nome;
    private Integer       idTributacaoPadrao;        // FK → tributacao_perfil (nullable = sem padrão)
    private boolean       ativo = true;
    private LocalDateTime dataCadastro;
}