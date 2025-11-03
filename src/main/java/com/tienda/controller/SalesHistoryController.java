package com.tienda.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.tienda.dao.CreditDAO;
import com.tienda.dao.PurchaseDAO;
import com.tienda.dao.PurchaseDetailsDAO;
import com.tienda.model.Credit;
import com.tienda.model.Purchase;

public class SalesHistoryController {

    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private ComboBox<String> cbSaleType;
    @FXML private TextField txtClientFilter;
    @FXML private TextField txtUserFilter;
    @FXML private TextField txtSaleIdFilter;
    @FXML private TextField txtMinAmount;
    @FXML private TextField txtMaxAmount;
    
    @FXML private Button btnSearch;
    @FXML private Button btnClearFilters;
    @FXML private Button btnExport;
    
    @FXML private Label lblTotalSales;
    @FXML private Label lblTotalAmount;
    @FXML private Label lblCashSales;
    @FXML private Label lblCreditSales;
    
    @FXML private TableView<Purchase> tblSales;
    @FXML private TableColumn<Purchase, Integer> colId;
    @FXML private TableColumn<Purchase, String> colDate;
    @FXML private TableColumn<Purchase, String> colClient;
    @FXML private TableColumn<Purchase, String> colUser;
    @FXML private TableColumn<Purchase, String> colSaleType;
    @FXML private TableColumn<Purchase, String> colSubtotal;
    @FXML private TableColumn<Purchase, String> colIva;
    @FXML private TableColumn<Purchase, String> colTotal;
    @FXML private TableColumn<Purchase, Integer> colProducts;
    @FXML private TableColumn<Purchase, Void> colActions;
    
    @FXML private Button btnFirstPage;
    @FXML private Button btnPrevPage;
    @FXML private Label lblPageInfo;
    @FXML private Button btnNextPage;
    @FXML private Button btnLastPage;
    @FXML private ComboBox<String> cbPageSize;
    
    private PurchaseDAO purchaseDAO;
    private PurchaseDetailsDAO purchaseDetailsDAO;
    private CreditDAO creditDAO;
    
    private ObservableList<Purchase> allSales;
    private ObservableList<Purchase> filteredSales;
    
    private int currentPage = 0;
    private int pageSize = 20;
    private int totalPages = 0;
    
    private DecimalFormat currencyFormat;
    private DateTimeFormatter dateFormatter;

    @FXML
    public void initialize() {
        purchaseDAO = new PurchaseDAO();
        purchaseDetailsDAO = new PurchaseDetailsDAO();
        creditDAO = new CreditDAO();
        
        currencyFormat = new DecimalFormat("$#,##0.00");
        dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        allSales = FXCollections.observableArrayList();
        filteredSales = FXCollections.observableArrayList();
        
        setupTableColumns();
        setupComboBoxListeners();
        loadInitialData();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        colDate.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDate().format(dateFormatter))
        );
        
        colClient.setCellValueFactory(cellData -> 
            new SimpleStringProperty(
                cellData.getValue().getClient() != null ? 
                cellData.getValue().getClient().getName() : "N/A"
            )
        );
        
        colUser.setCellValueFactory(cellData -> 
            new SimpleStringProperty(
                cellData.getValue().getUser() != null ? 
                cellData.getValue().getUser().getName() : "N/A"
            )
        );
        
        colSaleType.setCellValueFactory(cellData -> {
            String type = cellData.getValue().getSaleType();
            String displayType = type.equals("COUNT") ? "CONTADO" : "CRÉDITO";
            return new SimpleStringProperty(displayType);
        });
        
        colSubtotal.setCellValueFactory(cellData -> 
            new SimpleStringProperty(currencyFormat.format(cellData.getValue().getSubtotal()))
        );
        
        colIva.setCellValueFactory(cellData -> 
            new SimpleStringProperty(currencyFormat.format(cellData.getValue().getIvaTotal()))
        );
        
        colTotal.setCellValueFactory(cellData -> 
            new SimpleStringProperty(currencyFormat.format(cellData.getValue().getTotal()))
        );
        
        colProducts.setCellValueFactory(cellData -> {
            int saleId = cellData.getValue().getId();
            int productCount = purchaseDetailsDAO.countProductsBySale(saleId);
            return new javafx.beans.property.SimpleIntegerProperty(productCount).asObject();
        });
        
        // Columna de acciones con botones
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnDetails = new Button("Ver Detalles");
            private final Button btnCredit = new Button("Ver Crédito");
            private final HBox container = new HBox(5);
            
            {
                btnDetails.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 10px;");
                btnCredit.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 10px;");
                
                btnDetails.setOnAction(e -> {
                    Purchase sale = getTableView().getItems().get(getIndex());
                    showSaleDetails(sale);
                });
                
                btnCredit.setOnAction(e -> {
                    Purchase sale = getTableView().getItems().get(getIndex());
                    showCreditDetails(sale);
                });
                
                container.getChildren().add(btnDetails);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Purchase sale = getTableView().getItems().get(getIndex());
                    container.getChildren().clear();
                    container.getChildren().add(btnDetails);
                    
                    if ("CREDIT".equals(sale.getSaleType())) {
                        container.getChildren().add(btnCredit);
                    }
                    
                    setGraphic(container);
                }
            }
        });
    }

    private void setupComboBoxListeners() {
        cbSaleType.setValue("TODOS");
        
        cbPageSize.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                pageSize = Integer.parseInt(newVal);
                currentPage = 0;
                updateTableView();
            }
        });
    }

    private void loadInitialData() {
        // Cargar ventas del último mes por defecto
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(1);
        
        dpStartDate.setValue(startDate);
        dpEndDate.setValue(endDate);
        
        handleSearch();
    }

    @FXML
    private void handleSearch() {
        LocalDate startDate = dpStartDate.getValue();
        LocalDate endDate = dpEndDate.getValue();
        String saleType = cbSaleType.getValue();
        String clientName = txtClientFilter.getText().trim();
        String userName = txtUserFilter.getText().trim();
        String saleId = txtSaleIdFilter.getText().trim();
        
        Double minAmount = null;
        Double maxAmount = null;
        
        try {
            if (!txtMinAmount.getText().trim().isEmpty()) {
                minAmount = Double.parseDouble(txtMinAmount.getText().trim());
            }
            if (!txtMaxAmount.getText().trim().isEmpty()) {
                maxAmount = Double.parseDouble(txtMaxAmount.getText().trim());
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Los montos deben ser números válidos");
            return;
        }
        
        // Obtener todas las ventas con filtros desde el DAO
        allSales.clear();
        List<Purchase> sales = purchaseDAO.findWithFilters(
            startDate, endDate, saleType, clientName, userName, saleId, minAmount, maxAmount
        );
        allSales.addAll(sales);
        
        // Aplicar filtros locales adicionales si es necesario
        applyFilters();
        
        // Actualizar estadísticas
        updateStatistics();
        
        // Resetear paginación
        currentPage = 0;
        updateTableView();
    }

    private void applyFilters() {
        filteredSales.clear();
        filteredSales.addAll(allSales);
    }

    private void updateTableView() {
        totalPages = (int) Math.ceil((double) filteredSales.size() / pageSize);
        if (totalPages == 0) totalPages = 1;
        
        int fromIndex = currentPage * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, filteredSales.size());
        
        if (fromIndex < filteredSales.size()) {
            tblSales.setItems(FXCollections.observableArrayList(
                filteredSales.subList(fromIndex, toIndex)
            ));
        } else {
            tblSales.setItems(FXCollections.observableArrayList());
        }
        
        updatePaginationButtons();
    }

    private void updatePaginationButtons() {
        lblPageInfo.setText("Página " + (currentPage + 1) + " de " + totalPages);
        
        btnFirstPage.setDisable(currentPage == 0);
        btnPrevPage.setDisable(currentPage == 0);
        btnNextPage.setDisable(currentPage >= totalPages - 1);
        btnLastPage.setDisable(currentPage >= totalPages - 1);
    }

    private void updateStatistics() {
        int totalSales = filteredSales.size();
        double totalAmount = filteredSales.stream()
            .mapToDouble(Purchase::getTotal)
            .sum();
        
        double cashSales = filteredSales.stream()
            .filter(s -> "COUNT".equals(s.getSaleType()))
            .mapToDouble(Purchase::getTotal)
            .sum();
        
        double creditSales = filteredSales.stream()
            .filter(s -> "CREDIT".equals(s.getSaleType()))
            .mapToDouble(Purchase::getTotal)
            .sum();
        
        lblTotalSales.setText(String.valueOf(totalSales));
        lblTotalAmount.setText(currencyFormat.format(totalAmount));
        lblCashSales.setText(currencyFormat.format(cashSales));
        lblCreditSales.setText(currencyFormat.format(creditSales));
    }

    @FXML
    private void handleClearFilters() {
        dpStartDate.setValue(null);
        dpEndDate.setValue(null);
        cbSaleType.setValue("TODOS");
        txtClientFilter.clear();
        txtUserFilter.clear();
        txtSaleIdFilter.clear();
        txtMinAmount.clear();
        txtMaxAmount.clear();
        
        loadInitialData();
    }

    @FXML
    private void handleExport() {
        // Implementar exportación a Excel
        showAlert(Alert.AlertType.INFORMATION, "Exportar", 
                 "Funcionalidad de exportación a Excel en desarrollo");
    }

    @FXML
    private void handleFirstPage() {
        currentPage = 0;
        updateTableView();
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 0) {
            currentPage--;
            updateTableView();
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            updateTableView();
        }
    }

    @FXML
    private void handleLastPage() {
        currentPage = totalPages - 1;
        updateTableView();
    }

    private void showSaleDetails(Purchase sale) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/SaleDetailsView.fxml"));
            Parent root = loader.load();
            
            SaleDetailsController controller = loader.getController();
            controller.setSale(sale);
            
            Stage stage = new Stage();
            stage.setTitle("Detalles de Venta #" + sale.getId());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", 
                     "No se pudo abrir la vista de detalles: " + e.getMessage());
        }
    }

    private void showCreditDetails(Purchase sale) {
        Credit credit = creditDAO.findBySaleId(sale.getId());
        
        if (credit == null) {
            showAlert(Alert.AlertType.WARNING, "Sin Crédito", 
                     "Esta venta no tiene información de crédito asociada");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/CreditDetailsView.fxml"));
            Parent root = loader.load();
            
            CreditDetailsController controller = loader.getController();
            controller.setCredit(credit);
            
            Stage stage = new Stage();
            stage.setTitle("Detalles de Crédito - Venta #" + sale.getId());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", 
                     "No se pudo abrir la vista de crédito: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}