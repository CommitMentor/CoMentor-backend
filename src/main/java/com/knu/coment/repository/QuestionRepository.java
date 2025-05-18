package com.knu.coment.repository;

import com.knu.coment.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findAllByProjectId(Long projectId);
    List<Question> findAllByFileName(String fileName);

    Optional<Question> findByIdAndUserId(Long questionId, Long userId);
    @Query(value = """
    SELECT *
    FROM (
        SELECT
            q.*,
            ROW_NUMBER() OVER (PARTITION BY q.stack ORDER BY q.id DESC) AS rn
        FROM question q
        WHERE q.stack IN (:stacks)
          AND q.question_type = 'CS'
          AND q.id NOT IN (:excludedQuestionIds)
    ) t
    WHERE t.rn <= :slot
    LIMIT :total
""", nativeQuery = true)
    List<Question> findBalancedUnreceived(
            @Param("stacks") List<String> stacks,
            @Param("slot") int slotPerStack,
            @Param("total") int totalRows,
            @Param("excludedQuestionIds") List<Long> excludedQuestionIds
    );
    @Query(value = """
    SELECT *
    FROM (
        SELECT
            q.*,
            ROW_NUMBER() OVER (PARTITION BY q.stack ORDER BY q.id DESC) AS rn
        FROM question q
        WHERE q.stack IN (:stacks)
          AND q.question_type = 'CS'
    ) t
    WHERE t.rn <= :slot
    LIMIT :total
""", nativeQuery = true)
    List<Question> findBalancedUnreceivedWithoutExclude(
            @Param("stacks") List<String> stacks,
            @Param("slot") int slotPerStack,
            @Param("total") int totalRows
    );

    @Query("""
    SELECT q.csCategory, COUNT(q)
    FROM Question q
    WHERE q.userId = :userId
      AND q.questionStatus = 'DONE'
    GROUP BY q.csCategory
""")
    List<Object[]> countSolvedByCategory(@Param("userId") Long userId);

}
