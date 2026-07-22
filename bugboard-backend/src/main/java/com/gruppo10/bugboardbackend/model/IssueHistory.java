package com.gruppo10.bugboardbackend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "issue_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Il bug che ha subito la modifica
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;

    // Chi ha fatto l'azione
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    // Descrizione dell'azione (es. "Creazione", "Stato cambiato in CLOSED")
    @Column(nullable = false)
    private String actionDescription;

    @Column(updatable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}