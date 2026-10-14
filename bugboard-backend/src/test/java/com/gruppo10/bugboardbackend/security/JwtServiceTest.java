package com.gruppo10.bugboardbackend.security;

import com.gruppo10.bugboardbackend.model.Role;
import com.gruppo10.bugboardbackend.model.User;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

// Test unitari per JwtService (Requisito 1 - Autenticazione JWT).
// isTokenValid(token, userDetails) - Classi di equivalenza:
//   - token generato per lo stesso utente e non scaduto -> true
//   - token presentato da un utente con email diversa   -> false
class JwtServiceTest {

    private final JwtService jwtService = new JwtService();

    @Test
    void generateTokenAndValidate_whenSameUser_returnsTrueAndExtractsEmail() {
        User user = User.builder()
                .id(1L)
                .name("Mario Rossi")
                .email("mario@bugboard.com")
                .password("hash")
                .role(Role.USER)
                .build();

        String token = jwtService.generateToken(Map.of("role", user.getRole().name()), user);

        assertNotNull(token);
        assertEquals("mario@bugboard.com", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void isTokenValid_whenDifferentUser_returnsFalse() {
        User tokenOwner = User.builder()
                .id(1L)
                .name("Mario Rossi")
                .email("mario@bugboard.com")
                .password("hash")
                .role(Role.USER)
                .build();

        User otherUser = User.builder()
                .id(2L)
                .name("Luigi Verdi")
                .email("luigi@bugboard.com")
                .password("hash")
                .role(Role.USER)
                .build();

        String token = jwtService.generateToken(tokenOwner);

        assertFalse(jwtService.isTokenValid(token, otherUser));
    }
}