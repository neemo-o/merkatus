package main.database.DAOs;

import org.springframework.stereotype.Component;

import main.database.GenericDAO;
import main.models.Empresa;

@Component
public abstract class EmpresaDAO extends GenericDAO <Empresa, Integer> {

    public static java.util.List<Empresa> findAllStatic() {
        return GenericDAO.findAllStatic(EmpresaDAO.class);
    }

    public static Empresa findByIdStatic(Integer id) {
        return GenericDAO.findByIdStatic(EmpresaDAO.class, id);
    }

    public static boolean deleteByIdStatic(Integer id) {
        return GenericDAO.deleteByIdStatic(EmpresaDAO.class, id);
    }

    public static Empresa insertStatic(Empresa empresa) {
        return GenericDAO.insertStatic(EmpresaDAO.class, empresa);
    }

    public static boolean updateStatic(Empresa empresa) {
        return GenericDAO.updateStatic(EmpresaDAO.class, empresa);
    }

    @Override
    protected String getTabela() {
        return "empresa";
    }

    @Override
    protected String getColunaId() {
        return "id_empresa";
    }
    
    @Override
    protected void setIdGerado(Empresa e, java.sql.ResultSet keys) throws java.sql.SQLException {
        e.setIdEmpresa(keys.getInt(1));
    }

    @Override
    protected String getSqlInsert() {
        return """
                (razao_social, nome_fantasia, cnpj, inscricao_estadual, inscricao_municipal,
                 regime_tributario, crt, logradouro, numero, complemento, bairro,
                 cidade, uf, cep, cod_municipio_ibge, telefone, email,
                 logo, certificado_digital, senha_certificado,
                 ambiente_nfe, serie_nfce, serie_nfe,
                 proximo_nfce, proximo_nfe,
                 token_csc, id_csc)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """;
    }

    @Override
    protected void setParametrosInsert(java.sql.PreparedStatement stmt, Empresa e) throws java.sql.SQLException {
        stmt.setString(1, e.getRazaoSocial());
        stmt.setString(2, e.getNomeFantasia());
        stmt.setString(3, e.getCnpj());
        stmt.setString(4, e.getInscricaoEstadual());
        stmt.setString(5, e.getInscricaoMunicipal());
        stmt.setShort(6, e.getRegimeTributario());
        stmt.setShort(7, e.getCrt());
        stmt.setString(8, e.getLogradouro());
        stmt.setString(9, e.getNumero());
        stmt.setString(10, e.getComplemento());
        stmt.setString(11, e.getBairro());
        stmt.setString(12, e.getCidade());
        stmt.setString(13, e.getUf());
        stmt.setString(14, e.getCep());
        stmt.setString(15, e.getCodMunicipioIbge());
        stmt.setString(16, e.getTelefone());
        stmt.setString(17, e.getEmail());
        stmt.setBytes(18, e.getLogo());
        stmt.setBytes(19, e.getCertificadoDigital());
        stmt.setString(20, e.getSenhaCertificado());
        stmt.setShort(21, e.getAmbienteNfe());
        stmt.setShort(22, e.getSerieNfce());
        stmt.setShort(23, e.getSerieNfe());
        stmt.setInt(24, e.getProximoNfce());
        stmt.setInt(25, e.getProximoNfe());
        stmt.setString(26, e.getTokenCsc());
        stmt.setString(27, e.getIdCsc());
    }

    @Override
    protected void setParametrosUpdate(java.sql.PreparedStatement stmt, Empresa e) throws java.sql.SQLException {
        stmt.setString(1, e.getRazaoSocial());
        stmt.setString(2, e.getNomeFantasia());
        stmt.setString(3, e.getCnpj());
        stmt.setString(4, e.getInscricaoEstadual());
        stmt.setString(5, e.getInscricaoMunicipal());
        stmt.setShort(6, e.getRegimeTributario());
        stmt.setShort(7, e.getCrt());
        stmt.setString(8, e.getLogradouro());
        stmt.setString(9, e.getNumero());
        stmt.setString(10, e.getComplemento());
        stmt.setString(11, e.getBairro());
        stmt.setString(12, e.getCidade());
        stmt.setString(13, e.getUf());
        stmt.setString(14, e.getCep());
        stmt.setString(15, e.getCodMunicipioIbge());
        stmt.setString(16, e.getTelefone());
        stmt.setString(17, e.getEmail());
        stmt.setBytes(18, e.getLogo());
        stmt.setBytes(19, e.getCertificadoDigital());
        stmt.setString(20, e.getSenhaCertificado());
        stmt.setShort(21, e.getAmbienteNfe());
        stmt.setShort(22, e.getSerieNfce());
        stmt.setShort(23, e.getSerieNfe());
        stmt.setInt(24, e.getProximoNfce());
        stmt.setInt(25, e.getProximoNfe());
        stmt.setString(26, e.getTokenCsc());
        stmt.setString(27, e.getIdCsc());
        stmt.setInt(28, e.getIdEmpresa());
    }

    @Override
    protected String getSqlUpdate() {
        return """
                UPDATE empresa SET
                    razao_social = ?,
                    nome_fantasia = ?,
                    cnpj = ?,
                    inscricao_estadual = ?,
                    inscricao_municipal = ?,
                    regime_tributario = ?,
                    crt = ?,
                    logradouro = ?,
                    numero = ?,
                    complemento = ?,
                    bairro = ?,
                    cidade = ?,
                    uf = ?,
                    cep = ?,
                    cod_municipio_ibge = ?,
                    telefone = ?,
                    email = ?,
                    logo = ?,
                    certificado_digital = ?,
                    senha_certificado = ?,
                    ambiente_nfe = ?,
                    serie_nfce = ?,
                    serie_nfe = ?,
                    proximo_nfce = ?,
                    proximo_nfe = ?,
                    token_csc = ?,
                    id_csc = ?
                WHERE id_empresa = ?
                """;
    }

    @Override
    protected Empresa mapear(java.sql.ResultSet rs) throws java.sql.SQLException {
        Empresa e = new Empresa();
        e.setIdEmpresa(rs.getInt("id_empresa"));
        e.setRazaoSocial(rs.getString("razao_social"));
        e.setNomeFantasia(rs.getString("nome_fantasia"));
        e.setCnpj(rs.getString("cnpj"));
        e.setInscricaoEstadual(rs.getString("inscricao_estadual"));
        e.setInscricaoMunicipal(rs.getString("inscricao_municipal"));
        e.setRegimeTributario(rs.getShort("regime_tributario"));
        e.setCrt(rs.getShort("crt"));
        e.setLogradouro(rs.getString("logradouro"));
        e.setNumero(rs.getString("numero"));
        e.setComplemento(rs.getString("complemento"));
        e.setBairro(rs.getString("bairro"));
        e.setCidade(rs.getString("cidade"));
        e.setUf(rs.getString("uf"));
        e.setCep(rs.getString("cep"));
        e.setCodMunicipioIbge(rs.getString("cod_municipio_ibge"));
        e.setTelefone(rs.getString("telefone"));
        e.setEmail(rs.getString("email"));
        e.setLogo(rs.getBytes("logo"));
        e.setCertificadoDigital(rs.getBytes("certificado_digital"));
        e.setSenhaCertificado(rs.getString("senha_certificado"));
        e.setAmbienteNfe(rs.getShort("ambiente_nfe"));
        e.setSerieNfce(rs.getShort("serie_nfce"));
        e.setSerieNfe(rs.getShort("serie_nfe"));
        e.setProximoNfce(rs.getInt("proximo_nfce"));
        e.setProximoNfe(rs.getInt("proximo_nfe"));
        e.setTokenCsc(rs.getString("token_csc"));
        e.setIdCsc(rs.getString("id_csc"));

        return e;
    }
}
