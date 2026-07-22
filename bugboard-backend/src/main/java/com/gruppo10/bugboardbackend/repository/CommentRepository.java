package com.gruppo10.bugboardbackend.repository;

import com.gruppo10.bugboardbackend.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // Ci serve per prendere tutti i commenti di un singolo bug
    List<Comment> findByIssueId(Long issueId);
}