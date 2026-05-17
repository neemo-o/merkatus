package test;

import java.time.LocalDateTime;

import main.models.Fornecedor;
import main.models.Funcionario;
import main.models.TributacaoPerfil;
import main.models.Cliente;
import main.models.Endereco;
import main.models.NcmTributacao;

public class TestFactory {

    public static Fornecedor fornecedor(String cnpj) {
        Fornecedor f = new Fornecedor();
        f.setCnpj(cnpj);
        f.setRazaoSocial("Teste SA");
        f.setAtivo(true);
        f.setDataCadastro(LocalDateTime.now());
        f.setDataAtualizacao(LocalDateTime.now());
        return f;
    }

    public static Funcionario funcionario(String cpf) {
        Funcionario f = new Funcionario();
        f.setCpf(cpf);
        f.setNome("Funcionario Teste");
        f.setCargo("Operador");
        f.setDataAdmissao(java.time.LocalDate.now());
        f.setAtivo(true);
        f.setDataCadastro(LocalDateTime.now());
        f.setDataAtualizacao(LocalDateTime.now());
        return f;
    }

    public static Cliente cliente(String cnpj) {
        Cliente c = new Cliente();
        c.setCnpj(cnpj);
        c.setRazaoSocial("Cliente Teste SA");
        c.setStatusCliente("ATIVO");
        c.setAtivo(true);
        c.setDataCadastro(LocalDateTime.now());
        c.setDataAtualizacao(LocalDateTime.now());
        return c;
    }

    public static Endereco endereco() {
        Endereco e = new Endereco();
        e.setLogradouro("Rua Teste");
        e.setNumero("123");
        e.setBairro("Centro");
        e.setCidade("Cidade Teste");
        e.setEstado("BA");
        e.setCep("48400000");
        e.setDataCadastro(LocalDateTime.now()); // ← faltou isso
        return e;
    }

    public static TributacaoPerfil tributacaoPerfil(String nome) {
        TributacaoPerfil t = new TributacaoPerfil();
        t.setNome(nome);
        t.setDescricao("Perfil Teste");
        t.setCstIcms("00");
        t.setCsosn("400");
        t.setAliqIcms(new java.math.BigDecimal("12.00"));
        t.setCfopVenda("5102");
        t.setCstPis("07");
        t.setCstCofins("07");
        t.setAliqPis(new java.math.BigDecimal("0.65"));
        t.setAliqCofins(new java.math.BigDecimal("3.00"));
        t.setCstIpi("99");
        t.setAliqIpi(java.math.BigDecimal.ZERO);
        t.setAtivo(true);
        t.setDataCadastro(LocalDateTime.now());
        t.setDataAtualizacao(LocalDateTime.now());
        return t;
    }

    public static NcmTributacao ncmTributacao(String ncm, Integer idTributacao) {
        NcmTributacao n = new NcmTributacao();
        n.setNcm(ncm);
        n.setDescricaoNcm("Produto Teste NCM");
        n.setIdTributacao(idTributacao); // FK obrigatória
        n.setDataCadastro(LocalDateTime.now());
        return n;
    }
}