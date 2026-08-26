package com.gruppo10.bugboardbackend.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}