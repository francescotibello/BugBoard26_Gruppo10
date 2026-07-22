package com.gruppo10.bugboardbackend.service;

import com.gruppo10.bugboardbackend.model.*;
import com.gruppo10.bugboardbackend.repository.CommentRepository;
import com.gruppo10.bugboardbackend.repository.IssueHistoryRepository;
import com.gruppo10.bugboardbackend.repository.IssueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IssueService {

    private final IssueRepository issueRepository;
    private final CommentRepository commentRepository;
    private final IssueHistoryRepository historyRepository;

    // Requisito 2: Creazione di una issue (lo stato TO.DO è già di default nel Model)
    @Transactional
    public Issue createIssue(Issue issue) {
        Issue savedIssue = issueRepository.save(issue);
        recordHistory(savedIssue, savedIssue.getReporter(), "Creazione del ticket.");

        return savedIssue;
    }

    // Requisito 3: Visualizzare la vista riepilogativa delle issue
    public List<Issue> getAllIssues() {
        return issueRepository.findAll();
    }

    // Requisito 16: Contrassegnare come duplicato e chiudere
    @Transactional
    public Issue markAsDuplicate(Long issueId, User actor) {
        Issue duplicateIssue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue non trovata con ID: " + issueId));

        // Chiusura immediata del bug duplicato
        duplicateIssue.setStatus(IssueStatus.CLOSED);

        Issue savedIssue = issueRepository.save(duplicateIssue);
        recordHistory(savedIssue, actor, "Contrassegnato come duplicato. Stato cambiato in CLOSED.");

        return savedIssue;    }

    // Requisito 18: Impostare scadenze opzionali
    @Transactional
    public Issue setDeadline(Long issueId, LocalDate deadline, User actor) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue non trovata con ID: " + issueId));

        issue.setDeadline(deadline);
        Issue savedIssue = issueRepository.save(issue);

        recordHistory(savedIssue, actor, "Impostata nuova scadenza al: " + deadline.toString());

        return savedIssue;    }

    // Requisito 12: metodo helper per la cronologia
    private void recordHistory(Issue issue, User actor, String actionDescription) {
        IssueHistory history = IssueHistory.builder()
                .issue(issue)
                .actor(actor)
                .actionDescription(actionDescription)
                .build();
        historyRepository.save(history);
    }

    // Requisito 5: metodi per la gestione commenti
    @Transactional
    public Comment addComment(Long issueId, String text, User author) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue non trovata con ID: " + issueId));

        Comment comment = Comment.builder()
                .text(text)
                .issue(issue)
                .author(author)
                .build();

        Comment savedComment = commentRepository.save(comment);

        // Tracciamo anche l'aggiunta di commenti nella cronologia
        recordHistory(issue, author, "Aggiunto un nuovo commento.");
        return savedComment;
    }

    public List<Comment> getCommentsByIssue(Long issueId) {
        return commentRepository.findByIssueId(issueId);
    }

    // Requisito 12: metodo per la lettura della cronologia
    public List<IssueHistory> getIssueHistory(Long issueId) {
        // Restituisce la cronologia ordinata dalla più recente alla più vecchia
        return historyRepository.findByIssueIdOrderByTimestampDesc(issueId);
    }
}