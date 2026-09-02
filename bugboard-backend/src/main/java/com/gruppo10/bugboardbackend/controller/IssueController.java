package com.gruppo10.bugboardbackend.controller;

import com.gruppo10.bugboardbackend.dto.CommentCreateRequest;
import com.gruppo10.bugboardbackend.dto.IssueCreateRequest;
import com.gruppo10.bugboardbackend.model.Comment;
import com.gruppo10.bugboardbackend.model.Issue;
import com.gruppo10.bugboardbackend.model.IssueHistory;
import com.gruppo10.bugboardbackend.model.User;
import com.gruppo10.bugboardbackend.service.IssueService;
import lombok.RequiredArgsConstructor;
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

    // Crea un nuovo ticket impostando dinamicamente l'utente loggato come reporter
    @PostMapping
    public ResponseEntity<Issue> createIssue(
            @RequestBody IssueCreateRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        Issue issue = Issue.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .priority(request.getPriority())
                .imageUrl(request.getImageUrl())
                .reporter(currentUser)
                .build();
        return ResponseEntity.ok(issueService.createIssue(issue));
    }

    // Recupera la lista di tutte le issue
    @GetMapping
    public ResponseEntity<List<Issue>> getAllIssues() {
        return ResponseEntity.ok(issueService.getAllIssues());
    }

    // Contrassegna una issue come duplicata tracciando chi ha eseguito l'azione
    @PatchMapping("/{id}/duplicate")
    public ResponseEntity<Issue> markAsDuplicate(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(issueService.markAsDuplicate(id, currentUser));
    }

    // Imposta una scadenza
    @PatchMapping("/{id}/deadline")
    public ResponseEntity<Issue> setDeadline(
            @PathVariable Long id,
            @RequestParam LocalDate deadline,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(issueService.setDeadline(id, deadline, currentUser));
    }

    // Aggiunge un commento usando l'utente loggato come autore
    @PostMapping("/{id}/comments")
    public ResponseEntity<Comment> addComment(
            @PathVariable Long id,
            @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(issueService.addComment(id, request.getText(), currentUser));
    }

    // Recupera i commenti di una specifica issue
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(issueService.getCommentsByIssue(id));
    }

    // Recupera la cronologia di una specifica issue
    @GetMapping("/{id}/history")
    public ResponseEntity<List<IssueHistory>> getHistory(@PathVariable Long id) {
        return ResponseEntity.ok(issueService.getIssueHistory(id));
    }
}