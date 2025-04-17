package com.knu.coment.repository;

import com.knu.coment.entity.Repo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepoRepository extends JpaRepository<Repo, Long> {

    @Query("select p.repoId from Project p where p.userId = :userId and p.repoId is not null")
    List<Long> findRepoIdsByUserId(Long userId);

}
