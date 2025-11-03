package com.tienda.model;

import javafx.beans.property.*;

public class SaleItem {
    private IntegerProperty productId = new SimpleIntegerProperty();
    private StringProperty productCode = new SimpleStringProperty();
    private StringProperty productName = new SimpleStringProperty();
    private IntegerProperty quantity = new SimpleIntegerProperty();
    private DoubleProperty unitPrice = new SimpleDoubleProperty();
    private DoubleProperty subtotal = new SimpleDoubleProperty();
    private DoubleProperty ivaRate = new SimpleDoubleProperty();
    private DoubleProperty ivaAmount = new SimpleDoubleProperty();
    private DoubleProperty total = new SimpleDoubleProperty();
    
    public void calculateTotals() {
        double sub = getQuantity() * getUnitPrice();
        double iva = sub * getIvaRate();
        double tot = sub + iva;
        
        setSubtotal(sub);
        setIvaAmount(iva);
        setTotal(tot);
    }
    
    // Getters y Setters
    public int getProductId() { return productId.get(); }
    public void setProductId(int productId) { this.productId.set(productId); }
    public IntegerProperty productIdProperty() { return productId; }
    
    public String getProductCode() { return productCode.get(); }
    public void setProductCode(String productCode) { this.productCode.set(productCode); }
    public StringProperty productCodeProperty() { return productCode; }
    
    public String getProductName() { return productName.get(); }
    public void setProductName(String productName) { this.productName.set(productName); }
    public StringProperty productNameProperty() { return productName; }
    
    public int getQuantity() { return quantity.get(); }
    public void setQuantity(int quantity) { this.quantity.set(quantity); }
    public IntegerProperty quantityProperty() { return quantity; }
    
    public double getUnitPrice() { return unitPrice.get(); }
    public void setUnitPrice(double unitPrice) { this.unitPrice.set(unitPrice); }
    public DoubleProperty unitPriceProperty() { return unitPrice; }
    
    public double getSubtotal() { return subtotal.get(); }
    public void setSubtotal(double subtotal) { this.subtotal.set(subtotal); }
    public DoubleProperty subtotalProperty() { return subtotal; }
    
    public double getIvaRate() { return ivaRate.get(); }
    public void setIvaRate(double ivaRate) { this.ivaRate.set(ivaRate); }
    public DoubleProperty ivaRateProperty() { return ivaRate; }
    
    public double getIvaAmount() { return ivaAmount.get(); }
    public void setIvaAmount(double ivaAmount) { this.ivaAmount.set(ivaAmount); }
    public DoubleProperty ivaAmountProperty() { return ivaAmount; }
    
    public double getTotal() { return total.get(); }
    public void setTotal(double total) { this.total.set(total); }
    public DoubleProperty totalProperty() { return total; }
}