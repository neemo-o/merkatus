package main.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NcmTributacao {

    private Integer       idNcmTributacao;
    private String        ncm;                       // 8 dígitos — UNIQUE no banco
    private String        descricaoNcm;
    private Integer       idTributacao;              // FK → tributacao_perfil
    private LocalDateTime dataCadastro;
}
