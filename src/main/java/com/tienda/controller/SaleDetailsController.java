package com.tienda.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.tienda.dao.PurchaseDetailsDAO;
import com.tienda.model.Purchase;
import com.tienda.model.PurchaseDetails;

public class SaleDetailsController {

    @FXML private Label lblSaleTitle;
    @FXML private Label lblSaleDate;
    @FXML private Label lblSaleType;
    @FXML private Label lblClient;
    @FXML private Label lblCashier;
    @FXML private Label lblSubtotal;
    @FXML private Label lblIva;
    @FXML private Label lblTotal;
    
    @FXML private TableView<PurchaseDetails> tblProducts;
    @FXML private TableColumn<PurchaseDetails, String> colProductCode;
    @FXML private TableColumn<PurchaseDetails, String> colProductName;
    @FXML private TableColumn<PurchaseDetails, Integer> colQuantity;
    @FXML private TableColumn<PurchaseDetails, String> colUnitPrice;
    @FXML private TableColumn<PurchaseDetails, String> colIvaApplied;
    @FXML private TableColumn<PurchaseDetails, String> colSubtotalProduct;
    
    private PurchaseDetailsDAO purchaseDetailsDAO;
    private Purchase sale;
    private DecimalFormat currencyFormat;
    private DateTimeFormatter dateFormatter;

    @FXML
    public void initialize() {
        purchaseDetailsDAO = new PurchaseDetailsDAO();
        currencyFormat = new DecimalFormat("$#,##0.00");
        dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        setupTableColumns();
    }

    private void setupTableColumns() {
        colProductCode.setCellValueFactory(cellData -> 
            new SimpleStringProperty(
                cellData.getValue().getProduct() != null ? 
                cellData.getValue().getProduct().getCode() : "N/A"
            )
        );
        
        colProductName.setCellValueFactory(cellData -> 
            new SimpleStringProperty(
                cellData.getValue().getProduct() != null ? 
                cellData.getValue().getProduct().getName() : "N/A"
            )
        );
        
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("amount"));
        
        colUnitPrice.setCellValueFactory(cellData -> {
            Double price = cellData.getValue().getUnitPrice();
            if (price != null) {
                return new SimpleStringProperty(currencyFormat.format(price));
            }
            return new SimpleStringProperty("$0.00");
        });
        
        colIvaApplied.setCellValueFactory(cellData -> {
            Double iva = cellData.getValue().getIvaApplied();
            if (iva != null) {
                return new SimpleStringProperty(currencyFormat.format(iva));
            }
            return new SimpleStringProperty("$0.00");
        });
        
        colSubtotalProduct.setCellValueFactory(cellData -> 
            new SimpleStringProperty(currencyFormat.format(cellData.getValue().getSubtotal()))
        );
    }

    public void setSale(Purchase sale) {
        this.sale = sale;
        loadSaleData();
    }

    private void loadSaleData() {
        if (sale == null) return;
        
        // Información del encabezado
        lblSaleTitle.setText("Venta #" + sale.getId());
        lblSaleDate.setText("Fecha: " + sale.getDate().format(dateFormatter));
        
        // Tipo de venta
        String displayType = "COUNT".equals(sale.getSaleType()) ? "CONTADO" : "CRÉDITO";
        lblSaleType.setText(displayType);
        if ("COUNT".equals(sale.getSaleType())) {
            lblSaleType.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 5 15; -fx-background-radius: 15; -fx-font-weight: bold;");
        } else {
            lblSaleType.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-padding: 5 15; -fx-background-radius: 15; -fx-font-weight: bold;");
        }
        
        // Información general
        lblClient.setText(sale.getClient() != null ? sale.getClient().getName() : "N/A");
        lblCashier.setText(sale.getUser() != null ? sale.getUser().getName() : "N/A");
        lblSubtotal.setText(currencyFormat.format(sale.getSubtotal()));
        lblIva.setText(currencyFormat.format(sale.getIvaTotal()));
        lblTotal.setText(currencyFormat.format(sale.getTotal()));
        
        // Cargar productos
        loadProducts();
    }

    private void loadProducts() {
        List<PurchaseDetails> details = purchaseDetailsDAO.findBySaleId(sale.getId());
        tblProducts.setItems(FXCollections.observableArrayList(details));
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) lblSaleTitle.getScene().getWindow();
        stage.close();
    }
}