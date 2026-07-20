package com.gruppo10.bugboardbackend.service;

import com.gruppo10.bugboardbackend.model.Issue;
import com.gruppo10.bugboardbackend.model.IssueStatus;
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

    // Requisito 2: Creazione di una issue (lo stato TO.DO è già di default nel Model)
    @Transactional
    public Issue createIssue(Issue issue) {
        return issueRepository.save(issue);
    }

    // Requisito 3: Visualizzare la vista riepilogativa delle issue
    public List<Issue> getAllIssues() {
        return issueRepository.findAll();
    }

    // Requisito 16: Contrassegnare come duplicato e chiudere
    @Transactional
    public Issue markAsDuplicate(Long issueId) {
        Issue duplicateIssue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue non trovata con ID: " + issueId));

        // Chiusura immediata del bug duplicato
        duplicateIssue.setStatus(IssueStatus.CLOSED);

        return issueRepository.save(duplicateIssue);
    }

    // Requisito 18: Impostare scadenze opzionali
    @Transactional
    public Issue setDeadline(Long issueId, LocalDate deadline) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue non trovata con ID: " + issueId));

        issue.setDeadline(deadline);
        return issueRepository.save(issue);
    }
}