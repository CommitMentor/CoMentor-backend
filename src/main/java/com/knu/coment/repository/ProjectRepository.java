package com.knu.coment.repository;

import com.knu.coment.entity.Project;
import com.knu.coment.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findAllByUser(User user);

    @Query("select p.repo.id from Project p where p.user.githubId = :githubId")
    List<Long> findRepoIdsByUserGithubId(String githubId);

}
