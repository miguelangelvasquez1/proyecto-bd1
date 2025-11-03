package com.tienda.controller;

import com.tienda.dao.ClientDAO;
import com.tienda.model.Client;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.Optional;

public class ClientController {
    
    @FXML private ComboBox<String> cmbDocumentType;
    @FXML private TextField txtDocumentNumber;
    @FXML private TextField txtName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private TextArea txtAddress;
    @FXML private TextField txtSearch;
    
    @FXML private TableView<Client> tableClients;
    @FXML private TableColumn<Client, String> colDocumentType;
    @FXML private TableColumn<Client, String> colDocumentNumber;
    @FXML private TableColumn<Client, String> colName;
    @FXML private TableColumn<Client, String> colEmail;
    @FXML private TableColumn<Client, String> colPhone;
    
    @FXML private Button btnNew;
    @FXML private Button btnSave;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Label statusLabel;
    
    private ClientDAO clientDAO;
    private ObservableList<Client> clientList;
    private Client selectedClient;
    private boolean isEditing = false;
    
    public void initialize() {
        clientDAO = new ClientDAO();
        clientList = FXCollections.observableArrayList();
        
        setupTableColumns();
        setupTableSelection();
        loadClients();
        clearForm();
        
        cmbDocumentType.setValue("CC");
    }
    
    private void setupTableColumns() {
        colDocumentType.setCellValueFactory(new PropertyValueFactory<>("documentType"));
        colDocumentNumber.setCellValueFactory(new PropertyValueFactory<>("documentNumber"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
    }
    
    private void setupTableSelection() {
        tableClients.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    selectedClient = newSelection;
                    fillForm(selectedClient);
                    btnEdit.setDisable(false);
                    btnDelete.setDisable(false);
                } else {
                    selectedClient = null;
                    btnEdit.setDisable(true);
                    btnDelete.setDisable(true);
                }
            }
        );
    }
    
    private void loadClients() {
        try {
            List<Client> clients = clientDAO.findAll();
            clientList.clear();
            clientList.addAll(clients);
            tableClients.setItems(clientList);
            statusLabel.setText("Clientes cargados: " + clients.size());
        } catch (Exception e) {
            showAlert("Error", "Error al cargar clientes: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void newClient() {
        clearForm();
        enableForm(true);
        isEditing = false;
        btnSave.setDisable(false);
        statusLabel.setText("Nuevo cliente");
    }
    
    @FXML
    private void saveClient() {
        if (!validateForm()) {
            return;
        }
        
        try {
            Client client = isEditing ? selectedClient : new Client();
            
            client.setDocumentType(cmbDocumentType.getValue());
            client.setDocumentNumber(txtDocumentNumber.getText().trim());
            client.setName(txtName.getText().trim());
            client.setEmail(txtEmail.getText().trim());
            client.setPhoneNumber(txtPhone.getText().trim());
            client.setAddress(txtAddress.getText().trim());
            
            boolean success;
            if (isEditing) {
                success = clientDAO.update(client);
                statusLabel.setText("Cliente actualizado");
            } else {
                success = clientDAO.save(client);
                statusLabel.setText("Cliente guardado");
            }
            
            if (success) {
                loadClients();
                clearForm();
                enableForm(false);
                btnSave.setDisable(true);
                showAlert("Éxito", "Cliente guardado correctamente", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Error", "No se pudo guardar el cliente", Alert.AlertType.ERROR);
            }
            
        } catch (Exception e) {
            showAlert("Error", "Error al guardar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void editClient() {
        if (selectedClient == null) {
            showAlert("Advertencia", "Seleccione un cliente para editar", Alert.AlertType.WARNING);
            return;
        }
        
        enableForm(true);
        isEditing = true;
        btnSave.setDisable(false);
        statusLabel.setText("Editando cliente: " + selectedClient.getName());
    }
    
    @FXML
    private void deleteClient() {
        if (selectedClient == null) {
            showAlert("Advertencia", "Seleccione un cliente para eliminar", Alert.AlertType.WARNING);
            return;
        }
        
        Optional<ButtonType> result = showConfirmation("Confirmar eliminación", 
            "¿Está seguro de eliminar el cliente: " + selectedClient.getName() + "?");
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = clientDAO.delete(selectedClient.getId());
                if (success) {
                    loadClients();
                    clearForm();
                    statusLabel.setText("Cliente eliminado");
                    showAlert("Éxito", "Cliente eliminado correctamente", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Error", "No se pudo eliminar el cliente", Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                showAlert("Error", "Error al eliminar: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }
    
    @FXML
    private void refreshTable() {
        loadClients();
    }
    
    @FXML
    private void searchClient() {
        String searchText = txtSearch.getText().trim();
        if (searchText.isEmpty()) {
            loadClients();
            return;
        }
        
        try {
            Client client = clientDAO.findByDocumentNumber(searchText);
            clientList.clear();
            if (client != null) {
                clientList.add(client);
                statusLabel.setText("Cliente encontrado");
            } else {
                statusLabel.setText("Cliente no encontrado");
            }
            tableClients.setItems(clientList);
        } catch (Exception e) {
            showAlert("Error", "Error en búsqueda: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void fillForm(Client client) {
        cmbDocumentType.setValue(client.getDocumentType());
        txtDocumentNumber.setText(client.getDocumentNumber());
        txtName.setText(client.getName());
        txtEmail.setText(client.getEmail() != null ? client.getEmail() : "");
        txtPhone.setText(client.getPhoneNumber() != null ? client.getPhoneNumber() : "");
        txtAddress.setText(client.getAddress() != null ? client.getAddress() : "");
    }
    
    private void clearForm() {
        cmbDocumentType.setValue("CC");
        txtDocumentNumber.clear();
        txtName.clear();
        txtEmail.clear();
        txtPhone.clear();
        txtAddress.clear();
        
        enableForm(false);
        selectedClient = null;
        isEditing = false;
        
        btnSave.setDisable(true);
        btnEdit.setDisable(true);
        btnDelete.setDisable(true);
    }
    
    private void enableForm(boolean enable) {
        cmbDocumentType.setDisable(!enable);
        txtDocumentNumber.setDisable(!enable);
        txtName.setDisable(!enable);
        txtEmail.setDisable(!enable);
        txtPhone.setDisable(!enable);
        txtAddress.setDisable(!enable);
    }
    
    private boolean validateForm() {
        if (cmbDocumentType.getValue() == null) {
            showAlert("Error", "Seleccione el tipo de documento", Alert.AlertType.ERROR);
            return false;
        }
        
        if (txtDocumentNumber.getText().trim().isEmpty()) {
            showAlert("Error", "Ingrese el número de documento", Alert.AlertType.ERROR);
            txtDocumentNumber.requestFocus();
            return false;
        }
        
        if (txtName.getText().trim().isEmpty()) {
            showAlert("Error", "Ingrese el nombre del cliente", Alert.AlertType.ERROR);
            txtName.requestFocus();
            return false;
        }
        
        // Validar email si se ingresó
        String email = txtEmail.getText().trim();
        if (!email.isEmpty() && !isValidEmail(email)) {
            showAlert("Error", "Ingrese un email válido", Alert.AlertType.ERROR);
            txtEmail.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private Optional<ButtonType> showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait();
    }
}