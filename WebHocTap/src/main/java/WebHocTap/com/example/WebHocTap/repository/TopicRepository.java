package WebHocTap.com.example.WebHocTap.repository;

import WebHocTap.com.example.WebHocTap.entity.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {

    @Query("""
            SELECT t FROM Topic t
            JOIN FETCH t.subject
            JOIN FETCH t.grade
            WHERE t.id = :id
            """)
    Optional<Topic> findByIdWithSubjectAndGrade(@Param("id") Long id);

    @Query(
            value = """
                    SELECT t FROM Topic t
                    JOIN FETCH t.subject s
                    JOIN FETCH t.grade g
                    WHERE s.id = :subjectId
                      AND t.isActive = true
                      AND (:gradeId IS NULL OR g.id = :gradeId)
                    """,
            countQuery = """
                    SELECT COUNT(t) FROM Topic t
                    WHERE t.subject.id = :subjectId
                      AND t.isActive = true
                      AND (:gradeId IS NULL OR t.grade.id = :gradeId)
                    """
    )
    Page<Topic> findActiveBySubjectIdAndOptionalGradeId(
            @Param("subjectId") Long subjectId,
            @Param("gradeId") Long gradeId,
            Pageable pageable);
}
