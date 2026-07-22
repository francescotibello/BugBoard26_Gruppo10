package com.gruppo10.bugboardbackend.repository;

import com.gruppo10.bugboardbackend.model.IssueHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueHistoryRepository extends JpaRepository<IssueHistory, Long> {
    // Ci serve per mostrare la cronologia di uno specifico bug (ordinata dal più recente)
    List<IssueHistory> findByIssueIdOrderByTimestampDesc(Long issueId);
}