package com.knu.coment.repository;

import com.knu.coment.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByGithubId(String githubId);
    List<User> findByNotificationTrue();

    @Query("SELECT u FROM User u WHERE u.notification = true AND u.lastActivityAt < :cutoff")
    List<User> findInactiveSince(@Param("cutoff") LocalDateTime cutoff);
}
