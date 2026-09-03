package WebHocTap.com.example.WebHocTap.repository;

import WebHocTap.com.example.WebHocTap.entity.UserAnswer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {

    /**
     * Returns the question IDs from the most recent N attempts for a given user.
     * Used to exclude recently answered questions from the next question set.
     */
    @Query("""
            SELECT ua.question.id FROM UserAnswer ua
            WHERE ua.user.id = :userId
            ORDER BY ua.answeredAt DESC
            """)
    List<Long> findRecentAnsweredQuestionIds(
            @Param("userId") Long userId,
            Pageable pageable);

    /**
     * Count answers in a session.
     */
    @Query("""
            SELECT COUNT(ua) FROM UserAnswer ua
            WHERE ua.session.id = :sessionId
            """)
    int countBySessionId(@Param("sessionId") Long sessionId);

    /**
     * Count correct answers in a session.
     */
    @Query("""
            SELECT COUNT(ua) FROM UserAnswer ua
            WHERE ua.session.id = :sessionId AND ua.isCorrect = true
            """)
    int countCorrectBySessionId(@Param("sessionId") Long sessionId);
}
