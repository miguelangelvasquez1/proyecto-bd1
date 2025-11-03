package com.tienda.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import com.tienda.dao.QuotaDAO;
import com.tienda.model.Credit;
import com.tienda.model.Quota;

public class CreditDetailsController {

    @FXML private Label lblCreditTitle;
    @FXML private Label lblCreditDate;
    @FXML private Label lblCreditState;
    @FXML private Label lblAmountFinanced;
    @FXML private Label lblInitialQuota;
    @FXML private Label lblMonths;
    @FXML private Label lblInterestRate;
    @FXML private Label lblQuotaValue;
    @FXML private Label lblPaidQuotas;
    @FXML private Label lblPendingQuotas;
    @FXML private Label lblTotalPaid;
    @FXML private Label lblBalance;
    
    @FXML private TableView<Quota> tblQuotas;
    @FXML private TableColumn<Quota, Integer> colQuotaNumber;
    @FXML private TableColumn<Quota, String> colExpirationDate;
    @FXML private TableColumn<Quota, String> colQuotaValue;
    @FXML private TableColumn<Quota, String> colPayedValue;
    @FXML private TableColumn<Quota, String> colPayedDate;
    @FXML private TableColumn<Quota, String> colQuotaState;
    @FXML private TableColumn<Quota, Void> colActions;
    
    @FXML private Button btnRegisterPayment;
    
    private QuotaDAO quotaDAO;
    private Credit credit;
    private DecimalFormat currencyFormat;
    private DateTimeFormatter dateFormatter;

    @FXML
    public void initialize() {
        quotaDAO = new QuotaDAO();
        currencyFormat = new DecimalFormat("$#,##0.00");
        dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        setupTableColumns();
    }

    private void setupTableColumns() {
        colQuotaNumber.setCellValueFactory(new PropertyValueFactory<>("quotaNumber"));
        
        colExpirationDate.setCellValueFactory(cellData -> 
            new SimpleStringProperty(
                cellData.getValue().getExpirationDate() != null ?
                cellData.getValue().getExpirationDate().format(dateFormatter) : "N/A"
            )
        );
        
        colQuotaValue.setCellValueFactory(cellData -> 
            new SimpleStringProperty(currencyFormat.format(cellData.getValue().getQuotaValue()))
        );
        
        colPayedValue.setCellValueFactory(cellData -> {
            Double payed = cellData.getValue().getPayedValue();
            if (payed != null) {
                return new SimpleStringProperty(currencyFormat.format(payed));
            }
            return new SimpleStringProperty("-");
        });
        
        colPayedDate.setCellValueFactory(cellData -> {
            LocalDate payedDate = cellData.getValue().getPayedAt();
            if (payedDate != null) {
                return new SimpleStringProperty(payedDate.format(dateFormatter));
            }
            return new SimpleStringProperty("-");
        });
        
        colQuotaState.setCellValueFactory(cellData -> {
            String state = cellData.getValue().getState();
            String displayState = getQuotaStateDisplay(state);
            return new SimpleStringProperty(displayState);
        });
        
        // Columna de acciones
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnPay = new Button("Pagar");
            
            {
                btnPay.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 10px;");
                btnPay.setOnAction(e -> {
                    Quota quota = getTableView().getItems().get(getIndex());
                    handlePayQuota(quota);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Quota quota = getTableView().getItems().get(getIndex());
                    if ("PENDIENTE".equals(quota.getState()) || "MORA".equals(quota.getState())) {
                        setGraphic(btnPay);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }

    public void setCredit(Credit credit) {
        this.credit = credit;
        loadCreditData();
    }

    private void loadCreditData() {
        if (credit == null) return;
        
        // Información del encabezado
        lblCreditTitle.setText("Crédito #" + credit.getId());
        lblCreditDate.setText("Fecha: " + credit.getCreatedAt().format(dateFormatter));
        
        // Estado del crédito
        lblCreditState.setText(credit.getState());
        updateStateStyle(credit.getState());
        
        // Información del crédito
        lblAmountFinanced.setText(currencyFormat.format(credit.getAmountFinanced()));
        lblInitialQuota.setText(currencyFormat.format(credit.getInitialQuota()));
        lblMonths.setText(credit.getMonths() + " meses");
        lblInterestRate.setText(String.format("%.2f%%", credit.getInterestRate()));
        
        // Cargar cuotas
        loadQuotas();
        
        // Habilitar/deshabilitar botón según estado
        btnRegisterPayment.setDisable(!"VIGENTE".equals(credit.getState()));
    }

    private void loadQuotas() {
        List<Quota> quotas = quotaDAO.findByCreditId(credit.getId());
        tblQuotas.setItems(FXCollections.observableArrayList(quotas));
        
        // Calcular valor de cuota (asumiendo todas iguales)
        if (!quotas.isEmpty()) {
            lblQuotaValue.setText(currencyFormat.format(quotas.get(0).getQuotaValue()));
        }
        
        // Calcular estadísticas
        calculateStatistics(quotas);
    }

    private void calculateStatistics(List<Quota> quotas) {
        int paidCount = 0;
        int pendingCount = 0;
        double totalPaid = 0.0;
        double balance = 0.0;
        
        for (Quota quota : quotas) {
            if ("PAGADO".equals(quota.getState())) {
                paidCount++;
                if (quota.getPayedValue() != null) {
                    totalPaid += quota.getPayedValue();
                }
            } else {
                pendingCount++;
                balance += quota.getQuotaValue();
            }
        }
        
        lblPaidQuotas.setText(String.valueOf(paidCount));
        lblPendingQuotas.setText(String.valueOf(pendingCount));
        lblTotalPaid.setText(currencyFormat.format(totalPaid));
        lblBalance.setText(currencyFormat.format(balance));
    }

    private void handlePayQuota(Quota quota) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmar Pago");
        confirmDialog.setHeaderText("Registrar pago de cuota #" + quota.getQuotaNumber());
        confirmDialog.setContentText(
            "Valor de la cuota: " + currencyFormat.format(quota.getQuotaValue()) + "\n" +
            "Fecha de vencimiento: " + quota.getExpirationDate().format(dateFormatter) + "\n\n" +
            "¿Desea registrar el pago completo de esta cuota?"
        );
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Actualizar cuota
            quota.setPayedValue(quota.getQuotaValue());
            quota.setPayedAt(LocalDate.now());
            quota.setState("PAGADO");
            
            // Guardar en BD
            boolean success = quotaDAO.updateQuota(quota);
            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Pago Registrado", 
                         "El pago de la cuota #" + quota.getQuotaNumber() + " se ha registrado correctamente.");
                loadQuotas(); // Recargar tabla
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", 
                         "No se pudo registrar el pago. Intente nuevamente.");
            }
        }
    }

    @FXML
    private void handleRegisterPayment() {
        // Buscar primera cuota pendiente
        List<Quota> quotas = quotaDAO.findByCreditId(credit.getId());
        Quota pendingQuota = quotas.stream()
            .filter(q -> "PENDIENTE".equals(q.getState()) || "MORA".equals(q.getState()))
            .findFirst()
            .orElse(null);
        
        if (pendingQuota != null) {
            handlePayQuota(pendingQuota);
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Sin Cuotas Pendientes", 
                     "No hay cuotas pendientes de pago.");
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) lblCreditTitle.getScene().getWindow();
        stage.close();
    }

    private void updateStateStyle(String state) {
        String style = "-fx-text-fill: white; -fx-padding: 5 15; -fx-background-radius: 15; -fx-font-weight: bold;";
        
        switch (state) {
            case "VIGENTE":
                lblCreditState.setStyle("-fx-background-color: #4CAF50; " + style);
                break;
            case "CANCELADO":
                lblCreditState.setStyle("-fx-background-color: #2196F3; " + style);
                break;
            case "MORA":
                lblCreditState.setStyle("-fx-background-color: #F44336; " + style);
                break;
            default:
                lblCreditState.setStyle("-fx-background-color: #757575; " + style);
        }
    }

    private String getQuotaStateDisplay(String state) {
        if (state == null) return "N/A";
        
        switch (state) {
            case "PENDIENTE": return "Pendiente";
            case "PAGADO": return "Pagado";
            case "MORA": return "En Mora";
            default: return state;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}