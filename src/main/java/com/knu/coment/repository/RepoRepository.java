package com.knu.coment.repository;

import com.knu.coment.entity.Repo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RepoRepository extends JpaRepository<Repo, Long> {
    @Query("select r.id from Repo r")
    List<Long> findAllRepoIds();

}
