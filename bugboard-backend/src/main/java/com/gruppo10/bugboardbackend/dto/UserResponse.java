package com.gruppo10.bugboardbackend.dto;

import com.gruppo10.bugboardbackend.model.Role;
import com.gruppo10.bugboardbackend.model.User;
import lombok.Builder;
import lombok.Data;

// Versione "sicura" di User da restituire al client: niente password (nemmeno l'hash)
@Data
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}