package main.models;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Empresa {

    private Integer idEmpresa;
    private String razaoSocial;
    private String nomeFantasia;
    private String cnpj;
    private String inscricaoEstadual;
    private String inscricaoMunicipal;
    private Short regimeTributario;
    private Short crt;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String uf;
    private String cep;
    private String codMunicipioIbge;
    private String telefone;
    private String email;
    private byte[] logo;
    private byte[] certificadoDigital;
    private String senhaCertificado;
    private Short ambienteNfe;
    private Short serieNfce;
    private Short serieNfe;
    private Integer proximoNfce;
    private Integer proximoNfe;
    private String tokenCsc;
    private String idCsc;
    private LocalDateTime dataCadastro;
    private LocalDateTime dataAtualizacao;
}
