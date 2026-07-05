package main.database.DAOs;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import main.database.GenericDAO;
import main.models.Caixa;
import main.models.SangriaSuprimento;

@Component
public class CaixaDAO extends GenericDAO<Caixa, Integer> {

    @Override
    protected String getTabela() {
        return "caixas";
    }

    @Override
    protected String getColunaId() {
        return "id_caixa";
    }

    @Override
    protected void setGeneratedId(Caixa c, Number id) {
        c.setIdCaixa(id.intValue());
    }

    @Override
    protected String getSqlInsert() {
        return """
                INSERT INTO caixas
                (id_terminal, id_operador, data_abertura, valor_abertura, status, observacao)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
    }

    @Override
    protected void setParametrosInsert(PreparedStatement stmt, Caixa c) throws SQLException {
        stmt.setInt(1, c.getIdTerminal());
        stmt.setInt(2, c.getIdOperador());
        stmt.setObject(3, c.getDataAbertura());
        stmt.setBigDecimal(4, c.getValorAbertura());
        stmt.setString(5, c.getStatus());
        stmt.setString(6, c.getObservacao());
    }

    @Override
    protected String getSqlUpdate() {
        return """
                UPDATE caixas SET
                data_fechamento = ?, valor_fechamento = ?, valor_sistema = ?, diferenca = ?,
                status = ?, observacao = ?, id_supervisor_fechamento = ?
                WHERE id_caixa = ?
                """;
    }

    @Override
    protected void setParametrosUpdate(PreparedStatement stmt, Caixa c) throws SQLException {
        stmt.setObject(1, c.getDataFechamento());
        stmt.setBigDecimal(2, c.getValorFechamento());
        stmt.setBigDecimal(3, c.getValorSistema());
        stmt.setBigDecimal(4, c.getDiferenca());
        stmt.setString(5, c.getStatus());
        stmt.setString(6, c.getObservacao());
        stmt.setObject(7, c.getIdSupervisorFechamento());
        stmt.setInt(8, c.getIdCaixa());
    }

    @Override
    protected Caixa mapear(ResultSet rs) throws SQLException {
        Caixa c = new Caixa();
        c.setIdCaixa(rs.getInt("id_caixa"));
        c.setIdTerminal(rs.getObject("id_terminal", Integer.class));
        c.setIdOperador(rs.getObject("id_operador", Integer.class));
        java.sql.Timestamp tsAbertura = rs.getTimestamp("data_abertura");
        c.setDataAbertura(tsAbertura != null ? tsAbertura.toLocalDateTime() : null);
        java.sql.Timestamp tsFechamento = rs.getTimestamp("data_fechamento");
        c.setDataFechamento(tsFechamento != null ? tsFechamento.toLocalDateTime() : null);
        c.setValorAbertura(rs.getBigDecimal("valor_abertura"));
        c.setValorFechamento(rs.getBigDecimal("valor_fechamento"));
        c.setValorSistema(rs.getBigDecimal("valor_sistema"));
        c.setDiferenca(rs.getBigDecimal("diferenca"));
        c.setStatus(rs.getString("status"));
        c.setObservacao(rs.getString("observacao"));
        c.setIdSupervisorFechamento(rs.getObject("id_supervisor_fechamento", Integer.class));
        java.sql.Timestamp tsCadastro = rs.getTimestamp("data_cadastro");
        c.setDataCadastro(tsCadastro != null ? tsCadastro.toLocalDateTime() : null);
        return c;
    }

    /** Caixa aberto mais recente (um caixa por vez neste terminal). */
    public Optional<Caixa> findAberto() {
        String sql = "SELECT * FROM caixas WHERE status = 'ABERTO' ORDER BY data_abertura DESC LIMIT 1";
        List<Caixa> result = getJdbc().query(sql, (rs, rowNum) -> mapear(rs));
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    /** Caixas do mais recente para o mais antigo (para a tela de caixa). */
    public List<Caixa> findRecentes() {
        String sql = "SELECT * FROM caixas ORDER BY data_abertura DESC, id_caixa DESC";
        return getJdbc().query(sql, (rs, rowNum) -> mapear(rs));
    }

    public void registrarSangriaSuprimento(Integer idCaixa, String tipo, BigDecimal valor,
                                           String motivo, Integer idOperador) {
        String sql = """
                INSERT INTO sangrias_suprimentos (id_caixa, tipo, valor, motivo, id_operador)
                VALUES (?, ?, ?, ?, ?)
                """;
        getJdbc().update(sql, idCaixa, tipo, valor, motivo, idOperador);
    }

    /** Soma de sangrias ('S') ou suprimentos ('U') de um caixa. */
    public BigDecimal totalSangriasSuprimentos(Integer idCaixa, String tipo) {
        String sql = "SELECT COALESCE(SUM(valor), 0) FROM sangrias_suprimentos WHERE id_caixa = ? AND tipo = ?";
        BigDecimal total = getJdbc().queryForObject(sql, BigDecimal.class, idCaixa, tipo);
        return total != null ? total : BigDecimal.ZERO;
    }

    public List<SangriaSuprimento> listarSangriasSuprimentos(Integer idCaixa) {
        String sql = "SELECT * FROM sangrias_suprimentos WHERE id_caixa = ? ORDER BY data_cadastro";
        return getJdbc().query(sql, (rs, rowNum) -> {
            SangriaSuprimento s = new SangriaSuprimento();
            s.setIdSangriaSuprimento(rs.getInt("id_sangria_suprimento"));
            s.setIdCaixa(rs.getInt("id_caixa"));
            s.setTipo(rs.getString("tipo"));
            s.setValor(rs.getBigDecimal("valor"));
            s.setMotivo(rs.getString("motivo"));
            s.setIdOperador(rs.getObject("id_operador", Integer.class));
            s.setIdSupervisor(rs.getObject("id_supervisor", Integer.class));
            java.sql.Timestamp ts = rs.getTimestamp("data_cadastro");
            s.setDataCadastro(ts != null ? ts.toLocalDateTime() : null);
            return s;
        }, idCaixa);
    }

    /** Total recebido em dinheiro nas vendas finalizadas vinculadas ao caixa. */
    public BigDecimal totalDinheiroVendas(Integer idCaixa) {
        String sql = """
                SELECT COALESCE(SUM(vp.valor), 0)
                FROM vendas_pagamentos vp
                JOIN venda v ON v.id_venda = vp.id_venda
                JOIN formas_pagamento fp ON fp.id_forma_pagamento = vp.id_forma_pagamento
                WHERE v.id_caixa = ? AND v.status = 'FINALIZADA' AND fp.tipo = 'DINHEIRO'
                """;
        BigDecimal total = getJdbc().queryForObject(sql, BigDecimal.class, idCaixa);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Retorna o terminal ativo deste posto, criando o terminal padrão na primeira
     * abertura de caixa (a tabela terminais não tem seed e caixas.id_terminal é NOT NULL).
     */
    public Integer obterOuCriarTerminalPadrao() {
        List<Integer> ids = getJdbc().queryForList(
                "SELECT id_terminal FROM terminais WHERE status = 'ATIVO' ORDER BY id_terminal LIMIT 1",
                Integer.class);
        if (!ids.isEmpty()) {
            return ids.get(0);
        }

        String identificador;
        try {
            identificador = java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            identificador = "TERMINAL-01";
        }

        String sql = """
                INSERT INTO terminais (tipo, nome, identificador_maquina)
                VALUES ('PDV', 'CAIXA 01', ?)
                ON CONFLICT (identificador_maquina)
                DO UPDATE SET status = 'ATIVO', data_atualizacao = CURRENT_TIMESTAMP
                RETURNING id_terminal
                """;
        return getJdbc().queryForObject(sql, Integer.class, identificador);
    }
}
