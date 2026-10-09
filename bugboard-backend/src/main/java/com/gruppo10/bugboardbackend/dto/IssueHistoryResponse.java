package com.gruppo10.bugboardbackend.dto;

import com.gruppo10.bugboardbackend.model.IssueHistory;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class IssueHistoryResponse {
    private Long id;
    private String actorName;
    private String actorEmail;
    private String actionDescription;
    private LocalDateTime timestamp;

    // Va chiamato dentro una transazione, perché actor è lazy
    public static IssueHistoryResponse from(IssueHistory history) {
        return IssueHistoryResponse.builder()
                .id(history.getId())
                .actorName(history.getActor().getName())
                .actorEmail(history.getActor().getEmail())
                .actionDescription(history.getActionDescription())
                .timestamp(history.getTimestamp())
                .build();
    }
}