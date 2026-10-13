package com.gruppo10.bugboardbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

// DTO restituito al client dopo un login effettuato con successo:
// contiene il token JWT per autenticare le chiamate successive e i dati dell'utente loggato.
@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private UserResponse user;
}