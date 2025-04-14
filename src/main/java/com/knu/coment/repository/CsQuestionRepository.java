package com.knu.coment.repository;

import com.knu.coment.entity.CsQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CsQuestionRepository extends JpaRepository<CsQuestion, Long> {
    List<CsQuestion> findAllByCsStackIsNullAndProject_Id(Long projectId);

    List<CsQuestion> findTop3ByOrderByCreateAtDesc();
}
