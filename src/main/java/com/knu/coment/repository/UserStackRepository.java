package com.knu.coment.repository;

import com.knu.coment.entity.UserStack;
import com.knu.coment.entity.UserStackId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface UserStackRepository extends JpaRepository<UserStack, UserStackId> {
    void deleteAllByUserId(Long userId);
    Set<UserStack> findAllByUserId(Long userId);
}
