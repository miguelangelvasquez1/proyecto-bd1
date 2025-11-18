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

    public List<DefaulterClientDTO> getDefaulterClients() {
        List<DefaulterClientDTO> list = new ArrayList<>();

        String sql = """
                    SELECT
                        c.name AS client_name,
                        c.document_number,
                        c.phone_number,
                        c.email,
                        cr.id AS credit_id,
                        SUM(q.quota_value - ISNULL(q.payed_value, 0)) AS total_debt,
                        COUNT(q.id) AS overdue_quotas,
                        MAX(q.payed_at) AS last_payment_date,
                        DATEDIFF(DAY, MIN(q.expiration_date), GETDATE()) AS days_past_due
                    FROM clients c
                    JOIN sales s ON s.client_id = c.id
                    JOIN credits cr ON cr.sale_id = s.id
                    JOIN quotas q ON q.credit_id = cr.id
                    WHERE q.state = 'VENCIDA'
                    GROUP BY
                        c.name, c.document_number, c.phone_number, c.email, cr.id
                    HAVING COUNT(q.id) > 0
                    ORDER BY overdue_quotas DESC
                """;

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                DefaulterClientDTO dto = new DefaulterClientDTO();
                dto.setClientName(rs.getString("client_name"));
                dto.setDocumentNumber(rs.getString("document_number"));
                dto.setPhoneNumber(rs.getString("phone_number")); // ← CORREGIDO
                dto.setEmail(rs.getString("email"));
                dto.setCreditId(rs.getInt("credit_id"));
                dto.setTotalDebt(rs.getDouble("total_debt"));
                dto.setOverdueQuotas(rs.getInt("overdue_quotas"));
                dto.setLastPaymentDate(rs.getDate("last_payment_date"));
                dto.setDaysPastDue(rs.getInt("days_past_due"));
                list.add(dto);
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo clientes morosos: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

}
