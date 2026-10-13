package com.gruppo10.bugboardbackend.dto;

import com.gruppo10.bugboardbackend.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO ricevuto dal client quando un amministratore crea una nuova utenza (Requisito 1)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private Role role; // USER oppure ADMIN
}