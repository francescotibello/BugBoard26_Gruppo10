package com.gruppo10.bugboardbackend.controller;

import com.gruppo10.bugboardbackend.dto.AuthResponse;
import com.gruppo10.bugboardbackend.dto.LoginRequest;
import com.gruppo10.bugboardbackend.dto.UserResponse;
import com.gruppo10.bugboardbackend.model.User;
import com.gruppo10.bugboardbackend.repository.UserRepository;
import com.gruppo10.bugboardbackend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Requisito 1: Controller per l'autenticazione degli utenti tramite email e password
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // 1. Spring Security esegue il controllo delle credenziali
        // (Se l'email o la password sono sbagliate, lancia BadCredentialsException gestita in GlobalExceptionHandler)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Se arriviamo qui, le credenziali sono corrette. Recuperiamo l'utente dal DB
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(); // Sappiamo che esiste perché l'AuthenticationManager lo ha appena validato

        // 3. Generiamo il token JWT e restituiamo sia il token sia i dati sicuri dell'utente (incluso il ruolo)
        String jwtToken = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthResponse(jwtToken, UserResponse.from(user)));
    }
}