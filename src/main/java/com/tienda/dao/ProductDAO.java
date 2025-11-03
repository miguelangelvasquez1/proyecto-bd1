package com.tienda.dao;

import com.tienda.model.Product;
import com.tienda.model.ProductCategory;
import com.tienda.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    private final DatabaseConnection dbConnection;
    // private final ProductCategoryDAO categoryDAO;

    public ProductDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
        // this.categoryDAO = new ProductCategoryDAO();
    }

    public Product findByCode(String code) {
        String sql = """
                SELECT p.*, pc.name as category_name, pc.iva, pc.utility
                FROM products p
                INNER JOIN product_categories pc ON p.category_id = pc.id
                WHERE p.code = ?
                """;

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToProduct(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error buscando producto por código: " + e.getMessage());
        }

        return null;
    }

    // Método auxiliar para mapear ResultSet a Product (si no existe)
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getInt("id"));
        product.setCode(rs.getString("code"));
        product.setName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        product.setStock(rs.getInt("stock"));
        product.setAcquisitionValue(rs.getDouble("acquisition_value"));
        product.setSaleValue(rs.getDouble("sale_value"));

        ProductCategory category = new ProductCategory();
        category.setId(rs.getInt("category_id"));
        category.setName(rs.getString("category_name"));
        category.setIva(rs.getDouble("iva"));
        category.setUtility(rs.getDouble("utility"));
        product.setCategory(category);

        return product;
    }

    public boolean save(Product product) {
        String sql = "INSERT INTO products (code, name, description, stock, acquisition_value, sale_value, category_id) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, product.getCode());
            stmt.setString(2, product.getName());
            stmt.setString(3, product.getDescription());
            stmt.setInt(4, product.getStock());
            stmt.setDouble(5, product.getAcquisitionValue());
            stmt.setDouble(6, product.getSaleValue());
            stmt.setInt(7, product.getCategory().getId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    product.setId(rs.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error guardando producto: " + e.getMessage());
        }
        return false;
    }

    public Product findById(int id) {
        String sql = """
                SELECT p.*, pc.name as category_name, pc.iva, pc.utility
                FROM products p
                INNER JOIN product_categories pc ON p.category_id = pc.id
                WHERE p.id = ?
                """;

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToProduct(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error buscando producto: " + e.getMessage());
        }
        return null;
    }

    // public Product findByCode(String code) {
    //     String sql = """
    //             SELECT p.*, pc.name as category_name, pc.iva, pc.utility
    //             FROM products p
    //             INNER JOIN product_categories pc ON p.category_id = pc.id
    //             WHERE p.code = ?
    //             """;

    //     try (Connection conn = dbConnection.getConnection();
    //             PreparedStatement stmt = conn.prepareStatement(sql)) {

    //         stmt.setString(1, code);
    //         ResultSet rs = stmt.executeQuery();

    //         if (rs.next()) {
    //             return mapResultSetToProduct(rs);
    //         }

    //     } catch (SQLException e) {
    //         System.err.println("Error buscando producto por código: " + e.getMessage());
    //     }
    //     return null;
    // }

    public Product findByName(String name) {
        return null;
    }

    // public List<Product> findAll() {
    //     List<Product> products = new ArrayList<>();
    //     String sql = """
    //             SELECT p.*, pc.name as category_name, pc.iva, pc.utility
    //             FROM products p
    //             INNER JOIN product_categories pc ON p.category_id = pc.id
    //             """;

    //     try (Connection conn = dbConnection.getConnection();
    //             PreparedStatement stmt = conn.prepareStatement(sql);
    //             ResultSet rs = stmt.executeQuery()) {

    //         while (rs.next()) {
    //             products.add(mapResultSetToProduct(rs));
    //         }

    //     } catch (SQLException e) {
    //         System.err.println("Error obteniendo todos los productos: " + e.getMessage());
    //     }
    //     return products;
    // }

    public List<Product> findByCategory(int categoryId) {
        List<Product> products = new ArrayList<>();
        String sql = """
                SELECT p.*, pc.name as category_name, pc.iva, pc.utility
                FROM products p
                INNER JOIN product_categories pc ON p.category_id = pc.id
                WHERE p.category_id = ?
                """;

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo productos por categoría: " + e.getMessage());
        }
        return products;
    }

    // public boolean update(Product product) {
    //     String sql = "UPDATE products SET code = ?, name = ?, description = ?, stock = ?, acquisition_value = ?, sale_value = ?, category_id = ? WHERE id = ?";

    //     try (Connection conn = dbConnection.getConnection();
    //             PreparedStatement stmt = conn.prepareStatement(sql)) {

    //         stmt.setString(1, product.getCode());
    //         stmt.setString(2, product.getName());
    //         stmt.setString(3, product.getDescription());
    //         stmt.setInt(4, product.getStock());
    //         stmt.setDouble(5, product.getAcquisitionValue());
    //         stmt.setDouble(6, product.getSaleValue());
    //         stmt.setInt(7, product.getCategory().getId());
    //         stmt.setInt(8, product.getId());

    //         return stmt.executeUpdate() > 0;

    //     } catch (SQLException e) {
    //         System.err.println("Error actualizando producto: " + e.getMessage());
    //     }
    //     return false;
    // }

    // public boolean updateStock(int productId, int newStock) {
    //     String sql = "UPDATE products SET stock = ? WHERE id = ?";

    //     try (Connection conn = dbConnection.getConnection();
    //             PreparedStatement stmt = conn.prepareStatement(sql)) {

    //         stmt.setInt(1, newStock);
    //         stmt.setInt(2, productId);

    //         return stmt.executeUpdate() > 0;

    //     } catch (SQLException e) {
    //         System.err.println("Error actualizando stock: " + e.getMessage());
    //     }
    //     return false;
    // }

    // Consulta específica: Inventario por categoría con costo
    public List<Object[]> getInventoryByCategoryWithCost() {
        List<Object[]> inventory = new ArrayList<>();
        String sql = """
                SELECT
                    pc.name as categoria,
                    p.name as producto,
                    p.stock,
                    p.acquisition_value as costo_unitario,
                    (p.stock * p.acquisition_value) as costo_total
                FROM products p
                INNER JOIN product_categories pc ON p.category_id = pc.id
                ORDER BY pc.name, p.name
                """;

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Object[] row = {
                        rs.getString("categoria"),
                        rs.getString("producto"),
                        rs.getInt("stock"),
                        rs.getDouble("costo_unitario"),
                        rs.getDouble("costo_total")
                };
                inventory.add(row);
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo inventario por categoría: " + e.getMessage());
        }
        return inventory;
    }

    // private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
    //     Product product = new Product();
    //     product.setId(rs.getInt("id"));
    //     product.setCode(rs.getString("code"));
    //     product.setName(rs.getString("name"));
    //     product.setDescription(rs.getString("description"));
    //     product.setStock(rs.getInt("stock"));
    //     product.setAcquisitionValue(rs.getDouble("acquisition_value"));
    //     product.setSaleValue(rs.getDouble("sale_value"));

    //     ProductCategory category = new ProductCategory();
    //     category.setId(rs.getInt("category_id"));
    //     category.setName(rs.getString("category_name"));
    //     category.setIva(rs.getDouble("iva"));
    //     category.setUtility(rs.getDouble("utility"));

    //     product.setCategory(category);

    //     return product;
    // }

    
// ==========================================
// Métodos adicionales para ProductDAO
// ==========================================

/**
 * Obtiene todos los productos
 */
public List<Product> findAll() {
    String sql = """
        SELECT p.*, 
               pc.id as category_id,
               pc.name as category_name, 
               pc.iva, 
               pc.utility 
        FROM products p 
        INNER JOIN product_categories pc ON p.category_id = pc.id 
        ORDER BY p.id DESC
        """;
    
    List<Product> products = new ArrayList<>();
    
    try (Connection conn = dbConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {
        
        while (rs.next()) {
            products.add(mapResultSetToProduct(rs));
        }
        
    } catch (SQLException e) {
        System.err.println("Error obteniendo todos los productos: " + e.getMessage());
    }
    
    return products;
}

/**
 * Inserta un nuevo producto
 */
public boolean insert(Product product) {
    String sql = """
        INSERT INTO products (code, name, description, stock, acquisition_value, sale_value, category_id) 
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
    
    try (Connection conn = dbConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        
        stmt.setString(1, product.getCode());
        stmt.setString(2, product.getName());
        stmt.setString(3, product.getDescription());
        stmt.setInt(4, product.getStock());
        stmt.setDouble(5, product.getAcquisitionValue());
        stmt.setDouble(6, product.getSaleValue());
        stmt.setInt(7, product.getCategory().getId());
        
        int rowsAffected = stmt.executeUpdate();
        
        if (rowsAffected > 0) {
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                product.setId(generatedKeys.getInt(1));
            }
            return true;
        }
        
    } catch (SQLException e) {
        System.err.println("Error insertando producto: " + e.getMessage());
    }
    
    return false;
}

/**
 * Actualiza un producto existente
 */
public boolean update(Product product) {
    String sql = """
        UPDATE products 
        SET code = ?, 
            name = ?, 
            description = ?, 
            stock = ?, 
            acquisition_value = ?, 
            sale_value = ?, 
            category_id = ? 
        WHERE id = ?
        """;
    
    try (Connection conn = dbConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setString(1, product.getCode());
        stmt.setString(2, product.getName());
        stmt.setString(3, product.getDescription());
        stmt.setInt(4, product.getStock());
        stmt.setDouble(5, product.getAcquisitionValue());
        stmt.setDouble(6, product.getSaleValue());
        stmt.setInt(7, product.getCategory().getId());
        stmt.setInt(8, product.getId());
        
        int rowsAffected = stmt.executeUpdate();
        return rowsAffected > 0;
        
    } catch (SQLException e) {
        System.err.println("Error actualizando producto: " + e.getMessage());
    }
    
    return false;
}

/**
 * Elimina un producto por ID
 */
public boolean delete(int id) {
    String sql = "DELETE FROM products WHERE id = ?";
    
    try (Connection conn = dbConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setInt(1, id);
        int rowsAffected = stmt.executeUpdate();
        return rowsAffected > 0;
        
    } catch (SQLException e) {
        System.err.println("Error eliminando producto: " + e.getMessage());
        // Si hay error por foreign key, retornar false
        return false;
    }
}

/**
 * Actualiza solo el stock de un producto
 */
public boolean updateStock(int productId, int newStock) {
    String sql = "UPDATE products SET stock = ? WHERE id = ?";
    
    try (Connection conn = dbConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setInt(1, newStock);
        stmt.setInt(2, productId);
        
        int rowsAffected = stmt.executeUpdate();
        return rowsAffected > 0;
        
    } catch (SQLException e) {
        System.err.println("Error actualizando stock: " + e.getMessage());
    }
    
    return false;
}

}