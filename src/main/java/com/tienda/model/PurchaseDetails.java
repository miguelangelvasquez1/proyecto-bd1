package com.tienda.model;

import lombok.Data;

@Data
public class PurchaseDetails {
    private int id;
    private int amount;
    private Double unitPrice; // opcional
    private Double ivaApplied;
    private double subtotal;
    private Purchase sale;
    private Product product;
    private Purchase purchase;
}
