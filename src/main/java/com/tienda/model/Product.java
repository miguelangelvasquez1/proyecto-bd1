package com.tienda.model;

import lombok.Data;

    @Data
    public class Product {
        private int id;
        private String code;
        private String name;
        private String description;
        private int stock;
        private double acquisitionValue;
        private double saleValue;
        private ProductCategory category;
    }
