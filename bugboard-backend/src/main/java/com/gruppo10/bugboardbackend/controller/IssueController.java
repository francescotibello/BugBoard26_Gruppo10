package com.gruppo10.bugboardbackend.controller;

import com.gruppo10.bugboardbackend.dto.CommentCreateRequest;
import com.gruppo10.bugboardbackend.dto.CommentResponse;
import com.gruppo10.bugboardbackend.dto.IssueCreateRequest;
import com.gruppo10.bugboardbackend.dto.IssueHistoryResponse;
import com.gruppo10.bugboardbackend.dto.IssueResponse;
import com.gruppo10.bugboardbackend.model.IssuePriority;
import com.gruppo10.bugboardbackend.model.IssueStatus;
import com.gruppo10.bugboardbackend.model.IssueType;
import com.gruppo10.bugboardbackend.model.User;
import com.gruppo10.bugboardbackend.service.IssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor
public class IssueController {

    private final IssueService issueService;

    // Requisito 2: crea un nuovo ticket impostando l'utente loggato come reporter
    @PostMapping
    public ResponseEntity<IssueResponse> createIssue(
            @RequestBody IssueCreateRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(issueService.createIssue(request, currentUser));
    }

    // Requisito 3: lista delle issue con filtri opzionali e ordinamento
    // Esempio: GET /api/issues?type=BUG&status=TODO&sortBy=priority&direction=desc
    @GetMapping
    public ResponseEntity<List<IssueResponse>> getIssues(
            @RequestParam(required = false) IssueType type,
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(required = false) IssuePriority priority,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        return ResponseEntity.ok(issueService.searchIssues(type, status, priority, sortBy, direction));
    }

    // Dettaglio di una singola issue
    @GetMapping("/{id}")
    public ResponseEntity<IssueResponse> getIssue(@PathVariable Long id) {
        return ResponseEntity.ok(issueService.getIssue(id));
    }

    // Requisito 16: contrassegna come duplicato di un'altra issue e chiude (solo ADMIN)
    @PatchMapping("/{id}/duplicate")
    public ResponseEntity<IssueResponse> markAsDuplicate(
            @PathVariable Long id,
            @RequestParam Long originalId,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(issueService.markAsDuplicate(id, originalId, currentUser));
    }

    // Requisito 18: imposta una scadenza (solo ADMIN). Formato data: yyyy-MM-dd
    @PatchMapping("/{id}/deadline")
    public ResponseEntity<IssueResponse> setDeadline(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadline,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(issueService.setDeadline(id, deadline, currentUser));
    }

    // Requisito 5: aggiunge un commento usando l'utente loggato come autore
    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long id,
            @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(issueService.addComment(id, request.getText(), currentUser));
    }

    // Requisito 5: commenti di una specifica issue
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(issueService.getCommentsByIssue(id));
    }

    // Requisito 12: cronologia di una specifica issue
    @GetMapping("/{id}/history")
    public ResponseEntity<List<IssueHistoryResponse>> getHistory(@PathVariable Long id) {
        return ResponseEntity.ok(issueService.getIssueHistory(id));
    }
}