package com.knu.coment.repository;

import com.knu.coment.entity.Question;
import com.knu.coment.global.QuestionStatus;
import com.knu.coment.global.QuestionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findAllByProjectId(Long projectId);
    List<Question> findAllByFolderId(Long folderId);

    @Query("""
        select q
        from Question q
        where q.questionType   = :type
          and q.questionStatus  = :status
          and q.userId          = :uid
          and q.createAt        < :cutOff        
        order by q.createAt desc
    """)
    Page<Question> findAllUpToToday(@Param("type")   QuestionType   type,
                                    @Param("status") QuestionStatus status,
                                    @Param("uid")    Long           uid,
                                    @Param("cutOff") LocalDateTime cutOff,
                                    Pageable pageable);
    @Query(value = """
        SELECT *
        FROM (
            SELECT  q.*,
                    ROW_NUMBER() OVER (PARTITION BY q.stack ORDER BY q.id DESC) AS rn
            FROM    question q
            WHERE   q.stack IN (:stacks)
              AND    q.questionStatus = 'DONE'
        ) t
        WHERE t.rn <= :slot         
        LIMIT :total                
        """, nativeQuery = true)
    List<Question> findBalanced(@Param("stacks") List<String> stacks,
                                @Param("slot")   int slotPerStack,
                                @Param("total")  int totalRows);



}
