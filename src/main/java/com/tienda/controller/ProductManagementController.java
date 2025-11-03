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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.tienda.dao.ProductCategoryDAO;
import com.tienda.dao.ProductDAO;
import com.tienda.model.Product;
import com.tienda.model.ProductCategory;

public class ProductManagementController {

    @FXML private TextField txtSearch;
    @FXML private ComboBox<ProductCategory> cbCategoryFilter;
    @FXML private ComboBox<String> cbStockFilter;
    @FXML private Button btnClearFilters;
    @FXML private Button btnNew;
    @FXML private Button btnExport;
    @FXML private Label lblTotalProducts;
    
    @FXML private TableView<Product> tblProducts;
    @FXML private TableColumn<Product, Integer> colId;
    @FXML private TableColumn<Product, String> colCode;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colDescription;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private TableColumn<Product, Integer> colStock;
    @FXML private TableColumn<Product, String> colAcquisition;
    @FXML private TableColumn<Product, String> colSale;
    @FXML private TableColumn<Product, Void> colActions;
    
    @FXML private Button btnFirstPage;
    @FXML private Button btnPrevPage;
    @FXML private Label lblPageInfo;
    @FXML private Button btnNextPage;
    @FXML private Button btnLastPage;
    @FXML private ComboBox<String> cbPageSize;
    
    private ProductDAO productDAO;
    private ProductCategoryDAO categoryDAO;
    
    private ObservableList<Product> allProducts;
    private ObservableList<Product> filteredProducts;
    
    private int currentPage = 0;
    private int pageSize = 20;
    private int totalPages = 0;
    
    private DecimalFormat currencyFormat;

    @FXML
    public void initialize() {
        productDAO = new ProductDAO();
        categoryDAO = new ProductCategoryDAO();
        
        currencyFormat = new DecimalFormat("$#,##0.00");
        
        allProducts = FXCollections.observableArrayList();
        filteredProducts = FXCollections.observableArrayList();
        
        setupTableColumns();
        loadCategories();
        setupComboBoxListeners();
        loadProducts();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        colCategory.setCellValueFactory(cellData -> 
            new SimpleStringProperty(
                cellData.getValue().getCategory() != null ? 
                cellData.getValue().getCategory().getName() : "N/A"
            )
        );
        
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        
        // Estilo para stock bajo
        colStock.setCellFactory(column -> new TableCell<Product, Integer>() {
            @Override
            protected void updateItem(Integer stock, boolean empty) {
                super.updateItem(stock, empty);
                if (empty || stock == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.valueOf(stock));
                    if (stock == 0) {
                        setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
                    } else if (stock < 10) {
                        setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #4CAF50;");
                    }
                }
            }
        });
        
        colAcquisition.setCellValueFactory(cellData -> 
            new SimpleStringProperty(currencyFormat.format(cellData.getValue().getAcquisitionValue()))
        );
        
        colSale.setCellValueFactory(cellData -> 
            new SimpleStringProperty(currencyFormat.format(cellData.getValue().getSaleValue()))
        );
        
        // Columna de acciones
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Editar");
            private final Button btnDelete = new Button("Eliminar");
            private final HBox container = new HBox(5);
            
            {
                btnEdit.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 10px;");
                btnDelete.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-size: 10px;");
                
                btnEdit.setOnAction(e -> {
                    Product product = getTableView().getItems().get(getIndex());
                    handleEdit(product);
                });
                
                btnDelete.setOnAction(e -> {
                    Product product = getTableView().getItems().get(getIndex());
                    handleDelete(product);
                });
                
                container.getChildren().addAll(btnEdit, btnDelete);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });
    }

    private void loadCategories() {
        List<ProductCategory> categories = categoryDAO.findAll();
        
        ProductCategory allCategories = new ProductCategory();
        allCategories.setId(0);
        allCategories.setName("Todas las categorías");
        
        ObservableList<ProductCategory> categoryList = FXCollections.observableArrayList();
        categoryList.add(allCategories);
        categoryList.addAll(categories);
        
        cbCategoryFilter.setItems(categoryList);
        cbCategoryFilter.setValue(allCategories);
        
        // Configurar cómo se muestra el texto
        cbCategoryFilter.setButtonCell(new ListCell<ProductCategory>() {
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
        
        cbCategoryFilter.setCellFactory(lv -> new ListCell<ProductCategory>() {
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

    private void setupComboBoxListeners() {
        cbStockFilter.setValue("Todos");
        
        cbPageSize.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                pageSize = Integer.parseInt(newVal);
                currentPage = 0;
                updateTableView();
            }
        });
    }

    private void loadProducts() {
        allProducts.clear();
        List<Product> products = productDAO.findAll();
        allProducts.addAll(products);
        applyFilters();
    }

    @FXML
    private void handleSearch() {
        applyFilters();
    }

    private void applyFilters() {
        String searchText = txtSearch.getText().toLowerCase().trim();
        ProductCategory selectedCategory = cbCategoryFilter.getValue();
        String stockFilter = cbStockFilter.getValue();
        
        filteredProducts.clear();
        
        List<Product> filtered = allProducts.stream()
            .filter(product -> {
                // Filtro de búsqueda por texto
                boolean matchesSearch = searchText.isEmpty() || 
                    product.getCode().toLowerCase().contains(searchText) ||
                    product.getName().toLowerCase().contains(searchText) ||
                    (product.getDescription() != null && product.getDescription().toLowerCase().contains(searchText));
                
                // Filtro por categoría
                boolean matchesCategory = selectedCategory == null || 
                    selectedCategory.getId() == 0 ||
                    (product.getCategory() != null && product.getCategory().getId() == selectedCategory.getId());
                
                // Filtro por stock
                boolean matchesStock = true;
                if (stockFilter != null && !stockFilter.equals("Todos")) {
                    switch (stockFilter) {
                        case "Con Stock":
                            matchesStock = product.getStock() > 0;
                            break;
                        case "Sin Stock":
                            matchesStock = product.getStock() == 0;
                            break;
                        case "Stock Bajo":
                            matchesStock = product.getStock() > 0 && product.getStock() < 10;
                            break;
                    }
                }
                
                return matchesSearch && matchesCategory && matchesStock;
            })
            .collect(Collectors.toList());
        
        filteredProducts.addAll(filtered);
        
        // Actualizar contador
        lblTotalProducts.setText("Total: " + filteredProducts.size() + " productos");
        
        // Resetear paginación
        currentPage = 0;
        updateTableView();
    }

    private void updateTableView() {
        totalPages = (int) Math.ceil((double) filteredProducts.size() / pageSize);
        if (totalPages == 0) totalPages = 1;
        
        int fromIndex = currentPage * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, filteredProducts.size());
        
        if (fromIndex < filteredProducts.size()) {
            tblProducts.setItems(FXCollections.observableArrayList(
                filteredProducts.subList(fromIndex, toIndex)
            ));
        } else {
            tblProducts.setItems(FXCollections.observableArrayList());
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

    @FXML
    private void handleClearFilters() {
        txtSearch.clear();
        cbCategoryFilter.setValue(cbCategoryFilter.getItems().get(0));
        cbStockFilter.setValue("Todos");
        applyFilters();
    }

    @FXML
    private void handleNew() {
        showProductForm(null);
    }

    private void handleEdit(Product product) {
        showProductForm(product);
    }

    private void handleDelete(Product product) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmar Eliminación");
        confirmDialog.setHeaderText("¿Eliminar producto?");
        confirmDialog.setContentText(
            "¿Está seguro de eliminar el producto:\n" +
            product.getCode() + " - " + product.getName() + "?\n\n" +
            "Esta acción no se puede deshacer."
        );
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = productDAO.delete(product.getId());
            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Éxito", 
                         "El producto se ha eliminado correctamente.");
                loadProducts();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", 
                         "No se pudo eliminar el producto. Puede que esté asociado a ventas.");
            }
        }
    }

    private void showProductForm(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ProductFormView.fxml"));
            Parent root = loader.load();
            
            ProductFormController controller = loader.getController();
            if (product != null) {
                controller.setProduct(product);
            }
            
            Stage stage = new Stage();
            stage.setTitle(product == null ? "Nuevo Producto" : "Editar Producto");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            // Recargar productos después de cerrar el formulario
            if (controller.isSaved()) {
                loadProducts();
            }
            
        } catch (IOException e) {
            e.printStackTrace();    
            showAlert(Alert.AlertType.ERROR, "Error", 
                     "No se pudo abrir el formulario: " + e.getMessage());
        }
    }

    @FXML
    private void handleExport() {
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

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}