package com.knu.coment.repository;

import com.knu.coment.entity.Repo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepoRepository extends JpaRepository<Repo, Long> {

    @Query("select r.id from Repo r join r.projects p where p.user.githubId = :githubId")
    List<Long> findRepoIdsByUserGithubId(String githubId);

}
