package WebHocTap.com.example.WebHocTap.repository;

import WebHocTap.com.example.WebHocTap.entity.Question;
import WebHocTap.com.example.WebHocTap.enums.Difficulty;
import WebHocTap.com.example.WebHocTap.enums.QuestionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("""
            SELECT q FROM Question q
            WHERE q.isActive = true
              AND (:topicId IS NULL OR q.topic.id = :topicId)
              AND (:type IS NULL OR q.questionType = :type)
              AND (:difficulty IS NULL OR q.difficulty = :difficulty)
            """)
    Page<Question> findActiveFiltered(
            @Param("topicId") Long topicId,
            @Param("type") QuestionType type,
            @Param("difficulty") Difficulty difficulty,
            Pageable pageable);

    @Query("""
            SELECT DISTINCT q FROM Question q
            LEFT JOIN FETCH q.answers
            LEFT JOIN FETCH q.topic
            WHERE q.id = :id AND q.isActive = true
            """)
    Optional<Question> findActiveByIdWithAnswers(@Param("id") Long id);

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
