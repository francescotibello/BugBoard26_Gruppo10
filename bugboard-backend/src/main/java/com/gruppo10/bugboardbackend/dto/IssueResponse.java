package com.gruppo10.bugboardbackend.dto;

import com.gruppo10.bugboardbackend.model.Issue;
import com.gruppo10.bugboardbackend.model.IssuePriority;
import com.gruppo10.bugboardbackend.model.IssueStatus;
import com.gruppo10.bugboardbackend.model.IssueType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class IssueResponse {
    private Long id;
    private String title;
    private String description;
    private IssueType type;
    private IssueStatus status;
    private IssuePriority priority;
    private String imageUrl;
    private LocalDate deadline;
    private UserResponse reporter;
    private UserResponse assignee;      // null se non ancora assegnata
    private Long duplicateOfId;         // null se non è un duplicato
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Va chiamato dentro una transazione, perché reporter/assignee/duplicateOf sono lazy
    public static IssueResponse from(Issue issue) {
        return IssueResponse.builder()
                .id(issue.getId())
                .title(issue.getTitle())
                .description(issue.getDescription())
                .type(issue.getType())
                .status(issue.getStatus())
                .priority(issue.getPriority())
                .imageUrl(issue.getImageUrl())
                .deadline(issue.getDeadline())
                .reporter(UserResponse.from(issue.getReporter()))
                .assignee(issue.getAssignee() != null ? UserResponse.from(issue.getAssignee()) : null)
                .duplicateOfId(issue.getDuplicateOf() != null ? issue.getDuplicateOf().getId() : null)
                .createdAt(issue.getCreatedAt())
                .updatedAt(issue.getUpdatedAt())
                .build();
    }
}