package WebHocTap.com.example.WebHocTap.repository;

import WebHocTap.com.example.WebHocTap.entity.ExamResult;
import WebHocTap.com.example.WebHocTap.enums.ExamStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {

    @Query("""
            SELECT er FROM ExamResult er
            JOIN FETCH er.user
            JOIN FETCH er.exam
            WHERE er.id = :resultId
            """)
    Optional<ExamResult> findByIdWithUserAndExam(@Param("resultId") Long resultId);

    boolean existsByIdAndStatus(Long resultId, ExamStatus status);

    @Query(
            value = """
                    SELECT er FROM ExamResult er
                    JOIN FETCH er.exam
                    WHERE er.user.id = :userId
                    ORDER BY er.startedAt DESC
                    """,
            countQuery = """
                    SELECT COUNT(er) FROM ExamResult er
                    WHERE er.user.id = :userId
                    """
    )
    Page<ExamResult> findByUserId(@Param("userId") Long userId, Pageable pageable);
}
