package com.tienda.dao;

import com.tienda.model.Client;
import com.tienda.model.Purchase;
import com.tienda.model.User;
import com.tienda.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PurchaseDAO {
    private final DatabaseConnection dbConnection;
    private final ClientDAO clientDAO;
    private final UserDAO userDAO;

    public PurchaseDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
        this.clientDAO = new ClientDAO();
        this.userDAO = new UserDAO();
    }

    public List<Purchase> findWithFilters(
            LocalDate startDate,
            LocalDate endDate,
            String saleType,
            String clientName,
            String userName,
            String saleId,
            Double minAmount,
            Double maxAmount) {

        StringBuilder sql = new StringBuilder("""
                SELECT p.*,
                       c.name as client_name, c.document_number as client_document,
                       u.name as user_name, u.email as user_username
                FROM sales p
                LEFT JOIN clients c ON p.client_id = c.id
                LEFT JOIN users u ON p.user_id = u.id
                WHERE 1=1
                """);

        List<Object> params = new ArrayList<>();

        if (startDate != null) {
            sql.append(" AND p.date >= ?");
            params.add(Date.valueOf(startDate));
        }

        if (endDate != null) {
            sql.append(" AND p.date <= ?");
            params.add(Date.valueOf(endDate));
        }

        if (saleType != null && !"TODOS".equals(saleType)) {
            String dbType = "CONTADO".equals(saleType) ? "COUNT" : "CREDIT";
            sql.append(" AND p.sale_type = ?");
            params.add(dbType);
        }

        if (clientName != null && !clientName.isEmpty()) {
            sql.append(" AND c.name LIKE ?");
            params.add("%" + clientName + "%");
        }

        if (userName != null && !userName.isEmpty()) {
            sql.append(" AND u.name LIKE ?");
            params.add("%" + userName + "%");
        }

        if (saleId != null && !saleId.isEmpty()) {
            try {
                int id = Integer.parseInt(saleId);
                sql.append(" AND p.id = ?");
                params.add(id);
            } catch (NumberFormatException e) {
                // Ignorar si no es número válido
            }
        }

        if (minAmount != null) {
            sql.append(" AND p.total >= ?");
            params.add(minAmount);
        }

        if (maxAmount != null) {
            sql.append(" AND p.total <= ?");
            params.add(maxAmount);
        }

        sql.append(" ORDER BY p.date DESC, p.id DESC");

        List<Purchase> purchases = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Purchase purchase = new Purchase();
                purchase.setId(rs.getInt("id"));
                purchase.setDate(rs.getDate("date").toLocalDate());
                purchase.setSaleType(rs.getString("sale_type"));
                purchase.setSubtotal(rs.getDouble("subtotal"));
                purchase.setIvaTotal(rs.getDouble("iva_total"));
                purchase.setTotal(rs.getDouble("total"));

                // Cliente
                Client client = new Client();
                client.setName(rs.getString("client_name"));
                // client.setDocument(rs.getString("client_document"));
                purchase.setClient(client);

                // Usuario
                User user = new User();
                user.setName(rs.getString("user_name"));
                // user.setUsername(rs.getString("user_username"));
                purchase.setUser(user);

                purchases.add(purchase);
            }

        } catch (SQLException e) {
            System.err.println("Error buscando ventas con filtros: " + e.getMessage());
        }

        return purchases;
    }

    public boolean save(Purchase sale) {
        String sql = "INSERT INTO sales (date, sale_type, subtotal, iva_total, total, client_id, user_id) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setDate(1, Date.valueOf(sale.getDate()));
            stmt.setString(2, sale.getSaleType());
            stmt.setDouble(3, sale.getSubtotal());
            stmt.setDouble(4, sale.getIvaTotal());
            stmt.setDouble(5, sale.getTotal());
            stmt.setInt(6, sale.getClient().getId());
            stmt.setInt(7, sale.getUser().getId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    sale.setId(rs.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error guardando venta: " + e.getMessage());
        }
        return false;
    }

    public Purchase findById(int id) {
        String sql = "SELECT * FROM sales WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToSale(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error buscando venta: " + e.getMessage());
        }
        return null;
    }

    public List<Purchase> findAll() {
        List<Purchase> sales = new ArrayList<>();
        String sql = "SELECT * FROM sales ORDER BY date DESC";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                sales.add(mapResultSetToSale(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo todas las ventas: " + e.getMessage());
        }
        return sales;
    }

    // Consulta específica: Total de ventas por mes
    public double getTotalSalesByMonth(int year, int month) {
        String sql = "SELECT COALESCE(SUM(total), 0) as total FROM sales WHERE YEAR(date) = ? AND MONTH(date) = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, year);
            stmt.setInt(2, month);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total");
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo total de ventas por mes: " + e.getMessage());
        }
        return 0.0;
    }

    // Consulta específica: Cantidad de ventas por tipo en un período
    public Object[] getSalesCountByTypeAndPeriod(LocalDate startDate, LocalDate endDate) {
        String sql = """
                SELECT
                    SUM(CASE WHEN sale_type = 'CONTADO' THEN 1 ELSE 0 END) as contado,
                    SUM(CASE WHEN sale_type = 'CREDITO' THEN 1 ELSE 0 END) as credito
                FROM sales
                WHERE date BETWEEN ? AND ?
                """;

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Object[] {
                        rs.getInt("contado"),
                        rs.getInt("credito")
                };
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo cantidad de ventas por tipo: " + e.getMessage());
        }
        return new Object[] { 0, 0 };
    }

    private Purchase mapResultSetToSale(ResultSet rs) throws SQLException {
        Purchase sale = new Purchase();
        sale.setId(rs.getInt("id"));
        sale.setDate(rs.getDate("date").toLocalDate());
        sale.setSaleType(rs.getString("sale_type"));
        sale.setSubtotal(rs.getDouble("subtotal"));
        sale.setIvaTotal(rs.getDouble("iva_total"));
        sale.setTotal(rs.getDouble("total"));

        // Cargar cliente y usuario
        sale.setClient(clientDAO.findById(rs.getInt("client_id")));
        sale.setUser(userDAO.findById(rs.getInt("user_id")));

        return sale;
    }
}
