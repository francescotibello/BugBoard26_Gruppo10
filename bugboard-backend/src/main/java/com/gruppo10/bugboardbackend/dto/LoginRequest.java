package com.gruppo10.bugboardbackend.dto;

import lombok.Data;

// DTO ricevuto dal client per la richiesta di login (Requisito 1)
@Data
public class LoginRequest {
    private String email;
    private String password;
}