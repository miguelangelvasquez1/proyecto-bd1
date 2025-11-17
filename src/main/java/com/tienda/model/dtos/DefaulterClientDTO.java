package com.tienda.model.dtos;

import lombok.Data;
import java.sql.Date;

@Data
public class DefaulterClientDTO {
    private String clientName;
    private String documentNumber;
    private String phoneNumber;
    private String email;
    private int creditId;
    private double totalDebt;
    private int overdueQuotas;
    private Date lastPaymentDate;
    private int daysPastDue;
}
