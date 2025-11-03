package com.tienda.model;

import java.time.LocalDate;

import lombok.Data;

@Data
public class Purchase {
    
    private int id;
    private LocalDate date;
    private String saleType; // (COUNT, CREDIT)
    private double subtotal;
    private double ivaTotal;
    private double total;
    private Client client;
    private User user;
}
