package com.tienda.dao;

import com.tienda.model.Role;
import com.tienda.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoleDAO {
    private final DatabaseConnection dbConnection;
    
    public RoleDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    public Role findById(int id) {
        String sql = "SELECT * FROM roles WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToRole(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error buscando rol: " + e.getMessage());
        }
        return null;
    }
    
    public List<Role> findAll() {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT * FROM roles";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                roles.add(mapResultSetToRole(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error obteniendo todos los roles: " + e.getMessage());
        }
        return roles;
    }
    
    private Role mapResultSetToRole(ResultSet rs) throws SQLException {
        Role role = new Role();
        role.setId(rs.getInt("id"));
        role.setName(rs.getString("name"));
        role.setDescription(rs.getString("description"));
        return role;
    }
}
