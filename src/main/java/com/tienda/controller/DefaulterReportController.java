package com.tienda.controller;

import com.tienda.dao.ReportDAO;
import com.tienda.model.dtos.DefaulterClientDTO;
import com.tienda.util.DefaulterReportGenerator;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;

import java.io.File;
import java.util.List;

public class DefaulterReportController {

    private final ReportDAO reportDAO = new ReportDAO();

    @FXML
    private void handleGenerateReport() {
        try {
            List<DefaulterClientDTO> defaulters = reportDAO.getDefaulterClients();

            if (defaulters.isEmpty()) {
                showAlert("No hay clientes morosos actualmente.", Alert.AlertType.INFORMATION);
                return;
            }

            File file = DefaulterReportGenerator.generateReport(defaulters);

            showAlert("Reporte generado en:\n" + file.getAbsolutePath(), Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error generando el reporte: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}
