package main.models;

import java.time.LocalDateTime;

public class Fornecedor {
    private int idFornecedor;
    private String cnpj;
    private String razaoSocial;
    private String senhaHash;
    private String telefone;
    private String email;
    private Endereco endereco;
    private LocalDateTime dataCadastro;
    private LocalDateTime dataAtualizacao;

    // Construtores
    public Fornecedor() {}

    public Fornecedor(int idFornecedor, String razaoSocial) {
        this.idFornecedor = idFornecedor;
        this.razaoSocial = razaoSocial;
    }

    // Getters e Setters
    public int getIdFornecedor() {
        return idFornecedor;
    }

    public void setIdFornecedor(int idFornecedor) {
        this.idFornecedor = idFornecedor;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    // Método para obter endereço completo
    public String getEnderecoCompleto() {
        if (endereco == null) return "";
        return endereco.toString();
    }

    @Override
    public String toString() {
        return razaoSocial;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Fornecedor fornecedor = (Fornecedor) obj;
        return idFornecedor == fornecedor.idFornecedor;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idFornecedor);
    }
}
