package main.models;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LogLogin {

    private Long idLog;
    private Integer idUsuario;
    private Integer idTerminal;
    private String loginTentado;
    private Boolean sucesso;
    private String ipAddress;
    private String motivoFalha;
    private LocalDateTime dataLog;
}