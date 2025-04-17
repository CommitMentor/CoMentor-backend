package com.knu.coment.repository;

import com.knu.coment.entity.ProjectCsQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectCsQuestionRepository extends JpaRepository<ProjectCsQuestion, Long> {
    List<ProjectCsQuestion> findAllByProjectId(Long projectId);
    List<ProjectCsQuestion> findAllByFolderId(Long folderId);
}
