package com.tienda.dao;

import com.tienda.model.User;
import com.tienda.model.dtos.UserSalesDTO;
import com.tienda.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private final DatabaseConnection dbConnection;
    private final RoleDAO roleDAO;

    public UserDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
        this.roleDAO = new RoleDAO();
    }

    public boolean save(User user) {
        String sql = "INSERT INTO users (name, email, password, phone_number, role_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getPhoneNumber());
            stmt.setInt(5, user.getRole().getId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error guardando usuario: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT u.* FROM users u INNER JOIN roles r ON u.role_id = r.id ORDER BY u.id";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo usuarios: " + e.getMessage());
        }
        return users;
    }

    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error buscando usuario: " + e.getMessage());
        }
        return null;
    }

    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error buscando usuario por email: " + e.getMessage());
        }
        return null;
    }

    public boolean validateCredentials(String email, String password) {
        String sql = "SELECT COUNT(*) as count FROM users WHERE email = ? AND password = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password); // En producción usar hash

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error validando credenciales: " + e.getMessage());
        }
        return false;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setPhoneNumber(rs.getString("phone_number"));

        // Cargar rol
        user.setRole(roleDAO.findById(rs.getInt("role_id")));

        return user;
    }
    
    public List<UserSalesDTO> getUserSales(Date startDate, Date endDate) {
        List<UserSalesDTO> userSales = new ArrayList<>();
        String sql = """
                        SELECT u.id, u.name, (COUNT(s.user_id)) AS sales FROM users u 
                        LEFT JOIN sales s ON u.id = s.user_id 
                        WHERE s.date BETWEEN ? AND ? 
                        GROUP BY u.id, u.name
                        ORDER BY u.name ASC
                    """;
                
               
        try (Connection conn = dbConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                UserSalesDTO dto = new UserSalesDTO();
                dto.setId(rs.getInt("id"));
                dto.setUserName(rs.getString("name"));
                dto.setTotalSales(rs.getInt("sales"));
                userSales.add(dto);
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo ventas por usuario: " + e.getMessage());
            e.printStackTrace();
        }
        return userSales;
    }

    public int getTotalCountSales (Date startDate, Date endDate){
        String sql = """
                    SELECT COUNT(*) AS TotalCounts FROM sales s 
                    WHERE s.sale_type = 'COUNT' AND s.date BETWEEN ? AND ?
                    """;
        try (Connection conn = dbConnection.getConnection()){
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("TotalCounts");
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo cantidad de ventas tipo contado: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public int getTotalCreditSales (Date startDate, Date endDate){
        String sql = """
                    SELECT COUNT(*) AS TotalCredits FROM sales s 
                    WHERE s.sale_type = 'CREDIT' AND s.date BETWEEN ? AND ?
                    """;
        
        try (Connection conn = dbConnection.getConnection()){
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("TotalCredits");
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo cantidad de ventas tipo credito: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public int getTotalSales (List<UserSalesDTO> userSales){
        return userSales.stream().mapToInt(UserSalesDTO::getTotalSales).sum();
    }
}