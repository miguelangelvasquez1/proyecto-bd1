package com.tienda.controller;

import com.tienda.dao.AccessBinnacleDAO;
import com.tienda.dao.UserDAO;
import com.tienda.model.AccessBinnacle;
import com.tienda.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.util.StringConverter;

public class AccessLogController {

    @FXML
    private ComboBox<User> cmbUser;
    @FXML
    private DatePicker dateFrom;
    @FXML
    private DatePicker dateTo;

    @FXML
    private TableView<AccessLogEntry> tableAccessLog;
    @FXML
    private TableColumn<AccessLogEntry, Integer> colId;
    @FXML
    private TableColumn<AccessLogEntry, String> colUser;
    @FXML
    private TableColumn<AccessLogEntry, String> colEntryDateTime;
    @FXML
    private TableColumn<AccessLogEntry, String> colDepartureDateTime;
    @FXML
    private TableColumn<AccessLogEntry, String> colSessionDuration;
    @FXML
    private TableColumn<AccessLogEntry, String> colIpAddress;
    @FXML
    private TableColumn<AccessLogEntry, String> colStatus;

    @FXML
    private Label statusLabel;
    @FXML
    private Label recordCountLabel;
    @FXML
    private Label activeSessionsLabel;

    private AccessBinnacleDAO accessBinnacleDAO;
    private UserDAO userDAO;
    private ObservableList<AccessLogEntry> logEntries;

    public void initialize() {
        accessBinnacleDAO = new AccessBinnacleDAO();
        userDAO = new UserDAO();
        logEntries = FXCollections.observableArrayList();

        setupTableColumns();
        initializeDates();
        loadUsers();
        loadAccessLog();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("userName"));
        colEntryDateTime.setCellValueFactory(new PropertyValueFactory<>("entryDateTime"));
        colDepartureDateTime.setCellValueFactory(new PropertyValueFactory<>("departureDateTime"));
        colSessionDuration.setCellValueFactory(new PropertyValueFactory<>("sessionDuration"));
        colIpAddress.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        tableAccessLog.setItems(logEntries);
    }

    private void initializeDates() {
        LocalDate now = LocalDate.now();
        dateFrom.setValue(now.minusDays(30)); // Últimos 30 días
        dateTo.setValue(now);
    }

    private void loadUsers() {
        try {
            List<User> users = userDAO.findAll();
            ObservableList<User> userList = FXCollections.observableArrayList(users);
            cmbUser.setItems(userList);
            cmbUser.setConverter(new StringConverter<User>() {
                @Override
                public String toString(User user) {
                    return user != null ? user.getName() : "Todos los usuarios";
                }

                @Override
                public User fromString(String string) {
                    return null;
                }
            });
        } catch (Exception e) {
            statusLabel.setText("Error cargando usuarios");
        }
    }

    @FXML
    private void filterData() {
        loadAccessLog();
    }

    @FXML
    private void refreshData() {
        loadAccessLog();
    }

    private void loadAccessLog() {
        try {
            statusLabel.setText("Cargando bitácora de acceso...");

            // Limpiar lista
            logEntries.clear();

            List<AccessBinnacle> binnacles = accessBinnacleDAO.findAll();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

            for (AccessBinnacle ab : binnacles) {

                AccessLogEntry entry = new AccessLogEntry();
                entry.setId(ab.getId());
                entry.setUserName(ab.getUser() != null ? ab.getUser().getName() : "Desconocido");

                if (ab.getEntryDateTime() != null) {
                    entry.setEntryDateTime(ab.getEntryDateTime().format(formatter));
                } else {
                    entry.setEntryDateTime("N/A");
                }

                if (ab.getDepartureDateTime() != null) {
                    entry.setDepartureDateTime(ab.getDepartureDateTime().format(formatter));

                    Duration duration = Duration.between(ab.getEntryDateTime(), ab.getDepartureDateTime());
                    long hours = duration.toHours();
                    long minutes = duration.toMinutes() % 60;
                    entry.setSessionDuration(String.format("%02d:%02d", hours, minutes));
                    entry.setStatus("Cerrado");
                } else {
                    entry.setDepartureDateTime("Activa");
                    entry.setSessionDuration("En curso");
                    entry.setStatus("Activo");
                }

                entry.setIpAddress(ab.getIp() != null ? ab.getIp() : "N/A");

                logEntries.add(entry);
            }

            // Aplicar filtros si están seleccionados
            applyFilters();

            // Actualizar contadores
            recordCountLabel.setText("Registros: " + logEntries.size());

            long activeSessions = logEntries.stream()
                    .filter(entry -> "Activo".equals(entry.getStatus()))
                    .count();
            activeSessionsLabel.setText("Sesiones activas: " + activeSessions);

            statusLabel.setText("Bitácora cargada correctamente");

        } catch (Exception e) {
            statusLabel.setText("Error cargando bitácora");
            showAlert("Error", "Error al cargar la bitácora: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void applyFilters() {
        // Implementar lógica de filtros por usuario y fechas
        User selectedUser = cmbUser.getValue();

        if (selectedUser != null) {
            logEntries.removeIf(entry -> !entry.getUserName().equals(selectedUser.getName()));
        }

        // Filtros por fecha se aplicarían aquí
    }

    @FXML
    private void exportData() {
        statusLabel.setText("Función de exportación no implementada");
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Clase auxiliar para entradas de bitácora
    public static class AccessLogEntry {
        private int id;
        private String userName;
        private String entryDateTime;
        private String departureDateTime;
        private String sessionDuration;
        private String ipAddress;
        private String status;

        // Getters y setters
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getEntryDateTime() {
            return entryDateTime;
        }

        public void setEntryDateTime(String entryDateTime) {
            this.entryDateTime = entryDateTime;
        }

        public String getDepartureDateTime() {
            return departureDateTime;
        }

        public void setDepartureDateTime(String departureDateTime) {
            this.departureDateTime = departureDateTime;
        }

        public String getSessionDuration() {
            return sessionDuration;
        }

        public void setSessionDuration(String sessionDuration) {
            this.sessionDuration = sessionDuration;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
