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

    // Requisito 1: Account da amministratore già attivo
    @PostConstruct
    public void initDefaultAdmin() {
        // Controlla se l'admin esiste già per evitare duplicati ai successivi riavvii
        if (userRepository.findByEmail("admin@bugboard.com").isEmpty()) {
            User defaultAdmin = User.builder()
                    .email("admin@bugboard.com")
                    .password(passwordEncoder.encode("admin123")) // La password deve essere salvata criptata
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(defaultAdmin);
        }
    }

    // Requisito 1: Un amministratore può creare ulteriori utenze
    @Transactional
    public User createUser(String email, String rawPassword, Role role, User currentUser) {

        // Solo gli ADMIN possono creare utenti
        if (currentUser.getRole() != Role.ADMIN) {
            throw new SecurityException("Operazione negata: solo gli amministratori possono creare nuove utenze.");
        }

        // Verifica che l'email non sia già registrata
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Errore: Email già in uso.");
        }

        User newUser = User.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .build();

        return userRepository.save(newUser);
    }
}