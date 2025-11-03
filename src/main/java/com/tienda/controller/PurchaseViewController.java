package com.tienda.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.text.DecimalFormat;

import com.tienda.dao.ProductDAO;
import com.tienda.model.Product;

public class PurchaseViewController {

    @FXML private TextField txtProductSearch;
    @FXML private Button btnSearch;
    @FXML private Label lblProductCode;
    @FXML private Label lblProductName;
    @FXML private Label lblProductDescription;
    @FXML private Label lblProductCategory;
    @FXML private Label lblProductStock;
    @FXML private Label lblProductPrice;
    @FXML private Spinner<Integer> spinQuantity;
    @FXML private Label lblTotalPrice;
    @FXML private Button btnPurchase;
    @FXML private Button btnClear;

    private ProductDAO productDAO;
    private Product currentProduct;
    private DecimalFormat currencyFormat;

    @FXML
    public void initialize() {
        productDAO = new ProductDAO();
        currencyFormat = new DecimalFormat("$#,##0.00");
        
        // Configurar spinner de cantidad
        SpinnerValueFactory<Integer> valueFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1, 1);
        spinQuantity.setValueFactory(valueFactory);
        spinQuantity.setDisable(true);
        
        // Listener para actualizar el total cuando cambie la cantidad
        spinQuantity.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> obs, Integer oldVal, Integer newVal) {
                updateTotalPrice();
            }
        });
        
        // Permitir búsqueda con Enter
        txtProductSearch.setOnAction(e -> handleSearchProduct());
    }

    @FXML
    private void handleSearchProduct() {
        String searchText = txtProductSearch.getText().trim();
        
        if (searchText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Búsqueda vacía", 
                     "Por favor ingrese un código o ID de producto");
            return;
        }
        
        Product product = null;
        
        // Intentar buscar por ID si es número
        try {
            int id = Integer.parseInt(searchText);
            product = productDAO.findById(id);
        } catch (NumberFormatException e) {
            // Si no es número, buscar por código
            product = productDAO.findByCode(searchText);
        }
        
        if (product != null) {
            displayProduct(product);
        } else {
            showAlert(Alert.AlertType.ERROR, "Producto no encontrado", 
                     "No se encontró ningún producto con el criterio: " + searchText);
            clearProductInfo();
        }
    }

    private void displayProduct(Product product) {
        this.currentProduct = product;
        
        lblProductCode.setText(product.getCode());
        lblProductName.setText(product.getName());
        lblProductDescription.setText(product.getDescription());
        lblProductCategory.setText(product.getCategory().getName());
        lblProductStock.setText(String.valueOf(product.getStock()));
        lblProductPrice.setText(currencyFormat.format(product.getSaleValue()));
        
        // Configurar spinner según stock disponible
        if (product.getStock() > 0) {
            SpinnerValueFactory<Integer> valueFactory = 
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, product.getStock(), 1);
            spinQuantity.setValueFactory(valueFactory);
            spinQuantity.setDisable(false);
            btnPurchase.setDisable(false);
            updateTotalPrice();
        } else {
            spinQuantity.setDisable(true);
            btnPurchase.setDisable(true);
            lblTotalPrice.setText("Total: $0.00");
            showAlert(Alert.AlertType.WARNING, "Sin stock", 
                     "Este producto no tiene stock disponible");
        }
    }

    private void updateTotalPrice() {
        if (currentProduct != null) {
            int quantity = spinQuantity.getValue();
            double total = currentProduct.getSaleValue() * quantity;
            lblTotalPrice.setText("Total: " + currencyFormat.format(total));
        }
    }

    @FXML
    private void handlePurchase() {
        if (currentProduct == null) {
            showAlert(Alert.AlertType.WARNING, "Sin producto", 
                     "Debe seleccionar un producto primero");
            return;
        }
        
        int quantity = spinQuantity.getValue();
        double total = currentProduct.getSaleValue() * quantity;
        
        // Confirmación de compra
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmar Compra");
        confirmation.setHeaderText("¿Desea confirmar esta compra?");
        confirmation.setContentText(
            "Producto: " + currentProduct.getName() + "\n" +
            "Cantidad: " + quantity + "\n" +
            "Total: " + currencyFormat.format(total)
        );
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                processPurchase(quantity, total);
            }
        });
    }

    private void processPurchase(int quantity, double total) {
        // Aquí implementarías la lógica de compra real
        // Por ejemplo: registrar en BD, actualizar stock, generar factura, etc.
        
        try {
            // Ejemplo: actualizar stock (necesitarías un método en ProductDAO)
            // productDAO.updateStock(currentProduct.getId(), currentProduct.getStock() - quantity);
            
            showAlert(Alert.AlertType.INFORMATION, "Compra exitosa", 
                     "La compra se ha realizado correctamente.\n" +
                     "Total pagado: " + currencyFormat.format(total));
            
            handleClear();
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error en la compra", 
                     "Ocurrió un error al procesar la compra: " + e.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        txtProductSearch.clear();
        clearProductInfo();
        currentProduct = null;
    }

    private void clearProductInfo() {
        lblProductCode.setText("-");
        lblProductName.setText("-");
        lblProductDescription.setText("-");
        lblProductCategory.setText("-");
        lblProductStock.setText("-");
        lblProductPrice.setText("-");
        lblTotalPrice.setText("Total: $0.00");
        
        SpinnerValueFactory<Integer> valueFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1, 1);
        spinQuantity.setValueFactory(valueFactory);
        spinQuantity.setDisable(true);
        btnPurchase.setDisable(true);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}