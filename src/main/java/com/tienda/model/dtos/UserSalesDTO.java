package com.tienda.model.dtos;

import lombok.Data;

@Data
public class UserSalesDTO {
    private int id;
    private String userName;
    private int totalSales;
}
