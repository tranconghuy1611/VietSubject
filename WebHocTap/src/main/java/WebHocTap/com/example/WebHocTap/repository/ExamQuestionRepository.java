package WebHocTap.com.example.WebHocTap.repository;

import WebHocTap.com.example.WebHocTap.entity.ExamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Long> {

    /**
     * Fetch all exam-question join rows for a given exam,
     * ordered by question_order. Eagerly joins the Question and its Answers
     * so the service can map everything in one query.
     */
    @Query("""
            SELECT eq FROM ExamQuestion eq
            JOIN FETCH eq.question q
            LEFT JOIN FETCH q.answers
            WHERE eq.exam.id = :examId
            ORDER BY eq.questionOrder ASC
            """)
    List<ExamQuestion> findByExamIdOrderByQuestionOrder(@Param("examId") Long examId);

    /** Count how many questions belong to an exam (for score calculation). */
    int countByExamId(Long examId);
}
