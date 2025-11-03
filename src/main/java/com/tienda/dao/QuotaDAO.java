package com.tienda.dao;

import com.tienda.model.Quota;
import com.tienda.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuotaDAO {
    private final DatabaseConnection dbConnection;
    private final CreditDAO creditDAO;
    
    public QuotaDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
        this.creditDAO = new CreditDAO();
    }
    
public List<Quota> findByCreditId(int creditId) {
    String sql = """
        SELECT * FROM quotas 
        WHERE credit_id = ? 
        ORDER BY quota_number
        """;
    
    List<Quota> quotas = new ArrayList<>();
    
    try (Connection conn = dbConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setInt(1, creditId);
        ResultSet rs = stmt.executeQuery();
        
        while (rs.next()) {
            Quota quota = new Quota();
            quota.setId(rs.getInt("id"));
            quota.setQuotaNumber(rs.getInt("quota_number"));
            quota.setExpirationDate(rs.getDate("expiration_date").toLocalDate());
            quota.setQuotaValue(rs.getDouble("quota_value"));
            
            Double payedValue = rs.getDouble("payed_value");
            if (!rs.wasNull()) {
                quota.setPayedValue(payedValue);
            }
            
            Date payedAt = rs.getDate("payed_at");
            if (payedAt != null) {
                quota.setPayedAt(payedAt.toLocalDate());
            }
            
            quota.setState(rs.getString("state"));
            
            quotas.add(quota);
        }
        
    } catch (SQLException e) {
        System.err.println("Error buscando cuotas: " + e.getMessage());
    }
    
    return quotas;
}

public boolean updateQuota(Quota quota) {
    String sql = """
        UPDATE quotas 
        SET payed_value = ?, payed_at = ?, state = ?
        WHERE id = ?
        """;
    
    try (Connection conn = dbConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setDouble(1, quota.getPayedValue());
        stmt.setDate(2, Date.valueOf(quota.getPayedAt()));
        stmt.setString(3, quota.getState());
        stmt.setInt(4, quota.getId());
        
        int rowsAffected = stmt.executeUpdate();
        return rowsAffected > 0;
        
    } catch (SQLException e) {
        System.err.println("Error actualizando cuota: " + e.getMessage());
        return false;
    }
}

    public boolean save(Quota quota) {
        String sql = "INSERT INTO quotas (quota_number, expiration_date, quota_value, payed_value, payed_at, state, credit_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, quota.getQuotaNumber());
            stmt.setDate(2, Date.valueOf(quota.getExpirationDate()));
            stmt.setDouble(3, quota.getQuotaValue());
            if (quota.getPayedValue() != null) {
                stmt.setDouble(4, quota.getPayedValue());
            } else {
                stmt.setNull(4, Types.DOUBLE);
            }
            if (quota.getPayedAt() != null) {
                stmt.setDate(5, Date.valueOf(quota.getPayedAt()));
            } else {
                stmt.setNull(5, Types.DATE);
            }
            stmt.setString(6, quota.getState());
            stmt.setInt(7, quota.getCredit().getId());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    quota.setId(rs.getInt(1));
                }
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error guardando cuota: " + e.getMessage());
        }
        return false;
    }
    
    // public List<Quota> findByCreditId(int creditId) {
    //     List<Quota> quotas = new ArrayList<>();
    //     String sql = "SELECT * FROM quotas WHERE credit_id = ? ORDER BY quota_number";
        
    //     try (Connection conn = dbConnection.getConnection();
    //          PreparedStatement stmt = conn.prepareStatement(sql)) {
            
    //         stmt.setInt(1, creditId);
    //         ResultSet rs = stmt.executeQuery();
            
    //         while (rs.next()) {
    //             quotas.add(mapResultSetToQuota(rs));
    //         }
            
    //     } catch (SQLException e) {
    //         System.err.println("Error obteniendo cuotas por crédito: " + e.getMessage());
    //     }
    //     return quotas;
    // }
    
    public List<Quota> findOverdueQuotas() {
        List<Quota> quotas = new ArrayList<>();
        String sql = """
            SELECT * FROM quotas 
            WHERE expiration_date < GETDATE() 
            AND payed_value IS NULL 
            AND state = 'PENDIENTE'
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                quotas.add(mapResultSetToQuota(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error obteniendo cuotas vencidas: " + e.getMessage());
        }
        return quotas;
    }
    
    public boolean payQuota(int quotaId, double amount) {
        String sql = "UPDATE quotas SET payed_value = ?, payed_at = GETDATE(), state = 'PAGADA' WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, amount);
            stmt.setInt(2, quotaId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error pagando cuota: " + e.getMessage());
        }
        return false;
    }
    
    public boolean updateOverdueQuotas() {
        String sql = """
            UPDATE quotas 
            SET state = 'VENCIDA' 
            WHERE expiration_date < GETDATE() 
            AND payed_value IS NULL 
            AND state = 'PENDIENTE'
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error actualizando cuotas vencidas: " + e.getMessage());
        }
        return false;
    }
    
    private Quota mapResultSetToQuota(ResultSet rs) throws SQLException {
        Quota quota = new Quota();
        quota.setId(rs.getInt("id"));
        quota.setQuotaNumber(rs.getInt("quota_number"));
        quota.setExpirationDate(rs.getDate("expiration_date").toLocalDate());
        quota.setQuotaValue(rs.getDouble("quota_value"));
        
        Double payedValue = rs.getDouble("payed_value");
        if (!rs.wasNull()) {
            quota.setPayedValue(payedValue);
        }
        
        Date payedAt = rs.getDate("payed_at");
        if (payedAt != null) {
            quota.setPayedAt(payedAt.toLocalDate());
        }
        
        quota.setState(rs.getString("state"));
        
        // Cargar crédito
        quota.setCredit(creditDAO.findById(rs.getInt("credit_id")));
        
        return quota;
    }
}