package WebHocTap.com.example.WebHocTap.repository;

import WebHocTap.com.example.WebHocTap.entity.ExamAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamAnswerRepository extends JpaRepository<ExamAnswer, Long> {

    /**
     * Load all answers for a result, fetching the linked Question and
     * Answer entities in a single query (avoids N+1 during scoring).
     */
    @Query("""
            SELECT ea FROM ExamAnswer ea
            JOIN FETCH ea.question q
            LEFT JOIN FETCH ea.selectedAnswer
            WHERE ea.examResult.id = :resultId
            """)
    List<ExamAnswer> findByResultIdWithDetails(@Param("resultId") Long resultId);

    /**
     * Check whether the user has already submitted an answer for a specific
     * question within this result (idempotent submit-answer support).
     */
    Optional<ExamAnswer> findByExamResultIdAndQuestionId(Long resultId, Long questionId);
}
