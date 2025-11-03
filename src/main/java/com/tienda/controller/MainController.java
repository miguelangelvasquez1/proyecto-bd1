package com.tienda.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.tienda.util.SceneManager;

// import javafx.util.Duration for Timeline KeyFrame
import javafx.util.Duration;

public class MainController {
    
    @FXML private Label statusLabel;
    @FXML private Label userLabel;
    @FXML private Label dateLabel;
    
    public void initialize() {
        updateDateTime();
        Timeline timeline = new Timeline(new KeyFrame(Duration.minutes(1), e -> updateDateTime()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    
    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        dateLabel.setText("Fecha: " + now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }
    
    // Métodos para Entidades
    @FXML
    private void openClients() {
        openWindow("/views/ClientManagement.fxml", "Gestión de Clientes");
    }
    
    @FXML
    private void openProducts() {
        openWindow("/views/PurchaseWindow.fxml", "Gestión de Productos");
    }
    
    @FXML
    private void openCategories() {
        openWindow("/views/CategoryManagement.fxml", "Gestión de Categorías");
    }
    
    @FXML
    private void openUsers() {
        openWindow("/views/UserManagement.fxml", "Gestión de Usuarios");
    }
    
    // Métodos para Transacciones
    // @FXML
    // private void openSales() {
    //     openWindow("/views/SalesWindow.fxml", "Nueva Venta");
    // }
    @FXML
    private void openSales() {
        openWindow("/views/SalesHistoryView.fxml", "Nueva Venta");
    }

    @FXML
    private void openCredits() {
        openWindow("/views/CreditManagement.fxml", "Gestión de Créditos");
    }
    
    @FXML
    private void openPayments() {
        openWindow("/views/PaymentWindow.fxml", "Pagos de Cuotas");
    }
    
    // Métodos para Reportes
    @FXML
    private void openSalesReport() {
        openWindow("/views/SalesReport.fxml", "Reporte de Ventas");
    }
    
    @FXML
    private void openInventoryReport() {
        openWindow("/views/InventoryReport.fxml", "Inventario por Categoría");
    }
    
    @FXML
    private void openIvaReport() {
        openWindow("/views/IvaReport.fxml", "Reporte IVA DIAN");
    }
    
    @FXML
    private void openCreditReport() {
        openWindow("/views/CreditReport.fxml", "Reporte de Créditos");
    }
    
    @FXML
    private void openClientReport() {
        openWindow("/views/ClientReport.fxml", "Clientes Morosos");
    }
    
    // Métodos para Consultas
    @FXML
    private void openTopProducts() {
        openWindow("/views/TopProductsQuery.fxml", "Productos Más Vendidos");
    }
    
    @FXML
    private void openSalesByUser() {
        openWindow("/views/SalesByUserQuery.fxml", "Ventas por Usuario");
    }
    
    @FXML
    private void openCreditAnalysis() {
        openWindow("/views/CreditAnalysisQuery.fxml", "Análisis de Créditos");
    }
    
    @FXML
    private void openClientPurchases() {
        openWindow("/views/ClientPurchasesQuery.fxml", "Compras por Cliente");
    }
    
    @FXML
    private void openMonthlyTrends() {
        openWindow("/views/MonthlyTrendsQuery.fxml", "Tendencias Mensuales");
    }
    
    // Métodos para Utilidades
    @FXML
    private void openCalculator() {
        openWindow("/views/Calculator.fxml", "Calculadora");
    }
    
    @FXML
    private void openCalendar() {
        openWindow("/views/CalendarUtil.fxml", "Calendario");
    }
    
    @FXML
    private void openUserManagement() {
        openWindow("/views/UserManagement.fxml", "Gestión de Usuarios");
    }
    
    @FXML
    private void openAccessLog() {
        openWindow("/views/AccessLog.fxml", "Bitácora de Acceso");
    }
    
    // Métodos para Ayuda
    @FXML
    private void openAbout() {
        openWindow("/views/About.fxml", "Acerca de");
    }
    
    @FXML
    private void openUserManual() {
        openWindow("/views/UserManual.fxml", "Manual de Usuario");
    }
    
    private void openWindow(String fxmlPath, String title) {
        SceneManager.switchScene(fxmlPath, title);
    }
}