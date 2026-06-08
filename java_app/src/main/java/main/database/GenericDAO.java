package main.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public abstract class GenericDAO<T, ID> {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    protected JdbcTemplate getJdbc() {
        return jdbcTemplate;
    }

    // ==============================
    // Métodos que cada DAO implementa
    // ==============================

    protected abstract String getTabela();
    protected abstract String getColunaId();
    protected abstract T mapear(ResultSet rs) throws SQLException;
    protected abstract void setParametrosInsert(PreparedStatement stmt, T entidade) throws SQLException;
    protected abstract void setParametrosUpdate(PreparedStatement stmt, T entidade) throws SQLException;
    protected abstract String getSqlInsert();
    protected abstract String getSqlUpdate();

    // ==============================
    // 🆕 Configuração de Soft Delete
    // ==============================

    /**
     * Retorna o nome da coluna que controla o status (ex: "ativo").
     * Se retornar null, o DAO usará Hard Delete (DELETE físico).
     */
    protected String getColunaAtivo() {
        return null; // Padrão: Hard Delete
    }

    // ==============================
    // Operações genéricas (COM soft delete)
    // ==============================

    public List<T> findAll() {
        String sql = "SELECT * FROM " + getTabela();
        
        String coluna = getColunaAtivo();
        if (coluna != null) {
            sql += " WHERE " + coluna + " = true";
        }
        
        sql += " ORDER BY " + getColunaId();
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapear(rs));
    }

    public Optional<T> findById(ID id) {
        String sql = "SELECT * FROM " + getTabela() + " WHERE " + getColunaId() + " = ?";
        
        String coluna = getColunaAtivo();
        if (coluna != null) {
            sql += " AND " + coluna + " = true";
        }
        
        List<T> result = jdbcTemplate.query(sql, (rs, rowNum) -> mapear(rs), id);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    public boolean deleteById(ID id) {
        String colunaAtivo = getColunaAtivo();

        if (colunaAtivo != null) {
            // ✅ SOFT DELETE: Atualiza a coluna para false
            String sql = "UPDATE " + getTabela() + " SET " + colunaAtivo + " = false, data_atualizacao = ? WHERE " + getColunaId() + " = ?";
            return jdbcTemplate.update(sql, LocalDateTime.now(), id) > 0;
        } else {
            // ❌ HARD DELETE: Remove fisicamente
            String sql = "DELETE FROM " + getTabela() + " WHERE " + getColunaId() + " = ?";
            return jdbcTemplate.update(sql, id) > 0;
        }
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM " + getTabela();
        
        String coluna = getColunaAtivo();
        if (coluna != null) {
            sql += " WHERE " + coluna + " = true";
        }
        
        Long result = jdbcTemplate.queryForObject(sql, Long.class);
        return result != null ? result : 0L;
    }

    public T save(T entidade) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement stmt = con.prepareStatement(getSqlInsert(), new String[]{ getColunaId() });
            setParametrosInsert(stmt, entidade);
            return stmt;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            setGeneratedId(entidade, key);
        }

        return entidade;
    }

    public boolean update(T entidade) {
        return jdbcTemplate.update(con -> {
            PreparedStatement stmt = con.prepareStatement(getSqlUpdate());
            setParametrosUpdate(stmt, entidade);
            return stmt;
        }) > 0;
    }

    // ==============================
    // Métodos auxiliares para Lixeira
    // ==============================

    /**
     * Busca todos os registros, incluindo inativos.
     */
    public List<T> findAllIncludingInactive() {
        String sql = "SELECT * FROM " + getTabela() + " ORDER BY " + getColunaId();
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapear(rs));
    }

    /**
     * Busca um registro pelo ID, incluindo inativos.
     */
    public Optional<T> findByIdIncludingInactive(ID id) {
        String sql = "SELECT * FROM " + getTabela() + " WHERE " + getColunaId() + " = ?";
        List<T> result = jdbcTemplate.query(sql, (rs, rowNum) -> mapear(rs), id);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    protected void setGeneratedId(T entidade, Number id) {
        // Implementação padrão vazia
    }
}