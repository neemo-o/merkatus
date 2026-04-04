package main.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import main.util.SpringContextProvider;

public abstract class GenericDAO<T, ID> {

    @Autowired
    private DatabaseManager databaseManager;

    // ==============================
    // Conexão
    // ==============================

    protected Connection getConnection() throws SQLException {
        return databaseManager.getOficialConnection();
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
    // Operações genéricas prontas
    // ==============================

    public List<T> findAll() throws SQLException {
        List<T> lista = new ArrayList<>();
        String sql = "SELECT * FROM " + getTabela() + " ORDER BY " + getColunaId();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Optional<T> findById(ID id) throws SQLException {
        String sql = "SELECT * FROM " + getTabela() + " WHERE " + getColunaId() + " = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        }
        return Optional.empty();
    }

    public boolean deleteById(ID id) throws SQLException {
        String sql = "DELETE FROM " + getTabela() + " WHERE " + getColunaId() + " = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public long count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTabela();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) return rs.getLong(1);
        }
        return 0;
    }

    public static <T, ID, D extends GenericDAO<T, ID>> java.util.List<T> findAllStatic(Class<D> daoClass) {
        try {
            return SpringContextProvider.getBean(daoClass).findAll();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T, ID, D extends GenericDAO<T, ID>> T findByIdStatic(Class<D> daoClass, ID id) {
        try {
            return SpringContextProvider.getBean(daoClass).findById(id).orElse(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T, ID, D extends GenericDAO<T, ID>> boolean deleteByIdStatic(Class<D> daoClass, ID id) {
        try {
            return SpringContextProvider.getBean(daoClass).deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T, ID, D extends GenericDAO<T, ID>> T insertStatic(Class<D> daoClass, T entidade) {
        try {
            return SpringContextProvider.getBean(daoClass).save(entidade);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T, ID, D extends GenericDAO<T, ID>> boolean updateStatic(Class<D> daoClass, T entidade) {
        try {
            return SpringContextProvider.getBean(daoClass).update(entidade);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public T save(T entidade) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     getSqlInsert(), Statement.RETURN_GENERATED_KEYS)) {

            setParametrosInsert(stmt, entidade);
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    setIdGerado(entidade, keys);
                }
            }
        }
        return entidade;
    }

    public boolean update(T entidade) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(getSqlUpdate())) {

            setParametrosUpdate(stmt, entidade);
            return stmt.executeUpdate() > 0;
        }
    }

    // Sobrescreva nos DAOs que precisam retornar o ID gerado
    protected void setIdGerado(T entidade, ResultSet keys) throws SQLException {}
}