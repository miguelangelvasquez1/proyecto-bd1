package com.tienda.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.text.DecimalFormat;
import java.util.List;

import com.tienda.dao.ProductCategoryDAO;
import com.tienda.dao.ProductDAO;
import com.tienda.model.Product;
import com.tienda.model.ProductCategory;

public class ProductFormController {

    @FXML private Label lblTitle;
    @FXML private TextField txtCode;
    @FXML private Label lblCodeError;
    @FXML private TextField txtName;
    @FXML private Label lblNameError;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<ProductCategory> cbCategory;
    @FXML private Label lblCategoryError;
    @FXML private TextField txtStock;
    @FXML private Label lblStockError;
    @FXML private TextField txtAcquisition;
    @FXML private Label lblAcquisitionError;
    @FXML private TextField txtSale;
    @FXML private Label lblSaleError;
    @FXML private Label lblIvaInfo;
    @FXML private Label lblUtilityInfo;
    @FXML private Label lblProfitInfo;
    @FXML private Button btnSave;
    
    private ProductDAO productDAO;
    private ProductCategoryDAO categoryDAO;
    private Product currentProduct;
    private boolean saved = false;
    private DecimalFormat currencyFormat;

    @FXML
    public void initialize() {
        productDAO = new ProductDAO();
        categoryDAO = new ProductCategoryDAO();
        currencyFormat = new DecimalFormat("$#,##0.00");
        
        loadCategories();
        setupListeners();
    }

    private void loadCategories() {
        List<ProductCategory> categories = categoryDAO.findAll();
        cbCategory.setItems(FXCollections.observableArrayList(categories));
        
        // Configurar cómo se muestra el texto
        cbCategory.setButtonCell(new ListCell<ProductCategory>() {
            @Override
            protected void updateItem(ProductCategory item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });
        
        cbCategory.setCellFactory(lv -> new ListCell<ProductCategory>() {
            @Override
            protected void updateItem(ProductCategory item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });
    }

    private void setupListeners() {
        // Listener para calcular información automáticamente
        cbCategory.valueProperty().addListener((obs, oldVal, newVal) -> updateCalculatedInfo());
        txtAcquisition.textProperty().addListener((obs, oldVal, newVal) -> updateCalculatedInfo());
        txtSale.textProperty().addListener((obs, oldVal, newVal) -> updateCalculatedInfo());
        
        // Validación en tiempo real
        txtCode.textProperty().addListener((obs, oldVal, newVal) -> hideError(lblCodeError));
        txtName.textProperty().addListener((obs, oldVal, newVal) -> hideError(lblNameError));
        cbCategory.valueProperty().addListener((obs, oldVal, newVal) -> hideError(lblCategoryError));
        txtStock.textProperty().addListener((obs, oldVal, newVal) -> hideError(lblStockError));
        txtAcquisition.textProperty().addListener((obs, oldVal, newVal) -> hideError(lblAcquisitionError));
        txtSale.textProperty().addListener((obs, oldVal, newVal) -> hideError(lblSaleError));
    }

    private void updateCalculatedInfo() {
        ProductCategory category = cbCategory.getValue();
        
        if (category != null) {
            lblIvaInfo.setText(String.format("%.1f%%", category.getIva() * 100));
            lblUtilityInfo.setText(String.format("%.1f%%", category.getUtility() * 100));
        } else {
            lblIvaInfo.setText("0.0%");
            lblUtilityInfo.setText("0.0%");
        }
        
        try {
            double acquisition = parseDouble(txtAcquisition.getText());
            double sale = parseDouble(txtSale.getText());
            
            if (acquisition > 0 && sale > 0) {
                double profit = sale - acquisition;
                lblProfitInfo.setText(currencyFormat.format(profit));
                
                if (profit < 0) {
                    lblProfitInfo.setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
                } else {
                    lblProfitInfo.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                }
            } else {
                lblProfitInfo.setText("$0.00");
            }
        } catch (Exception e) {
            lblProfitInfo.setText("$0.00");
        }
    }

    public void setProduct(Product product) {
        this.currentProduct = product;
        lblTitle.setText("Editar Producto");
        
        if (product != null) {
            txtCode.setText(product.getCode());
            txtName.setText(product.getName());
            txtDescription.setText(product.getDescription());
            txtStock.setText(String.valueOf(product.getStock()));
            txtAcquisition.setText(String.valueOf(product.getAcquisitionValue()));
            txtSale.setText(String.valueOf(product.getSaleValue()));
            
            // Seleccionar categoría
            if (product.getCategory() != null) {
                for (ProductCategory cat : cbCategory.getItems()) {
                    if (cat.getId() == product.getCategory().getId()) {
                        cbCategory.setValue(cat);
                        break;
                    }
                }
            }
        }
    }

    @FXML
    private void handleSave() {
        if (validateForm()) {
            Product product = currentProduct != null ? currentProduct : new Product();
            
            product.setCode(txtCode.getText().trim());
            product.setName(txtName.getText().trim());
            product.setDescription(txtDescription.getText().trim());
            product.setStock(Integer.parseInt(txtStock.getText().trim()));
            product.setAcquisitionValue(parseDouble(txtAcquisition.getText()));
            product.setSaleValue(parseDouble(txtSale.getText()));
            product.setCategory(cbCategory.getValue());
            
            boolean success;
            if (currentProduct == null) {
                success = productDAO.insert(product);
            } else {
                success = productDAO.update(product);
            }
            
            if (success) {
                saved = true;
                showAlert(Alert.AlertType.INFORMATION, "Éxito", 
                         "El producto se ha guardado correctamente.");
                handleCancel();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", 
                         "No se pudo guardar el producto. Verifique que el código no esté duplicado.");
            }
        }
    }

    private boolean validateForm() {
        boolean isValid = true;
        
        // Validar código
        if (txtCode.getText().trim().isEmpty()) {
            showError(lblCodeError, "El código es obligatorio");
            isValid = false;
        } else if (currentProduct == null) {
            // Solo validar duplicado en nuevos productos
            Product existing = productDAO.findByCode(txtCode.getText().trim());
            if (existing != null) {
                showError(lblCodeError, "Este código ya existe");
                isValid = false;
            }
        }
        
        // Validar nombre
        if (txtName.getText().trim().isEmpty()) {
            showError(lblNameError, "El nombre es obligatorio");
            isValid = false;
        }
        
        // Validar categoría
        if (cbCategory.getValue() == null) {
            showError(lblCategoryError, "Debe seleccionar una categoría");
            isValid = false;
        }
        
        // Validar stock
        try {
            int stock = Integer.parseInt(txtStock.getText().trim());
            if (stock < 0) {
                showError(lblStockError, "El stock no puede ser negativo");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            showError(lblStockError, "Ingrese un número válido");
            isValid = false;
        }
        
        // Validar valor de adquisición
        try {
            double acquisition = parseDouble(txtAcquisition.getText());
            if (acquisition <= 0) {
                showError(lblAcquisitionError, "Debe ser mayor a 0");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            showError(lblAcquisitionError, "Ingrese un valor válido");
            isValid = false;
        }
        
        // Validar valor de venta
        try {
            double sale = parseDouble(txtSale.getText());
            if (sale <= 0) {
                showError(lblSaleError, "Debe ser mayor a 0");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            showError(lblSaleError, "Ingrese un valor válido");
            isValid = false;
        }
        
        // Validar que el valor de venta sea mayor al de adquisición
        try {
            double acquisition = parseDouble(txtAcquisition.getText());
            double sale = parseDouble(txtSale.getText());
            if (sale <= acquisition) {
                showError(lblSaleError, "Debe ser mayor al valor de adquisición");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            // Ya se validó arriba
        }
        
        return isValid;
    }

    private void showError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
    }

    private void hideError(Label label) {
        label.setVisible(false);
    }

    private double parseDouble(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }
        // Remover símbolos de moneda y comas
        String cleaned = text.trim().replaceAll("[^0-9.]", "");
        return Double.parseDouble(cleaned);
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
    }

    public boolean isSaved() {
        return saved;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}