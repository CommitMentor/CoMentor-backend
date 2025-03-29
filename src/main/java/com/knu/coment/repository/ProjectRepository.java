package com.knu.coment.repository;

import com.knu.coment.entity.Project;
import com.knu.coment.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findAllByUser(User user);
}
