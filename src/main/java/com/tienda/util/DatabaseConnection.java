    package com.tienda.util;

    import java.sql.Connection;
    import java.sql.DriverManager;
    import java.sql.SQLException;

    public class DatabaseConnection {
        private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=TiendaElectrodomesticos;encrypt=true;trustServerCertificate=true";
        private static final String USERNAME = "sa";
        private static final String PASSWORD = "1234";
        
        private static DatabaseConnection instance;
        
        private DatabaseConnection() {}
        
        public static DatabaseConnection getInstance() {
            if (instance == null) {
                synchronized (DatabaseConnection.class) {
                    if (instance == null) {
                        instance = new DatabaseConnection();
                    }
                }
            }
            return instance;
        }
        
        public Connection getConnection() throws SQLException {
            try {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                return DriverManager.getConnection(URL, USERNAME, PASSWORD);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver de SQL Server no encontrado", e);
            }
        }
    
    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error cerrando conexión: " + e.getMessage());
            }
        }
    }
}