package com.tienda.controller;

import com.tienda.dao.UserDAO;
import com.tienda.model.dtos.UserSalesDTO;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class UserSalesController implements Initializable {

    @FXML private TableView<UserSalesDTO> tableUserSales;
    @FXML private TableColumn<UserSalesDTO, Integer> colId;
    @FXML private TableColumn<UserSalesDTO, String> colName;
    @FXML private TableColumn<UserSalesDTO, Integer> colTotalSales;
    
    @FXML private DatePicker datePickerStart;
    @FXML private DatePicker datePickerEnd;
    @FXML private Label lblTotalUsers;
    @FXML private Label lblTotalSales;
    @FXML private Label lblCountSales;
    @FXML private Label lblCreditSales;
    @FXML private Label lblPeriodInfo;
    @FXML private Button btnBack;
    @FXML private Button btnRefresh;
    @FXML private Button btnFilter;
    @FXML private Button btnClearFilter;

    private UserDAO userDAO;
    private ObservableList<UserSalesDTO> userSalesList;
    private Date currentStartDate;
    private Date currentEndDate;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userDAO = new UserDAO();
        userSalesList = FXCollections.observableArrayList();
        
        // Configurar las columnas
        setupTableColumns();
        
        // Configurar DatePickers
        setupDatePickers();
        
        // Cargar datos iniciales (último mes)
        loadDefaultData();
    }

    /**
     * Configura las columnas de la tabla
     */
    private void setupTableColumns() {
        // Usar SimpleProperty para evitar problemas con módulos
        colId.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getId()).asObject()
        );
        
        colName.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getUserName())
        );
        
        colTotalSales.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getTotalSales()).asObject()
        );
        
        // Centrar columnas
        colId.setStyle("-fx-alignment: CENTER;");
        colTotalSales.setStyle("-fx-alignment: CENTER;");
    }

    /**
     * Configura los DatePickers con valores por defecto
     */
    private void setupDatePickers() {
        // Establecer el mes actual por defecto
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = now.withDayOfMonth(1);
        LocalDate lastDayOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        
        datePickerStart.setValue(firstDayOfMonth);
        datePickerEnd.setValue(lastDayOfMonth);
        
        // Configurar formato de fecha
        datePickerStart.setPromptText("dd/MM/yyyy");
        datePickerEnd.setPromptText("dd/MM/yyyy");
    }

    /**
     * Carga datos con el rango de fechas por defecto (mes actual)
     */
    private void loadDefaultData() {
        LocalDate startDate = datePickerStart.getValue();
        LocalDate endDate = datePickerEnd.getValue();
        
        if (startDate != null && endDate != null) {
            loadUserSales(Date.valueOf(startDate), Date.valueOf(endDate));
        }
    }

    /**
     * Carga los datos de ventas por usuario
     */
    private void loadUserSales(Date startDate, Date endDate) {
        try {
            // Validar fechas
            if (startDate.after(endDate)) {
                showError("Error de Fechas", "La fecha inicial no puede ser posterior a la fecha final", "");
                return;
            }
            
            currentStartDate = startDate;
            currentEndDate = endDate;
            
            // Cargar ventas por usuario
            List<UserSalesDTO> salesData = userDAO.getUserSales(startDate, endDate);
            userSalesList.clear();
            userSalesList.addAll(salesData);
            tableUserSales.setItems(userSalesList);
            
            // Actualizar estadísticas
            updateStatistics(startDate, endDate);
            
            // Actualizar info del periodo
            updatePeriodInfo(startDate, endDate);
            
            System.out.println("Datos cargados: " + salesData.size() + " usuarios");
            
        } catch (Exception e) {
            System.err.println("Error al cargar datos de ventas: " + e.getMessage());
            e.printStackTrace();
            showError("Error", "No se pudieron cargar los datos", e.getMessage());
        }
    }

    /**
     * Actualiza las estadísticas generales
     */
    private void updateStatistics(Date startDate, Date endDate) {
        try {
            // Total de usuarios
            int totalUsers = tableUserSales.getItems().size();
            lblTotalUsers.setText(String.valueOf(totalUsers));
            
            // Total de ventas
            int totalSales = userDAO.getTotalSales(userSalesList);
            lblTotalSales.setText(String.valueOf(totalSales));
            
            // Ventas de contado
            int countSales = userDAO.getTotalCountSales(startDate, endDate);
            lblCountSales.setText(String.valueOf(countSales));
            
            // Ventas a crédito
            int creditSales = userDAO.getTotalCreditSales(startDate, endDate);
            lblCreditSales.setText(String.valueOf(creditSales));
            
        } catch (Exception e) {
            System.err.println("Error actualizando estadísticas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Actualiza la información del periodo en el footer
     */
    private void updatePeriodInfo(Date startDate, Date endDate) {
        String info = String.format("📊 Mostrando ventas del %s al %s", 
                                   startDate.toString(), 
                                   endDate.toString());
        lblPeriodInfo.setText(info);
    }

    @FXML
    private void handleFilter() {
        LocalDate startDate = datePickerStart.getValue();
        LocalDate endDate = datePickerEnd.getValue();
        
        if (startDate == null || endDate == null) {
            showError("Fechas Requeridas", "Debe seleccionar ambas fechas", 
                     "Por favor seleccione una fecha inicial y una fecha final.");
            return;
        }
        
        loadUserSales(Date.valueOf(startDate), Date.valueOf(endDate));
    }

    @FXML
    private void handleClearFilter() {
        // Restablecer al mes actual
        setupDatePickers();
        loadDefaultData();
    }

    @FXML
    private void handleRefresh() {
        if (currentStartDate != null && currentEndDate != null) {
            loadUserSales(currentStartDate, currentEndDate);
        } else {
            loadDefaultData();
        }
        showInfo("Actualizado", "Los datos han sido actualizados.");
    }

    @FXML
    private void handleBack() {
        try {
            Stage currentStage = (Stage) btnBack.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainWindow.fxml"));
            Parent root = loader.load();
            
            URL cssUrl = getClass().getResource("/com/tienda/css/home.css");
            if (cssUrl != null) {
                root.getStylesheets().add(cssUrl.toExternalForm());
            }
            
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle("Home");
            
        } catch (IOException e) {
            System.err.println("Error al volver al Home: " + e.getMessage());
            e.printStackTrace();
            showError("Error", "No se pudo volver al Home", e.getMessage());
        }
    }

    // Métodos auxiliares para alertas
    private void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}