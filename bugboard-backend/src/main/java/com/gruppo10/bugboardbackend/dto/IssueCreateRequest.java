package com.gruppo10.bugboardbackend.dto;

import com.gruppo10.bugboardbackend.model.IssuePriority;
import com.gruppo10.bugboardbackend.model.IssueType;
import lombok.Data;

// DTO ricevuto dal client per la segnalazione di una nuova issue (Requisito 2)
@Data
public class IssueCreateRequest {
    private String title;           // Obbligatorio
    private String description;     // Obbligatorio
    private IssueType type;         // Obbligatorio (QUESTION, BUG, DOCUMENTATION, FEATURE)
    private IssuePriority priority; // Opzionale (LOW, MEDIUM, HIGH, CRITICAL)
    private String imageUrl;        // Opzionale (URL o immagine in Base64)
}