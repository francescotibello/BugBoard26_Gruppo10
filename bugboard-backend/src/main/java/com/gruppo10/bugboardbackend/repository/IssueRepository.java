package com.gruppo10.bugboardbackend.repository;

import com.gruppo10.bugboardbackend.model.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long>, JpaSpecificationExecutor<Issue> {
    // Estende JpaSpecificationExecutor per costruire dinamicamente i filtri opzionali
    // (per tipo, stato e priorità) richiesti dal Requisito 3 in IssueService.searchIssues
}