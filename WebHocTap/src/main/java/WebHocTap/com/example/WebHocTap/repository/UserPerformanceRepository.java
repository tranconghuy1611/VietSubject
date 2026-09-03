package WebHocTap.com.example.WebHocTap.repository;

import WebHocTap.com.example.WebHocTap.entity.UserPerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPerformanceRepository extends JpaRepository<UserPerformance, Long> {

    Optional<UserPerformance> findByUserIdAndTopicId(Long userId, Long topicId);

    /** Load all performance rows for a user (used when topic is null → mix). */
    List<UserPerformance> findByUserId(Long userId);
}
