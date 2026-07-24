package com.gruppo10.bugboardbackend.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CommentResponse {
    private Long id;
    private String text;
    private String authorEmail;
    private LocalDateTime createdAt;
}