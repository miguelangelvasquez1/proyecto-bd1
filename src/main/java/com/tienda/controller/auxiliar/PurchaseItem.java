package com.tienda.controller.auxiliar;

import javafx.beans.property.*;
import lombok.Data;

@Data
public class PurchaseItem {
    private IntegerProperty productId = new SimpleIntegerProperty();
    private StringProperty productCode = new SimpleStringProperty();
    private StringProperty productName = new SimpleStringProperty();
    private StringProperty category = new SimpleStringProperty();
    private IntegerProperty quantity = new SimpleIntegerProperty();
    private DoubleProperty unitCost = new SimpleDoubleProperty();
    private DoubleProperty subtotal = new SimpleDoubleProperty();
    private DoubleProperty ivaRate = new SimpleDoubleProperty();
    private DoubleProperty ivaAmount = new SimpleDoubleProperty();
    private DoubleProperty total = new SimpleDoubleProperty();
    
    public void calculateTotals() {
        // double sub = getQuantity() * getUnitCost();
        // double iva = sub * getIvaRate();
        // double tot = sub + iva;
        
        // setSubtotal(sub);
        // setIvaAmount(iva);
        // setTotal(tot);
    }
}