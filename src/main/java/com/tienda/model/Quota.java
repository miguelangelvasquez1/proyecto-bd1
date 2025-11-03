package com.tienda.model;

import java.time.LocalDate;

import lombok.Data;

@Data
public class Quota {
    private int id;
    private int quotaNumber;
    private LocalDate expirationDate;
    private double quotaValue;
    private Double payedValue;
    private LocalDate payedAt;
    private String state;
    private Credit credit;

}   
