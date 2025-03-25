package com.knu.coment.repository;

import com.knu.coment.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userStacks WHERE u.githubId = :githubId")
    Optional<User> findByGithubIdFetchStacks(@Param("githubId") String githubId);

//    @EntityGraph(attributePaths = "userStacks")
//    Optional<User> findByGithubId(String githubId);
}
