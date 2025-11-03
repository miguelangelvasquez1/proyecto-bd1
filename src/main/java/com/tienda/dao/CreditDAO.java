package com.tienda.dao;

import com.tienda.model.Credit;
import com.tienda.model.Purchase;
import com.tienda.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CreditDAO {
    private final DatabaseConnection dbConnection;
    private final PurchaseDAO saleDAO;

    public CreditDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
        this.saleDAO = new PurchaseDAO();
    }

    public Credit findBySaleId(int saleId) {
        String sql = """
                SELECT c.*, p.date as sale_date, p.total as sale_total
                FROM credits c
                INNER JOIN sales p ON c.sale_id = p.id
                WHERE c.sale_id = ?
                """;

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, saleId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Credit credit = new Credit();
                credit.setId(rs.getInt("id"));
                credit.setInitialQuota(rs.getDouble("initial_quota"));
                credit.setAmountFinanced(rs.getDouble("amount_financed"));
                credit.setMonths(rs.getInt("months"));
                credit.setInterestRate(rs.getDouble("interest_rate"));
                credit.setCreatedAt(rs.getDate("created_at").toLocalDate());
                credit.setState(rs.getString("state"));

                // Información básica de la venta
                Purchase sale = new Purchase();
                sale.setId(saleId);
                sale.setDate(rs.getDate("sale_date").toLocalDate());
                sale.setTotal(rs.getDouble("sale_total"));
                credit.setSale(sale);

                return credit;
            }

        } catch (SQLException e) {
            System.err.println("Error buscando crédito: " + e.getMessage());
        }

        return null;
    }

    public boolean save(Credit credit) {
        String sql = "INSERT INTO credits (initial_quota, amount_financed, months, interest_rate, created_at, state, sale_id) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setDouble(1, credit.getInitialQuota());
            stmt.setDouble(2, credit.getAmountFinanced());
            stmt.setInt(3, credit.getMonths());
            stmt.setDouble(4, credit.getInterestRate());
            stmt.setDate(5, Date.valueOf(credit.getCreatedAt()));
            stmt.setString(6, credit.getState());
            stmt.setInt(7, credit.getSale().getId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    credit.setId(rs.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error guardando crédito: " + e.getMessage());
        }
        return false;
    }

    public Credit findById(int id) {
        String sql = "SELECT * FROM credits WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToCredit(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error buscando crédito: " + e.getMessage());
        }
        return null;
    }

    // public Credit findBySaleId(int saleId) {
    //     String sql = "SELECT * FROM credits WHERE sale_id = ?";

    //     try (Connection conn = dbConnection.getConnection();
    //             PreparedStatement stmt = conn.prepareStatement(sql)) {

    //         stmt.setInt(1, saleId);
    //         ResultSet rs = stmt.executeQuery();

    //         if (rs.next()) {
    //             return mapResultSetToCredit(rs);
    //         }

    //     } catch (SQLException e) {
    //         System.err.println("Error buscando crédito por venta: " + e.getMessage());
    //     }
    //     return null;
    // }

    public List<Credit> findByClientId(int clientId) {
        List<Credit> credits = new ArrayList<>();
        String sql = """
                SELECT c.* FROM credits c
                INNER JOIN sales s ON c.sale_id = s.id
                WHERE s.client_id = ?
                """;

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                credits.add(mapResultSetToCredit(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo créditos por cliente: " + e.getMessage());
        }
        return credits;
    }

    public boolean hasActiveCredit(int clientId) {
        String sql = """
                SELECT COUNT(*) as count FROM credits c
                INNER JOIN sales s ON c.sale_id = s.id
                WHERE s.client_id = ? AND c.state = 'VIGENTE'
                """;

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count") > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error verificando crédito activo: " + e.getMessage());
        }
        return false;
    }

    public boolean updateState(int creditId, String newState) {
        String sql = "UPDATE credits SET state = ? WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newState);
            stmt.setInt(2, creditId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error actualizando estado del crédito: " + e.getMessage());
        }
        return false;
    }

    private Credit mapResultSetToCredit(ResultSet rs) throws SQLException {
        Credit credit = new Credit();
        credit.setId(rs.getInt("id"));
        credit.setInitialQuota(rs.getDouble("initial_quota"));
        credit.setAmountFinanced(rs.getDouble("amount_financed"));
        credit.setMonths(rs.getInt("months"));
        credit.setInterestRate(rs.getDouble("interest_rate"));
        credit.setCreatedAt(rs.getDate("created_at").toLocalDate());
        credit.setState(rs.getString("state"));

        // Cargar venta
        credit.setSale(saleDAO.findById(rs.getInt("sale_id")));

        return credit;
    }
}
