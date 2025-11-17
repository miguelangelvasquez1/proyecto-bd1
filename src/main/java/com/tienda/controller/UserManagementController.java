package com.tienda.controller;

import com.tienda.dao.UserDAO;
import com.tienda.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class UserManagementController implements Initializable {

    @FXML private TableView<User> tableUsers;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colName;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colPhone;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, Void> colActions;
    
    @FXML private TextField txtSearch;
    @FXML private Label lblTotalUsers;
    @FXML private Button btnBack;
    @FXML private Button btnRefresh;

    private UserDAO userDAO;
    private ObservableList<User> userList;
    private FilteredList<User> filteredData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userDAO = new UserDAO();
        userList = FXCollections.observableArrayList();
        
        // Configurar las columnas
        setupTableColumns();
        
        // Cargar usuarios
        loadUsers();
        
        // Configurar búsqueda
        setupSearch();
    }

    /**
     * Configura las columnas de la tabla
     */
    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        
        // Para el rol, necesitamos acceder a role.name
        colRole.setCellValueFactory(cellData -> {
            if (cellData.getValue().getRole() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getRole().getName()
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        
        // Centrar el contenido de las columnas
        colId.setStyle("-fx-alignment: CENTER;");
        colRole.setStyle("-fx-alignment: CENTER;");
        
        // Configurar columna de acciones con botón eliminar
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnDelete = new Button("🗑️ Eliminar");

            {
                btnDelete.getStyleClass().add("delete-button");
                btnDelete.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleDeleteUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox hBox = new HBox(btnDelete);
                    hBox.setAlignment(Pos.CENTER);
                    setGraphic(hBox);
                }
            }
        });
    }

    /**
     * Carga todos los usuarios desde la base de datos
     */
    private void loadUsers() {
        try {
            List<User> users = userDAO.findAll();
            userList.clear();
            userList.addAll(users);
            tableUsers.setItems(userList);
            
            // Actualizar contador
            updateUserCount();
            System.out.println("Usuarios cargados: " + users.size());
            
        } catch (Exception e) {
            System.err.println("Error al cargar usuarios: " + e.getMessage());
            e.printStackTrace();
            showError("Error", "No se pudieron cargar los usuarios", e.getMessage());
        }
    }

    /**
     * Configura la búsqueda en tiempo real (filtrado en memoria)
     */
    private void setupSearch() {
        filteredData = new FilteredList<>(userList, p -> true);
        
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(user -> {
                // Si el campo está vacío, mostrar todos
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                
                String lowerCaseFilter = newValue.toLowerCase();
                
                // Buscar en nombre
                if (user.getName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                // Buscar en email
                else if (user.getEmail().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                // Buscar en rol
                else if (user.getRole() != null && 
                         user.getRole().getName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                return false;
            });
            
            tableUsers.setItems(filteredData);
            updateUserCount();
        });
    }

    /**
     * Maneja la eliminación de un usuario
     */
    private void handleDeleteUser(User user) {
        // Confirmación de eliminación
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmar Eliminación");
        confirmAlert.setHeaderText("¿Está seguro de eliminar este usuario?");
        confirmAlert.setContentText(
            "Usuario: " + user.getName() + "\n" +
            "Email: " + user.getEmail() + "\n" +
            "Rol: " + (user.getRole() != null ? user.getRole().getName() : "N/A") + "\n\n" +
            "Esta acción no se puede deshacer."
        );

        Optional<ButtonType> result = confirmAlert.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Eliminar el usuario
                boolean deleted = userDAO.deleteUser(user.getId());
                
                if (deleted) {
                    // Mostrar mensaje de éxito
                    showSuccess("Usuario Eliminado", "El usuario ha sido eliminado correctamente.");
                    
                    // Recargar la tabla
                    loadUsers();
                } else {
                    showError("Error", "No se pudo eliminar el usuario", "El usuario no existe o ya fue eliminado.");
                }
                
            } catch (SQLException e) {
                System.err.println("Error al eliminar usuario: " + e.getMessage());
                e.printStackTrace();
                
                // Verificar si es error de constraint (llave foránea)
                if (e.getMessage().contains("REFERENCE constraint") || e.getMessage().contains("FOREIGN KEY")) {
                    showError(
                        "No se puede eliminar",
                        "El usuario tiene registros relacionados",
                        "Este usuario no puede ser eliminado porque tiene registros relacionados en otras tablas (ventas, bitácora, etc.).\n\n" +
                        "Debe eliminar primero esos registros o considerar desactivar el usuario en lugar de eliminarlo."
                    );
                } else {
                    showError("Error al eliminar", "No se pudo eliminar el usuario", e.getMessage());
                }
            }
        }
    }

    /**
     * Actualiza el contador de usuarios
     */
    private void updateUserCount() {
        int total = tableUsers.getItems().size();
        lblTotalUsers.setText("Total: " + total + (total == 1 ? " usuario" : " usuarios"));
    }

    @FXML
    private void handleSearch() {
        // La búsqueda ya está configurada en tiempo real
        txtSearch.requestFocus();
    }

    @FXML
    private void handleClearSearch() {
        txtSearch.clear();
        tableUsers.setItems(userList);
        updateUserCount();
    }

    @FXML
    private void handleRefresh() {
        loadUsers();
        txtSearch.clear();
        showInfo("Actualizado", "La lista de usuarios ha sido actualizada.");
    }

    @FXML
    private void handleBack() {
        try {
            // Obtener el Stage actual
            Stage currentStage = (Stage) btnBack.getScene().getWindow();
            
            // Cargar la vista del Home
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainWindow.fxml"));
            Parent root = loader.load();
            
            // Cargar CSS si existe
            URL cssUrl = getClass().getResource("/com/tienda/css/home.css");
            if (cssUrl != null) {
                root.getStylesheets().add(cssUrl.toExternalForm());
            }
            
            // Cambiar la escena
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle("Home");
            
        } catch (IOException e) {
            System.err.println("Error al volver al Home: " + e.getMessage());
            e.printStackTrace();
            showError("Error", "No se pudo volver al Home", e.getMessage());
        }
    }

    // Métodos auxiliares para mostrar alertas
    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

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