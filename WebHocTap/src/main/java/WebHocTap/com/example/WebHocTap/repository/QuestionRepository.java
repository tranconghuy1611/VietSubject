package WebHocTap.com.example.WebHocTap.repository;

import WebHocTap.com.example.WebHocTap.entity.Question;
import WebHocTap.com.example.WebHocTap.enums.Difficulty;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    /**
     * Fetch questions by topic and difficulty, excluding already-answered question IDs,
     * ordered randomly.
     */
    @Query("""
            SELECT q FROM Question q
            WHERE q.topic.id = :topicId
              AND q.difficulty = :difficulty
              AND q.isActive = true
              AND q.id NOT IN :excludedIds
            ORDER BY FUNCTION('RAND')
            """)
    List<Question> findByTopicAndDifficultyExcluding(
            @Param("topicId") Long topicId,
            @Param("difficulty") Difficulty difficulty,
            @Param("excludedIds") List<Long> excludedIds,
            Pageable pageable);

    /**
     * Overload without exclusions.
     */
    @Query("""
            SELECT q FROM Question q
            WHERE q.topic.id = :topicId
              AND q.difficulty = :difficulty
              AND q.isActive = true
            ORDER BY FUNCTION('RAND')
            """)
    List<Question> findByTopicAndDifficulty(
            @Param("topicId") Long topicId,
            @Param("difficulty") Difficulty difficulty,
            Pageable pageable);

    /**
     * Fetch questions from any topic in a subject, excluding answered ids.
     */
    @Query("""
            SELECT q FROM Question q
            WHERE q.topic.subject.id = :subjectId
              AND q.difficulty = :difficulty
              AND q.isActive = true
              AND q.id NOT IN :excludedIds
            ORDER BY FUNCTION('RAND')
            """)
    List<Question> findBySubjectAndDifficultyExcluding(
            @Param("subjectId") Long subjectId,
            @Param("difficulty") Difficulty difficulty,
            @Param("excludedIds") List<Long> excludedIds,
            Pageable pageable);

    /**
     * Overload without exclusions.
     */
    @Query("""
            SELECT q FROM Question q
            WHERE q.topic.subject.id = :subjectId
              AND q.difficulty = :difficulty
              AND q.isActive = true
            ORDER BY FUNCTION('RAND')
            """)
    List<Question> findBySubjectAndDifficulty(
            @Param("subjectId") Long subjectId,
            @Param("difficulty") Difficulty difficulty,
            Pageable pageable);
}
