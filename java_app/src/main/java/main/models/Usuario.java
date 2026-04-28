package main.models;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    private Integer idUsuario;
    private Integer idFuncionario;
    private Integer idPerfil;
    private String login;
    private String senhaHash;
    private String nomeExibicao;
    private String email;
    private Short tentativasLogin;
    private Boolean bloqueado;
    private LocalDateTime ultimoLogin;
    private Boolean ativo;
    private LocalDateTime dataCadastro;
    private LocalDateTime dataAtualizacao;

    @Override
    public String toString() {
        return nomeExibicao != null ? nomeExibicao : "Usuario #" + idUsuario;
    }
}
