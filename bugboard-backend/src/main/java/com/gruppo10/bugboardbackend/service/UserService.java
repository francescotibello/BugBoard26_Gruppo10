package com.gruppo10.bugboardbackend.service;

import com.gruppo10.bugboardbackend.model.Role;
import com.gruppo10.bugboardbackend.model.User;
import com.gruppo10.bugboardbackend.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Requisito 1: Account da amministratore già attivo all'avvio del sistema
    @PostConstruct
    public void initDefaultAdmin() {
        // Controlla se l'admin esiste già per evitare duplicati ai successivi riavvii
        if (userRepository.findByEmail("admin@bugboard.com").isEmpty()) {
            User defaultAdmin = User.builder()
                    .name("Amministratore di Sistema")
                    .email("admin@bugboard.com")
                    .password(passwordEncoder.encode("admin123")) // La password deve essere salvata criptata
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(defaultAdmin);
        }
    }

    // Requisito 1: Un amministratore può creare ulteriori utenze (normali o di amministrazione)
    @Transactional
    public User createUser(String name, String email, String rawPassword, Role role, User currentUser) {
        // Solo gli ADMIN possono creare nuovi utenti
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            throw new SecurityException("Operazione negata: solo gli amministratori possono creare nuove utenze.");
        }

        // Controllo validità dei campi obbligatori
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Il nome è obbligatorio.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("L'email è obbligatoria.");
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("La password è obbligatoria.");
        }
        if (role == null) {
            throw new IllegalArgumentException("Il ruolo è obbligatorio (USER o ADMIN).");
        }

        // Verifica che l'email non sia già registrata nel database
        if (userRepository.findByEmail(email.trim()).isPresent()) {
            throw new IllegalArgumentException("Errore: Email già in uso.");
        }

        // Creazione dell'utente con password cifrata tramite BCrypt
        User newUser = User.builder()
                .name(name.trim())
                .email(email.trim())
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .build();

        return userRepository.save(newUser);
    }
}