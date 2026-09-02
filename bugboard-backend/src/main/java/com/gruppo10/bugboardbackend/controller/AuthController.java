package com.gruppo10.bugboardbackend.controller;

import com.gruppo10.bugboardbackend.dto.AuthResponse;
import com.gruppo10.bugboardbackend.dto.LoginRequest;
import com.gruppo10.bugboardbackend.dto.RegisterRequest;
import com.gruppo10.bugboardbackend.model.Role;
import com.gruppo10.bugboardbackend.model.User;
import com.gruppo10.bugboardbackend.repository.UserRepository;
import com.gruppo10.bugboardbackend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {

        // 1. Controllo base: l'email esiste già?
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        // 2. Creazione dell'utente
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : Role.USER)
                .build();

        // 3. Salvataggio nel DB
        userRepository.save(user);

        // 4. Generazione del token e invio della risposta
        String jwtToken = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthResponse(jwtToken));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {

        // 1. Spring Security esegue il controllo delle credenziali
        // (Se la password è sbagliata, lancia un'eccezione in automatico)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Se arriviamo qui, le credenziali sono corrette. Recuperiamo l'utente
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(); // Sappiamo che esiste perché il manager lo ha appena validato

        // 3. Generiamo il token per il login e lo restituiamo
        String jwtToken = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthResponse(jwtToken));
    }
}