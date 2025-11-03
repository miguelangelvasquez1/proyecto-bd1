package com.tienda.controller;

import com.tienda.dao.UserDAO;
import com.tienda.model.User;
import com.tienda.util.SceneManager;
import com.tienda.util.SessionManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Hyperlink registerLink;
    @FXML private Label errorLabel;

    private UserDAO userDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Inicializar el servicio de usuario
        userDAO = new UserDAO();
        
        // Configurar validación en tiempo real
        setupValidation();
    }

    private void setupValidation() {
        // Listener para habilitar/deshabilitar el botón de login
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateForm();
        });

        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateForm();
        });
    }

    private void validateForm() {
        boolean isValid = !emailField.getText().trim().isEmpty() && 
                         !passwordField.getText().trim().isEmpty() &&
                         isValidEmail(emailField.getText().trim());
        
        loginButton.setDisable(!isValid);
        
        if (!errorLabel.getText().isEmpty() && isValid) {
            hideError();
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validaciones básicas
        if (email.isEmpty() || password.isEmpty()) {
            showError("Por favor, completa todos los campos.");
            return;
        }

        if (!isValidEmail(email)) {
            showError("Por favor, ingresa un email válido.");
            return;
        }

        try {
            // Intentar autenticar al usuario
            User user = userDAO.validateCredentials(email, password) ? userDAO.findByEmail(email) : null;
            
            if (user != null) {
                hideError();

                SessionManager.startSession(user);
                
                // Mostrar mensaje de éxito
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Login Exitoso");
                alert.setHeaderText(null);
                alert.setContentText("¡Bienvenido, " + user.getName() + "!");
                alert.showAndWait();
                
                SceneManager.switchScene("/views/MainWindow.fxml", "Dashboard");
                
            } else {
                showError("Email o contraseña incorrectos.");
            }
            
        } catch (Exception e) {
            showError("Error al iniciar sesión: " + e.getMessage());
        }
    }

    @FXML
    private void showRegister(ActionEvent event) {
        try {
            SceneManager.switchScene("/views/Register.fxml", "Dashboard");
            
        } catch (Exception e) {
            showError("Error al cargar la ventana de registro: " + e.getMessage());
        }
    }

    @FXML
    private void showMain(ActionEvent event) {
        try {
            SceneManager.switchScene("/views/MainWindow.fxml", "Dashboard");
            
        } catch (Exception e) {
            showError("Error al cargar la ventana de registro: " + e.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setText("");
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    }

    // Método para limpiar los campos
    public void clearFields() {
        emailField.clear();
        passwordField.clear();
        hideError();
    }
}