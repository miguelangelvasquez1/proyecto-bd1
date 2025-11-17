package com.tienda.controller;

import com.tienda.dao.ProductDAO;
import com.tienda.model.dtos.InventoryItemDTO;

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
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class InventoryController implements Initializable {

    @FXML private TableView<InventoryItemDTO> tableInventory;

    @FXML private TableColumn<InventoryItemDTO, Integer> colId;
    @FXML private TableColumn<InventoryItemDTO, String> colName;
    @FXML private TableColumn<InventoryItemDTO, String> colCategory;
    @FXML private TableColumn<InventoryItemDTO, Integer> colStock;

    @FXML private TableColumn<InventoryItemDTO, String> colCost;
    @FXML private TableColumn<InventoryItemDTO, String> colPrice;
    @FXML private TableColumn<InventoryItemDTO, String> colTotalCost;
    @FXML private TableColumn<InventoryItemDTO, String> colTotalValue;
    @FXML private TableColumn<InventoryItemDTO, String> colProfit;
    @FXML private Button btnBack;

    private ProductDAO productDAO;
    private ObservableList<InventoryItemDTO> inventoryList;
    private NumberFormat currencyFormat;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        productDAO = new ProductDAO();
        inventoryList = FXCollections.observableArrayList();
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

        setupTableColumns();
        loadInventory();
    }

    private void setupTableColumns() {

        colId.setCellValueFactory(cd ->
                new SimpleIntegerProperty(cd.getValue().getId()).asObject()
        );

        colName.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getName())
        );

        colCategory.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getCategory())
        );

        colStock.setCellValueFactory(cd ->
                new SimpleIntegerProperty(cd.getValue().getStock()).asObject()
        );

        colCost.setCellValueFactory(cd ->
                new SimpleStringProperty(currencyFormat.format(cd.getValue().getCost()))
        );

        colPrice.setCellValueFactory(cd ->
                new SimpleStringProperty(currencyFormat.format(cd.getValue().getPrice()))
        );

        colTotalCost.setCellValueFactory(cd -> {
            double total = cd.getValue().getCost() * cd.getValue().getStock();
            return new SimpleStringProperty(currencyFormat.format(total));
        });

        colTotalValue.setCellValueFactory(cd -> {
            double total = cd.getValue().getPrice() * cd.getValue().getStock();
            return new SimpleStringProperty(currencyFormat.format(total));
        });

        colProfit.setCellValueFactory(cd -> {
            double value = cd.getValue().getPrice() * cd.getValue().getStock();
            double cost = cd.getValue().getCost() * cd.getValue().getStock();
            return new SimpleStringProperty(currencyFormat.format(value - cost));
        });

        // Estilos
        colId.setStyle("-fx-alignment: CENTER;");
        colStock.setStyle("-fx-alignment: CENTER;");
        colCost.setStyle("-fx-alignment: CENTER-RIGHT;");
        colPrice.setStyle("-fx-alignment: CENTER-RIGHT;");
        colTotalCost.setStyle("-fx-alignment: CENTER-RIGHT;");
        colTotalValue.setStyle("-fx-alignment: CENTER-RIGHT;");
        colProfit.setStyle("-fx-alignment: CENTER-RIGHT;");
    }

    private void loadInventory() {
        try {
            List<InventoryItemDTO> inventory = productDAO.getInventoryByCategory();

            inventoryList.clear();
            inventoryList.addAll(inventory);

            tableInventory.setItems(inventoryList);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "No se pudo cargar el inventario.", e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            Stage currentStage = (Stage) btnBack.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainWindow.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle("Home");
            
        } catch (IOException e) {
            System.err.println("Error al volver al Home: " + e.getMessage());
            e.printStackTrace();
            showError("Error", "No se pudo volver al Home", e.getMessage());
        }
    }

    private void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
