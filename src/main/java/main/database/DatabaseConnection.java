package main.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Conexões
    private static Connection connectionBanco1;
    private static Connection connectionBanco2;

    public static Connection getConnectionLicenses() throws SQLException {
        if (connectionBanco1 == null || connectionBanco1.isClosed()) {
            try {
                Class.forName(Data_Config.Geral.DRIVER);
                connectionBanco1 = DriverManager.getConnection(
                    Data_Config.Banco1.URL,
                    Data_Config.Banco1.USER,
                    Data_Config.Banco1.PASSWORD
                );
                System.out.println("Conexão com o banco " + Data_Config.Banco1.NOME + " estabelecida com sucesso!");
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver PostgreSQL não encontrado!", e);
            }
        }
        return connectionBanco1;
    }

    public static Connection getConnectionMercado() throws SQLException {
        if (connectionBanco2 == null || connectionBanco2.isClosed()) {
            try {
                Class.forName(Data_Config.Geral.DRIVER);
                connectionBanco2 = DriverManager.getConnection(
                    Data_Config.Banco2.URL,
                    Data_Config.Banco2.USER,
                    Data_Config.Banco2.PASSWORD
                );
                System.out.println("Conexão com o banco " + Data_Config.Banco2.NOME + " estabelecida com sucesso!");
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver PostgreSQL não encontrado!", e);
            }
        }
        return connectionBanco2;
    }

    public static void closeConnectionLicenses() {
        if (connectionBanco1 != null) {
            try {
                connectionBanco1.close();
                System.out.println("Conexão com o banco " + Data_Config.Banco1.NOME + " fechada!");
            } catch (SQLException e) {
                System.err.println("Erro ao fechar conexão com o banco " + Data_Config.Banco1.NOME + ": " + e.getMessage());
            }
        }
    }

    public static void closeConnectionMercado() {
        if (connectionBanco2 != null) {
            try {
                connectionBanco2.close();
                System.out.println("Conexão com o banco " + Data_Config.Banco2.NOME + " fechada!");
            } catch (SQLException e) {
                System.err.println("Erro ao fechar conexão com o banco " + Data_Config.Banco2.NOME + ": " + e.getMessage());
            }
        }
    }

    public static void closeAllConnections() {
        closeConnectionLicenses();
        closeConnectionMercado();
    }
}
