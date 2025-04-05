package com.knu.coment.repository;

import com.knu.coment.entity.Project;
import com.knu.coment.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findAllByUser(User user);
    boolean existsByUserAndRepoId(User user, Long repoId);
}
