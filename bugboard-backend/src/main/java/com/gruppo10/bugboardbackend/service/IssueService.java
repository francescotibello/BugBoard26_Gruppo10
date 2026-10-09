package com.gruppo10.bugboardbackend.service;

import com.gruppo10.bugboardbackend.dto.CommentResponse;
import com.gruppo10.bugboardbackend.dto.IssueCreateRequest;
import com.gruppo10.bugboardbackend.dto.IssueHistoryResponse;
import com.gruppo10.bugboardbackend.dto.IssueResponse;
import com.gruppo10.bugboardbackend.model.*;
import com.gruppo10.bugboardbackend.repository.CommentRepository;
import com.gruppo10.bugboardbackend.repository.IssueHistoryRepository;
import com.gruppo10.bugboardbackend.repository.IssueRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

// Requisiti coperti: 2 (creazione), 3 (vista con filtri), 5 (commenti), 12 (cronologia),
// 16 (duplicato), 18 (scadenza). Il Requisito 1 è in UserService.
// Tutti i metodi restituiscono DTO costruiti DENTRO la transazione: così le relazioni lazy
// (reporter, author, actor) vengono lette in sicurezza e le entità non escono dal service.
@Service
@RequiredArgsConstructor
public class IssueService {

    private final IssueRepository issueRepository;
    private final CommentRepository commentRepository;
    private final IssueHistoryRepository historyRepository;

    // Requisito 2: tutti gli utenti autenticati possono segnalare una issue (titolo e descrizione obbligatori)
    @Transactional
    public IssueResponse createIssue(IssueCreateRequest request, User reporter) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("Il titolo è obbligatorio.");
        }
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new IllegalArgumentException("La descrizione è obbligatoria.");
        }
        if (request.getType() == null) {
            throw new IllegalArgumentException("Il tipo della issue è obbligatorio (QUESTION, BUG, DOCUMENTATION, FEATURE).");
        }

        Issue issue = Issue.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .type(request.getType())
                .status(IssueStatus.TODO) // stato iniziale "todo"
                .priority(request.getPriority())
                .imageUrl(request.getImageUrl())
                .reporter(reporter)
                .build();

        Issue saved = issueRepository.save(issue);
        recordHistory(saved, reporter, "Creazione del ticket.");
        return IssueResponse.from(saved);
    }

    // Requisito 3: vista riepilogativa con filtri opzionali (tipo, stato, priorità) e ordinamento
    @Transactional(readOnly = true)
    public List<IssueResponse> searchIssues(IssueType type, IssueStatus status, IssuePriority priority,
                                            String sortBy, String direction) {

        Specification<Issue> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // L'ordinamento è fatto in Java perché le enum sono salvate come stringhe:
        // ordinare nel DB darebbe un ordine alfabetico (CRITICAL prima di HIGH)
        // invece di quello logico dichiarato nelle enum (LOW < MEDIUM < HIGH < CRITICAL).
        Comparator<Issue> comparator = comparatorFor(sortBy);
        List<Issue> issues = new ArrayList<>(issueRepository.findAll(spec));
        issues.sort(isAscending(direction) ? comparator : comparator.reversed());

        return issues.stream().map(IssueResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public IssueResponse getIssue(Long issueId) {
        return IssueResponse.from(findIssueOrThrow(issueId));
    }

    // Requisito 16: un amministratore contrassegna una issue come duplicato di un'altra e la chiude subito
    @Transactional
    public IssueResponse markAsDuplicate(Long issueId, Long originalId, User actor) {
        requireAdmin(actor, "contrassegnare una issue come duplicato");

        if (issueId.equals(originalId)) {
            throw new IllegalArgumentException("Una issue non può essere duplicato di se stessa.");
        }

        Issue duplicate = findIssueOrThrow(issueId);
        Issue original = findIssueOrThrow(originalId);

        duplicate.setDuplicateOf(original);
        duplicate.setStatus(IssueStatus.CLOSED); // chiusura immediata del bug duplicato
        Issue saved = issueRepository.save(duplicate);

        recordHistory(saved, actor, "Contrassegnata come duplicato della issue #" + originalId
                + ". Stato cambiato in CLOSED.");
        return IssueResponse.from(saved);
    }

    // Requisito 18: solo gli amministratori possono impostare una scadenza
    @Transactional
    public IssueResponse setDeadline(Long issueId, LocalDate deadline, User actor) {
        requireAdmin(actor, "impostare le scadenze");

        Issue issue = findIssueOrThrow(issueId);
        issue.setDeadline(deadline);
        Issue saved = issueRepository.save(issue);

        recordHistory(saved, actor, "Impostata nuova scadenza al: " + deadline + ".");
        return IssueResponse.from(saved);
    }

    // Requisito 5: commenti, aperti a tutti gli utenti autenticati
    @Transactional
    public CommentResponse addComment(Long issueId, String text, User author) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Il testo del commento non può essere vuoto.");
        }

        Issue issue = findIssueOrThrow(issueId);

        Comment comment = Comment.builder()
                .text(text)
                .issue(issue)
                .author(author)
                .build();
        Comment saved = commentRepository.save(comment);

        // Tracciamo anche l'aggiunta di commenti nella cronologia
        recordHistory(issue, author, "Aggiunto un nuovo commento.");
        return toCommentResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByIssue(Long issueId) {
        findIssueOrThrow(issueId); // 404 se la issue non esiste
        return commentRepository.findByIssueId(issueId).stream()
                .sorted(Comparator.comparing(Comment::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toCommentResponse)
                .toList();
    }

    // Requisito 12: cronologia, dalla modifica più recente alla più vecchia
    @Transactional(readOnly = true)
    public List<IssueHistoryResponse> getIssueHistory(Long issueId) {
        findIssueOrThrow(issueId); // 404 se la issue non esiste
        return historyRepository.findByIssueIdOrderByTimestampDesc(issueId).stream()
                .map(IssueHistoryResponse::from)
                .toList();
    }

    // ---------- metodi di supporto ----------

    private Issue findIssueOrThrow(Long issueId) {
        return issueRepository.findById(issueId)
                .orElseThrow(() -> new EntityNotFoundException("Issue non trovata con ID: " + issueId));
    }

    private void requireAdmin(User actor, String operation) {
        if (actor.getRole() != Role.ADMIN) {
            throw new SecurityException("Operazione negata: solo gli amministratori possono " + operation + ".");
        }
    }

    // Requisito 12: metodo helper per la cronologia
    private void recordHistory(Issue issue, User actor, String actionDescription) {
        IssueHistory history = IssueHistory.builder()
                .issue(issue)
                .actor(actor)
                .actionDescription(actionDescription)
                .build();
        historyRepository.save(history);
    }

    private CommentResponse toCommentResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorEmail(comment.getAuthor().getEmail())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    private boolean isAscending(String direction) {
        if ("asc".equalsIgnoreCase(direction)) {
            return true;
        }
        if ("desc".equalsIgnoreCase(direction)) {
            return false;
        }
        throw new IllegalArgumentException("Direzione di ordinamento non valida: usare 'asc' o 'desc'.");
    }

    private Comparator<Issue> comparatorFor(String sortBy) {
        return switch (sortBy) {
            case "createdAt" -> nullsLast(Issue::getCreatedAt);
            case "updatedAt" -> nullsLast(Issue::getUpdatedAt);
            case "deadline" -> nullsLast(Issue::getDeadline);
            case "priority" -> nullsLast(Issue::getPriority); // ordine logico LOW < MEDIUM < HIGH < CRITICAL
            case "status" -> nullsLast(Issue::getStatus);
            case "type" -> nullsLast(Issue::getType);
            case "title" -> Comparator.comparing(Issue::getTitle, String.CASE_INSENSITIVE_ORDER);
            default -> throw new IllegalArgumentException("Campo di ordinamento non valido: " + sortBy
                    + ". Valori ammessi: createdAt, updatedAt, deadline, priority, status, type, title.");
        };
    }

    private static <T extends Comparable<? super T>> Comparator<Issue> nullsLast(Function<Issue, T> extractor) {
        return Comparator.comparing(extractor, Comparator.nullsLast(Comparator.naturalOrder()));
    }
}