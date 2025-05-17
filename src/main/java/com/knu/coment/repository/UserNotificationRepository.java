package com.knu.coment.repository;

import com.knu.coment.entity.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    List<UserNotification> findByUserIdOrderBySentAtDesc(Long userId);
    void deleteBySentAtBefore(LocalDateTime cutoff);

}
