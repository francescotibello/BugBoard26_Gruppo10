package com.gruppo10.bugboardbackend.service;

import com.gruppo10.bugboardbackend.dto.IssueResponse;
import com.gruppo10.bugboardbackend.model.*;
import com.gruppo10.bugboardbackend.repository.CommentRepository;
import com.gruppo10.bugboardbackend.repository.IssueHistoryRepository;
import com.gruppo10.bugboardbackend.repository.IssueRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Test unitari di IssueService, senza contesto Spring né database (i repository sono mock).
//
// markAsDuplicate(issueId, originalId, actor) - Requisito 16. Classi di equivalenza:
//   - actor NON admin                         -> SecurityException, nessun accesso al DB
//   - issueId == originalId                   -> IllegalArgumentException
//   - issue da chiudere inesistente           -> EntityNotFoundException
//   - issue originale inesistente             -> EntityNotFoundException
//   - tutto valido                            -> issue CLOSED, collegata all'originale, riga di cronologia
//
// setDeadline(issueId, deadline, actor) - Requisito 18. Classi di equivalenza:
//   - actor NON admin                         -> SecurityException, nessun accesso al DB
//   - issue inesistente                       -> EntityNotFoundException
//   - tutto valido                            -> scadenza impostata, riga di cronologia
@ExtendWith(MockitoExtension.class)
class IssueServiceTest {

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private IssueHistoryRepository historyRepository;

    @InjectMocks
    private IssueService issueService;

    // ---------- markAsDuplicate ----------

    @Test
    void markAsDuplicate_whenActorIsNotAdmin_throwsSecurityException() {
        User normalUser = user(2L, Role.USER);

        assertThrows(SecurityException.class, () -> issueService.markAsDuplicate(1L, 2L, normalUser));

        verifyNoInteractions(issueRepository, historyRepository);
    }

    @Test
    void markAsDuplicate_whenIssueIsDuplicateOfItself_throwsIllegalArgumentException() {
        User admin = user(1L, Role.ADMIN);

        assertThrows(IllegalArgumentException.class, () -> issueService.markAsDuplicate(5L, 5L, admin));

        verifyNoInteractions(issueRepository, historyRepository);
    }

    @Test
    void markAsDuplicate_whenIssueDoesNotExist_throwsEntityNotFoundException() {
        User admin = user(1L, Role.ADMIN);
        when(issueRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> issueService.markAsDuplicate(1L, 2L, admin));

        verify(issueRepository, never()).save(any(Issue.class));
    }

    @Test
    void markAsDuplicate_whenOriginalDoesNotExist_throwsEntityNotFoundException() {
        User admin = user(1L, Role.ADMIN);
        when(issueRepository.findById(1L)).thenReturn(Optional.of(issue(1L, admin)));
        when(issueRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> issueService.markAsDuplicate(1L, 2L, admin));

        verify(issueRepository, never()).save(any(Issue.class));
    }

    @Test
    void markAsDuplicate_whenValid_closesIssueLinksOriginalAndRecordsHistory() {
        User admin = user(1L, Role.ADMIN);
        Issue duplicate = issue(1L, admin);
        Issue original = issue(2L, admin);
        when(issueRepository.findById(1L)).thenReturn(Optional.of(duplicate));
        when(issueRepository.findById(2L)).thenReturn(Optional.of(original));
        when(issueRepository.save(any(Issue.class))).then(returnsFirstArg());

        IssueResponse response = issueService.markAsDuplicate(1L, 2L, admin);

        assertEquals(IssueStatus.CLOSED, response.getStatus());
        assertEquals(2L, response.getDuplicateOfId());

        ArgumentCaptor<IssueHistory> history = ArgumentCaptor.forClass(IssueHistory.class);
        verify(historyRepository).save(history.capture());
        assertEquals(admin, history.getValue().getActor());
        assertTrue(history.getValue().getActionDescription().contains("#2"));
    }

    // ---------- setDeadline ----------

    @Test
    void setDeadline_whenActorIsNotAdmin_throwsSecurityException() {
        User normalUser = user(2L, Role.USER);

        assertThrows(SecurityException.class, () ->
                issueService.setDeadline(1L, LocalDate.of(2026, 12, 31), normalUser));

        verifyNoInteractions(issueRepository, historyRepository);
    }

    @Test
    void setDeadline_whenIssueDoesNotExist_throwsEntityNotFoundException() {
        User admin = user(1L, Role.ADMIN);
        when(issueRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                issueService.setDeadline(99L, LocalDate.of(2026, 12, 31), admin));

        verify(issueRepository, never()).save(any(Issue.class));
    }

    @Test
    void setDeadline_whenValid_setsDeadlineAndRecordsHistory() {
        User admin = user(1L, Role.ADMIN);
        LocalDate deadline = LocalDate.of(2026, 12, 31);
        when(issueRepository.findById(1L)).thenReturn(Optional.of(issue(1L, admin)));
        when(issueRepository.save(any(Issue.class))).then(returnsFirstArg());

        IssueResponse response = issueService.setDeadline(1L, deadline, admin);

        assertEquals(deadline, response.getDeadline());

        ArgumentCaptor<IssueHistory> history = ArgumentCaptor.forClass(IssueHistory.class);
        verify(historyRepository).save(history.capture());
        assertEquals(admin, history.getValue().getActor());
        assertTrue(history.getValue().getActionDescription().contains(deadline.toString()));
    }

    // ---------- metodi di supporto ----------

    private User user(Long id, Role role) {
        return User.builder()
                .id(id)
                .name("Test " + id)
                .email("utente" + id + "@test.com")
                .password("hash")
                .role(role)
                .build();
    }

    private Issue issue(Long id, User reporter) {
        return Issue.builder()
                .id(id)
                .title("Issue " + id)
                .description("Descrizione")
                .type(IssueType.BUG)
                .status(IssueStatus.TODO)
                .reporter(reporter)
                .build();
    }
}