package com.tienda.model;

import java.time.LocalDate;

import lombok.Data;

@Data
public class Credit {
    private int id;
    private double initialQuota;
    private double amountFinanced;
    private int months;
    private double interestRate;
    private LocalDate createdAt;
    private String state; // (VIGENTE, CANCELADO, MORA)
    private Purchase sale;
}
