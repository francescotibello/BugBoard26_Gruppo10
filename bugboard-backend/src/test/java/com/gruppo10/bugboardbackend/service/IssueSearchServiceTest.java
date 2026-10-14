package com.gruppo10.bugboardbackend.service;

import com.gruppo10.bugboardbackend.dto.IssueResponse;
import com.gruppo10.bugboardbackend.model.*;
import com.gruppo10.bugboardbackend.repository.CommentRepository;
import com.gruppo10.bugboardbackend.repository.IssueHistoryRepository;
import com.gruppo10.bugboardbackend.repository.IssueRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Test unitari per searchIssues(type, status, priority, sortBy, direction) - Requisito 3.
// Classi di equivalenza:
//   - sortBy non ammesso                      -> IllegalArgumentException, nessuna query al DB
//   - direction diversa da 'asc'/'desc'       -> IllegalArgumentException, nessuna query al DB
//   - ordinamento per priority 'desc'         -> CRITICAL -> HIGH -> LOW -> null sempre in coda
//   - ordinamento per priority 'asc'          -> LOW -> HIGH -> CRITICAL -> null sempre in coda
//   - ordinamento per deadline e title        -> ordine corretto con valori null in fondo
@ExtendWith(MockitoExtension.class)
class IssueSearchServiceTest {

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private IssueHistoryRepository historyRepository;

    @InjectMocks
    private IssueService issueService;

    @Test
    void searchIssues_whenSortByIsInvalid_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                issueService.searchIssues(null, null, null, "campoInesistente", "asc"));
        verifyNoInteractions(issueRepository);
    }

    @Test
    void searchIssues_whenDirectionIsInvalid_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                issueService.searchIssues(null, null, null, "priority", "random"));
        verifyNoInteractions(issueRepository);
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchIssues_sortByPriorityDesc_ordersFromCriticalToLowKeepingNullsLast() {
        User reporter = reporter();
        Issue lowIssue = issueWithPriority(1L, IssuePriority.LOW, reporter);
        Issue nullPriorityIssue = issueWithPriority(2L, null, reporter);
        Issue criticalIssue = issueWithPriority(3L, IssuePriority.CRITICAL, reporter);
        Issue highIssue = issueWithPriority(4L, IssuePriority.HIGH, reporter);

        when(issueRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(lowIssue, nullPriorityIssue, criticalIssue, highIssue));

        List<IssueResponse> result = issueService.searchIssues(
                IssueType.BUG, IssueStatus.TODO, null, "priority", "desc");

        assertEquals(4, result.size());
        assertEquals(3L, result.get(0).getId()); // CRITICAL
        assertEquals(4L, result.get(1).getId()); // HIGH
        assertEquals(1L, result.get(2).getId()); // LOW
        assertEquals(2L, result.get(3).getId()); // null resta in fondo anche in desc
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchIssues_sortByPriorityAsc_ordersFromLowToCriticalKeepingNullsLast() {
        User reporter = reporter();
        Issue criticalIssue = issueWithPriority(1L, IssuePriority.CRITICAL, reporter);
        Issue nullPriorityIssue = issueWithPriority(2L, null, reporter);
        Issue lowIssue = issueWithPriority(3L, IssuePriority.LOW, reporter);

        when(issueRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(criticalIssue, nullPriorityIssue, lowIssue));

        List<IssueResponse> result = issueService.searchIssues(
                null, null, null, "priority", "asc");

        assertEquals(3, result.size());
        assertEquals(3L, result.get(0).getId()); // LOW
        assertEquals(1L, result.get(1).getId()); // CRITICAL
        assertEquals(2L, result.get(2).getId()); // null in fondo
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchIssues_sortByDeadlineAsc_ordersByDateKeepingNullsLast() {
        User reporter = reporter();
        Issue late = issueWithPriority(1L, IssuePriority.MEDIUM, reporter);
        late.setDeadline(LocalDate.of(2026, 12, 31));

        Issue noDeadline = issueWithPriority(2L, IssuePriority.MEDIUM, reporter);

        Issue early = issueWithPriority(3L, IssuePriority.MEDIUM, reporter);
        early.setDeadline(LocalDate.of(2026, 6, 15));

        when(issueRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(late, noDeadline, early));

        List<IssueResponse> result = issueService.searchIssues(
                null, null, null, "deadline", "asc");

        assertEquals(3L, result.get(0).getId()); // 2026-06-15
        assertEquals(1L, result.get(1).getId()); // 2026-12-31
        assertEquals(2L, result.get(2).getId()); // null in fondo
    }

    // ---------- metodi di supporto ----------

    private User reporter() {
        return User.builder()
                .id(1L)
                .name("Reporter")
                .email("reporter@bugboard.com")
                .password("hash")
                .role(Role.USER)
                .build();
    }

    private Issue issueWithPriority(Long id, IssuePriority priority, User reporter) {
        return Issue.builder()
                .id(id)
                .title("Issue " + id)
                .description("Descrizione")
                .type(IssueType.BUG)
                .status(IssueStatus.TODO)
                .priority(priority)
                .reporter(reporter)
                .build();
    }
}
