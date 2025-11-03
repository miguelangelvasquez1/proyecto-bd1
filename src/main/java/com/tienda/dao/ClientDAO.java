package com.tienda.dao;

import com.tienda.model.Client;
import com.tienda.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientDAO {
    private final DatabaseConnection dbConnection;
    
    public ClientDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    public boolean save(Client client) {
        String sql = "INSERT INTO clients (document_type, document_number, name, email, phone_number, address) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, client.getDocumentType());
            stmt.setString(2, client.getDocumentNumber());
            stmt.setString(3, client.getName());
            stmt.setString(4, client.getEmail());
            stmt.setString(5, client.getPhoneNumber());
            stmt.setString(6, client.getAddress());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    client.setId(rs.getInt(1));
                }
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error guardando cliente: " + e.getMessage());
        }
        return false;
    }
    
    public Client findById(int id) {
        String sql = "SELECT * FROM clients WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToClient(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error buscando cliente: " + e.getMessage());
        }
        return null;
    }
    
    public Client findByDocumentNumber(String documentNumber) {
        String sql = "SELECT * FROM clients WHERE document_number = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, documentNumber);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToClient(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error buscando cliente por documento: " + e.getMessage());
        }
        return null;
    }
    
    public List<Client> findAll() {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT * FROM clients";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                clients.add(mapResultSetToClient(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error obteniendo todos los clientes: " + e.getMessage());
        }
        return clients;
    }
    
    public boolean update(Client client) {
        String sql = "UPDATE clients SET document_type = ?, document_number = ?, name = ?, email = ?, phone_number = ?, address = ? WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, client.getDocumentType());
            stmt.setString(2, client.getDocumentNumber());
            stmt.setString(3, client.getName());
            stmt.setString(4, client.getEmail());
            stmt.setString(5, client.getPhoneNumber());
            stmt.setString(6, client.getAddress());
            stmt.setInt(7, client.getId());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error actualizando cliente: " + e.getMessage());
        }
        return false;
    }
    
    public boolean delete(int id) {
        String sql = "DELETE FROM clients WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error eliminando cliente: " + e.getMessage());
        }
        return false;
    }
    
    // Consulta específica: Clientes morosos
    public List<Client> findClientsInDefault() {
        List<Client> clients = new ArrayList<>();
        String sql = 
            "SELECT DISTINCT c.* " +
            "FROM clients c " +
            "INNER JOIN sales s ON c.id = s.client_id " +
            "INNER JOIN credits cr ON s.id = cr.sale_id " +
            "INNER JOIN quotas q ON cr.id = q.credit_id " +
            "WHERE q.state = 'VENCIDA' AND q.payed_value IS NULL " +
            "AND cr.state = 'VIGENTE'";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                clients.add(mapResultSetToClient(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error obteniendo clientes morosos: " + e.getMessage());
        }
        return clients;
    }
    
    private Client mapResultSetToClient(ResultSet rs) throws SQLException {
        Client client = new Client();
        client.setId(rs.getInt("id"));
        client.setDocumentType(rs.getString("document_type"));
        client.setDocumentNumber(rs.getString("document_number"));
        client.setName(rs.getString("name"));
        client.setEmail(rs.getString("email"));
        client.setPhoneNumber(rs.getString("phone_number"));
        client.setAddress(rs.getString("address"));
        return client;
    }
}
