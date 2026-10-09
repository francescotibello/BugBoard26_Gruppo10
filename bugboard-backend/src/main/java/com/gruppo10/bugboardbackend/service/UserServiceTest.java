package com.gruppo10.bugboardbackend.service;

import com.gruppo10.bugboardbackend.model.Role;
import com.gruppo10.bugboardbackend.model.User;
import com.gruppo10.bugboardbackend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Test unitari di UserService.createUser (Requisito 1).
// Classi di equivalenza per il chiamante e per l'email:
//   - chiamante NON admin                 -> SecurityException, nessun accesso al DB
//   - chiamante admin, email già in uso   -> IllegalArgumentException, nessun salvataggio
//   - chiamante admin, email libera       -> utente salvato con password cifrata (per ogni ruolo richiesto)
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_whenCallerIsNotAdmin_throwsSecurityException() {
        User normalUser = user(Role.USER, "utente@test.com");

        assertThrows(SecurityException.class, () ->
                userService.createUser("Mario", "mario@test.com", "password123", Role.USER, normalUser));

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void createUser_whenEmailAlreadyInUse_throwsIllegalArgumentException() {
        User admin = user(Role.ADMIN, "admin@test.com");
        when(userRepository.findByEmail("mario@test.com")).thenReturn(Optional.of(user(Role.USER, "mario@test.com")));

        assertThrows(IllegalArgumentException.class, () ->
                userService.createUser("Mario", "mario@test.com", "password123", Role.USER, admin));

        verify(userRepository, never()).save(any(User.class));
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void createUser_whenCallerIsAdminAndEmailIsFree_savesUserWithEncodedPassword(Role requestedRole) {
        User admin = user(Role.ADMIN, "admin@test.com");
        when(userRepository.findByEmail("nuovo@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("PASSWORD_CIFRATA");
        when(userRepository.save(any(User.class))).then(returnsFirstArg());

        User created = userService.createUser("Nuovo", "nuovo@test.com", "password123", requestedRole, admin);

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertEquals("nuovo@test.com", savedUser.getValue().getEmail());
        assertEquals(requestedRole, savedUser.getValue().getRole());
        assertEquals("PASSWORD_CIFRATA", savedUser.getValue().getPassword());
        assertNotEquals("password123", savedUser.getValue().getPassword()); // la password in chiaro non va mai salvata
        assertEquals(requestedRole, created.getRole());
    }

    private User user(Role role, String email) {
        return User.builder()
                .id(1L)
                .name("Test")
                .email(email)
                .password("hash")
                .role(role)
                .build();
    }
}