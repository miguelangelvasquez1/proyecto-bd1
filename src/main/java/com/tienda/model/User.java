package com.tienda.model;

import lombok.Data;

@Data
public class User {
    private int id;
    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    private Role role;
}
