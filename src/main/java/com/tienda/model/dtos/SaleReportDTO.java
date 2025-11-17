package com.tienda.model.dtos;

import lombok.Data;
import java.sql.Date;

@Data
public class SaleReportDTO {
    private int saleId;
    private String productName;
    private int quantity;
    private Date date;
    private String saleType;
    private double subtotal;
    private double ivaTotal;
    private double total;
    private String clientName;
}
