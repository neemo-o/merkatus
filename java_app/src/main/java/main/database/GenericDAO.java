package main.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    // Operações genéricas (agora via JdbcTemplate)
    // ==============================

    public List<T> findAll() {
        String sql = "SELECT * FROM " + getTabela() + " ORDER BY " + getColunaId();
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapear(rs));
    }

    public Optional<T> findById(ID id) {
        String sql = "SELECT * FROM " + getTabela() + " WHERE " + getColunaId() + " = ?";
        List<T> result = jdbcTemplate.query(sql, (rs, rowNum) -> mapear(rs), id);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    public boolean deleteById(ID id) {
        String sql = "DELETE FROM " + getTabela() + " WHERE " + getColunaId() + " = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM " + getTabela();
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
    // Sobrescrever nos DAOs filhos quando necessário
    // ==============================

    protected void setGeneratedId(T entidade, Number id) {
    }
}
