package com.tienda.controller;

import com.tienda.dao.ClientDAO;
import com.tienda.dao.ProductDAO;
import com.tienda.dao.PurchaseDAO;
import com.tienda.dao.PurchaseDetailsDAO;
import com.tienda.model.Client;
import com.tienda.model.Product;
import com.tienda.model.Purchase;
import com.tienda.model.PurchaseDetails;
import com.tienda.util.InvoiceGenerator;
import com.tienda.util.SessionManager;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.File;
import java.time.LocalDate;

public class CreatePurchaseController {

    @FXML private ComboBox<Client> clientCombo;
    @FXML private ComboBox<Product> productCombo;
    @FXML private TextField amountField;
    @FXML private ComboBox<String> saleTypeCombo;

    @FXML private Label subtotalLabel;
    @FXML private Label ivaLabel;
    @FXML private Label totalLabel;

    private Purchase lastSavedPurchase;

    @FXML
    public void initialize() {
        clientCombo.getItems().addAll(new ClientDAO().findAll());
        productCombo.getItems().addAll(new ProductDAO().findAll());
        saleTypeCombo.getItems().addAll("COUNT", "CREDIT");

        amountField.textProperty().addListener((obs, o, n) -> updateTotals());
        productCombo.valueProperty().addListener((obs, o, n) -> updateTotals());
    }

    private double getIVA(String category) {
        return switch (category.toLowerCase()) {
            case "audio" -> 0.16;
            case "video" -> 0.19;
            case "tecnología", "tecnologia" -> 0.12;
            case "cocina" -> 0.12;
            default -> 0.19;
        };
    }

    private double getUtilidad(String category) {
        return switch (category.toLowerCase()) {
            case "audio" -> 0.35;
            case "video" -> 0.39;
            case "tecnología", "tecnologia" -> 0.40;
            case "cocina" -> 0.35;
            default -> 0.35;
        };
    }

    private void updateTotals() {
        try {
            Product product = productCombo.getValue();
            if (product == null) return;

            int amount = Integer.parseInt(amountField.getText());

            String category = product.getCategory().getName();
            double ivaRate = getIVA(category);
            double utilidadRate = getUtilidad(category);

            double acquisition = product.getAcquisitionValue();
            double utilidad = acquisition * utilidadRate;
            double precioVenta = acquisition + utilidad;

            double subtotal = precioVenta * amount;
            double iva = subtotal * ivaRate;
            double total = subtotal + iva;

            subtotalLabel.setText("$" + subtotal);
            ivaLabel.setText("$" + iva);
            totalLabel.setText("$" + total);

        } catch (Exception e) {
            subtotalLabel.setText("$0");
            ivaLabel.setText("$0");
            totalLabel.setText("$0");
        }
    }

    @FXML
    public void handleSaveSale() {
        try {
            Client client = clientCombo.getValue();
            Product product = productCombo.getValue();
            int amount = Integer.parseInt(amountField.getText());
            String saleType = saleTypeCombo.getValue();

            String category = product.getCategory().getName();
            double ivaRate = getIVA(category);
            double utilidadRate = getUtilidad(category);

            double acquisition = product.getAcquisitionValue();
            double utilidad = acquisition * utilidadRate;
            double precioVenta = acquisition + utilidad;

            double subtotal = precioVenta * amount;
            double iva = subtotal * ivaRate;
            double total = subtotal + iva;

            Purchase purchase = new Purchase();
            purchase.setDate(LocalDate.now());
            purchase.setSaleType(saleType);
            purchase.setSubtotal(subtotal);
            purchase.setIvaTotal(iva);
            purchase.setTotal(total);
            purchase.setClient(client);
            purchase.setUser(SessionManager.getCurrentUser());

            PurchaseDAO purchaseDAO = new PurchaseDAO();
            boolean saved = purchaseDAO.save(purchase);

            if (!saved) {
                showError("No se pudo guardar la venta.");
                return;
            }

            PurchaseDetails details = new PurchaseDetails();
            details.setSale(purchase);
            details.setProduct(product);
            details.setAmount(amount);
            details.setUnitPrice(precioVenta);
            details.setIvaApplied(iva);
            details.setSubtotal(subtotal);

            new PurchaseDetailsDAO().save(details);

            lastSavedPurchase = purchase;

            showInfo("Venta registrada con éxito. ID: " + purchase.getId());

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error al guardar la venta.");
        }
    }

    @FXML
    public void handleGenerateInvoice() {
        try {
            if (lastSavedPurchase == null) {
                showError("Primero debe guardar una venta antes de generar la factura.");
                return;
            }

            File pdf = InvoiceGenerator.generateInvoice(lastSavedPurchase);

            showInfo("Factura generada exitosamente:\n" + pdf.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error al generar la factura.");
        }
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setContentText(msg);
        a.show();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText(msg);
        a.show();
    }
}
