package com.tienda.dao;

import com.tienda.model.dtos.DefaulterClientDTO;
import com.tienda.model.dtos.SaleReportDTO;
import com.tienda.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {
    private final DatabaseConnection dbConnection;

    public ReportDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Obtiene las ventas para el reporte de IVA en un periodo
     */
    public List<SaleReportDTO> getSalesForIVAReport(Date startDate, Date endDate) {
        List<SaleReportDTO> sales = new ArrayList<>();
        String sql = """
                SELECT s.id, p.name AS product, sd.amount AS quantity, s.date, 
                       s.sale_type, s.subtotal, s.iva_total, s.total, c.name AS client
                FROM sale_details sd 
                INNER JOIN sales s ON s.id = sd.sale_id
                INNER JOIN products p ON p.id = sd.product_id
                INNER JOIN clients c ON s.client_id = c.id
                WHERE s.date BETWEEN ? AND ?
                ORDER BY s.date DESC
                """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                SaleReportDTO dto = new SaleReportDTO();
                dto.setSaleId(rs.getInt("id"));
                dto.setProductName(rs.getString("product"));
                dto.setQuantity(rs.getInt("quantity"));
                dto.setDate(rs.getDate("date"));
                dto.setSaleType(rs.getString("sale_type"));
                dto.setSubtotal(rs.getDouble("subtotal"));
                dto.setIvaTotal(rs.getDouble("iva_total"));
                dto.setTotal(rs.getDouble("total"));
                dto.setClientName(rs.getString("client"));
                sales.add(dto);
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo ventas para reporte IVA: " + e.getMessage());
            e.printStackTrace();
        }

        return sales;
    }

    /**
     * Calcula el total de IVA en un periodo
     */
    public double getTotalIVA(Date startDate, Date endDate) {
        String sql = """
                SELECT SUM(s.iva_total) AS TaxAmount 
                FROM sales s
                WHERE s.date BETWEEN ? AND ?
                """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("TaxAmount");
            }

        } catch (SQLException e) {
            System.err.println("Error calculando total IVA: " + e.getMessage());
            e.printStackTrace();
        }

        return 0.0;
    }

    /**
     * Obtiene clientes morosos (con cuotas vencidas)
     */
    public List<DefaulterClientDTO> getDefaulterClients() {
        List<DefaulterClientDTO> defaulters = new ArrayList<>();
        String sql = """
                SELECT 
                    c.name AS clientName,
                    c.document_number AS documentNumber,
                    c.phone_number AS phoneNumber,
                    c.email,
                    cr.id AS creditId,
                    cr.amount_financed AS totalDebt,
                    COUNT(q.id) AS overdueQuotas,
                    MAX(q.payed_at) AS lastPaymentDate,
                    DATEDIFF(DAY, MIN(q.expiration_date), GETDATE()) AS daysPastDue
                FROM clients c
                INNER JOIN sales s ON c.id = s.client_id
                INNER JOIN credits cr ON s.id = cr.sale_id
                INNER JOIN quotas q ON cr.id = q.credit_id
                WHERE q.state IN ('VENCIDA', 'PENDIENTE')
                  AND q.expiration_date < CAST(GETDATE() AS DATE)
                  AND cr.state = 'VIGENTE'
                GROUP BY c.name, c.document_number, c.phone_number, c.email, cr.id, cr.amount_financed
                HAVING COUNT(q.id) > 0
                ORDER BY daysPastDue DESC
                """;

        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                DefaulterClientDTO dto = new DefaulterClientDTO();
                dto.setClientName(rs.getString("clientName"));
                dto.setDocumentNumber(rs.getString("documentNumber"));
                dto.setPhoneNumber(rs.getString("phoneNumber"));
                dto.setEmail(rs.getString("email"));
                dto.setCreditId(rs.getInt("creditId"));
                dto.setTotalDebt(rs.getDouble("totalDebt"));
                dto.setOverdueQuotas(rs.getInt("overdueQuotas"));
                dto.setLastPaymentDate(rs.getDate("lastPaymentDate"));
                dto.setDaysPastDue(rs.getInt("daysPastDue"));
                defaulters.add(dto);
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo clientes morosos: " + e.getMessage());
            e.printStackTrace();
        }

        return defaulters;
    }
}
