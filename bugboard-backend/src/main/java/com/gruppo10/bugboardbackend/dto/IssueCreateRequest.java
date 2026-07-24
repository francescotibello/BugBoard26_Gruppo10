package com.gruppo10.bugboardbackend.dto;

import com.gruppo10.bugboardbackend.model.IssuePriority;
import com.gruppo10.bugboardbackend.model.IssueType;
import lombok.Data;

@Data
public class IssueCreateRequest {
    private String title;
    private String description;
    private IssueType type;
    private IssuePriority priority;
    private String imageUrl;
}