package com.tienda.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AccessBinnacle {
    private int id;
    private LocalDateTime entryDateTime;
    private LocalDateTime departureDateTime;
    private String ip;
    private User user;
}
