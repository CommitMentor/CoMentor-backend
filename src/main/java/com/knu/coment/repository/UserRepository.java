package com.knu.coment.repository;

import com.knu.coment.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(attributePaths = "userStacks")
    Optional<User> findByGithubId(String githubId);
}
