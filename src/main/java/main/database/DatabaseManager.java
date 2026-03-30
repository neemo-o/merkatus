package main.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class DatabaseManager {

    @Autowired
    @Qualifier("oficialDataSource")
    private DataSource oficialDataSource;

    @Autowired
    @Qualifier("licencasDataSource")
    private DataSource licencasDataSource;

    public Connection getOficialConnection() throws SQLException {
        return oficialDataSource.getConnection();
    }

    public Connection getLicencasConnection() throws SQLException {
        return licencasDataSource.getConnection();
    }
}