package com.tienda.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    private final DatabaseConnection dbConnection;
    private static final String DATABASE_NAME = "TiendaElectrodomesticos";

    public DatabaseInitializer() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Inicializa la base de datos completa
     */
    public void initializeDatabase() {
        try {
            System.out.println("Iniciando configuración de base de datos...");

            // Crear base de datos si no existe
            createDatabaseIfNotExists();

            // Crear todas las tablas
            createAllTables();

            // Insertar datos iniciales
            insertInitialData();

            System.out.println("Base de datos configurada correctamente.");

        } catch (Exception e) {
            System.err.println("Error inicializando base de datos: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Fallo en la inicialización de la base de datos", e);
        }
    }

    /**
     * Crea la base de datos si no existe
     */
    private void createDatabaseIfNotExists() {
        String masterUrl = "jdbc:sqlserver://localhost:1433;encrypt=true;trustServerCertificate=true";
        String username = "sa";
        String password = "1234";

        try (Connection masterConn = java.sql.DriverManager.getConnection(masterUrl, username, password)) {

            // Verificar si la base de datos existe
            String checkDbSql = "SELECT name FROM sys.databases WHERE name = ?";
            try (PreparedStatement stmt = masterConn.prepareStatement(checkDbSql)) {
                stmt.setString(1, DATABASE_NAME);
                ResultSet rs = stmt.executeQuery();

                if (!rs.next()) {
                    // La base de datos no existe, crearla
                    System.out.println("Creando base de datos: " + DATABASE_NAME);
                    String createDbSql = "CREATE DATABASE " + DATABASE_NAME;
                    try (Statement createStmt = masterConn.createStatement()) {
                        createStmt.executeUpdate(createDbSql);
                        System.out.println("Base de datos creada exitosamente.");
                    }
                } else {
                    System.out.println("Base de datos ya existe: " + DATABASE_NAME);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error creando base de datos: " + e.getMessage());
            throw new RuntimeException("No se pudo crear la base de datos", e);
        }
    }

    /**
     * Crea todas las tablas necesarias
     */
    private void createAllTables() {
        try (Connection conn = dbConnection.getConnection()) {

            System.out.println("Verificando y creando tablas...");

            // Lista de todas las tablas en orden de dependencias
            createRolesTable(conn);
            createUsersTable(conn);
            createAccessBinnacleTable(conn);
            createClientsTable(conn);
            createProductCategoriesTable(conn);
            createProductsTable(conn);
            createSalesTable(conn);
            createSaleDetailsTable(conn);
            createCreditsTable(conn);
            createQuotasTable(conn);

            System.out.println("Todas las tablas verificadas/creadas correctamente.");

        } catch (SQLException e) {
            throw new RuntimeException("Error creando tablas: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica si una tabla existe
     */
    private boolean tableExists(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        try (ResultSet rs = metaData.getTables(null, null, tableName.toUpperCase(), new String[] { "TABLE" })) {
            return rs.next();
        }
    }

    /**
     * Crea tabla de roles
     */
    private void createRolesTable(Connection conn) throws SQLException {
        if (!tableExists(conn, "roles")) {
            System.out.println("Creando tabla: roles");
            String sql = """
                    CREATE TABLE roles (
                        id INT IDENTITY(1,1) PRIMARY KEY,
                        name NVARCHAR(50) NOT NULL UNIQUE,
                        description NVARCHAR(200),
                        created_at DATETIME2 DEFAULT GETDATE()
                    )
                    """;

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
                System.out.println("Tabla 'roles' creada exitosamente.");
            }
        } else {
            System.out.println("Tabla 'roles' ya existe.");
        }
    }

    /**
     * Crea tabla de usuarios
     */
    private void createUsersTable(Connection conn) throws SQLException {
        if (!tableExists(conn, "users")) {
            System.out.println("Creando tabla: users");
            String sql = """
                    CREATE TABLE users (
                        id INT IDENTITY(1,1) PRIMARY KEY,
                        name NVARCHAR(100) NOT NULL,
                        email NVARCHAR(100) NOT NULL UNIQUE,
                        password NVARCHAR(255) NOT NULL,
                        phone_number NVARCHAR(20),
                        role_id INT NOT NULL,
                        FOREIGN KEY (role_id) REFERENCES roles(id)
                    )
                    """;

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
                System.out.println("Tabla 'users' creada exitosamente.");
            }
        } else {
            System.out.println("Tabla 'users' ya existe.");
        }
    }

    /**
     * Crea tabla de bitácora de acceso
     */
    private void createAccessBinnacleTable(Connection conn) throws SQLException {
        if (!tableExists(conn, "access_binnacle")) {
            System.out.println("Creando tabla: access_binnacle");
            String sql = """
                    CREATE TABLE access_binnacle (
                        id INT IDENTITY(1,1) PRIMARY KEY,
                        entry_date_time DATETIME2 NOT NULL DEFAULT GETDATE(),
                        departure_date_time DATETIME2,
                        ip NVARCHAR(45),
                        user_id INT,
                        FOREIGN KEY (user_id) REFERENCES users(id)
                    )
                    """;

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
                System.out.println("Tabla 'access_binnacle' creada exitosamente.");
            }
        } else {
            System.out.println("Tabla 'access_binnacle' ya existe.");
        }
    }

    /**
     * Crea tabla de clientes
     */
    private void createClientsTable(Connection conn) throws SQLException {
        if (!tableExists(conn, "clients")) {
            System.out.println("Creando tabla: clients");
            String sql = """
                    CREATE TABLE clients (
                        id INT IDENTITY(1,1) PRIMARY KEY,
                        document_type NVARCHAR(20) NOT NULL CHECK (document_type IN ('CC', 'NIT', 'CE', 'PAS')),
                        document_number NVARCHAR(50) NOT NULL UNIQUE,
                        name NVARCHAR(100) NOT NULL,
                        email NVARCHAR(100),
                        phone_number NVARCHAR(20),
                        address NVARCHAR(200)
                    )
                    """;

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
                System.out.println("Tabla 'clients' creada exitosamente.");
            }
        } else {
            System.out.println("Tabla 'clients' ya existe.");
        }
    }

    /**
     * Crea tabla de categorías de productos
     */
    private void createProductCategoriesTable(Connection conn) throws SQLException {
        if (!tableExists(conn, "product_categories")) {
            System.out.println("Creando tabla: product_categories");
            String sql = """
                    CREATE TABLE product_categories (
                        id INT IDENTITY(1,1) PRIMARY KEY,
                        name NVARCHAR(50) NOT NULL UNIQUE,
                        iva DECIMAL(5,4) NOT NULL DEFAULT 0.19,
                        utility DECIMAL(5,4) NOT NULL DEFAULT 0.30,
                    )
                    """;

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
                System.out.println("Tabla 'product_categories' creada exitosamente.");
            }
        } else {
            System.out.println("Tabla 'product_categories' ya existe.");
        }
    }

    /**
     * Crea tabla de productos
     */
    private void createProductsTable(Connection conn) throws SQLException {
        if (!tableExists(conn, "products")) {
            System.out.println("Creando tabla: products");
            String sql = """
                    CREATE TABLE products (
                        id INT IDENTITY(1,1) PRIMARY KEY,
                        code NVARCHAR(50) NOT NULL UNIQUE,
                        name NVARCHAR(100) NOT NULL,
                        description NVARCHAR(500),
                        stock INT NOT NULL DEFAULT 0 CHECK (stock >= 0),
                        acquisition_value DECIMAL(12,2) NOT NULL CHECK (acquisition_value >= 0),
                        sale_value DECIMAL(12,2) NOT NULL CHECK (sale_value >= 0),
                        category_id INT NOT NULL,
                        FOREIGN KEY (category_id) REFERENCES product_categories(id)
                    )
                    """;

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
                System.out.println("Tabla 'products' creada exitosamente.");
            }
        } else {
            System.out.println("Tabla 'products' ya existe.");
        }
    }

    /**
     * Crea tabla de ventas
     */
    private void createSalesTable(Connection conn) throws SQLException {
        if (!tableExists(conn, "sales")) {
            System.out.println("Creando tabla: sales");
            String sql = """
                    CREATE TABLE sales (
                        id INT IDENTITY(1,1) PRIMARY KEY,
                        date DATE NOT NULL DEFAULT CAST(GETDATE() AS DATE),
                        sale_type NVARCHAR(20) NOT NULL CHECK (sale_type IN ('COUNT', 'CREDIT')),
                        subtotal DECIMAL(12,2) NOT NULL CHECK (subtotal >= 0),
                        iva_total DECIMAL(12,2) NOT NULL CHECK (iva_total >= 0),
                        total DECIMAL(12,2) NOT NULL CHECK (total >= 0),
                        client_id INT NOT NULL,
                        user_id INT NOT NULL,
                        FOREIGN KEY (client_id) REFERENCES clients(id),
                        FOREIGN KEY (user_id) REFERENCES users(id)
                    )
                    """;

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
                System.out.println("Tabla 'sales' y trigger creados exitosamente.");
            }
        } else {
            System.out.println("Tabla 'sales' ya existe.");
        }
    }

    /**
     * Crea tabla de detalles de venta
     */
    private void createSaleDetailsTable(Connection conn) throws SQLException {
        if (!tableExists(conn, "sale_details")) {
            System.out.println("Creando tabla: sale_details");
            String sql = """
                    CREATE TABLE sale_details (
                        id INT IDENTITY(1,1) PRIMARY KEY,
                        amount INT NOT NULL CHECK (amount > 0),
                        unit_price DECIMAL(12,2) NOT NULL CHECK (unit_price >= 0),
                        iva_applied DECIMAL(12,2) DEFAULT 0 CHECK (iva_applied >= 0),
                        subtotal DECIMAL(12,2) NOT NULL CHECK (subtotal >= 0),
                        sale_id INT NOT NULL,
                        product_id INT NOT NULL,
                        created_at DATETIME2 DEFAULT GETDATE(),
                        FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE,
                        FOREIGN KEY (product_id) REFERENCES products(id)
                    )
                    """;

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
                System.out.println("Tabla 'sale_details' creada exitosamente.");
            }
        } else {
            System.out.println("Tabla 'sale_details' ya existe.");
        }
    }

    /**
     * Crea tabla de créditos
     */
    private void createCreditsTable(Connection conn) throws SQLException {
        if (!tableExists(conn, "credits")) {
            System.out.println("Creando tabla: credits");
            String sql = """
                    CREATE TABLE credits (
                        id INT IDENTITY(1,1) PRIMARY KEY,
                        initial_quota DECIMAL(12,2) NOT NULL CHECK (initial_quota >= 0),
                        amount_financed DECIMAL(12,2) NOT NULL CHECK (amount_financed >= 0),
                        months INT NOT NULL,
                        interest_rate DECIMAL(5,4) NOT NULL DEFAULT 0.05,
                        created_at DATE NOT NULL DEFAULT CAST(GETDATE() AS DATE),
                        state NVARCHAR(20) NOT NULL DEFAULT 'VIGENTE' CHECK (state IN ('VIGENTE', 'CANCELADO', 'MORA')),
                        sale_id INT NOT NULL UNIQUE,
                        FOREIGN KEY (sale_id) REFERENCES sales(id)
                    )
                    """;

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
                System.out.println("Tabla 'credits' y trigger creados exitosamente.");
            }
        } else {
            System.out.println("Tabla 'credits' ya existe.");
        }
    }

    /**
     * Crea tabla de cuotas
     */
    private void createQuotasTable(Connection conn) throws SQLException {
        if (!tableExists(conn, "quotas")) {
            System.out.println("Creando tabla: quotas");
            String sql = """
                    CREATE TABLE quotas (
                        id INT IDENTITY(1,1) PRIMARY KEY,
                        quota_number INT NOT NULL CHECK (quota_number > 0),
                        expiration_date DATE NOT NULL,
                        quota_value DECIMAL(12,2) NOT NULL CHECK (quota_value >= 0),
                        payed_value DECIMAL(12,2),
                        payed_at DATE,
                        state NVARCHAR(20) NOT NULL DEFAULT 'PENDIENTE'
                            CHECK (state IN ('PENDIENTE', 'PAGADA', 'VENCIDA', 'PARCIAL')),
                        credit_id INT NOT NULL,
                        FOREIGN KEY (credit_id) REFERENCES credits(id) ON DELETE CASCADE
                    )
                    """;

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
                System.out.println("Tabla 'quotas' creada exitosamente.");
            }
        } else {
            System.out.println("Tabla 'quotas' ya existe.");
        }
    }

    /**
     * Inserta datos iniciales en las tablas
     */
    private void insertInitialData() {
        try (Connection conn = dbConnection.getConnection()) {

            System.out.println("Insertando datos iniciales...");

            insertRoles(conn);
            insertProductCategories(conn);
            insertClients(conn);
            insertUsers(conn);
            insertProducts(conn);
            insertPurchases(conn);
            insertPurchaseDetails(conn);
            insertCredits(conn);
            insertQuotas(conn);

            System.out.println("Datos iniciales insertados correctamente.");

        } catch (SQLException e) {
            System.err.println("Error insertando datos iniciales: " + e.getMessage());
        }
    }

    /**
     * Inserta roles iniciales
     */
    private void insertRoles(Connection conn) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM roles";
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(checkSql)) {

            if (rs.next() && rs.getInt(1) == 0) {
                String insertSql = """
                        INSERT INTO roles (name, description) VALUES
                        ('ADMIN', 'Administrador del sistema con acceso completo'),
                        ('GERENTE', 'Gerente con acceso a reportes y configuraciones'),
                        ('VENDEDOR', 'Vendedor con acceso a ventas y clientes'),
                        ('CAJERO', 'Cajero con acceso limitado a ventas de contado'),
                        ('AUDITOR', 'Auditor con acceso solo a consultas y reportes')
                        """;

                try (Statement insertStmt = conn.createStatement()) {
                    insertStmt.executeUpdate(insertSql);
                    System.out.println("Roles iniciales insertados.");
                }
            }
        }
    }

    /**
     * Inserta categorías de productos iniciales
     */
    private void insertProductCategories(Connection conn) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM product_categories";
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(checkSql)) {

            if (rs.next() && rs.getInt(1) == 0) {
                String insertSql = """
                        INSERT INTO product_categories (name, iva, utility) VALUES
                        ('Audio', 0.16, 0.35),
                        ('Video', 0.19, 0.39),
                        ('Tecnología', 0.12, 0.40),
                        ('Cocina', 0.12, 0.35)
                        """;

                try (Statement insertStmt = conn.createStatement()) {
                    insertStmt.executeUpdate(insertSql);
                    System.out.println("Categorías de productos insertadas.");
                }
            }
        }
    }

    /**
     * Inserta clientes de prueba
     */
    private void insertClients(Connection conn) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM clients";
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(checkSql)) {

            if (rs.next() && rs.getInt(1) == 0) {
                String insertSql = """
                        INSERT INTO clients (name, document_type, document_number, phone_number, email, address) VALUES
                        ('Juan Pérez García', 'CC', '1234567890', '3001234567', 'juan.perez@email.com', 'Calle 10 #20-30'),
                        ('María López Rodríguez', 'CC', '0987654321', '3109876543', 'maria.lopez@email.com', 'Carrera 15 #25-40'),
                        ('Carlos Martínez Sánchez', 'CC', '1122334455', '3201122334', 'carlos.martinez@email.com', 'Avenida 20 #30-50'),
                        ('Ana Gómez Torres', 'CC', '5544332211', '3115544332', 'ana.gomez@email.com', 'Calle 5 #10-15'),
                        ('Pedro Ramírez Castro', 'CC', '6677889900', '3126677889', 'pedro.ramirez@email.com', 'Carrera 8 #12-18')
                        """;

                try (Statement insertStmt = conn.createStatement()) {
                    insertStmt.executeUpdate(insertSql);
                    System.out.println("Clientes insertados.");
                }
            }
        }
    }

    /**
     * Inserta usuarios (cajeros) de prueba
     */
    private void insertUsers(Connection conn) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM users";
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(checkSql)) {

            if (rs.next() && rs.getInt(1) == 0) {
                String insertSql = """
                        INSERT INTO users (name, phone_number, password, role_id, email) VALUES
                        ('Laura Hernández', '123', 'cajero123', '1', 'laura.hernandez@empresa.com'),
                        ('Roberto Silva', '123', 'cajero456', '1', 'roberto.silva@empresa.com'),
                        ('Patricia Morales', '123', 'cajero789', '1', 'patricia.morales@empresa.com'),
                        ('Diego Vargas', '123', 'admin123', '1', 'diego.vargas@empresa.com')
                        """;

                try (Statement insertStmt = conn.createStatement()) {
                    insertStmt.executeUpdate(insertSql);
                    System.out.println("Usuarios insertados.");
                }
            }
        }
    }

    /**
     * Inserta productos de prueba
     */
    private void insertProducts(Connection conn) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM products";
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(checkSql)) {

            if (rs.next() && rs.getInt(1) == 0) {
                String insertSql = """
                        INSERT INTO products (code, name, description, stock, acquisition_value, sale_value, category_id) VALUES
                        ('AUD001', 'Parlante Bluetooth JBL', 'Parlante portátil con bluetooth 5.0', 25, 120000, 189000, 1),
                        ('AUD002', 'Audífonos Sony WH-1000XM4', 'Audífonos con cancelación de ruido', 15, 850000, 1350000, 1),
                        ('VID001', 'Televisor Samsung 55"', 'Smart TV 4K UHD', 10, 1800000, 2700000, 2),
                        ('VID002', 'Consola PlayStation 5', 'Consola de videojuegos última generación', 8, 2200000, 3300000, 2),
                        ('TEC001', 'Laptop HP Pavilion', 'Intel i5, 8GB RAM, 512GB SSD', 12, 1900000, 2850000, 3),
                        ('TEC002', 'Tablet Samsung Galaxy Tab S8', 'Pantalla 11", 128GB', 20, 1200000, 1800000, 3),
                        ('TEC003', 'Celular iPhone 13', '128GB, Cámara 12MP', 18, 2800000, 4200000, 3),
                        ('COC001', 'Licuadora Oster', '600W, 3 velocidades', 30, 180000, 270000, 4),
                        ('COC002', 'Microondas Samsung', '1.1 cu ft, 1000W', 15, 350000, 525000, 4),
                        ('COC003', 'Cafetera Nespresso', 'Sistema de cápsulas', 22, 420000, 630000, 4)
                        """;

                try (Statement insertStmt = conn.createStatement()) {
                    insertStmt.executeUpdate(insertSql);
                    System.out.println("Productos insertados.");
                }
            }
        }
    }

    /**
     * Inserta ventas de prueba
     */
    private void insertPurchases(Connection conn) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM sales";
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(checkSql)) {

            if (rs.next() && rs.getInt(1) == 0) {
                String insertSql = """
                        INSERT INTO sales (date, sale_type, subtotal, iva_total, total, client_id, user_id) VALUES
                        -- Ventas del mes actual (Septiembre 2025)
                        ('2025-09-28', 'COUNT', 2700000, 513000, 3213000, 1, 1),
                        ('2025-09-27', 'CREDIT', 4200000, 504000, 4704000, 2, 2),
                        ('2025-09-26', 'COUNT', 1800000, 216000, 2016000, 3, 1),
                        ('2025-09-25', 'CREDIT', 2850000, 342000, 3192000, 4, 3),
                        ('2025-09-24', 'COUNT', 630000, 75600, 705600, 5, 4),
                        ('2025-09-23', 'COUNT', 270000, 32400, 302400, 1, 4),
                        ('2025-09-22', 'CREDIT', 3300000, 627000, 3927000, 2, 3),
                        ('2025-09-21', 'COUNT', 1350000, 216000, 1566000, 3, 2),
                        ('2025-09-20', 'COUNT', 525000, 63000, 588000, 4, 1),
                        ('2025-09-19', 'CREDIT', 1800000, 216000, 2016000, 5, 3),

                        -- Ventas de agosto 2025
                        ('2025-08-15', 'COUNT', 189000, 30240, 219240, 1, 2),
                        ('2025-08-10', 'CREDIT', 4200000, 504000, 4704000, 5, 1),
                        ('2025-08-05', 'COUNT', 2700000, 513000, 3213000, 2, 3),

                        -- Ventas de julio 2025
                        ('2025-07-20', 'CREDIT', 2850000, 342000, 3192000, 3, 2),
                        ('2025-07-15', 'COUNT', 1350000, 216000, 1566000, 4, 1),

                        -- Ventas de junio 2025
                        ('2025-06-25', 'COUNT', 630000, 75600, 705600, 5, 3),
                        ('2025-06-18', 'CREDIT', 3300000, 627000, 3927000, 1, 2),
                        ('2025-06-10', 'COUNT', 270000, 32400, 302400, 2, 1)
                        """;

                try (Statement insertStmt = conn.createStatement()) {
                    insertStmt.executeUpdate(insertSql);
                    System.out.println("Ventas insertadas.");
                }
            }
        }
    }

    /**
     * Inserta detalles de ventas de prueba
     */
    private void insertPurchaseDetails(Connection conn) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM sale_details";
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(checkSql)) {

            if (rs.next() && rs.getInt(1) == 0) {
                String insertSql = """
                        INSERT INTO sale_details (amount, unit_price, iva_applied, subtotal, sale_id, product_id) VALUES
                        -- Venta 1: TV Samsung
                        (1, 2700000, 513000, 2700000, 4, 3),

                        -- Venta 2: iPhone 13
                        (1, 4200000, 504000, 4200000, 4, 7),

                        -- Venta 3: Tablet Samsung
                        (1, 1800000, 216000, 1800000, 4, 6),

                        -- Venta 4: Laptop HP
                        (1, 2850000, 342000, 2850000, 4, 5),

                        -- Venta 5: Cafetera + Licuadora
                        (1, 630000, 75600, 630000, 5, 10),

                        -- Venta 6: Licuadora
                        (1, 270000, 32400, 270000, 6, 8),

                        -- Venta 7: PS5
                        (1, 3300000, 627000, 3300000, 7, 4),

                        -- Venta 8: Audífonos Sony
                        (1, 1350000, 216000, 1350000, 8, 2),

                        -- Venta 9: Microondas
                        (1, 525000, 63000, 525000, 9, 9),

                        -- Venta 10: Tablet Samsung
                        (1, 1800000, 216000, 1800000, 10, 6),

                        -- Venta 11: Parlante JBL
                        (1, 189000, 30240, 189000, 11, 1),

                        -- Venta 12: iPhone 13
                        (1, 4200000, 504000, 4200000, 12, 7),

                        -- Venta 13: TV Samsung
                        (1, 2700000, 513000, 2700000, 13, 3),

                        -- Venta 14: Laptop HP
                        (1, 2850000, 342000, 2850000, 14, 5),

                        -- Venta 15: Audífonos Sony
                        (1, 1350000, 216000, 1350000, 15, 2),

                        -- Venta 16: Cafetera
                        (1, 630000, 75600, 630000, 16, 10),

                        -- Venta 17: PS5
                        (1, 3300000, 627000, 3300000, 17, 4),

                        -- Venta 18: Licuadora
                        (1, 270000, 32400, 270000, 18, 8)
                        """;

                try (Statement insertStmt = conn.createStatement()) {
                    insertStmt.executeUpdate(insertSql);
                    System.out.println("Detalles de ventas insertados.");
                }
            }
        }
    }

    /**
     * Inserta créditos de prueba
     */
    private void insertCredits(Connection conn) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM credits";
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(checkSql)) {

            if (rs.next() && rs.getInt(1) == 0) {
                String insertSql = """
                        INSERT INTO credits (initial_quota, amount_financed, months, interest_rate, created_at, state, sale_id) VALUES
                        -- Crédito 1: iPhone 13 (Venta 2)
                        (940800, 3763200, 12, 2.5, '2025-09-27', 'VIGENTE', 4),

                        -- Crédito 2: Laptop HP (Venta 4)
                        (638400, 2553600, 6, 2.0, '2025-09-25', 'VIGENTE', 5),

                        -- Crédito 3: PS5 (Venta 7)
                        (785400, 3141600, 12, 2.5, '2025-09-22', 'VIGENTE', 7),

                        -- Crédito 4: Tablet Samsung (Venta 10)
                        (403200, 1612800, 6, 2.0, '2025-09-19', 'VIGENTE', 10),

                        -- Crédito 5: iPhone 13 (Venta 12) - Agosto
                        (940800, 3763200, 12, 2.5, '2025-08-10', 'VIGENTE', 12),

                        -- Crédito 6: Laptop HP (Venta 14) - Julio
                        (638400, 2553600, 6, 2.0, '2025-07-20', 'VIGENTE', 14),

                        -- Crédito 7: PS5 (Venta 17) - Junio
                        (785400, 3141600, 12, 2.5, '2025-06-18', 'VIGENTE', 17)
                        """;

                try (Statement insertStmt = conn.createStatement()) {
                    insertStmt.executeUpdate(insertSql);
                    System.out.println("Créditos insertados.");
                }
            }
        }
    }

    /**
     * Inserta cuotas de prueba para los créditos
     */
    private void insertQuotas(Connection conn) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM quotas";
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(checkSql)) {

            if (rs.next() && rs.getInt(1) == 0) {
                String insertSql = """
                        INSERT INTO quotas (quota_number, expiration_date, quota_value, payed_value, payed_at, state, credit_id) VALUES
                        -- Cuotas para Crédito 1 (12 meses - iPhone)
                        (1, '2025-10-27', 313600, 313600, '2025-10-25', 'PAGADA', 6),
                        (2, '2025-11-27', 313600, NULL, NULL, 'PENDIENTE', 6),
                        (3, '2025-12-27', 313600, NULL, NULL, 'PENDIENTE', 6),
                        (4, '2026-01-27', 313600, NULL, NULL, 'PENDIENTE', 6),
                        (5, '2026-02-27', 313600, NULL, NULL, 'PENDIENTE', 6),
                        (6, '2026-03-27', 313600, NULL, NULL, 'PENDIENTE', 6),
                        (7, '2026-04-27', 313600, NULL, NULL, 'PENDIENTE', 6),
                        (8, '2026-05-27', 313600, NULL, NULL, 'PENDIENTE', 6),
                        (9, '2026-06-27', 313600, NULL, NULL, 'PENDIENTE', 6),
                        (10, '2026-07-27', 313600, NULL, NULL, 'PENDIENTE', 6),
                        (11, '2026-08-27', 313600, NULL, NULL, 'PENDIENTE', 6),
                        (12, '2026-09-27', 313600, NULL, NULL, 'PENDIENTE', 6),

                        -- Cuotas para Crédito 2 (6 meses - Laptop)
                        (1, '2025-10-25', 425600, 425600, '2025-10-23', 'PAGADA', 7),
                        (2, '2025-11-25', 425600, NULL, NULL, 'PENDIENTE', 7),
                        (3, '2025-12-25', 425600, NULL, NULL, 'PENDIENTE', 7),
                        (4, '2026-01-25', 425600, NULL, NULL, 'PENDIENTE', 7),
                        (5, '2026-02-25', 425600, NULL, NULL, 'PENDIENTE', 7),
                        (6, '2026-03-25', 425600, NULL, NULL, 'PENDIENTE', 7),

                        -- Cuotas para Crédito 3 (12 meses - PS5)
                        (1, '2025-10-22', 261800, 261800, '2025-10-20', 'PAGADA', 8),
                        (2, '2025-11-22', 261800, NULL, NULL, 'PENDIENTE', 8),
                        (3, '2025-12-22', 261800, NULL, NULL, 'PENDIENTE', 8),
                        (4, '2026-01-22', 261800, NULL, NULL, 'PENDIENTE', 8),
                        (5, '2026-02-22', 261800, NULL, NULL, 'PENDIENTE', 8),
                        (6, '2026-03-22', 261800, NULL, NULL, 'PENDIENTE', 8),
                        (7, '2026-04-22', 261800, NULL, NULL, 'PENDIENTE', 8),
                        (8, '2026-05-22', 261800, NULL, NULL, 'PENDIENTE', 8),
                        (9, '2026-06-22', 261800, NULL, NULL, 'PENDIENTE', 8),
                        (10, '2026-07-22', 261800, NULL, NULL, 'PENDIENTE', 8),
                        (11, '2026-08-22', 261800, NULL, NULL, 'PENDIENTE', 8),
                        (12, '2026-09-22', 261800, NULL, NULL, 'PENDIENTE', 8),

                        -- Cuotas para Crédito 4 (6 meses - Tablet)
                        (1, '2025-10-19', 268800, 268800, '2025-10-18', 'PAGADA', 9),
                        (2, '2025-11-19', 268800, NULL, NULL, 'PENDIENTE', 9),
                        (3, '2025-12-19', 268800, NULL, NULL, 'PENDIENTE', 9),
                        (4, '2026-01-19', 268800, NULL, NULL, 'PENDIENTE', 9),
                        (5, '2026-02-19', 268800, NULL, NULL, 'PENDIENTE', 9),
                        (6, '2026-03-19', 268800, NULL, NULL, 'PENDIENTE', 9),

                        -- Cuotas para Crédito 5 (12 meses - iPhone Agosto) - algunas pagadas
                        (1, '2025-09-10', 313600, 313600, '2025-09-08', 'PAGADA', 10),
                        (2, '2025-10-10', 313600, NULL, NULL, 'VENCIDA', 10),
                        (3, '2025-11-10', 313600, NULL, NULL, 'PENDIENTE', 10),
                        (4, '2025-12-10', 313600, NULL, NULL, 'PENDIENTE', 10),
                        (5, '2026-01-10', 313600, NULL, NULL, 'PENDIENTE', 10),
                        (6, '2026-02-10', 313600, NULL, NULL, 'PENDIENTE', 10),
                        (7, '2026-03-10', 313600, NULL, NULL, 'PENDIENTE', 10),
                        (8, '2026-04-10', 313600, NULL, NULL, 'PENDIENTE', 10),
                        (9, '2026-05-10', 313600, NULL, NULL, 'PENDIENTE', 10),
                        (10, '2026-06-10', 313600, NULL, NULL, 'PENDIENTE', 10),
                        (11, '2026-07-10', 313600, NULL, NULL, 'PENDIENTE', 10),
                        (12, '2026-08-10', 313600, NULL, NULL, 'PENDIENTE', 10)
                        """;

                try (Statement insertStmt = conn.createStatement()) {
                    insertStmt.executeUpdate(insertSql);
                    System.out.println("Cuotas insertadas.");
                }
            }
        }
    }

}