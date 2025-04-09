package com.knu.coment.repository;

import com.knu.coment.entity.CsQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CsQuestionRepository extends JpaRepository<CsQuestion, Long> {

}
