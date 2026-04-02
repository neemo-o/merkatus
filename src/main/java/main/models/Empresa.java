package main.models;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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

    public Empresa() {}

    public Integer getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(Integer idEmpresa) { this.idEmpresa = idEmpresa; }

    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial; }

    public String getNomeFantasia() { return nomeFantasia; }
    public void setNomeFantasia(String nomeFantasia) { this.nomeFantasia = nomeFantasia; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getInscricaoEstadual() { return inscricaoEstadual; }
    public void setInscricaoEstadual(String inscricaoEstadual) { this.inscricaoEstadual = inscricaoEstadual; }

    public String getInscricaoMunicipal() { return inscricaoMunicipal; }
    public void setInscricaoMunicipal(String inscricaoMunicipal) { this.inscricaoMunicipal = inscricaoMunicipal; }

    public Short getRegimeTributario() { return regimeTributario; }
    public void setRegimeTributario(Short regimeTributario) { this.regimeTributario = regimeTributario; }

    public Short getCrt() { return crt; }
    public void setCrt(Short crt) { this.crt = crt; }

    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getComplemento() { return complemento; }
    public void setComplemento(String complemento) { this.complemento = complemento; }

    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public String getCodMunicipioIbge() { return codMunicipioIbge; }
    public void setCodMunicipioIbge(String codMunicipioIbge) { this.codMunicipioIbge = codMunicipioIbge; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public byte[] getLogo() { return logo; }
    public void setLogo(byte[] logo) { this.logo = logo; }

    public byte[] getCertificadoDigital() { return certificadoDigital; }
    public void setCertificadoDigital(byte[] certificadoDigital) { this.certificadoDigital = certificadoDigital; }

    public String getSenhaCertificado() { return senhaCertificado; }
    public void setSenhaCertificado(String senhaCertificado) { this.senhaCertificado = senhaCertificado; }

    public Short getAmbienteNfe() { return ambienteNfe; }
    public void setAmbienteNfe(Short ambienteNfe) { this.ambienteNfe = ambienteNfe; }

    public Short getSerieNfce() { return serieNfce; }
    public void setSerieNfce(Short serieNfce) { this.serieNfce = serieNfce; }

    public Short getSerieNfe() { return serieNfe; }
    public void setSerieNfe(Short serieNfe) { this.serieNfe = serieNfe; }

    public Integer getProximoNfce() { return proximoNfce; }
    public void setProximoNfce(Integer proximoNfce) { this.proximoNfce = proximoNfce; }

    public Integer getProximoNfe() { return proximoNfe; }
    public void setProximoNfe(Integer proximoNfe) { this.proximoNfe = proximoNfe; }

    public String getTokenCsc() { return tokenCsc; }
    public void setTokenCsc(String tokenCsc) { this.tokenCsc = tokenCsc; }

    public String getIdCsc() { return idCsc; }
    public void setIdCsc(String idCsc) { this.idCsc = idCsc; }

    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }

    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }
}
