package com.knu.coment.repository;

import com.knu.coment.entity.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    Optional<FcmToken> findByFcmToken(String token);
    List<FcmToken> findByUserId(Long userId);
    void deleteByFcmToken(String token);
}
