package com.gruppo10.bugboardbackend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

// DTO restituito al client per rappresentare un commento di una issue (Requisito 5)
@Data
@Builder
public class CommentResponse {
    private Long id;
    private String text;
    private String authorName;
    private String authorEmail;
    private LocalDateTime createdAt;
}