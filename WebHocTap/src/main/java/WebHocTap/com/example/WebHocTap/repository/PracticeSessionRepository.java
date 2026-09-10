package WebHocTap.com.example.WebHocTap.repository;

import WebHocTap.com.example.WebHocTap.entity.PracticeSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PracticeSessionRepository extends JpaRepository<PracticeSession, Long> {
    @Query("""
    SELECT s FROM PracticeSession s
    WHERE s.user.id = :userId
    ORDER BY s.startedAt DESC
""")
    List<PracticeSession> findByUserIdOrderByStartedAtDesc(Long userId);
}
