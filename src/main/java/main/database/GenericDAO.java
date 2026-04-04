package main.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
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
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public T save(T entidade) {
        jdbcTemplate.update(con -> {
            PreparedStatement stmt = con.prepareStatement(getSqlInsert(), Statement.RETURN_GENERATED_KEYS);
            setParametrosInsert(stmt, entidade);
            return stmt;
        });

        // We need a KeyHolder to get the generated key
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement stmt = con.prepareStatement(getSqlInsert(), Statement.RETURN_GENERATED_KEYS);
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
    // Wrappers estáticos para uso em controllers JavaFX sem @Autowired
    // ==============================

    @SuppressWarnings("unchecked")
    public static <T, D extends GenericDAO<T, ?>> java.util.List<T> findAllStatic(Class<D> daoClass) {
        try {
            return main.util.SpringContextProvider.getBean(daoClass).findAll();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar todos os registros de " + daoClass.getSimpleName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T, ID, D extends GenericDAO<T, ID>> T findByIdStatic(Class<D> daoClass, ID id) {
        try {
            return main.util.SpringContextProvider.getBean(daoClass).findById(id).orElse(null);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar registro por ID em " + daoClass.getSimpleName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T, ID, D extends GenericDAO<T, ID>> boolean deleteByIdStatic(Class<D> daoClass, ID id) {
        try {
            return main.util.SpringContextProvider.getBean(daoClass).deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao deletar registro em " + daoClass.getSimpleName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T, D extends GenericDAO<T, ?>> T insertStatic(Class<D> daoClass, T entidade) {
        try {
            return main.util.SpringContextProvider.getBean(daoClass).save(entidade);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao inserir registro em " + daoClass.getSimpleName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T, D extends GenericDAO<T, ?>> boolean updateStatic(Class<D> daoClass, T entidade) {
        try {
            return main.util.SpringContextProvider.getBean(daoClass).update(entidade);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao atualizar registro em " + daoClass.getSimpleName(), e);
        }
    }

    // ==============================
    // Sobrescrever nos DAOs filhos quando necessário
    // ==============================

    protected void setGeneratedId(T entidade, Number id) {}
}
