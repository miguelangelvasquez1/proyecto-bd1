package com.tienda.dao;

import com.tienda.model.ProductCategory;
import com.tienda.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductCategoryDAO {
    private final DatabaseConnection dbConnection;
    
    public ProductCategoryDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    public boolean save(ProductCategory category) {
        String sql = "INSERT INTO product_categories (name, iva, utility) VALUES (?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, category.getName());
            stmt.setDouble(2, category.getIva());
            stmt.setDouble(3, category.getUtility());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    category.setId(rs.getInt(1));
                }
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error guardando categoría: " + e.getMessage());
        }
        return false;
    }
    
    public ProductCategory findById(int id) {
        String sql = "SELECT * FROM product_categories WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToCategory(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error buscando categoría: " + e.getMessage());
        }
        return null;
    }
    
    public ProductCategory findByName(String name) {
        String sql = "SELECT * FROM product_categories WHERE name = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToCategory(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error buscando categoría por nombre: " + e.getMessage());
        }
        return null;
    }
    
    public List<ProductCategory> findAll() {
        List<ProductCategory> categories = new ArrayList<>();
        String sql = "SELECT * FROM product_categories";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error obteniendo todas las categorías: " + e.getMessage());
        }
        return categories;
    }
    
    private ProductCategory mapResultSetToCategory(ResultSet rs) throws SQLException {
        ProductCategory category = new ProductCategory();
        category.setId(rs.getInt("id"));
        category.setName(rs.getString("name"));
        category.setIva(rs.getDouble("iva"));
        category.setUtility(rs.getDouble("utility"));
        return category;
    }
}
