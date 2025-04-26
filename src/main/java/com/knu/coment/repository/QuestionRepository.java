package com.knu.coment.repository;

import com.knu.coment.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findAllByProjectId(Long projectId);
    List<Question> findAllByFolderId(Long folderId);
    @Query(value = """
    SELECT *
    FROM (
            SELECT
                    q.*,
            ROW_NUMBER() OVER (PARTITION BY q.stack ORDER BY q.id DESC) AS rn
    FROM question q
    WHERE q.stack IN (:stacks)
    AND q.question_type = 'CS'
    AND q.id NOT IN (:answeredQuestionIds)
    AND NOT EXISTS (
            SELECT 1
            FROM answer a
            WHERE a.question_id = q.id
            AND a.user_id     = :userId
        )
    ) t
    WHERE t.rn <= :slot
    LIMIT :total
    """, nativeQuery = true)
    List<Question> findBalancedUnanswered(
        @Param("stacks") List<String> stacks,
        @Param("slot") int slotPerStack,
        @Param("total") int totalRows,
        @Param("answeredQuestionIds") List<Long> answeredQuestionIds,
        @Param("userId") Long userId
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
          AND NOT EXISTS (
              SELECT 1
              FROM answer a
              WHERE a.question_id = q.id
                AND a.user_id     = :userId
          )
    ) t
    WHERE t.rn <= :slot
    LIMIT :total
""", nativeQuery = true)
    List<Question> findBalancedUnansweredWithoutExclude(
            @Param("stacks") List<String> stacks,
            @Param("slot") int slotPerStack,
            @Param("total") int totalRows,
            @Param("userId") Long userId
    );

}
