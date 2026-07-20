package com.gruppo10.bugboardbackend.repository;

import com.gruppo10.bugboardbackend.model.Issue;
import com.gruppo10.bugboardbackend.model.IssueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {

    // Per il Requisito 3 (Dashboard e filtri)
    List<Issue> findByStatus(IssueStatus status);

    // Per il Requisito 9 (gli utenti vedono/modificano solo i propri bug assegnati)
    List<Issue> findByAssigneeId(Long assigneeId);
}