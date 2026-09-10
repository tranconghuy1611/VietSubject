package WebHocTap.com.example.WebHocTap.repository;

import WebHocTap.com.example.WebHocTap.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {

    List<Grade> findAllByOrderByLevelAsc();
}
