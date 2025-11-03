package com.tienda.dao;

import com.tienda.model.Product;
import com.tienda.model.ProductCategory;
import com.tienda.model.PurchaseDetails;
import com.tienda.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PurchaseDetailsDAO {
    private final DatabaseConnection dbConnection;
    private final PurchaseDAO saleDAO;
    private final ProductDAO productDAO;

    public PurchaseDetailsDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
        this.saleDAO = new PurchaseDAO();
        this.productDAO = new ProductDAO();
    }

    public List<PurchaseDetails> findBySaleId(int saleId) {
        String sql = """
                SELECT pd.*,
                       p.code as product_code,
                       p.name as product_name,
                       p.description as product_description,
                       pc.name as category_name
                FROM sale_details pd
                INNER JOIN products p ON pd.product_id = p.id
                LEFT JOIN product_categories pc ON p.category_id = pc.id
                WHERE pd.sale_id = ?
                ORDER BY pd.id
                """;

        List<PurchaseDetails> details = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, saleId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                PurchaseDetails detail = new PurchaseDetails();
                detail.setId(rs.getInt("id"));
                detail.setAmount(rs.getInt("amount"));
                detail.setUnitPrice(rs.getDouble("unit_price"));
                detail.setIvaApplied(rs.getDouble("iva_applied"));
                detail.setSubtotal(rs.getDouble("subtotal"));

                // Producto
                Product product = new Product();
                product.setCode(rs.getString("product_code"));
                product.setName(rs.getString("product_name"));
                product.setDescription(rs.getString("product_description"));

                ProductCategory category = new ProductCategory();
                category.setName(rs.getString("category_name"));
                product.setCategory(category);

                detail.setProduct(product);
                details.add(detail);
            }

        } catch (SQLException e) {
            System.err.println("Error buscando detalles de venta: " + e.getMessage());
        }

        return details;
    }

    public int countProductsBySale(int saleId) {
        String sql = "SELECT COUNT(*) as total FROM sale_details WHERE sale_id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, saleId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            System.err.println("Error contando productos: " + e.getMessage());
        }

        return 0;
    }

    public boolean save(PurchaseDetails saleDetails) {
        String sql = "INSERT INTO sale_details (amount, unit_price, iva_applied, subtotal, sale_id, product_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, saleDetails.getAmount());
            stmt.setDouble(2, saleDetails.getUnitPrice());
            stmt.setDouble(3, saleDetails.getIvaApplied());
            stmt.setDouble(4, saleDetails.getSubtotal());
            stmt.setInt(5, saleDetails.getSale().getId());
            stmt.setInt(6, saleDetails.getProduct().getId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    saleDetails.setId(rs.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error guardando detalle de venta: " + e.getMessage());
        }
        return false;
    }

    // public List<PurchaseDetails> findBySaleId(int saleId) {
    //     List<PurchaseDetails> details = new ArrayList<>();
    //     String sql = "SELECT * FROM sale_details WHERE sale_id = ?";

    //     try (Connection conn = dbConnection.getConnection();
    //             PreparedStatement stmt = conn.prepareStatement(sql)) {

    //         stmt.setInt(1, saleId);
    //         ResultSet rs = stmt.executeQuery();

    //         while (rs.next()) {
    //             details.add(mapResultSetToSaleDetails(rs));
    //         }

    //     } catch (SQLException e) {
    //         System.err.println("Error obteniendo detalles de venta: " + e.getMessage());
    //     }
    //     return details;
    // }

    private PurchaseDetails mapResultSetToSaleDetails(ResultSet rs) throws SQLException {
        PurchaseDetails details = new PurchaseDetails();
        details.setId(rs.getInt("id"));
        details.setAmount(rs.getInt("amount"));
        details.setUnitPrice(rs.getDouble("unit_price"));
        details.setIvaApplied(rs.getDouble("iva_applied"));
        details.setSubtotal(rs.getDouble("subtotal"));

        // Cargar venta y producto
        details.setSale(saleDAO.findById(rs.getInt("sale_id")));
        details.setProduct(productDAO.findById(rs.getInt("product_id")));

        return details;
    }
}