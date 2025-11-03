package com.tienda.dao;

import com.tienda.model.AccessBinnacle;
import com.tienda.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AccessBinnacleDAO {
    private final DatabaseConnection dbConnection;
    
    public AccessBinnacleDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    public int create(AccessBinnacle binnacle) throws Exception {
        String sql = "INSERT INTO access_binnacle (entry_date_time, ip, user_id) VALUES (?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setTimestamp(1, Timestamp.valueOf(binnacle.getEntryDateTime()));
            ps.setString(2, binnacle.getIp());
            ps.setInt(3, binnacle.getUser().getId());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("No se pudo obtener id generado para AccessBinnacle");
                }
            }
        }
    }
    
    public List<AccessBinnacle> findAll() {
        List<AccessBinnacle> entries = new ArrayList<>();
        String sql = "SELECT * FROM access_binnacle ORDER BY entry_date_time DESC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                entries.add(mapResultSetToAccessBinnacle(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error obteniendo bitácora de acceso: " + e.getMessage());
        }
        return entries;
    }
    
    public boolean updateDepartureTime(int id, LocalDateTime departureTime) {
        String sql = "UPDATE access_binnacle SET departure_date_time = ? WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(departureTime));
            stmt.setInt(2, id);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error actualizando hora de salida: " + e.getMessage());
        }
        return false;
    }
    
    private AccessBinnacle mapResultSetToAccessBinnacle(ResultSet rs) throws SQLException {
        AccessBinnacle entry = new AccessBinnacle();
        entry.setId(rs.getInt("id"));
        entry.setEntryDateTime(rs.getTimestamp("entry_date_time").toLocalDateTime());
        
        Timestamp departure = rs.getTimestamp("departure_date_time");
        if (departure != null) {
            entry.setDepartureDateTime(departure.toLocalDateTime());
        }
        
        entry.setIp(rs.getString("ip"));
        entry.setUser(new UserDAO().findById(rs.getInt("user_id")));
        return entry;
    }
}