package com.tienda.model.dtos;

import lombok.Data;

@Data
public class InventoryItemDTO {
    private int id;
    private String name;
    private double cost;
    private double price;
    private int stock;
    private String category;
}
