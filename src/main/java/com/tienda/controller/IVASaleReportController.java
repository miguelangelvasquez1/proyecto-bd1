package com.tienda.controller;

import com.tienda.model.dtos.SaleReportDTO;
import com.tienda.dao.ReportDAO;
import com.tienda.util.IVAReportGenerator;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.Date;
import java.util.List;

public class IVASaleReportController {

    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;

    @FXML private Button btnSearch;
    @FXML private Button btnPdf;

    @FXML private TableView<SaleReportDTO> reportTable;

    @FXML private TableColumn<SaleReportDTO, Integer> colSaleId;
    @FXML private TableColumn<SaleReportDTO, String> colProduct;
    @FXML private TableColumn<SaleReportDTO, Integer> colQty;
    @FXML private TableColumn<SaleReportDTO, Date> colDate;
    @FXML private TableColumn<SaleReportDTO, String> colType;
    @FXML private TableColumn<SaleReportDTO, Double> colSubtotal;
    @FXML private TableColumn<SaleReportDTO, Double> colIVA;
    @FXML private TableColumn<SaleReportDTO, Double> colTotal;
    @FXML private TableColumn<SaleReportDTO, String> colClient;

    @FXML private Label lblSubtotal;
    @FXML private Label lblIva;
    @FXML private Label lblTotal;

    private final ReportDAO reportDAO = new ReportDAO();

    @FXML
    public void initialize() {
        // Binding de columnas
        colSaleId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getSaleId()).asObject());
        colProduct.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getProductName()));
        colQty.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getQuantity()).asObject());
        colDate.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getDate()));
        colType.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getSaleType()));
        colSubtotal.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getSubtotal()).asObject());
        colIVA.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getIvaTotal()).asObject());
        colTotal.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getTotal()).asObject());
        colClient.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getClientName()));

        btnSearch.setOnAction(e -> loadReportData());
        btnPdf.setOnAction(e -> generatePdf());
    }

    // CARGAR DATOS
    private void loadReportData() {
        try {
            Date from = (fromDatePicker.getValue() != null) ? Date.valueOf(fromDatePicker.getValue()) : null;
            Date to = (toDatePicker.getValue() != null) ? Date.valueOf(toDatePicker.getValue()) : null;

            List<SaleReportDTO> list = reportDAO.getSalesForIVAReport(from, to);

            reportTable.getItems().setAll(list);
            updateTotals(list);

        } catch (Exception ex) {
            showAlert("Error al cargar datos: " + ex.getMessage());
        }
    }

    // CALCULAR TOTALES
    private void updateTotals(List<SaleReportDTO> list) {
        double subtotal = list.stream().mapToDouble(SaleReportDTO::getSubtotal).sum();
        double iva = list.stream().mapToDouble(SaleReportDTO::getIvaTotal).sum();
        double total = list.stream().mapToDouble(SaleReportDTO::getTotal).sum();

        lblSubtotal.setText(String.format("$ %.2f", subtotal));
        lblIva.setText(String.format("$ %.2f", iva));
        lblTotal.setText(String.format("$ %.2f", total));
    }

    // GENERAR PDF
    private void generatePdf() {
    try {
        List<SaleReportDTO> list = reportTable.getItems();

        if (list.isEmpty()) {
            showAlert("There are not data to generate PDF");
            return;
        }

        // Obtener fechas del selector
        Date startDate = (fromDatePicker.getValue() != null)
                ? Date.valueOf(fromDatePicker.getValue())
                : null;

        Date endDate = (toDatePicker.getValue() != null)
                ? Date.valueOf(toDatePicker.getValue())
                : null;

        if (startDate == null || endDate == null) {
            showAlert("You have to select a initial date and final date");
            return;
        }

        // Calcular IVA total
        double totalIVA = list.stream()
                .mapToDouble(SaleReportDTO::getIvaTotal)
                .sum();

        // Llamar correctamente al método del generador
        IVAReportGenerator.generateReport(list, totalIVA, startDate, endDate);

        showAlert("Reporte PDF generado con éxito.");

    } catch (Exception ex) {
        ex.printStackTrace();
        showAlert("Error al generar PDF: " + ex.getMessage());
    }
}


    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Mensaje");
        alert.setContentText(msg);
        alert.show();
    }
}
