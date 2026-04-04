package main.models;

import java.time.LocalDateTime;

public class Usuario {

    private Integer idUsuario;
    private String nome;
    private String login;
    private LocalDateTime dataCadastro;

    public Usuario() {}

    public Usuario(Integer idUsuario, String nome, String login) {
        this.idUsuario = idUsuario;
        this.nome = nome;
        this.login = login;
    }

    public Integer getIdUsuario()           { return idUsuario; }
    public void setIdUsuario(Integer v)     { this.idUsuario = v; }

    public String getNome()                 { return nome; }
    public void setNome(String v)           { this.nome = v; }

    public String getLogin()                { return login; }
    public void setLogin(String v)          { this.login = v; }

    public LocalDateTime getDataCadastro()  { return dataCadastro; }
    public void setDataCadastro(LocalDateTime v) { this.dataCadastro = v; }

    @Override
    public String toString() {
        return nome != null ? nome : "Usuario #" + idUsuario;
    }
}
