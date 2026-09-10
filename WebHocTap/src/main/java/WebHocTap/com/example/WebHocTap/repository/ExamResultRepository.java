package WebHocTap.com.example.WebHocTap.repository;

import WebHocTap.com.example.WebHocTap.entity.ExamResult;
import WebHocTap.com.example.WebHocTap.enums.ExamStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {

    /**
     * Fetch the result row together with its exam and user references
     * to avoid lazy-loading issues in the service.
     */
    @Query("""
            SELECT er FROM ExamResult er
            JOIN FETCH er.user
            JOIN FETCH er.exam
            WHERE er.id = :resultId
            """)
    Optional<ExamResult> findByIdWithUserAndExam(@Param("resultId") Long resultId);

    /**
     * Guard against double-submission: check whether a result already has
     * the given status (typically SUBMITTED).
     */
    // ✅ FIX: đổi createdAt → submittedAt
    List<ExamResult> findByUserIdOrderBySubmittedAtDesc(Long userId);

    // ✅ thêm nếu cần lấy bài đang làm
    List<ExamResult> findByUserIdAndStatus(Long userId, ExamStatus status);

    boolean existsByIdAndStatus(Long resultId, ExamStatus status);
}
