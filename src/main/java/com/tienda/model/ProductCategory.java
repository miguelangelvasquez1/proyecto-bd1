package com.tienda.model;

import lombok.Data;

@Data
public class ProductCategory {
    private int id;
    private String name;
    private double iva;
    private double utility;
}
