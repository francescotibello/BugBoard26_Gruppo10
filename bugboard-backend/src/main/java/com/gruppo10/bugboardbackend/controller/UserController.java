package com.gruppo10.bugboardbackend.controller;

import com.gruppo10.bugboardbackend.dto.RegisterRequest;
import com.gruppo10.bugboardbackend.dto.UserResponse;
import com.gruppo10.bugboardbackend.model.Role;
import com.gruppo10.bugboardbackend.model.User;
import com.gruppo10.bugboardbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Requisito 1: solo un ADMIN può creare nuove utenze (il controllo vero è dentro UserService).
    // Riusiamo RegisterRequest perché ha già name, email, password e role.
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @RequestBody RegisterRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        User created = userService.createUser(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                request.getRole() != null ? request.getRole() : Role.USER,
                currentUser
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(created));
    }
}