package com.tienda.model;

import lombok.Data;

@Data
public class Client {
    private int id;
    private String documentType;
    private String documentNumber;
    private String name;
    private String email;
    private String phoneNumber;
    private String address;
}
