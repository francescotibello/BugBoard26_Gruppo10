package com.gruppo10.bugboardbackend.service;

import com.gruppo10.bugboardbackend.dto.CommentResponse;
import com.gruppo10.bugboardbackend.dto.IssueCreateRequest;
import com.gruppo10.bugboardbackend.dto.IssueResponse;
import com.gruppo10.bugboardbackend.model.*;
import com.gruppo10.bugboardbackend.repository.CommentRepository;
import com.gruppo10.bugboardbackend.repository.IssueHistoryRepository;
import com.gruppo10.bugboardbackend.repository.IssueRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Test unitari per la creazione di issue (Requisito 2) e l'aggiunta di commenti (Requisito 5).
// createIssue(request, reporter) - Requisito 2. Classi di equivalenza:
//   - titolo nullo o vuoto                    -> IllegalArgumentException, nessun salvataggio
//   - descrizione nulla o vuota               -> IllegalArgumentException, nessun salvataggio
//   - tipo della issue nullo                  -> IllegalArgumentException, nessun salvataggio
//   - dati validi                             -> issue salvata in stato TODO, reporter associato, riga in cronologia
// addComment(issueId, text, author) - Requisito 5. Classi di equivalenza:
//   - testo del commento nullo o vuoto        -> IllegalArgumentException, nessun accesso al DB
//   - issue inesistente                       -> EntityNotFoundException, nessun salvataggio del commento
//   - dati validi                             -> commento salvato e associato all'autore, riga in cronologia
@ExtendWith(MockitoExtension.class)
class IssueCreationAndCommentTest {

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private IssueHistoryRepository historyRepository;

    @InjectMocks
    private IssueService issueService;

    // ---------- createIssue (Requisito 2) ----------

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void createIssue_whenTitleIsNullOrBlank_throwsIllegalArgumentException(String invalidTitle) {
        User reporter = user(1L, Role.USER);
        IssueCreateRequest request = validRequest();
        request.setTitle(invalidTitle);

        assertThrows(IllegalArgumentException.class, () -> issueService.createIssue(request, reporter));
        verifyNoInteractions(issueRepository, historyRepository);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void createIssue_whenDescriptionIsNullOrBlank_throwsIllegalArgumentException(String invalidDescription) {
        User reporter = user(1L, Role.USER);
        IssueCreateRequest request = validRequest();
        request.setDescription(invalidDescription);

        assertThrows(IllegalArgumentException.class, () -> issueService.createIssue(request, reporter));
        verifyNoInteractions(issueRepository, historyRepository);
    }

    @Test
    void createIssue_whenTypeIsNull_throwsIllegalArgumentException() {
        User reporter = user(1L, Role.USER);
        IssueCreateRequest request = validRequest();
        request.setType(null);

        assertThrows(IllegalArgumentException.class, () -> issueService.createIssue(request, reporter));
        verifyNoInteractions(issueRepository, historyRepository);
    }

    @Test
    void createIssue_whenValid_savesIssueWithTodoStatusAndRecordsHistory() {
        User reporter = user(1L, Role.USER);
        IssueCreateRequest request = validRequest();
        request.setTitle("  Crash al login  "); // verifica anche il trim del titolo

        when(issueRepository.save(any(Issue.class))).then(returnsFirstArg());

        IssueResponse response = issueService.createIssue(request, reporter);

        assertEquals("Crash al login", response.getTitle());
        assertEquals(IssueStatus.TODO, response.getStatus()); // stato iniziale obbligatorio TODO
        assertEquals(IssueType.BUG, response.getType());
        assertEquals(IssuePriority.HIGH, response.getPriority());
        assertEquals(reporter.getEmail(), response.getReporter().getEmail());

        ArgumentCaptor<IssueHistory> history = ArgumentCaptor.forClass(IssueHistory.class);
        verify(historyRepository).save(history.capture());
        assertEquals(reporter, history.getValue().getActor());
        assertTrue(history.getValue().getActionDescription().contains("Creazione"));
    }

    // ---------- addComment (Requisito 5) ----------

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void addComment_whenTextIsNullOrBlank_throwsIllegalArgumentException(String invalidText) {
        User author = user(2L, Role.USER);

        assertThrows(IllegalArgumentException.class, () -> issueService.addComment(1L, invalidText, author));
        verifyNoInteractions(issueRepository, commentRepository, historyRepository);
    }

    @Test
    void addComment_whenIssueDoesNotExist_throwsEntityNotFoundException() {
        User author = user(2L, Role.USER);
        when(issueRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> issueService.addComment(99L, "Commento valido", author));
        verify(commentRepository, never()).save(any(Comment.class));
        verifyNoInteractions(historyRepository);
    }

    @Test
    void addComment_whenValid_savesCommentAndRecordsHistory() {
        User reporter = user(1L, Role.USER);
        User author = user(2L, Role.USER);
        Issue existingIssue = issue(10L, reporter);

        when(issueRepository.findById(10L)).thenReturn(Optional.of(existingIssue));
        when(commentRepository.save(any(Comment.class))).then(returnsFirstArg());

        CommentResponse response = issueService.addComment(10L, "Ho riprodotto il bug.", author);

        assertEquals("Ho riprodotto il bug.", response.getText());
        assertEquals(author.getName(), response.getAuthorName());
        assertEquals(author.getEmail(), response.getAuthorEmail());

        ArgumentCaptor<IssueHistory> history = ArgumentCaptor.forClass(IssueHistory.class);
        verify(historyRepository).save(history.capture());
        assertEquals(author, history.getValue().getActor());
        assertEquals(existingIssue, history.getValue().getIssue());
    }

    // ---------- metodi di supporto ----------

    private IssueCreateRequest validRequest() {
        IssueCreateRequest req = new IssueCreateRequest();
        req.setTitle("Errore salvataggio");
        req.setDescription("Passi per riprodurre il problema");
        req.setType(IssueType.BUG);
        req.setPriority(IssuePriority.HIGH);
        req.setImageUrl(null);
        return req;
    }

    private User user(Long id, Role role) {
        return User.builder()
                .id(id)
                .name("Utente " + id)
                .email("user" + id + "@bugboard.com")
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
