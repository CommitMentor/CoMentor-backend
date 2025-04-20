package com.knu.coment.repository;

import com.knu.coment.entity.UserStack;
import com.knu.coment.entity.UserStackId;
import com.knu.coment.global.Stack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface UserStackRepository extends JpaRepository<UserStack, UserStackId> {
    void deleteAllByUserId(Long userId);
    Set<UserStack> findAllByUserId(Long userId);
    @Query("select us.stack from UserStack us where us.userId = :uid")
    List<Stack> findStacksByUserId(@Param("uid") Long userId);
}
