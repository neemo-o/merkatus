package main.models;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Fornecedor {

    private Integer idFornecedor;
    private String cnpj;
    private String razaoSocial;
    private String nomeFantasia;
    private String telefone;
    private String eMail;
    private Integer idEndereco;
    private Boolean ativo;
    private LocalDateTime dataCadastro;
    private LocalDateTime dataAtualizacao;

    public Fornecedor() {}

    public Integer getIdFornecedor() { return idFornecedor; }
    public void setIdFornecedor(Integer idFornecedor) { this.idFornecedor = idFornecedor; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial; }

    public String getNomeFantasia() { return nomeFantasia; }
    public void setNomeFantasia(String nomeFantasia) { this.nomeFantasia = nomeFantasia; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEMail() { return eMail; }
    public void setEMail(String eMail) { this.eMail = eMail; }

    public Integer getIdEndereco() { return idEndereco; }
    public void setIdEndereco(Integer idEndereco) { this.idEndereco = idEndereco; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }

    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }
}
