package WebHocTap.com.example.WebHocTap.repository;

import WebHocTap.com.example.WebHocTap.entity.PracticeSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PracticeSessionRepository extends JpaRepository<PracticeSession, Long> {

    @Query("""
            SELECT s FROM PracticeSession s
            JOIN FETCH s.user
            JOIN FETCH s.subject
            LEFT JOIN FETCH s.topic
            WHERE s.id = :sessionId
            """)
    Optional<PracticeSession> findByIdWithDetails(@Param("sessionId") Long sessionId);

    @Query(
            value = """
                    SELECT s FROM PracticeSession s
                    JOIN FETCH s.subject
                    LEFT JOIN FETCH s.topic
                    WHERE s.user.id = :userId
                    ORDER BY s.startedAt DESC
                    """,
            countQuery = """
                    SELECT COUNT(s) FROM PracticeSession s
                    WHERE s.user.id = :userId
                    """
    )
    Page<PracticeSession> findByUserId(@Param("userId") Long userId, Pageable pageable);
}
