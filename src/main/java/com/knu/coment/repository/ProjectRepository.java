package com.knu.coment.repository;

import com.knu.coment.entity.Project;
import com.knu.coment.entity.User;
import com.knu.coment.global.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Page<Project> findAllByUser(User user,Pageable pageable);
    boolean existsByUserAndRepoId(User user, Long repoId);
    Page<Project> findByUserAndStatus(User user, Status status, Pageable pageable);

}
