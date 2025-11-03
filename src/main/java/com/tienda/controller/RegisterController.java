package com.tienda.controller;

import com.tienda.dao.RoleDAO;
import com.tienda.dao.UserDAO;
import com.tienda.model.Role;
import com.tienda.model.User;
import com.tienda.util.SceneManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class RegisterController implements Initializable {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<Role> roleComboBox;
    @FXML private Button registerButton;
    @FXML private Hyperlink loginLink;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    private RoleDAO roleDAO;
    private UserDAO userDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Inicializar servicios
        roleDAO = new RoleDAO();
        userDAO = new UserDAO();
        
        // Configurar el ComboBox de roles
        setupRoleComboBox();
        
        // Configurar validación en tiempo real
        setupValidation();
    }

    private void setupRoleComboBox() {
        try {
            List<Role> roles = roleDAO.findAll();
            roleComboBox.getItems().addAll(roles);
            
            // Configurar cómo se muestra cada rol en el ComboBox
            roleComboBox.setCellFactory(listView -> new ListCell<Role>() {
                @Override
                protected void updateItem(Role role, boolean empty) {
                    super.updateItem(role, empty);
                    if (empty || role == null) {
                        setText(null);
                    } else {
                        setText(role.getName());
                    }
                }
            });
            
            roleComboBox.setButtonCell(new ListCell<Role>() {
                @Override
                protected void updateItem(Role role, boolean empty) {
                    super.updateItem(role, empty);
                    if (empty || role == null) {
                        setText(null);
                    } else {
                        setText(role.getName());
                    }
                }
            });
            
        } catch (Exception e) {
            showError("Error al cargar los roles: " + e.getMessage());
        }
    }

    private void setupValidation() {
        // Listeners para validación en tiempo real
        nameField.textProperty().addListener((observable, oldValue, newValue) -> validateForm());
        emailField.textProperty().addListener((observable, oldValue, newValue) -> validateForm());
        phoneField.textProperty().addListener((observable, oldValue, newValue) -> validateForm());
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> validateForm());
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> validateForm());
        roleComboBox.valueProperty().addListener((observable, oldValue, newValue) -> validateForm());
    }

    private void validateForm() {
        boolean isValid = !nameField.getText().trim().isEmpty() &&
                         !emailField.getText().trim().isEmpty() &&
                         !phoneField.getText().trim().isEmpty() &&
                         !passwordField.getText().isEmpty() &&
                         !confirmPasswordField.getText().isEmpty() &&
                         roleComboBox.getValue() != null &&
                         isValidEmail(emailField.getText().trim()) &&
                         passwordField.getText().equals(confirmPasswordField.getText()) &&
                         passwordField.getText().length() >= 6;

        registerButton.setDisable(!isValid);
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        // Obtener valores de los campos
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        Role selectedRole = roleComboBox.getValue();

        // Validaciones
        if (!validateInputs(name, email, phone, password, confirmPassword, selectedRole)) {
            return;
        }

        try {
            // Verificar si el email ya existe
            if (userDAO.findByEmail(email) != null) {
                showError("Ya existe un usuario registrado con este email.");
                return;
            }

            // Crear nuevo usuario
            User newUser = new User();
            newUser.setName(name);
            newUser.setEmail(email);
            newUser.setPhoneNumber(phone);
            newUser.setPassword(password); // En producción, deberías encriptar la contraseña
            newUser.setRole(selectedRole);

            // Registrar usuario
            boolean registeredUser = userDAO.save(newUser);
            
            if (registeredUser) {
                showSuccess("¡Usuario registrado exitosamente!");
                
                // Limpiar formulario
                clearForm();
                
                // Mostrar diálogo de confirmación y redirigir al login
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Registro Exitoso");
                alert.setHeaderText(null);
                alert.setContentText("Usuario registrado correctamente. Ahora puedes iniciar sesión.");
                alert.showAndWait();
                
                // Redirigir al login después de 2 segundos
                showLogin(event);
                
            } else {
                showError("Error al registrar el usuario. Intenta nuevamente.");
            }

        } catch (Exception e) {
            showError("Error durante el registro: " + e.getMessage());
        }
    }

    private boolean validateInputs(String name, String email, String phone, 
                                 String password, String confirmPassword, Role role) {
        // Validar campos vacíos
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || 
            password.isEmpty() || confirmPassword.isEmpty() || role == null) {
            showError("Por favor, completa todos los campos.");
            return false;
        }

        // Validar nombre
        if (name.length() < 2) {
            showError("El nombre debe tener al menos 2 caracteres.");
            return false;
        }

        // Validar email
        if (!isValidEmail(email)) {
            showError("Por favor, ingresa un email válido.");
            return false;
        }

        // Validar teléfono
        if (!isValidPhone(phone)) {
            showError("Por favor, ingresa un número de teléfono válido.");
            return false;
        }

        // Validar contraseña
        if (password.length() < 6) {
            showError("La contraseña debe tener al menos 6 caracteres.");
            return false;
        }

        // Validar confirmación de contraseña
        if (!password.equals(confirmPassword)) {
            showError("Las contraseñas no coinciden.");
            return false;
        }

        return true;
    }

    @FXML
    private void showLogin(ActionEvent event) {
        SceneManager.switchScene("/views/Login.fxml", "Login");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        successLabel.setVisible(false);
    }

    private void showSuccess(String message) {
        successLabel.setText(message);
        successLabel.setVisible(true);
        errorLabel.setVisible(false);
    }

    private void hideMessages() {
        errorLabel.setVisible(false);
        successLabel.setVisible(false);
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    }

    private boolean isValidPhone(String phone) {
        // Validar que solo contenga números, espacios, guiones y paréntesis
        return phone.matches("^[\\d\\s\\-\\(\\)\\+]{7,15}$");
    }

    private void clearForm() {
        nameField.clear();
        emailField.clear();
        phoneField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        roleComboBox.setValue(null);
        hideMessages();
    }
}