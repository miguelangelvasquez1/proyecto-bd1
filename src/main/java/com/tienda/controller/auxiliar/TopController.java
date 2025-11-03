package com.tienda.controller.auxiliar;

import com.tienda.util.SceneManager;
import com.tienda.util.SessionManager;

import javafx.fxml.FXML;

public class TopController {
    
    // Métodos para Entidades
    @FXML
    private void openClients() {
        openWindow("/views/ClientManagement.fxml", "Gestión de Clientes");
    }
    
    @FXML
    private void openProducts() {
        openWindow("/views/ProductManagementView.fxml", "Gestión de Productos");
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
    
    // Métodos para Reportes
    @FXML
    private void openIvaReport() {
        openWindow("/views/IvaReport.fxml", "Reporte IVA DIAN");
    }
    
    @FXML
    private void openClientReport() {
        openWindow("/views/ClientMorososReport.fxml", "Clientes Morosos");
    }
    
    // Métodos para Consultas
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
        openModal("/views/Calculator.fxml", "Calculadora");
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

    @FXML
    private void logout() {
        SessionManager.endSession();
        openWindow("/views/Login.fxml", "Manual de Usuario");
    }
    
    /* Métodos estáticos */
    private void openWindow(String fxmlPath, String title) {
        SceneManager.switchScene(fxmlPath, title);
    }

    private void openModal(String fxmlPath, String title) {
        SceneManager.openModal(fxmlPath, title, true);
    }
}
