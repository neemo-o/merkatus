package main.database;

public class Data_Config {

    // licenças
    public static final class Banco1 {
        public static final String URL = "jdbc:postgresql://localhost:5432/erp_licencas";
        public static final String USER = "postgres";
        public static final String PASSWORD = "postgres";
        public static final String NOME = "ERP_LICENCAS";
    }

    // mercado
    public static final class Banco2 {
        public static final String URL = "jdbc:postgresql://localhost:5432/erp_oficial";
        public static final String USER = "postgres";
        public static final String PASSWORD = "postgres";
        public static final String NOME = "ERP_MERCADO";
    }

    // Configurações gerais do banco
    public static final class Geral {
        public static final String DRIVER = "org.postgresql.Driver";
        public static final int TIMEOUT = 30; 
    }
}
