package main.models;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogLogin {

    private Long idLog;
    private Integer idUsuario;
    private Integer idTerminal;
    private String loginTentado;
    private Boolean sucesso;
    private String ipAddress;
    private String motivoFalha;
    private LocalDateTime dataLog;

    public LogLogin() {}

    public Long getIdLog() { return idLog; }
    public void setIdLog(Long idLog) { this.idLog = idLog; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public Integer getIdTerminal() { return idTerminal; }
    public void setIdTerminal(Integer idTerminal) { this.idTerminal = idTerminal; }

    public String getLoginTentado() { return loginTentado; }
    public void setLoginTentado(String loginTentado) { this.loginTentado = loginTentado; }

    public Boolean getSucesso() { return sucesso; }
    public void setSucesso(Boolean sucesso) { this.sucesso = sucesso; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getMotivoFalha() { return motivoFalha; }
    public void setMotivoFalha(String motivoFalha) { this.motivoFalha = motivoFalha; }

    public LocalDateTime getDataLog() { return dataLog; }
    public void setDataLog(LocalDateTime dataLog) { this.dataLog = dataLog; }
}