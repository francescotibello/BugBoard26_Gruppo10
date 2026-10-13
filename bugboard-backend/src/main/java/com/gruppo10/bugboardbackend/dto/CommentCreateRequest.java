package com.gruppo10.bugboardbackend.dto;

import lombok.Data;

// DTO ricevuto dal client per aggiungere un nuovo commento a una issue (Requisito 5)
@Data
public class CommentCreateRequest {
    private String text;
}